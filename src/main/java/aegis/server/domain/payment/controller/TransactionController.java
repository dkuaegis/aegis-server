package aegis.server.domain.payment.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.payment.service.TransactionService;

@Tag(name = "Transaction (Internal)", description = "내부 거래 처리 API - 외부 시스템에서만 호출")
@RestController
@RequestMapping("/internal/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Hidden
    @Operation(
            summary = "거래 생성",
            description = "외부 시스템에서 거래 데이터를 생성합니다. (내부 API)",
            responses = {
                @ApiResponse(responseCode = "200", description = "거래 생성 성공"),
                @ApiResponse(responseCode = "400", description = "거래 데이터 형식 오류", content = @Content),
                @ApiResponse(responseCode = "500", description = "거래 로그 파싱 실패 또는 데이터베이스 오류", content = @Content)
            })
    @PostMapping
    public void createTransaction(@RequestBody String request) {
        transactionService.createTransaction(request);
    }
}
