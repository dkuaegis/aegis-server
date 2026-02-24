package aegis.server.domain.payment.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.domain.Transaction;
import aegis.server.domain.payment.domain.TransactionType;
import aegis.server.domain.payment.domain.event.PaymentCompletedEvent;
import aegis.server.domain.payment.dto.internal.PaymentInfo;
import aegis.server.domain.payment.dto.response.AdminPaymentItemResponse;
import aegis.server.domain.payment.dto.response.AdminPaymentPageResponse;
import aegis.server.domain.payment.dto.response.AdminTransactionPageResponse;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.domain.payment.repository.TransactionRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPaymentService {

    private static final int MAX_PAGE_SIZE = 100;

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AdminPaymentPageResponse getPayments(
            int page, int size, YearSemester yearSemester, PaymentStatus status, String memberKeyword, String sort) {
        int normalizedSize = Math.min(size, MAX_PAGE_SIZE);
        String normalizedKeyword = normalizeKeyword(memberKeyword);
        String orderByClause = resolvePaymentOrderBy(sort);
        PageRequest pageRequest = PageRequest.of(page, normalizedSize);

        Page<Payment> paymentPage = paymentRepository.searchAdminPayments(
                yearSemester, status, normalizedKeyword, pageRequest, orderByClause);
        return AdminPaymentPageResponse.from(paymentPage);
    }

    public AdminTransactionPageResponse getTransactions(
            int page,
            int size,
            YearSemester yearSemester,
            TransactionType transactionType,
            String depositorKeyword,
            LocalDate from,
            LocalDate to,
            String sort) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        int normalizedSize = Math.min(size, MAX_PAGE_SIZE);
        String normalizedKeyword = normalizeKeyword(depositorKeyword);
        String orderByClause = resolveTransactionOrderBy(sort);
        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.plusDays(1).atStartOfDay();
        PageRequest pageRequest = PageRequest.of(page, normalizedSize);

        Page<Transaction> transactionPage = transactionRepository.searchAdminTransactions(
                yearSemester, transactionType, normalizedKeyword, fromDateTime, toDateTime, pageRequest, orderByClause);
        return AdminTransactionPageResponse.from(transactionPage);
    }

    @Transactional
    public AdminPaymentItemResponse forceCompletePayment(Long paymentId) {
        Payment payment = paymentRepository
                .findByIdWithLock(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_COMPLETED);
        }

        PaymentStatus previousStatus = payment.getStatus();
        payment.completePayment();
        paymentRepository.saveAndFlush(payment);
        applicationEventPublisher.publishEvent(new PaymentCompletedEvent(PaymentInfo.from(payment)));

        log.info(
                "[AdminPaymentService] 관리자 결제 강제 완료: paymentId={}, memberId={}, status={} -> {}",
                payment.getId(),
                payment.getMember().getId(),
                previousStatus,
                payment.getStatus());

        return AdminPaymentItemResponse.from(payment);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }

        String trimmedKeyword = keyword.trim();
        return trimmedKeyword.isEmpty() ? null : trimmedKeyword;
    }

    private String resolvePaymentOrderBy(String sort) {
        if (sort == null || sort.isBlank()) {
            return "p.id DESC";
        }

        return switch (sort.trim().toLowerCase()) {
            case "id,asc" -> "p.id ASC";
            case "id,desc" -> "p.id DESC";
            case "createdat,asc" -> "p.createdAt ASC, p.id ASC";
            case "createdat,desc" -> "p.createdAt DESC, p.id DESC";
            case "finalprice,asc" -> "p.finalPrice ASC, p.id ASC";
            case "finalprice,desc" -> "p.finalPrice DESC, p.id DESC";
            case "status,asc" -> "p.status ASC, p.id ASC";
            case "status,desc" -> "p.status DESC, p.id DESC";
            case "membername,asc" -> "m.name ASC, p.id ASC";
            case "membername,desc" -> "m.name DESC, p.id DESC";
            default -> throw new CustomException(ErrorCode.BAD_REQUEST);
        };
    }

    private String resolveTransactionOrderBy(String sort) {
        if (sort == null || sort.isBlank()) {
            return "t.transactionTime DESC, t.id DESC";
        }

        return switch (sort.trim().toLowerCase()) {
            case "id,asc" -> "t.id ASC";
            case "id,desc" -> "t.id DESC";
            case "transactiontime,asc" -> "t.transactionTime ASC, t.id ASC";
            case "transactiontime,desc" -> "t.transactionTime DESC, t.id DESC";
            case "amount,asc" -> "t.amount ASC, t.id ASC";
            case "amount,desc" -> "t.amount DESC, t.id DESC";
            case "balance,asc" -> "t.balance ASC, t.id ASC";
            case "balance,desc" -> "t.balance DESC, t.id DESC";
            case "depositorname,asc" -> "t.depositorName ASC, t.id ASC";
            case "depositorname,desc" -> "t.depositorName DESC, t.id DESC";
            default -> throw new CustomException(ErrorCode.BAD_REQUEST);
        };
    }
}
