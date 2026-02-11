package aegis.server.domain.payment.service.parser;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.payment.domain.Transaction;
import aegis.server.domain.payment.domain.TransactionType;

@RequiredArgsConstructor
public class IbkTransactionParser implements TransactionParser {

    private static final Pattern TX_TYPE_AMOUNT_NAME_PATTERN = Pattern.compile("^\\[(입금|출금)]\\s*([\\d,]+)원\\s*(.+)$");
    private static final Pattern TX_TIME_BALANCE_PATTERN =
            Pattern.compile("(\\d{2}/\\d{2}\\s+\\d{2}:\\d{2})\\s*/\\s*잔액\\s*([\\d,]+)원");

    private final Clock clock;

    @Override
    public Transaction parse(String transactionLog) {
        // 1. transactionLog를 줄바꿈 문자로 분리하여 lines 변수에 할당
        String[] lines = transactionLog.split("\n");

        // 2. 첫번째 줄에서 거래유형, 거래금액, 이름을 추출
        Matcher matcher = TX_TYPE_AMOUNT_NAME_PATTERN.matcher(lines[0]);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("거래유형, 거래금액, 이름을 추출할 수 없습니다");
        }

        TransactionType transactionType =
                switch (matcher.group(1)) {
                    case "입금" -> TransactionType.DEPOSIT;
                    case "출금" -> TransactionType.WITHDRAWAL;
                    default -> throw new IllegalArgumentException("알 수 없는 거래유형입니다");
                };

        BigDecimal amount = BigDecimal.valueOf(Long.parseLong(matcher.group(2).replace(",", "")));

        String name = matcher.group(3);

        // 3. 세번째 줄에서 거래시간, 잔액을 추출
        matcher = TX_TIME_BALANCE_PATTERN.matcher(lines[2]);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("거래시간, 잔액을 추출할 수 없습니다");
        }

        LocalDateTime currentTime = LocalDateTime.now(clock);
        LocalDateTime parsedTime = LocalDateTime.parse(
                currentTime.getYear() + "/" + matcher.group(1), DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));

        // IBK의 경우 거래 내역에 년도 정보가 없으므로
        // 12/31 23:59라고 적힌 거래가 그 다음 년도에 서버에서 파싱되면
        // Y년 12월 31일 23시 59분으로 파싱되야 할 것이, Y+1년 12월 31일 23시 59분으로 파싱됨
        // 따라서 파싱된 시각이 현재 시각보다 미래라면 작년으로 간주
        LocalDateTime transactionTime = parsedTime.isAfter(currentTime) ? parsedTime.minusYears(1) : parsedTime;

        BigDecimal balance = BigDecimal.valueOf(Long.parseLong(matcher.group(2).replace(",", "")));

        return Transaction.of(transactionTime, name, transactionType, amount, balance);
    }
}
