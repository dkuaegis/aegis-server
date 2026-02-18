package aegis.server.domain.point.controller;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.point.domain.PointTransactionType;
import aegis.server.domain.point.dto.request.AdminPointBatchGrantRequest;
import aegis.server.domain.point.dto.request.AdminPointGrantRequest;
import aegis.server.domain.point.dto.response.AdminPointBatchGrantResultResponse;
import aegis.server.domain.point.dto.response.AdminPointGrantResultResponse;
import aegis.server.domain.point.dto.response.AdminPointLedgerPageResponse;
import aegis.server.domain.point.dto.response.AdminPointMemberPointResponse;
import aegis.server.domain.point.dto.response.AdminPointMemberSearchResponse;
import aegis.server.domain.point.service.AdminPointService;

@Tag(name = "Admin Point", description = "관리자 포인트 관리 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/points")
public class AdminPointController {

    private final AdminPointService adminPointService;

    @Operation(
            summary = "통합 포인트 원장 조회",
            description = "관리자가 전체 포인트 거래 원장을 필터와 페이지네이션으로 조회합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "원장 조회 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 조회 조건", content = @Content),
                @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content)
            })
    @GetMapping("/ledger")
    public ResponseEntity<AdminPointLedgerPageResponse> getLedger(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String memberKeyword,
            @RequestParam(required = false) PointTransactionType transactionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        AdminPointLedgerPageResponse response =
                adminPointService.getLedger(page, size, memberKeyword, transactionType, from, to);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "회원별 포인트 상세 조회",
            description = "관리자가 특정 회원의 포인트 잔액/누적 적립/최근 거래 내역을 조회합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "회원 포인트 조회 성공"),
                @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content),
                @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음", content = @Content)
            })
    @GetMapping("/members/{memberId}")
    public ResponseEntity<AdminPointMemberPointResponse> getMemberPoint(
            @Parameter(description = "조회할 회원 ID") @PathVariable Long memberId) {
        AdminPointMemberPointResponse response = adminPointService.getMemberPoint(memberId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "수동 지급 대상 회원 검색",
            description = "학번 또는 이름 키워드로 수동 지급 대상 회원 목록을 검색합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "회원 검색 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
                @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content)
            })
    @GetMapping("/members/search")
    public ResponseEntity<List<AdminPointMemberSearchResponse>> searchMembers(
            @RequestParam @Size(min = 2, max = 100) String keyword,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit) {
        List<AdminPointMemberSearchResponse> response = adminPointService.searchMembers(keyword, limit);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "포인트 수동 지급(단건)",
            description = "관리자가 특정 회원에게 포인트를 수동 지급합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "수동 지급 처리 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
                @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content),
                @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음", content = @Content)
            })
    @PostMapping("/grants")
    public ResponseEntity<AdminPointGrantResultResponse> grant(@Valid @RequestBody AdminPointGrantRequest request) {
        AdminPointGrantResultResponse response = adminPointService.grant(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "포인트 수동 지급(일괄)",
            description = "관리자가 여러 회원에게 포인트를 일괄 지급하고 회원별 처리 결과를 반환합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "일괄 지급 처리 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
                @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content)
            })
    @PostMapping("/grants/batch")
    public ResponseEntity<AdminPointBatchGrantResultResponse> grantBatch(
            @Valid @RequestBody AdminPointBatchGrantRequest request) {
        AdminPointBatchGrantResultResponse response = adminPointService.grantBatch(request);
        return ResponseEntity.ok(response);
    }
}
