package aegis.server.domain.payment.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
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
import aegis.server.domain.payment.dto.response.PaymentResponse;
import aegis.server.domain.payment.dto.response.PaymentStatusResponse;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final IssuedCouponRepository issuedCouponRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${email-restriction.admin-email}")
    private String adminEmail;

    public PaymentStatusResponse checkPaymentStatus(UserDetails userDetails) {
        if (userDetails.getEmail().equals(adminEmail)) {
            return new PaymentStatusResponse(PaymentStatus.COMPLETED, BigDecimal.ZERO);
        }

        Payment payment = paymentRepository
                .findByMemberIdInCurrentYearSemester(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        return PaymentStatusResponse.from(payment);
    }

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request, UserDetails userDetails) {
        Member member = memberRepository
                .findByIdWithLock(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        validateNoPaymentInCurrentSemester(userDetails.getMemberId());
        List<IssuedCoupon> issuedCoupons =
                getUsableCouponsWithLock(userDetails.getMemberId(), request.issuedCouponIds());

        Payment payment = Payment.of(member);
        applyCoupons(payment, issuedCoupons);
        paymentRepository.save(payment);

        if (payment.getFinalPrice().compareTo(BigDecimal.ZERO) == 0) {
            payment.completePayment();
            paymentRepository.saveAndFlush(payment);
            applicationEventPublisher.publishEvent(new PaymentCompletedEvent(PaymentInfo.from(payment)));
        }

        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse updatePayment(PaymentRequest request, UserDetails userDetails) {
        Payment payment = paymentRepository
                .findByMemberIdAndCurrentYearSemesterAndStatusIsPendingWithLock(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
        List<IssuedCoupon> issuedCoupons =
                getUsableCouponsWithLock(userDetails.getMemberId(), request.issuedCouponIds());
        applyCoupons(payment, issuedCoupons);

        if (payment.getFinalPrice().compareTo(BigDecimal.ZERO) == 0) {
            payment.completePayment();
            paymentRepository.saveAndFlush(payment);
            applicationEventPublisher.publishEvent(new PaymentCompletedEvent(PaymentInfo.from(payment)));
        }

        return PaymentResponse.from(payment);
    }

    private void validateNoPaymentInCurrentSemester(Long memberId) {
        if (paymentRepository.existsByMemberIdAndCurrentYearSemester(memberId)) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }
    }

    private List<IssuedCoupon> getUsableCouponsWithLock(Long memberId, List<Long> issuedCouponIds) {
        if (issuedCouponIds.isEmpty()) {
            return List.of();
        }
        List<IssuedCoupon> issuedCoupons =
                issuedCouponRepository.findByIdInAndMemberIdAndValidWithLock(issuedCouponIds, memberId);
        if (issuedCoupons.size() != issuedCouponIds.size()) {
            throw new CustomException(ErrorCode.INVALID_ISSUED_COUPON_INCLUDED);
        }
        return issuedCoupons;
    }

    private void applyCoupons(Payment payment, List<IssuedCoupon> issuedCoupons) {
        payment.applyCoupons(issuedCoupons);
    }
}
