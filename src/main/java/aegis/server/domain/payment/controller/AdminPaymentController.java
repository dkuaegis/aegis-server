package aegis.server.domain.payment.controller;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.domain.TransactionType;
import aegis.server.domain.payment.dto.response.AdminPaymentItemResponse;
import aegis.server.domain.payment.dto.response.AdminPaymentPageResponse;
import aegis.server.domain.payment.dto.response.AdminTransactionPageResponse;
import aegis.server.domain.payment.service.AdminPaymentService;

@Tag(name = "Admin Payment", description = "관리자 결제 관리 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/payments")
public class AdminPaymentController {

    private final AdminPaymentService adminPaymentService;

    @Operation(
            summary = "결제 목록 조회",
            description = "관리자가 결제 목록을 필터와 페이지네이션으로 조회합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "결제 목록 조회 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
                @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content)
            })
    @GetMapping
    public ResponseEntity<AdminPaymentPageResponse> getPayments(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size,
            @RequestParam(required = false) YearSemester yearSemester,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String memberKeyword,
            @RequestParam(required = false) String sort) {
        AdminPaymentPageResponse response =
                adminPaymentService.getPayments(page, size, yearSemester, status, memberKeyword, sort);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "결제 강제 완료 처리",
            description = "관리자가 PENDING 상태 결제를 강제로 COMPLETED 처리합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "강제 완료 성공"),
                @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content),
                @ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음", content = @Content),
                @ApiResponse(responseCode = "409", description = "이미 완료된 결제", content = @Content)
            })
    @PatchMapping("/{paymentId}/complete")
    public ResponseEntity<AdminPaymentItemResponse> forceCompletePayment(@PathVariable @Positive Long paymentId) {
        AdminPaymentItemResponse response = adminPaymentService.forceCompletePayment(paymentId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "거래 목록 조회",
            description = "관리자가 입출금 거래 목록을 필터와 페이지네이션으로 조회합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "거래 목록 조회 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
                @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content)
            })
    @GetMapping("/transactions")
    public ResponseEntity<AdminTransactionPageResponse> getTransactions(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size,
            @RequestParam(required = false) YearSemester yearSemester,
            @RequestParam(required = false) TransactionType transactionType,
            @RequestParam(required = false) String depositorKeyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String sort) {
        AdminTransactionPageResponse response = adminPaymentService.getTransactions(
                page, size, yearSemester, transactionType, depositorKeyword, from, to, sort);
        return ResponseEntity.ok(response);
    }
}
