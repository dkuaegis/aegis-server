package aegis.server.domain.payment.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.coupon.repository.IssuedCouponRepository;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.event.PaymentCompletedEvent;
import aegis.server.domain.payment.dto.internal.PaymentInfo;
import aegis.server.domain.payment.dto.request.DevPaymentCreateRequest;
import aegis.server.domain.payment.dto.request.DevPaymentUpdateRequest;
import aegis.server.domain.payment.dto.response.DevPaymentResponse;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;

@Service
@Profile({"dev", "local", "test"})
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DevPaymentService {

    private final PaymentRepository paymentRepository;
    private final IssuedCouponRepository issuedCouponRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public List<DevPaymentResponse> getMyAllPayments(UserDetails userDetails) {
        List<Payment> payments =
                paymentRepository.findAllByMemberIdOrderByYearSemesterDescCreatedAtDesc(userDetails.getMemberId());

        return payments.stream().map(DevPaymentResponse::from).toList();
    }

    @Transactional
    public DevPaymentResponse createPayment(DevPaymentCreateRequest request, UserDetails userDetails) {
        validateNoPendingPaymentInCurrentSemester(userDetails.getMemberId());
        validateUsableCoupons(userDetails.getMemberId(), request.issuedCouponIds());

        Member member = memberRepository
                .findById(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Payment payment = Payment.createForDev(member, request.status(), request.yearSemester());
        applyCoupons(payment, request.issuedCouponIds());
        paymentRepository.save(payment);

        if (payment.getFinalPrice().compareTo(BigDecimal.ZERO) == 0) {
            payment.completePayment();
            paymentRepository.saveAndFlush(payment);
            applicationEventPublisher.publishEvent(new PaymentCompletedEvent(PaymentInfo.from(payment)));
        }

        return DevPaymentResponse.from(payment);
    }

    @Transactional
    public DevPaymentResponse updatePayment(Long paymentId, DevPaymentUpdateRequest request, UserDetails userDetails) {
        validateUsableCoupons(userDetails.getMemberId(), request.issuedCouponIds());

        Payment payment = paymentRepository
                .findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getMember().getId().equals(userDetails.getMemberId())) {
            throw new CustomException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        payment.updateForDev(request.status(), request.yearSemester());
        applyCoupons(payment, request.issuedCouponIds());

        if (payment.getFinalPrice().compareTo(BigDecimal.ZERO) == 0) {
            payment.completePayment();
            paymentRepository.saveAndFlush(payment);
            applicationEventPublisher.publishEvent(new PaymentCompletedEvent(PaymentInfo.from(payment)));
        }

        return DevPaymentResponse.from(payment);
    }

    @Transactional
    public void deletePayment(Long paymentId, UserDetails userDetails) {
        Payment payment = paymentRepository
                .findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getMember().getId().equals(userDetails.getMemberId())) {
            throw new CustomException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        // 결제에 연결된 쿠폰이 있으면 우선 연결을 해제한다(FK 제약 회피, 데이터 정합성 유지)
        issuedCouponRepository.findAllByPaymentId(paymentId).forEach(IssuedCoupon::detachFromPayment);

        paymentRepository.delete(payment);
    }

    private void validateNoPendingPaymentInCurrentSemester(Long memberId) {
        if (paymentRepository.existsByMemberIdAndCurrentYearSemesterAndStatusIsPending(memberId)) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }
    }

    private void validateUsableCoupons(Long memberId, List<Long> issuedCouponIds) {
        long validIssuedCouponCount = issuedCouponRepository.countValidByIdInAndMemberId(issuedCouponIds, memberId);
        if (validIssuedCouponCount != issuedCouponIds.size()) {
            throw new CustomException(ErrorCode.INVALID_ISSUED_COUPON_INCLUDED);
        }
    }

    private void applyCoupons(Payment payment, List<Long> issuedCouponIds) {
        List<IssuedCoupon> issuedCoupons = issuedCouponRepository.findByIdInAndMemberIdAndValid(
                issuedCouponIds, payment.getMember().getId());
        payment.applyCoupons(issuedCoupons);
    }
}
