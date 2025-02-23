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

        // 식별자 중복 검사
        if (timetableRepository.existsByIdentifierAndYearSemester(identifier, CURRENT_YEAR_SEMESTER)) {
            throw new CustomException(ErrorCode.TIMETABLE_IDENTIFIER_ALREADY_EXISTS);
        }

        // 크롤러를 통해 시간표 데이터 가져오기 및 변환
        EverytimeResponse response = timetableCrawlerService.fetchAndParseTimetable(identifier);
        timetableCrawlerService.validateResponse(response);
        Map<String, List<LectureInfo>> timetable = timetableCrawlerService.convertToTimetable(response);
        String timetableJsonString = timetableCrawlerService.convertTimetableToJson(timetable);

        // 기존 시간표 업데이트 또는 신규 생성
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
