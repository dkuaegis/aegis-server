package aegis.server.domain.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.payment.dto.request.PaymentRequest;
import aegis.server.domain.payment.dto.response.PaymentResponse;
import aegis.server.domain.payment.dto.response.PaymentStatusResponse;
import aegis.server.domain.payment.service.PaymentService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;

@Tag(name = "Payment", description = "결제 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(
            summary = "결제 요청 생성",
            description = "쿠폰을 사용하여 결제 요청을 생성합니다.",
            responses = {
                @ApiResponse(responseCode = "201", description = "결제 요청 생성 성공"),
                @ApiResponse(responseCode = "400", description = "쿠폰이 해당 사용자에게 발급되지 않음", content = @Content),
                @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
                @ApiResponse(responseCode = "404", description = "학생 정보를 찾을 수 없음", content = @Content),
                @ApiResponse(responseCode = "409", description = "이미 PENDING 상태의 결제가 존재함", content = @Content)
            })
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestBody PaymentRequest request, @Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        PaymentResponse response = paymentService.createPayment(request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "결제 요청 수정",
            description = "기존 PENDING 상태의 결제 요청을 수정합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "결제 요청 수정 성공"),
                @ApiResponse(responseCode = "400", description = "쿠폰이 해당 사용자에게 발급되지 않음", content = @Content),
                @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
                @ApiResponse(responseCode = "404", description = "PENDING 상태의 결제 정보를 찾을 수 없음", content = @Content),
                @ApiResponse(responseCode = "409", description = "이미 완료된 결제이거나 초과 결제 상태", content = @Content)
            })
    @PutMapping
    public ResponseEntity<PaymentResponse> updatePayment(
            @RequestBody PaymentRequest request, @Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        PaymentResponse response = paymentService.updatePayment(request, userDetails);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "결제 상태 조회",
            description = "로그인한 사용자의 결제 상태를 조회합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "결제 상태 조회 성공"),
                @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
                @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음", content = @Content)
            })
    @GetMapping("/status")
    public ResponseEntity<PaymentStatusResponse> checkPaymentStatus(
            @Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        return ResponseEntity.ok(paymentService.checkPaymentStatus(userDetails));
    }
}
