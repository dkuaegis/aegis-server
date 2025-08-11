package aegis.server.domain.payment.controller;

import java.util.List;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.payment.dto.request.DevPaymentCreateRequest;
import aegis.server.domain.payment.dto.request.DevPaymentUpdateRequest;
import aegis.server.domain.payment.dto.response.DevPaymentResponse;
import aegis.server.domain.payment.service.DevPaymentService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;

@Tag(name = "Dev Payment", description = "개발 환경용 결제 관리 API")
@RestController
@Profile({"dev", "local", "test"})
@RequiredArgsConstructor
@RequestMapping("/dev/payments")
public class DevPaymentController {

    private final DevPaymentService devPaymentService;

    @Operation(
            summary = "개발용 결제 생성",
            description = "개발 환경에서 결제를 생성합니다. 쿠폰 ID, 결제 상태, 학기를 지정할 수 있습니다.",
            responses = {
                @ApiResponse(responseCode = "201", description = "결제 생성 성공"),
                @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
                @ApiResponse(responseCode = "404", description = "학생 정보를 찾을 수 없음", content = @Content)
            })
    @PostMapping
    public ResponseEntity<DevPaymentResponse> createPayment(
            @Valid @RequestBody DevPaymentCreateRequest request,
            @Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        DevPaymentResponse response = devPaymentService.createPayment(request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "개발용 결제 수정",
            description = "개발 환경에서 결제를 수정합니다. 본인의 결제만 수정할 수 있습니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "결제 수정 성공"),
                @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
                @ApiResponse(responseCode = "403", description = "권한이 없음", content = @Content),
                @ApiResponse(responseCode = "404", description = "결제 정보를 찾을 수 없음", content = @Content)
            })
    @PutMapping("/{id}")
    public ResponseEntity<DevPaymentResponse> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody DevPaymentUpdateRequest request,
            @Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        DevPaymentResponse response = devPaymentService.updatePayment(id, request, userDetails);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "본인의 모든 결제 조회",
            description = "학기 구분 없이 본인 명의로 된 모든 결제를 조회합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "결제 조회 성공"),
                @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content)
            })
    @GetMapping("/my")
    public ResponseEntity<List<DevPaymentResponse>> getMyAllPayments(
            @Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        List<DevPaymentResponse> responses = devPaymentService.getMyAllPayments(userDetails);
        return ResponseEntity.ok(responses);
    }

    @Operation(
            summary = "개발용 결제 삭제",
            description = "개발 환경에서 결제를 삭제합니다. 본인의 결제만 삭제할 수 있습니다.",
            responses = {
                @ApiResponse(responseCode = "204", description = "결제 삭제 성공"),
                @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
                @ApiResponse(responseCode = "403", description = "권한이 없음", content = @Content),
                @ApiResponse(responseCode = "404", description = "결제 정보를 찾을 수 없음", content = @Content)
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(
            @PathVariable Long id, @Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        devPaymentService.deletePayment(id, userDetails);
        return ResponseEntity.noContent().build();
    }
}
