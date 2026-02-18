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
import aegis.server.domain.member.domain.Role;
import aegis.server.domain.member.dto.response.AdminMemberRecordPageResponse;
import aegis.server.domain.member.dto.response.AdminMemberRecordSemesterOptionResponse;
import aegis.server.domain.member.dto.response.AdminMemberRecordTimelineResponse;
import aegis.server.domain.member.dto.response.AdminMemberSemesterActivityDetailResponse;
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
            description = "관리자가 특정 학기의 회원 기록을 검색/필터/정렬과 함께 페이지네이션으로 조회합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "회원 기록 조회 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
                @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content)
            })
    @GetMapping("/records")
    public ResponseEntity<AdminMemberRecordPageResponse> getMemberRecords(
            @RequestParam YearSemester yearSemester,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String sort) {
        AdminMemberRecordPageResponse response =
                memberRecordService.getMemberRecordsByYearSemester(yearSemester, page, size, keyword, role, sort);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "회원 기록 조회 학기 옵션 조회",
            description = "관리자 화면에서 사용할 전체 학기 옵션 목록을 조회합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "학기 옵션 조회 성공"),
                @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content)
            })
    @GetMapping("/records/meta/semesters")
    public ResponseEntity<List<AdminMemberRecordSemesterOptionResponse>> getMemberRecordSemesters() {
        List<AdminMemberRecordSemesterOptionResponse> response = memberRecordService.getMemberRecordSemesterOptions();
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
            summary = "회원별 학기 활동 상세 조회",
            description = "관리자가 특정 회원의 특정 학기 활동(스터디 참여/출석/활동 참여) 상세를 조회합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "회원 학기 활동 상세 조회 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
                @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content),
                @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음", content = @Content)
            })
    @GetMapping("/{memberId}/activities")
    public ResponseEntity<AdminMemberSemesterActivityDetailResponse> getMemberSemesterActivities(
            @PathVariable @Positive Long memberId, @RequestParam YearSemester yearSemester) {
        AdminMemberSemesterActivityDetailResponse response =
                memberRecordService.getMemberSemesterActivityDetail(memberId, yearSemester);
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
