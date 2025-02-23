package aegis.server.domain.timetable.service;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.timetable.domain.Timetable;
import aegis.server.domain.timetable.dto.external.EverytimeResponse;
import aegis.server.domain.timetable.dto.internal.LectureInfo;
import aegis.server.domain.timetable.dto.request.TimetableCreateRequest;
import aegis.server.domain.timetable.repository.TimetableRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableRepository timetableRepository;
    private final MemberRepository memberRepository;
    private final TimetableCrawlerService timetableCrawlerService;

    @Transactional
    public Map<String, List<LectureInfo>> createOrUpdateTimetable(UserDetails userDetails, TimetableCreateRequest request) {
        Member member = memberRepository.findById(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        String identifier = extractIdentifier(request.url());

        // 크롤러를 통해 시간표 데이터 가져오기 및 변환
        EverytimeResponse response = timetableCrawlerService.fetchAndParseTimetable(identifier);
        timetableCrawlerService.validateResponse(response);
        Map<String, List<LectureInfo>> timetable = timetableCrawlerService.convertToTimetable(response);
        String timetableJsonString = timetableCrawlerService.convertTimetableToJson(timetable);

        // 식별자 중복 체크: 동일 학기 내에 동일 식별자가 존재하는지 확인
        Optional<Timetable> timetableByIdentifier = timetableRepository.findByIdentifierAndYearSemester(identifier, CURRENT_YEAR_SEMESTER);
        if (timetableByIdentifier.isPresent()) {
            Timetable existingByIdentifier = timetableByIdentifier.get();
            // 식별자가 다른 회원의 것이라면 오류 발생
            if (!existingByIdentifier.getMember().equals(member)) {
                throw new CustomException(ErrorCode.TIMETABLE_IDENTIFIER_ALREADY_EXISTS);
            }
            // 동일 회원의 시간표인 경우 업데이트
            existingByIdentifier.updateIdentifier(identifier);
            existingByIdentifier.updateJsonData(timetableJsonString);
            return timetable;
        }

        // 식별자가 존재하지 않을 경우, 현재 회원의 기존 시간표가 있는지 확인
        Optional<Timetable> optionalTimetable = timetableRepository.findByMemberAndYearSemester(member, CURRENT_YEAR_SEMESTER);
        if (optionalTimetable.isPresent()) {
            Timetable existingTimetable = optionalTimetable.get();
            existingTimetable.updateIdentifier(identifier);
            existingTimetable.updateJsonData(timetableJsonString);
        } else {
            Timetable newTimetable = Timetable.create(member, identifier, timetableJsonString);
            timetableRepository.save(newTimetable);
        }
        return timetable;
    }

    private String extractIdentifier(String url) {
        String[] parts = url.split("/@");
        if (parts.length < 2) {
            throw new CustomException(ErrorCode.TIMETABLE_INVALID_URL);
        }
        return parts[1];
    }
}