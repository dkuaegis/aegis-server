package aegis.server.global.config;

import aegis.server.domain.bank.service.parser.IbkTransactionParser;
import aegis.server.domain.bank.service.parser.TransactionParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BankConfig {

    @Value("${bank.type}")
    private String bankType;

    @Bean
    public TransactionParser transactionParser() {
        return switch (bankType) {
            case "ibk" -> new IbkTransactionParser();
            default -> throw new IllegalStateException("지원하지 않는 은행입니다: " + bankType);
        };
    }

}
