package aegis.server.domain.survey.controller;

import aegis.server.domain.survey.domain.SurveyDto;
import aegis.server.domain.survey.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    /**
     * /api/survey/1
     */
    @GetMapping("/api/survey/{memberId}")
    public ResponseEntity<SurveyDto> getSurvey(@PathVariable Long memberId) {
        SurveyDto surveyDto = surveyService.findByMemberId(memberId);
        return new ResponseEntity<>(surveyDto, HttpStatus.OK);
    }

    /**
     * {
     *     "memberId": 1,
     *     "interestFields": [
     *         "SECURITY_WEBHACKING",
     *         "WEB_FRONTEND"
     *     ],
     *     "registrationReason": "JUST DO IT",
     *     "feedBack": "GOOD"
     * }
     */
    @PostMapping("/api/survey")
    public ResponseEntity<SurveyDto> survey(@Validated @RequestBody SurveyDto surveyDto) {
        SurveyDto save = surveyService.save(surveyDto);
        return new ResponseEntity<>(save, HttpStatus.CREATED);
    }
}