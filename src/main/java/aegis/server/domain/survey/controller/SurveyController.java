package aegis.server.domain.survey.controller;

import aegis.server.domain.survey.dto.SurveyRequest;
import aegis.server.domain.survey.service.SurveyService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.dto.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/survey")
public class SurveyController {

    private final SurveyService surveyService;

    @GetMapping("/{memberId}")
    public ResponseEntity<SurveyRequest> getSurvey(@PathVariable Long memberId) {
        SurveyRequest surveyRequest = surveyService.findByMemberId(memberId);
        return new ResponseEntity<>(surveyRequest, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<SurveyRequest> createSurvey(@Validated @RequestBody SurveyRequest surveyRequest, @LoginUser SessionUser sessionUser) {
        surveyService.save(surveyRequest, sessionUser);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SurveyRequest>> getAllSurveys() {
        List<SurveyRequest> allSurveys = surveyService.getAllSurveys();
        return new ResponseEntity<>(allSurveys, HttpStatus.OK);
    }
}