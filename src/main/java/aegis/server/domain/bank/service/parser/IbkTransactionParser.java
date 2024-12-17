package aegis.server.domain.bank.service.parser;

import aegis.server.domain.bank.domain.Transaction;
import aegis.server.domain.bank.domain.TransactionType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IbkTransactionParser implements TransactionParser {

    private static final Pattern TX_TYPE_AMOUNT_NAME_PATTERN =
            Pattern.compile("^\\[(입금|출금)]\\s*(\\d+)원\\s*(.+)$");
    private static final Pattern TX_TIME_BALANCE_PATTERN =
            Pattern.compile("(\\d{2}/\\d{2}\\s+\\d{2}:\\d{2})\\s*/\\s*잔액\\s*(\\d+)원");

    @Override
    public Transaction parse(String transactionLog) {
        // 1. transactionLog를 줄바꿈 문자로 분리하여 lines 변수에 할당
        String[] lines = transactionLog.split("\n");
        if (lines.length != 3) {
            throw new IllegalArgumentException("IBK의 거래 내역은 3줄로 구성되어야 합니다");
        }

        // 2. 첫번째 줄에서 거래유형, 거래금액, 이름을 추출
        Matcher matcher = TX_TYPE_AMOUNT_NAME_PATTERN.matcher(lines[0]);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("거래유형, 거래금액, 이름을 추출할 수 없습니다");
        }

        TransactionType transactionType = switch (matcher.group(1)) {
            case "입금" -> TransactionType.DEPOSIT;
            case "출금" -> TransactionType.WITHDRAWAL;
            default -> throw new IllegalArgumentException("알 수 없는 거래유형입니다");
        };

        Long amount = Long.parseLong(matcher.group(2));

        String depositorName = matcher.group(3);

        // 3. 세번째 줄에서 거래시간, 잔액을 추출
        matcher = TX_TIME_BALANCE_PATTERN.matcher(lines[2]);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("거래시간, 잔액을 추출할 수 없습니다");
        }

        String timeStr = LocalDateTime.now().getYear() + "/" + matcher.group(1);
        LocalDateTime transactionTime = LocalDateTime.parse(
                timeStr,
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
        );

        Long balance = Long.parseLong(matcher.group(2));

        return Transaction.builder()
                .transactionTime(transactionTime)
                .depositorName(depositorName)
                .transactionType(transactionType)
                .amount(amount)
                .balance(balance)
                .build();
    }
}
