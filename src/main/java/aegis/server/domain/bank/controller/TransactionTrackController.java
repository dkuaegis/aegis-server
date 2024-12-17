package aegis.server.domain.bank.controller;

import aegis.server.domain.bank.service.TransactionTrackService;
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
