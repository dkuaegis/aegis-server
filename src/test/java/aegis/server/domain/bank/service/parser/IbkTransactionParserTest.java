package aegis.server.domain.bank.service.parser;

import aegis.server.domain.bank.domain.Transaction;
import aegis.server.domain.bank.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IbkTransactionParserTest {

    private IbkTransactionParser parser;

    @BeforeEach
    void setUp() {
        parser = new IbkTransactionParser();
    }

    @Test
    @DisplayName("입금 거래를 정상적으로 파싱한다")
    void parseDepositTransaction() {
        // given
        String log = """
                [입금] 50000원 홍길동
                982-******-01-017
                12/17 14:30 /잔액 150000원""";

        // when
        Transaction transaction = parser.parse(log);

        // then
        assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(transaction.getAmount()).isEqualTo(50000);
        assertThat(transaction.getName()).isEqualTo("홍길동");
        assertThat(transaction.getBalance()).isEqualTo(150000);

        String expectedTimeStr = LocalDateTime.now().getYear() + "/12/17 14:30";
        LocalDateTime expectedTime = LocalDateTime.parse(
                expectedTimeStr,
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
        );
        assertThat(transaction.getTransactionTime()).isEqualTo(expectedTime);
    }

    @Test
    @DisplayName("출금 거래를 정상적으로 파싱한다")
    void parseWithdrawalTransaction() {
        // given
        String log = """
                [출금] 30000원 ATM출금
                982-******-01-017
                12/17 15:30 /잔액 120000원""";

        // when
        Transaction transaction = parser.parse(log);

        // then
        assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(transaction.getAmount()).isEqualTo(30000);
        assertThat(transaction.getName()).isEqualTo("ATM출금");
        assertThat(transaction.getBalance()).isEqualTo(120000);

        String expectedTimeStr = LocalDateTime.now().getYear() + "/12/17 15:30";
        LocalDateTime expectedTime = LocalDateTime.parse(
                expectedTimeStr,
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
        );
        assertThat(transaction.getTransactionTime()).isEqualTo(expectedTime);
    }

    @Test
    @DisplayName("잘못된 형식의 로그는 예외를 발생시킨다")
    void parseInvalidLog() {
        // given
        String invalidLog = """
                잘못된 형식의 로그
                입니다""";

        // when & then
        assertThatThrownBy(() -> parser.parse(invalidLog))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("IBK의 거래 내역은 3줄로 구성되어야 합니다");
    }

    @Test
    @DisplayName("잘못된 거래 유형은 예외를 발생시킨다")
    void parseInvalidTransactionType() {
        // given
        String invalidLog = """
                [송금] 50000원 홍길동
                982-******-01-017
                12/25 14:30 /잔액 150000원""";

        // when & then
        assertThatThrownBy(() -> parser.parse(invalidLog))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("거래유형, 거래금액, 이름을 추출할 수 없습니다");
    }

    @Test
    @DisplayName("잘못된 시간/잔액 형식은 예외를 발생시킨다")
    void parseInvalidTimeFormat() {
        // given
        String invalidLog = """
                [입금] 50000원 홍길동
                982-******-01-017
                2023년 12월 25일 /잔액 150000원""";

        // when & then
        assertThatThrownBy(() -> parser.parse(invalidLog))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("거래시간, 잔액을 추출할 수 없습니다");
    }
}
