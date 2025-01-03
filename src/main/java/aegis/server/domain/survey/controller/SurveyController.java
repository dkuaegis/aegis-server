package aegis.server.domain.survey.controller;

import aegis.server.domain.survey.domain.SurveyDto;
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
    public ResponseEntity<SurveyDto> getSurvey(@PathVariable Long memberId) {
        SurveyDto surveyDto = surveyService.findByMemberId(memberId);
        return new ResponseEntity<>(surveyDto, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<SurveyDto> createSurvey(@Validated @RequestBody SurveyDto surveyDto, @LoginUser SessionUser sessionUser) {
        surveyService.save(surveyDto, sessionUser);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SurveyDto>> getAllSurveys() {
        List<SurveyDto> allSurveys = surveyService.getAllSurveys();
        return new ResponseEntity<>(allSurveys, HttpStatus.OK);
    }
}