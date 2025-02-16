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
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TimetableService {

    private static final String[] DAYS = {"월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일"};

    private final TimetableRepository timetableRepository;
    private final MemberRepository memberRepository;

    private final RestTemplate restTemplate;
    private final HttpHeaders timetableHttpHeaders;

    private final JAXBContext jaxbContext;

    private final PasswordEncoder bcryptEncoder;

    @Transactional
    public Map<String, List<LectureInfo>> createOrUpdateTimetable(UserDetails userDetails, TimetableCreateRequest request) {
        // 1. 회원 정보 및 식별자 추출
        Member member = memberRepository.findById(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        String oidcId = member.getOidcId();
        String identifier = extractIdentifier(request.url());

        // 2. 시간표 API 호출 및 XML 파싱
        EverytimeResponse everytimeResponse = fetchAndParseTimetable(identifier);
        validateResponse(everytimeResponse);

        // 3. 과목 정보를 시간표 포맷으로 변환 후 JSON 문자열 생성
        Map<String, List<LectureInfo>> timetable = convertToTimetable(everytimeResponse.getTable().getSubjects());
        String timetableJsonString = convertTimetableToJson(timetable);

        // 4. 기존 시간표 검색 (식별자와 oidcId를 통한 검사)
        Timetable existingTimetable = findExistingTimetable(identifier, oidcId);

        // 5. 업데이트 또는 신규 저장 처리
        if (existingTimetable != null) {
            updateTimetable(existingTimetable, identifier, timetableJsonString);
        } else {
            saveNewTimetable(oidcId, identifier, timetableJsonString);
        }
        return timetable; // 반환값이 컨트롤러에서 사용되진 않지만 테스트 코드에서 사용되므로 유지
    }

    private EverytimeResponse fetchAndParseTimetable(String identifier) {
        String timetableXmlString = fetchTimetable(identifier);
        return parseTimetable(timetableXmlString);
    }

    private void validateResponse(EverytimeResponse everytimeResponse) {
        if (everytimeResponse.getTable() == null) {
            throw new CustomException(ErrorCode.TIMETABLE_PARSE_NOT_FOUND);
        }
        if (everytimeResponse.getTable().getSubjects() == null) {
            throw new CustomException(ErrorCode.TIMETABLE_PARSE_PRIVATE);
        }
    }

    private String convertTimetableToJson(Map<String, List<LectureInfo>> timetable) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(timetable);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.TIMETABLE_PARSE_JSON_FAILED);
        }
    }

    private Timetable findExistingTimetable(String identifier, String oidcId) {
        List<Timetable> currentSemesterTimetables = timetableRepository.findByYearSemester(CURRENT_YEAR_SEMESTER);
        // 1) identifier가 존재하는지 확인 (다른 계정이면 예외)
        for (Timetable tt : currentSemesterTimetables) {
            if (bcryptEncoder.matches(identifier, tt.getHashedIdentifier())) {
                if (!bcryptEncoder.matches(oidcId, tt.getHashedOidcId())) {
                    throw new CustomException(ErrorCode.TIMETABLE_IDENTIFIER_ALREADY_EXISTS);
                }
                return tt;
            }
        }
        // 2) 동일 계정(oidcId)으로 저장된 시간표가 있는지 확인
        for (Timetable tt : currentSemesterTimetables) {
            if (bcryptEncoder.matches(oidcId, tt.getHashedOidcId())) {
                return tt;
            }
        }
        return null;
    }

    private void updateTimetable(Timetable timetableToUpdate, String identifier, String timetableJsonString) {
        // 식별자가 변경되었으면 해시값 갱신
        if (!bcryptEncoder.matches(identifier, timetableToUpdate.getHashedIdentifier())) {
            timetableToUpdate.updateHashedIdentifier(bcryptEncoder.encode(identifier));
        }
        timetableToUpdate.updateJsonData(timetableJsonString);
    }

    private void saveNewTimetable(String oidcId, String identifier, String timetableJsonString) {
        String hashedOidcId = bcryptEncoder.encode(oidcId);
        String hashedIdentifier = bcryptEncoder.encode(identifier);
        Timetable newTimetable = Timetable.create(
                hashedOidcId,
                hashedIdentifier,
                timetableJsonString
        );
        timetableRepository.save(newTimetable);
    }

    private String extractIdentifier(String url) {
        // 예: "https://everytime.kr/@Redte0RhSbJRibpczui0"에서 "/@" 이후 문자열 추출
        String[] parts = url.split("/@");
        if (parts.length < 2) {
            throw new CustomException(ErrorCode.TIMETABLE_INVALID_URL);
        }
        return parts[1];
    }

    private String fetchTimetable(String identifier) {
        URI uri = UriComponentsBuilder
                .fromUriString("https://api.everytime.kr")
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

    private EverytimeResponse parseTimetable(String xml) {
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (EverytimeResponse) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw new CustomException(ErrorCode.TIMETABLE_PARSE_XML_FAILED);
        }
    }

    private Map<String, List<LectureInfo>> convertToTimetable(List<EverytimeResponse.Subject> subjects) {
        Map<String, List<LectureInfo>> timetable = new LinkedHashMap<>();
        // 요일 초기화
        for (String day : DAYS) {
            timetable.put(day, new ArrayList<>());
        }

        // 과목별 시간 데이터를 timetable 포맷으로 변환
        for (EverytimeResponse.Subject subject : subjects) {
            if (subject.getTime() == null || subject.getTime().getData().isEmpty()) {
                continue;
            }
            for (EverytimeResponse.Data timeData : subject.getTime().getData()) {
                String dayName = DAYS[timeData.getDay()];
                LectureInfo lecture = new LectureInfo(
                        subject.getName().getValue(),
                        subject.getProfessor().getValue(),
                        formatTime(timeData.getStartTime()) + "-" + formatTime(timeData.getEndTime()),
                        Integer.parseInt(subject.getCredit().getValue()),
                        timeData.getPlace() != null ? timeData.getPlace() : ""
                );
                timetable.get(dayName).add(lecture);
            }
        }

        // 각 요일별 강의 시작시간 기준 정렬
        for (List<LectureInfo> lectures : timetable.values()) {
            lectures.sort(Comparator.comparing(lecture -> lecture.time().split("-")[0]));
        }
        return timetable;
    }

    private String formatTime(Long timeValue) {
        long minutes = timeValue * 5;
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }
}
