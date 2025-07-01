package aegis.server.global.config;

import java.time.Clock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import aegis.server.domain.payment.service.parser.IbkTransactionParser;
import aegis.server.domain.payment.service.parser.TransactionParser;

@Configuration
public class BankConfig {

    @Value("${payment.bank-type}")
    private String bankType;

    @Bean
    public TransactionParser transactionParser(Clock clock) {
        return switch (bankType) {
            case "ibk" -> new IbkTransactionParser(clock);
            default -> throw new IllegalStateException("지원하지 않는 은행입니다: " + bankType);
        };
    }

}
