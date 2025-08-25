package aegis.server.domain.study.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.Department;
import aegis.server.domain.member.domain.Gender;
import aegis.server.domain.member.domain.Grade;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.study.domain.Study;
import aegis.server.domain.study.domain.StudyCategory;
import aegis.server.domain.study.domain.StudyLevel;
import aegis.server.domain.study.domain.StudyMember;
import aegis.server.domain.study.domain.StudyRecruitmentMethod;
import aegis.server.domain.study.dto.request.StudyEnrollRequest;
import aegis.server.domain.study.repository.StudyMemberRepository;
import aegis.server.domain.study.repository.StudyRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTestWithoutTransactional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("concurrency")
@ActiveProfiles("postgres")
class StudyEnrollConcurrencyTest extends IntegrationTestWithoutTransactional {

    @Autowired
    StudyGeneralService studyGeneralService;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    StudyMemberRepository studyMemberRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void 동시_신청_테스트() throws InterruptedException {
        // 테스트 변수 설정
        int maxParticipants = 5; // 스터디 정원
        int applicantCount = 1000; // 신청자 수

        // given
        Study study = createStudyWithMaxParticipants(maxParticipants, StudyRecruitmentMethod.FCFS);

        // 미리 멤버들을 생성하여 동시성 문제 방지
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < applicantCount; i++) {
            members.add(createUniqueTestMember(i));
        }

        ExecutorService executorService = Executors.newFixedThreadPool(applicantCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(applicantCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        List<CompletableFuture<Void>> futures = IntStream.range(0, applicantCount)
                .mapToObj(i -> CompletableFuture.runAsync(
                        () -> {
                            try {
                                startLatch.await();

                                Member member = members.get(i);
                                UserDetails userDetails = createUserDetails(member);
                                StudyEnrollRequest request = new StudyEnrollRequest("동시성 테스트 신청");

                                studyGeneralService.enrollInStudy(study.getId(), request, userDetails);
                                successCount.incrementAndGet();

                            } catch (CustomException e) {
                                if (e.getErrorCode() == ErrorCode.STUDY_FULL) {
                                    failCount.incrementAndGet();
                                } else {
                                    throw new RuntimeException("예상하지 못한 예외 발생: " + e.getErrorCode());
                                }
                            } catch (Exception e) {
                                throw new RuntimeException("예상하지 못한 예외 발생", e);
                            } finally {
                                endLatch.countDown();
                            }
                        },
                        executorService))
                .toList();

        // 모든 스레드가 동시에 시작되도록 신호 전송
        startLatch.countDown();

        // 모든 스레드가 완료될 때까지 대기
        endLatch.await();

        // CompletableFuture 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        executorService.shutdown();

        // then
        Study updatedStudy = studyRepository.findById(study.getId()).get();
        List<StudyMember> studyMembers = studyMemberRepository.findByStudy(study);

        // 검증: 최대 참가자 수를 초과하지 않음
        assertTrue(
                updatedStudy.getCurrentParticipants() <= maxParticipants,
                "현재 참가자 수가 최대 참가자 수를 초과했습니다: " + updatedStudy.getCurrentParticipants() + "/" + maxParticipants);

        // 검증: 성공한 신청 수가 현재 참가자 수와 일치
        assertEquals(updatedStudy.getCurrentParticipants(), successCount.get(), "현재 참가자 수와 성공한 신청 수가 일치하지 않습니다");

        // 검증: DB에 저장된 StudyMember 수가 현재 참가자 수와 일치
        assertEquals(
                updatedStudy.getCurrentParticipants(),
                studyMembers.size(),
                "현재 참가자 수와 DB에 저장된 StudyMember 수가 일치하지 않습니다");

        // 검증: 총 요청 수 = 성공 수 + 실패 수
        assertEquals(applicantCount, successCount.get() + failCount.get(), "총 요청 수와 성공/실패 수의 합이 일치하지 않습니다");

        // 검증: 성공 수는 최대 참가자 수를 초과하지 않음
        assertTrue(
                successCount.get() <= maxParticipants,
                "성공한 신청 수가 최대 참가자 수를 초과했습니다: " + successCount.get() + "/" + maxParticipants);

        // 검증: 실패 수는 총 요청 수 - 성공 수
        assertEquals(applicantCount - successCount.get(), failCount.get(), "실패 수가 예상 값과 일치하지 않습니다");

        // 결과 출력
        System.out.printf(
                "테스트 결과 - 정원: %d명, 신청자: %d명, 성공: %d명, 실패: %d명%n",
                maxParticipants, applicantCount, successCount.get(), failCount.get());
    }

    private Member createUniqueTestMember(int index) {
        String uniqueId = "test_user_" + System.currentTimeMillis() + "_" + index;
        Member member = Member.create(uniqueId, uniqueId + "@dankook.ac.kr", "테스트사용자" + index);
        member.updatePersonalInfo(
                "010-1234-567" + (index % 10),
                "32000" + String.format("%03d", index % 1000),
                Department.SW융합대학_컴퓨터공학과,
                Grade.THREE,
                "010101",
                Gender.MALE);
        member.promoteToUser();

        return memberRepository.save(member);
    }

    private Study createStudyWithMaxParticipants(int maxParticipants, StudyRecruitmentMethod recruitmentMethod) {
        Study study = Study.create(
                "동시성 테스트 스터디",
                StudyCategory.WEB,
                StudyLevel.INTERMEDIATE,
                "동시성 테스트용 스터디 설명",
                recruitmentMethod,
                maxParticipants,
                "주 2회",
                List.of("테스트 커리큘럼 1", "테스트 커리큘럼 2"),
                List.of("테스트 자격 요건 1", "테스트 자격 요건 2"));
        return studyRepository.save(study);
    }
}
