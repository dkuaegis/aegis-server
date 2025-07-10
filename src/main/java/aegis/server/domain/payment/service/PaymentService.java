package aegis.server.domain.payment.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.coupon.repository.IssuedCouponRepository;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.domain.event.PaymentCompletedEvent;
import aegis.server.domain.payment.dto.internal.PaymentInfo;
import aegis.server.domain.payment.dto.request.PaymentRequest;
import aegis.server.domain.payment.dto.response.PaymentStatusResponse;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.domain.payment.repository.TransactionRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final IssuedCouponRepository issuedCouponRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public PaymentStatusResponse checkPaymentStatus(UserDetails userDetails) {
        Member member = memberRepository
                .findById(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Payment payment = paymentRepository
                .findByMemberInCurrentYearSemester(member)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        BigDecimal currentDepositAmount = transactionRepository.sumAmountByDepositorName(
                payment.getMember().getName());

        return PaymentStatusResponse.from(payment, currentDepositAmount);
    }

    @Transactional
    public void createOrUpdatePendingPayment(PaymentRequest request, UserDetails userDetails) {
        Member member = memberRepository
                .findById(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Payment payment =
                paymentRepository.findByMemberInCurrentYearSemester(member).orElseGet(() -> createNewPayment(member));

        validatePaymentStatus(payment);
        applyCouponsIfPresent(payment, request.getIssuedCouponIds());

        if (payment.getFinalPrice().equals(BigDecimal.ZERO)) {
            payment.confirmPayment(PaymentStatus.COMPLETED);
            applicationEventPublisher.publishEvent(new PaymentCompletedEvent(PaymentInfo.from(payment)));
        }
    }

    private Payment createNewPayment(Member member) {
        Payment payment = Payment.of(member);
        return paymentRepository.save(payment);
    }

    private void validatePaymentStatus(Payment payment) {
        if (payment.getStatus().equals(PaymentStatus.COMPLETED)) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_COMPLETED);
        }
        if (payment.getStatus().equals(PaymentStatus.OVERPAID)) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_OVER_PAID);
        }
    }

    private void applyCouponsIfPresent(Payment payment, List<Long> issuedCouponIds) {
        Member member = payment.getMember();
        List<IssuedCoupon> validIssuedCoupons = issuedCouponIds.stream()
                .map(issuedCouponId -> issuedCouponRepository
                        .findByIdAndMember(issuedCouponId, member)
                        .orElseThrow(() -> new CustomException(ErrorCode.ISSUED_COUPON_NOT_FOUND_FOR_MEMBER)))
                .collect(Collectors.toList());
        payment.applyCoupons(validIssuedCoupons);
    }
}
