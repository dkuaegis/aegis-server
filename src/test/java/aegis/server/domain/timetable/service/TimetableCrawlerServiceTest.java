package aegis.server.domain.timetable.service;

import aegis.server.domain.timetable.dto.external.EverytimeResponse;
import aegis.server.domain.timetable.dto.internal.LectureInfo;
import aegis.server.global.exception.CustomException;
import aegis.server.helper.IntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.JAXBContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class TimetableCrawlerServiceTest extends IntegrationTest {

    @Autowired
    TimetableCrawlerService timetableCrawlerService;

    @MockitoBean
    RestTemplate restTemplate;

    @Autowired
    HttpHeaders timetableHttpHeaders;

    @Autowired
    JAXBContext jaxbContext;

    @Autowired
    ObjectMapper objectMapper;

    private static final String VALID_TIMETABLE_XML_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><response><table year=\"2024\" semester=\"2\" status=\"1\" identifier=\"test\"><subject id=\"7002187\"><internal value=\"540650-1\"/><name value=\"데이터사이언스\"/><professor value=\"오세종\"/><time value=\"수15~19(미디어102)\"><data day=\"2\" starttime=\"192\" endtime=\"226\" place=\"미디어102\"/></time><place value=\"\"/><credit value=\"3\"/><closed value=\"0\"/></subject></table></response>";

    @Test
    void 시간표_데이터_가져오기_및_파싱_성공() {
        // given
        String identifier = "test_id";
        ResponseEntity<String> mockResponse = new ResponseEntity<>(VALID_TIMETABLE_XML_RESPONSE, HttpStatus.OK);
        when(restTemplate.exchange(any(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(mockResponse);

        // when
        EverytimeResponse response = timetableCrawlerService.fetchAndParseTimetable(identifier);

        // then
        assertNotNull(response);
        assertNotNull(response.getTable());
        assertNotNull(response.getTable().getSubjects());
        assertEquals(1, response.getTable().getSubjects().size());
    }

    @Test
    void 시간표_변환_성공() {
        // given
        ResponseEntity<String> mockResponse = new ResponseEntity<>(VALID_TIMETABLE_XML_RESPONSE, HttpStatus.OK);
        when(restTemplate.exchange(any(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(mockResponse);
        EverytimeResponse response = timetableCrawlerService.fetchAndParseTimetable("test_id");

        // when
        Map<String, List<LectureInfo>> timetable = timetableCrawlerService.convertToTimetable(response);

        // then
        assertNotNull(timetable.get("수요일"));
        assertEquals(1, timetable.get("수요일").size());
        LectureInfo lecture = timetable.get("수요일").get(0);
        assertEquals("데이터사이언스", lecture.name());
        assertEquals("오세종", lecture.professor());
        assertEquals("16:00-18:50", lecture.time());
        assertEquals("미디어102", lecture.place());
    }

    @Test
    void API_호출_실패시_예외발생() {
        // given
        String identifier = "test_id";
        when(restTemplate.exchange(any(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        // when & then
        assertThrows(CustomException.class, () ->
                timetableCrawlerService.fetchAndParseTimetable(identifier));
    }

    @Test
    void 비공개_시간표_접근시_예외발생() {
        // given
        String identifier = "test_id";
        String privateResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><response><table year=\"2024\" semester=\"2\" status=\"1\" identifier=\"test\"></table></response>";
        ResponseEntity<String> mockResponse = new ResponseEntity<>(privateResponse, HttpStatus.OK);
        when(restTemplate.exchange(any(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(mockResponse);

        // when & then
        EverytimeResponse response = timetableCrawlerService.fetchAndParseTimetable(identifier);
        assertThrows(CustomException.class, () ->
                timetableCrawlerService.validateResponse(response));
    }
}