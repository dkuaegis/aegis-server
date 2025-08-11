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
        Member member = memberRepository
                .findById(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Payment payment = Payment.createForDev(member, request.getStatus(), request.getYearSemester());
        applyCoupons(payment, request.getIssuedCouponIds());
        paymentRepository.save(payment);

        if (payment.getFinalPrice().equals(BigDecimal.ZERO)) {
            payment.completePayment();
            applicationEventPublisher.publishEvent(new PaymentCompletedEvent(PaymentInfo.from(payment)));
        }

        return DevPaymentResponse.from(payment);
    }

    @Transactional
    public DevPaymentResponse updatePayment(Long paymentId, DevPaymentUpdateRequest request, UserDetails userDetails) {
        Payment payment = paymentRepository
                .findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getMember().getId().equals(userDetails.getMemberId())) {
            throw new CustomException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        payment.updateForDev(request.getStatus(), request.getYearSemester());
        applyCoupons(payment, request.getIssuedCouponIds());

        if (payment.getFinalPrice().equals(BigDecimal.ZERO)) {
            payment.completePayment();
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

        paymentRepository.delete(payment);
    }

    private void applyCoupons(Payment payment, List<Long> issuedCouponIds) {
        if (issuedCouponIds == null || issuedCouponIds.isEmpty()) {
            payment.applyCoupons(List.of());
            return;
        }

        List<IssuedCoupon> issuedCoupons = issuedCouponRepository.findByIdInAndMemberIdAndValid(
                issuedCouponIds, payment.getMember().getId());
        payment.applyCoupons(issuedCoupons);
    }
}
