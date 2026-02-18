package aegis.server.domain.member.controller;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.dto.response.AdminMemberRecordPageResponse;
import aegis.server.domain.member.dto.response.AdminMemberRecordTimelineResponse;
import aegis.server.domain.member.dto.response.MemberRecordBackfillResponse;
import aegis.server.domain.member.service.MemberRecordService;

@Tag(name = "Admin Member Record", description = "관리자 회원 기록 API")
@Validated
@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class AdminMemberRecordController {

    private final MemberRecordService memberRecordService;

    @Operation(
            summary = "학기별 회원 기록 조회",
            description = "관리자가 특정 학기의 회원 기록을 페이지네이션으로 조회합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "회원 기록 조회 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
                @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content)
            })
    @GetMapping("/records")
    public ResponseEntity<AdminMemberRecordPageResponse> getMemberRecords(
            @RequestParam YearSemester yearSemester,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size) {
        AdminMemberRecordPageResponse response =
                memberRecordService.getMemberRecordsByYearSemester(yearSemester, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "회원별 기록 타임라인 조회",
            description = "관리자가 특정 회원의 학기별 기록 타임라인을 조회합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "회원 기록 타임라인 조회 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
                @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content),
                @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음", content = @Content)
            })
    @GetMapping("/{memberId}/records")
    public ResponseEntity<List<AdminMemberRecordTimelineResponse>> getMemberRecordTimeline(
            @PathVariable @Positive Long memberId) {
        List<AdminMemberRecordTimelineResponse> response = memberRecordService.getMemberRecordTimeline(memberId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "결제 완료 기반 회원 기록 백필",
            description = "관리자가 결제 완료 데이터를 기준으로 회원 기록을 멱등적으로 백필합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "회원 기록 백필 성공"),
                @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content)
            })
    @PostMapping("/records/backfill")
    public ResponseEntity<MemberRecordBackfillResponse> backfillMemberRecords() {
        MemberRecordBackfillResponse response = memberRecordService.backfillFromCompletedPayments();
        return ResponseEntity.ok(response);
    }
}
