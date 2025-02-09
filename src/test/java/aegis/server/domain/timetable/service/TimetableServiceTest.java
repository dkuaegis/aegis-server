package aegis.server.domain.timetable.service;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.timetable.domain.Timetable;
import aegis.server.domain.timetable.dto.internal.LectureInfo;
import aegis.server.domain.timetable.dto.request.TimetableCreateRequest;
import aegis.server.domain.timetable.repository.TimetableRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.JAXBContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class TimetableServiceTest extends IntegrationTest {

    @Autowired
    TimetableService timetableService;

    @Autowired
    TimetableRepository timetableRepository;

    @Autowired
    MemberRepository memberRepository;

    @MockitoBean
    RestTemplate restTemplate;

    @Autowired
    HttpHeaders timetableHttpHeaders;

    @Autowired
    JAXBContext jaxbContext;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    private static final String VALID_IDENTIFIER = "valid_identifier";
    private static final String VALID_EVERYTIME_URL = "https://everytime.kr/@" + VALID_IDENTIFIER;
    private static final String PRIVATE_TIMETABLE_XML_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><response><table year=\"2024\" semester=\"2\" status=\"1\" identifier=\"Redte0RhSbJRibpczui0\"></table></response>";
    private static final String VALID_TIMETABLE_XML_RESPONSE_V1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><response><table year=\"2024\" semester=\"2\" status=\"1\" identifier=\"Redte0RhSbJRibpczui0\"><subject id=\"7002187\"><internal value=\"540650-1\"/><name value=\"데이터사이언스\"/><professor value=\"오세종\"/><time value=\"수15~19(미디어102)\"><data day=\"2\" starttime=\"192\" endtime=\"226\" place=\"미디어102\"/></time><place value=\"\"/><credit value=\"3\"/><closed value=\"0\"/></subject></table></response>";
    private static final String VALID_TIMETABLE_XML_RESPONSE_V2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><response><table year=\"2024\" semester=\"2\" status=\"1\" identifier=\"Redte0RhSbJRibpczui0\"><subject id=\"7002187\"><internal value=\"540650-1\"/><name value=\"데이터사이언스\"/><professor value=\"오세종\"/><time value=\"수15~19(미디어103)\"><data day=\"2\" starttime=\"192\" endtime=\"226\" place=\"미디어103\"/></time><place value=\"\"/><credit value=\"3\"/><closed value=\"0\"/></subject></table></response>"; // 장소 변경
    private static final String EMPTY_SUBJECT_TIMETABLE_XML_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><response><table year=\"2024\" semester=\"2\" status=\"1\" identifier=\"Redte0RhSbJRibpczui0\"><subject></subject></table></response>";
    private static final String EMPTY_TIMETABLE_JSON_STRING = "{\"월요일\":[],\"화요일\":[],\"수요일\":[],\"목요일\":[],\"금요일\":[],\"토요일\":[],\"일요일\":[]}";

    @Nested
    class 시간표_생성_및_업데이트 {

        @Test
        void 새로운_시간표_생성에_성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            TimetableCreateRequest request = new TimetableCreateRequest(VALID_EVERYTIME_URL);

            ResponseEntity<String> mockResponse = new ResponseEntity<>(VALID_TIMETABLE_XML_RESPONSE_V1, HttpStatus.OK);
            when(restTemplate.exchange(any(), eq(HttpMethod.POST), any(), eq(String.class)))
                    .thenReturn(mockResponse);

            // when
            Map<String, List<LectureInfo>> timetable = timetableService.createOrUpdateTimetable(userDetails, request);

            // then
            Optional<Timetable> foundTimetable = timetableRepository.findByYearSemester(CURRENT_YEAR_SEMESTER).stream()
                    .filter(tt -> passwordEncoder.matches(member.getOidcId(), tt.getHashedOidcId()))
                    .findFirst();
            assertTrue(foundTimetable.get().getJsonData().contains("데이터사이언스")); // JSON data에 과목 정보가 포함되어 있는지 확인
            assertEquals(1, timetable.get("수요일").size());
            assertEquals("데이터사이언스", timetable.get("수요일").getFirst().name());
        }

        @Test
        void 기존_시간표_업데이트에_성공한다() throws JsonProcessingException {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            TimetableCreateRequest request = new TimetableCreateRequest(VALID_EVERYTIME_URL);

            // Mock 첫 번째 API 응답 (기존 시간표)
            ResponseEntity<String> mockResponseV1 = new ResponseEntity<>(VALID_TIMETABLE_XML_RESPONSE_V1, HttpStatus.OK);
            when(restTemplate.exchange(any(), eq(HttpMethod.POST), any(), eq(String.class)))
                    .thenReturn(mockResponseV1);

            // 처음 시간표 생성
            timetableService.createOrUpdateTimetable(userDetails, request);
            Timetable originalTimetable = timetableRepository.findAll().getFirst();
            String originalJsonData = originalTimetable.getJsonData();

            // Mock 두 번째 API 응답 (업데이트된 시간표 - 데이터사이언스 강의실을 변경)
            ResponseEntity<String> mockResponseV2 = new ResponseEntity<>(VALID_TIMETABLE_XML_RESPONSE_V2, HttpStatus.OK);
            when(restTemplate.exchange(any(), eq(HttpMethod.POST), any(), eq(String.class)))
                    .thenReturn(mockResponseV2);

            // when
            Map<String, List<LectureInfo>> updatedTimetable = timetableService.createOrUpdateTimetable(userDetails, request);

            // then
            Timetable updatedTimetableEntity = timetableRepository.findAll().getFirst();
            assertEquals(originalTimetable.getId(), updatedTimetableEntity.getId());
            assertNotEquals(originalJsonData, updatedTimetableEntity.getJsonData()); // JSON 데이터가 업데이트 되었는지 확인
            assertEquals(updatedTimetableEntity.getJsonData(), objectMapper.writeValueAsString(updatedTimetable));
        }

        @Test
        void 다른_계정의_identifier로_시간표_생성_시_identifier_중복_에러가_발생한다() {
            // given
            Member member1 = createMember();
            UserDetails userDetails1 = createUserDetails(member1);
            TimetableCreateRequest request1 = new TimetableCreateRequest(VALID_EVERYTIME_URL);
            ResponseEntity<String> mockResponse1 = new ResponseEntity<>(VALID_TIMETABLE_XML_RESPONSE_V1, HttpStatus.OK);
            when(restTemplate.exchange(any(), eq(HttpMethod.POST), any(), eq(String.class)))
                    .thenReturn(mockResponse1);
            timetableService.createOrUpdateTimetable(userDetails1, request1);

            Member member2 = createMember();
            UserDetails userDetails2 = createUserDetails(member2);
            TimetableCreateRequest request2 = new TimetableCreateRequest(VALID_EVERYTIME_URL);

            // when-then
            CustomException exception = assertThrows(CustomException.class, () -> {
                timetableService.createOrUpdateTimetable(userDetails2, request2);
            });
            assertEquals(ErrorCode.TIMETABLE_IDENTIFIER_ALREADY_EXISTS, exception.getErrorCode());
        }

        @Test
        void 시간표_정보는_있지만_과목이_없는_경우_빈_시간표를_생성한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            TimetableCreateRequest request = new TimetableCreateRequest(VALID_EVERYTIME_URL);

            ResponseEntity<String> mockResponse = new ResponseEntity<>(EMPTY_SUBJECT_TIMETABLE_XML_RESPONSE, HttpStatus.OK);
            when(restTemplate.exchange(any(), eq(HttpMethod.POST), any(), eq(String.class)))
                    .thenReturn(mockResponse);

            // when
            timetableService.createOrUpdateTimetable(userDetails, request);

            // then
            Optional<Timetable> foundTimetable = timetableRepository.findByYearSemester(CURRENT_YEAR_SEMESTER).stream()
                    .filter(tt -> passwordEncoder.matches(member.getOidcId(), tt.getHashedOidcId()))
                    .findFirst();
            assertEquals(EMPTY_TIMETABLE_JSON_STRING, foundTimetable.get().getJsonData());
        }
    }

    @Nested
    class 시간표_생성_실패 {
        @Test
        void URL_형식이_아니면_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            TimetableCreateRequest invalidRequest1 = new TimetableCreateRequest("invalidUrl");
            TimetableCreateRequest invalidRequest2 = new TimetableCreateRequest("https://everytime.kr/");

            // when-then
            CustomException exception1 = assertThrows(CustomException.class, () -> {
                timetableService.createOrUpdateTimetable(userDetails, invalidRequest1);
            });
            assertEquals(ErrorCode.TIMETABLE_INVALID_URL, exception1.getErrorCode());

            CustomException exception2 = assertThrows(CustomException.class, () -> {
                timetableService.createOrUpdateTimetable(userDetails, invalidRequest2);
            });
            assertEquals(ErrorCode.TIMETABLE_INVALID_URL, exception2.getErrorCode());
        }


        @Test
        void Everytime_API_호출에_실패하면_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            TimetableCreateRequest request = new TimetableCreateRequest(VALID_EVERYTIME_URL);

            when(restTemplate.exchange(any(), eq(HttpMethod.POST), any(), eq(String.class)))
                    .thenReturn(new ResponseEntity<>(HttpStatus.GATEWAY_TIMEOUT));

            // when-then
            CustomException exception = assertThrows(CustomException.class, () -> {
                timetableService.createOrUpdateTimetable(userDetails, request);
            });
            assertEquals(ErrorCode.TIMETABLE_FETCH_EVERYTIME_NOT_WORKING, exception.getErrorCode());
        }

        @Test
        void XML_파싱에_실패하면_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            TimetableCreateRequest request = new TimetableCreateRequest(VALID_EVERYTIME_URL);

            ResponseEntity<String> mockResponse = new ResponseEntity<>("Invalid XML", HttpStatus.OK);
            when(restTemplate.exchange(any(), eq(HttpMethod.POST), any(), eq(String.class)))
                    .thenReturn(mockResponse);

            // when-then
            CustomException exception = assertThrows(CustomException.class, () -> {
                timetableService.createOrUpdateTimetable(userDetails, request);
            });
            assertEquals(ErrorCode.TIMETABLE_PARSE_XML_FAILED, exception.getErrorCode());
        }

        @Test
        void 시간표_정보가_없는_경우_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            TimetableCreateRequest request = new TimetableCreateRequest(VALID_EVERYTIME_URL);

            ResponseEntity<String> mockResponse = new ResponseEntity<>(PRIVATE_TIMETABLE_XML_RESPONSE, HttpStatus.OK);
            when(restTemplate.exchange(any(), eq(HttpMethod.POST), any(), eq(String.class)))
                    .thenReturn(mockResponse);

            // when-then
            CustomException exception = assertThrows(CustomException.class, () -> {
                timetableService.createOrUpdateTimetable(userDetails, request);
            });
            assertEquals(ErrorCode.TIMETABLE_PARSE_PRIVATE, exception.getErrorCode());
        }

        @Test
        void JSON_변환에_실패하면_실패한다() throws JsonProcessingException {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            TimetableCreateRequest request = new TimetableCreateRequest(VALID_EVERYTIME_URL);

            ResponseEntity<String> mockResponse = new ResponseEntity<>(VALID_TIMETABLE_XML_RESPONSE_V1, HttpStatus.OK);
            when(restTemplate.exchange(any(), eq(HttpMethod.POST), any(), eq(String.class)))
                    .thenReturn(mockResponse);
            // ObjectMapper mock으로 주입하여 JSON 변환 시 에러 발생하도록 설정 (강제로 에러를 발생시키는 mock)
            ObjectMapper mockObjectMapper = Mockito.mock(ObjectMapper.class);
            TimetableService serviceWithMockObjectMapper = new TimetableService(timetableRepository, memberRepository, restTemplate, timetableHttpHeaders, jaxbContext, mockObjectMapper, passwordEncoder);
            when(mockObjectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("JSON 변환 실패"));

            // when-then
            CustomException exception = assertThrows(CustomException.class, () -> {
                serviceWithMockObjectMapper.createOrUpdateTimetable(userDetails, request);
            });
            assertEquals(ErrorCode.TIMETABLE_PARSE_JSON_FAILED, exception.getErrorCode());
        }
    }
}
