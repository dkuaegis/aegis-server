package aegis.server.domain.timetable.service;

import aegis.server.domain.timetable.dto.external.EverytimeResponse;
import aegis.server.domain.timetable.dto.internal.LectureInfo;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TimetableCrawlerService {

    private static final String[] DAYS = {"월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일"};
    private static final String TIMETABLE_API_BASE = "https://api.everytime.kr";

    private final RestTemplate restTemplate;
    private final HttpHeaders timetableHttpHeaders;
    private final JAXBContext jaxbContext;
    private final ObjectMapper objectMapper;

    public EverytimeResponse fetchAndParseTimetable(String identifier) {
        String xml = fetchTimetableXml(identifier);
        return parseTimetableXml(xml);
    }

    private String fetchTimetableXml(String identifier) {
        URI uri = UriComponentsBuilder.fromUriString(TIMETABLE_API_BASE)
                .path("/find/timetable/table/friend")
                .queryParam("friendInfo", false)
                .queryParam("identifier", identifier)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();

        ResponseEntity<String> response = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                new HttpEntity<>("", timetableHttpHeaders),
                String.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new CustomException(ErrorCode.TIMETABLE_FETCH_EVERYTIME_NOT_WORKING);
        }
        return response.getBody();
    }

    private EverytimeResponse parseTimetableXml(String xml) {
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (EverytimeResponse) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw new CustomException(ErrorCode.TIMETABLE_PARSE_XML_FAILED);
        }
    }

    public void validateResponse(EverytimeResponse response) {
        if (response.getTable() == null) {
            throw new CustomException(ErrorCode.TIMETABLE_PARSE_NOT_FOUND);
        }
        if (response.getTable().getSubjects() == null) {
            throw new CustomException(ErrorCode.TIMETABLE_PARSE_PRIVATE);
        }
    }

    public Map<String, List<LectureInfo>> convertToTimetable(EverytimeResponse response) {
        Map<String, List<LectureInfo>> timetable = new LinkedHashMap<>();
        // 요일별 리스트 초기화
        for (String day : DAYS) {
            timetable.put(day, new ArrayList<>());
        }

        response.getTable().getSubjects().stream()
                .filter(subject -> subject.getTime() != null && !subject.getTime().getData().isEmpty())
                .forEach(subject -> {
                    subject.getTime().getData().forEach(timeData -> {
                        String dayName = DAYS[timeData.getDay()];
                        LectureInfo lecture = new LectureInfo(
                                subject.getName().getValue(),
                                subject.getProfessor().getValue(),
                                formatTime(timeData.getStartTime()) + "-" + formatTime(timeData.getEndTime()),
                                Integer.parseInt(subject.getCredit().getValue()),
                                timeData.getPlace() != null ? timeData.getPlace() : ""
                        );
                        timetable.get(dayName).add(lecture);
                    });
                });

        // 각 요일별 강의 시작시간 기준 정렬
        timetable.values().forEach(lectures -> lectures.sort(Comparator.comparing(
                lecture -> lecture.time().split("-")[0])));
        return timetable;
    }

    public String convertTimetableToJson(Map<String, List<LectureInfo>> timetable) {
        try {
            return objectMapper.writeValueAsString(timetable);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.TIMETABLE_PARSE_JSON_FAILED);
        }
    }

    private String formatTime(Long timeValue) {
        long minutes = timeValue * 5;
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }
}
