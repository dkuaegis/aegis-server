package aegis.server.domain.payment.service;

import java.math.BigDecimal;
import java.util.List;

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
        Payment payment = paymentRepository
                .findByMemberIdInCurrentYearSemester(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        BigDecimal currentDepositAmount = transactionRepository.sumAmountByDepositorName(
                payment.getMember().getName());

        return PaymentStatusResponse.from(payment, currentDepositAmount);
    }

    @Transactional
    public void createPayment(PaymentRequest request, UserDetails userDetails) {
        validateNoPendingPayment(userDetails.getMemberId());
        validateUsableCoupons(userDetails.getMemberId(), request.getIssuedCouponIds());

        Member member = memberRepository
                .findById(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Payment payment = Payment.of(member);
        applyCoupons(payment, request.getIssuedCouponIds());
        paymentRepository.save(payment);

        if (payment.getFinalPrice().equals(BigDecimal.ZERO)) {
            payment.confirmPayment(PaymentStatus.COMPLETED);
            applicationEventPublisher.publishEvent(new PaymentCompletedEvent(PaymentInfo.from(payment)));
        }
    }

    @Transactional
    public void updatePayment(PaymentRequest request, UserDetails userDetails) {
        Payment payment = paymentRepository
                .findByMemberIdAndCurrentYearSemesterAndStatusIsPending(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        payment.validateMutable();
        validateUsableCoupons(userDetails.getMemberId(), request.getIssuedCouponIds());

        applyCoupons(payment, request.getIssuedCouponIds());
    }

    private void validateNoPendingPayment(Long memberId) {
        if (paymentRepository
                .findByMemberIdAndCurrentYearSemesterAndStatusIsPending(memberId)
                .isPresent()) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }
    }

    private void validateUsableCoupons(Long memberId, List<Long> issuedCouponIds) {
        long validIssuedCouponCount = issuedCouponRepository.countByIdInAndMemberIdAndValid(issuedCouponIds, memberId);
        if (validIssuedCouponCount != issuedCouponIds.size()) {
            throw new CustomException(ErrorCode.INVALID_ISSUED_COUPON);
        }
    }

    private void applyCoupons(Payment payment, List<Long> issuedCouponIds) {
        List<IssuedCoupon> issuedCoupons = issuedCouponRepository.findByIdInAndMemberIdAndValid(
                issuedCouponIds, payment.getMember().getId());
        payment.applyCoupons(issuedCoupons);
    }
}
