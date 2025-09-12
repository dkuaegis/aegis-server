package aegis.server.domain.study.controller;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.study.dto.request.AttendanceMarkRequest;
import aegis.server.domain.study.dto.response.AttendanceMarkResponse;
import aegis.server.domain.study.service.StudyParticipantService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;

@Tag(name = "Study Participant", description = "스터디원용 API")
@RestController
@RequiredArgsConstructor
public class StudyParticipantController {

    private final StudyParticipantService studyParticipantService;

    @Operation(
            summary = "출석 처리",
            description = "스터디원이 오늘 세션의 출석 코드를 입력해 출석을 완료합니다.",
            responses = {
                @ApiResponse(responseCode = "201", description = "출석 완료"),
                @ApiResponse(responseCode = "400", description = "잘못된 출석 코드", content = @Content),
                @ApiResponse(responseCode = "403", description = "스터디원이 아님", content = @Content),
                @ApiResponse(responseCode = "404", description = "오늘 세션 없음", content = @Content),
                @ApiResponse(responseCode = "409", description = "이미 출석 완료", content = @Content)
            })
    @PostMapping("/studies/{studyId}/attendance")
    public ResponseEntity<AttendanceMarkResponse> markAttendance(
            @PathVariable Long studyId,
            @Parameter(hidden = true) @LoginUser UserDetails userDetails,
            @Valid @RequestBody AttendanceMarkRequest request) {
        AttendanceMarkResponse response = studyParticipantService.markAttendance(studyId, request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
