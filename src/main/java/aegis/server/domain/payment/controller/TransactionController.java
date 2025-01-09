package aegis.server.domain.payment.controller;

import aegis.server.domain.payment.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transaction-track")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/ibk")
    public void ibkTransactionTrack(@RequestBody String request) {
        transactionService.createTransaction(request);
    }
}
