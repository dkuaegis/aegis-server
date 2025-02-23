package aegis.server.domain.timetable.service;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.timetable.domain.Timetable;
import aegis.server.domain.timetable.dto.external.EverytimeResponse;
import aegis.server.domain.timetable.dto.internal.LectureInfo;
import aegis.server.domain.timetable.dto.request.TimetableCreateRequest;
import aegis.server.domain.timetable.repository.TimetableRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.*;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TimetableServiceTest extends IntegrationTest {

    @Autowired
    TimetableService timetableService;

    @Autowired
    TimetableRepository timetableRepository;

    @Autowired
    MemberRepository memberRepository;

    @MockitoBean
    TimetableCrawlerService timetableCrawlerService;

    private static final String VALID_IDENTIFIER = "valid_identifier";
    private static final String VALID_EVERYTIME_URL = "https://everytime.kr/@" + VALID_IDENTIFIER;

    @Nested
    class 시간표_생성_및_업데이트 {
        @Test
        void 새로운_시간표_생성에_성공한다() {
            // given
            Member member = createMember();
            memberRepository.save(member);
            UserDetails userDetails = createUserDetails(member);
            TimetableCreateRequest request = new TimetableCreateRequest(VALID_EVERYTIME_URL);

            Map<String, List<LectureInfo>> mockTimetable = createMockTimetable();
            when(timetableCrawlerService.fetchAndParseTimetable(any())).thenReturn(new EverytimeResponse());
            when(timetableCrawlerService.convertToTimetable(any())).thenReturn(mockTimetable);
            when(timetableCrawlerService.convertTimetableToJson(any())).thenReturn("json_data");

            // when
            Map<String, List<LectureInfo>> result = timetableService.createOrUpdateTimetable(userDetails, request);

            // then
            Optional<Timetable> savedTimetable = timetableRepository.findByMemberAndYearSemester(
                    member, CURRENT_YEAR_SEMESTER);
            assertTrue(savedTimetable.isPresent());
            assertEquals("json_data", savedTimetable.get().getJsonData());
            assertEquals(mockTimetable, result);
        }

        @Test
        void 식별자가_다른_시간표_업데이트에_성공한다() {
            // given
            Member member = createMember();
            memberRepository.save(member);
            UserDetails userDetails = createUserDetails(member);
            TimetableCreateRequest request = new TimetableCreateRequest(VALID_EVERYTIME_URL);

            Timetable existingTimetable = Timetable.create(member, "old_identifier", "old_json");
            timetableRepository.save(existingTimetable);

            Map<String, List<LectureInfo>> mockTimetable = createMockTimetable();
            when(timetableCrawlerService.fetchAndParseTimetable(any())).thenReturn(new EverytimeResponse());
            when(timetableCrawlerService.convertToTimetable(any())).thenReturn(mockTimetable);
            when(timetableCrawlerService.convertTimetableToJson(any())).thenReturn("new_json");

            // when
            Map<String, List<LectureInfo>> result = timetableService.createOrUpdateTimetable(userDetails, request);

            // then
            Timetable updatedTimetable = timetableRepository.findById(existingTimetable.getId()).get();
            assertEquals(VALID_IDENTIFIER, updatedTimetable.getIdentifier());
            assertEquals("new_json", updatedTimetable.getJsonData());
            assertEquals(mockTimetable, result);
        }

        @Test
        void 식별자가_같은_시간표_업데이트에_성공한다() {
            // given
            Member member = createMember();
            memberRepository.save(member);
            UserDetails userDetails = createUserDetails(member);
            TimetableCreateRequest request = new TimetableCreateRequest(VALID_EVERYTIME_URL);

            Timetable existingTimetable = Timetable.create(member, VALID_IDENTIFIER, "old_json");
            timetableRepository.save(existingTimetable);

            Map<String, List<LectureInfo>> mockTimetable = createMockTimetable();
            when(timetableCrawlerService.fetchAndParseTimetable(any())).thenReturn(new EverytimeResponse());
            when(timetableCrawlerService.convertToTimetable(any())).thenReturn(mockTimetable);
            when(timetableCrawlerService.convertTimetableToJson(any())).thenReturn("new_json");

            // when
            Map<String, List<LectureInfo>> result = timetableService.createOrUpdateTimetable(userDetails, request);

            // then
            Timetable updatedTimetable = timetableRepository.findById(existingTimetable.getId()).get();
            assertEquals(VALID_IDENTIFIER, updatedTimetable.getIdentifier());
            assertEquals("new_json", updatedTimetable.getJsonData());
            assertEquals(mockTimetable, result);
        }

        @Test
        void 다른_계정의_identifier로_시간표_생성_시_identifier_중복_에러가_발생한다() {
            // given
            Member member1 = createMember();
            memberRepository.save(member1);
            Timetable existingTimetable = Timetable.create(member1, VALID_IDENTIFIER, "json");
            timetableRepository.save(existingTimetable);

            Member member2 = createMember();
            memberRepository.save(member2);
            UserDetails userDetails2 = createUserDetails(member2);
            TimetableCreateRequest request = new TimetableCreateRequest(VALID_EVERYTIME_URL);

            // when & then
            assertThrows(CustomException.class, () ->
                    timetableService.createOrUpdateTimetable(userDetails2, request));
        }
    }

    private Map<String, List<LectureInfo>> createMockTimetable() {
        Map<String, List<LectureInfo>> timetable = new LinkedHashMap<>();
        String[] days = {"월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일"};

        // 각 요일별 빈 리스트로 초기화
        for (String day : days) {
            timetable.put(day, new ArrayList<>());
        }

        // 샘플 강의 데이터 추가
        LectureInfo lecture = new LectureInfo(
                "데이터사이언스",
                "오세종",
                "16:00-18:50",
                3,
                "미디어102"
        );
        timetable.get("수요일").add(lecture);

        return timetable;
    }
}
