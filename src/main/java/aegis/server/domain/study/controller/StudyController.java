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

import aegis.server.domain.study.dto.request.StudyCreateUpdateRequest;
import aegis.server.domain.study.service.StudyService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;

@Tag(name = "Study", description = "스터디 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/studies")
public class StudyController {

    private final StudyService studyService;

    @Operation(
            summary = "스터디 생성",
            description = "새로운 스터디를 생성합니다. 생성한 사용자는 자동으로 스터디장이 됩니다.",
            responses = {
                @ApiResponse(responseCode = "201", description = "스터디 생성 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
                @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
                @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음", content = @Content),
                @ApiResponse(responseCode = "409", description = "이미 존재하는 스터디", content = @Content)
            })
    @PostMapping
    public ResponseEntity<Void> createStudy(
            @Parameter(hidden = true) @LoginUser UserDetails userDetails,
            @Valid @RequestBody StudyCreateUpdateRequest request) {
        studyService.createStudy(request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "스터디 수정",
            description = "기존 스터디 정보를 수정합니다. 스터디장만 수정할 수 있습니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "스터디 수정 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
                @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
                @ApiResponse(responseCode = "403", description = "스터디 수정 권한 없음", content = @Content),
                @ApiResponse(responseCode = "404", description = "스터디 또는 사용자 정보를 찾을 수 없음", content = @Content)
            })
    @PutMapping("/{studyId}")
    public ResponseEntity<Void> updateStudy(
            @PathVariable Long studyId,
            @Parameter(hidden = true) @LoginUser UserDetails userDetails,
            @Valid @RequestBody StudyCreateUpdateRequest request) {
        studyService.updateStudy(studyId, request, userDetails);
        return ResponseEntity.ok().build();
    }
}
