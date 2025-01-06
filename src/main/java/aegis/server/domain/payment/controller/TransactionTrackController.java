package aegis.server.domain.payment.controller;

import aegis.server.domain.payment.service.TransactionTrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transaction-track")
@RequiredArgsConstructor
public class TransactionTrackController {

    private final TransactionTrackService transactionTrackService;

    @PostMapping("/ibk")
    public void ibkTransactionTrack(@RequestBody String request) {
        transactionTrackService.parseAndLogTransaction(request);
    }
}
