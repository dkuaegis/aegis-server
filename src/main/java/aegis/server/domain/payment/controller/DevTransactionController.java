package aegis.server.domain.payment.controller;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.payment.dto.request.DevTransactionCreateRequest;
import aegis.server.domain.payment.dto.response.DevTransactionResponse;
import aegis.server.domain.payment.service.DevTransactionService;

@Tag(name = "Dev Transaction", description = "개발 환경용 거래 시뮬레이션 API")
@RestController
@Profile({"dev", "local", "test"})
@RequiredArgsConstructor
@RequestMapping("/dev/transactions")
public class DevTransactionController {

    private final DevTransactionService devTransactionService;

    @Operation(
            summary = "개발용 거래 생성",
            description = "개발 환경에서 은행 거래를 시뮬레이션합니다. 입금자명과 금액을 지정하여 거래를 생성할 수 있습니다.",
            responses = {
                @ApiResponse(responseCode = "201", description = "거래 생성 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content)
            })
    @PostMapping
    public ResponseEntity<DevTransactionResponse> createTransaction(
            @Valid @RequestBody DevTransactionCreateRequest request) {
        DevTransactionResponse response = devTransactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
