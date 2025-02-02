package aegis.server.domain.survey.controller;

import aegis.server.domain.survey.dto.SurveyCommon;
import aegis.server.domain.survey.service.SurveyService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/survey")
public class SurveyController {

    private final SurveyService surveyService;

    @GetMapping
    public ResponseEntity<SurveyCommon> getSurvey(
            @LoginUser UserDetails userDetails
    ) {
        return ResponseEntity.ok(surveyService.getSurvey(userDetails));
    }

    @PostMapping
    public ResponseEntity<Void> createOrUpdateSurvey(
            @LoginUser UserDetails userDetails,
            @Valid @RequestBody SurveyCommon request
    ) {
        surveyService.createOrUpdateSurvey(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
