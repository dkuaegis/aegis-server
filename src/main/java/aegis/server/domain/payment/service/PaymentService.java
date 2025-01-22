package aegis.server.domain.payment.service;

import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.coupon.repository.IssuedCouponRepository;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.dto.request.PaymentRequest;
import aegis.server.domain.payment.dto.response.PaymentStatusResponse;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.domain.payment.repository.TransactionRepository;
import aegis.server.global.security.dto.SessionUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static aegis.server.global.constant.Constant.CURRENT_SEMESTER;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final MemberRepository memberRepository;
    private final IssuedCouponRepository issuedCouponRepository;

    @Transactional
    public void createOrUpdatePendingPayment(PaymentRequest request, SessionUser sessionUser) {
        Member member = memberRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        Payment payment = paymentRepository
                .findByMemberAndCurrentSemester(member, CURRENT_SEMESTER)
                .orElseGet(() -> createNewPayment(member));

        validatePaymentStatus(payment);
        applyCouponsIfPresent(payment, request.getIssuedCouponIds());
    }

    public PaymentStatusResponse checkPaymentStatus(SessionUser sessionUser) {
        Member member = memberRepository.findById(sessionUser.getId()).orElseThrow();

        Payment payment = paymentRepository.findByMemberAndCurrentSemester(member, CURRENT_SEMESTER)
                .orElseThrow();

        BigDecimal currentDepositAmount = transactionRepository.sumAmountByDepositorName(payment.getExpectedDepositorName());

        return PaymentStatusResponse.from(payment, currentDepositAmount);
    }

    private Payment createNewPayment(Member member) {
        Payment payment = Payment.of(member);
        return paymentRepository.save(payment);
    }

    private void validatePaymentStatus(Payment payment) {
        if (payment.getStatus().equals(PaymentStatus.COMPLETED)) {
            throw new IllegalStateException("완료된 결제 정보가 존재합니다.");
        }
        if (payment.getStatus().equals(PaymentStatus.OVERPAID)) {
            throw new IllegalStateException("초과입금된 결제 정보가 존재합니다.");
        }
    }

    private void applyCouponsIfPresent(Payment payment, List<Long> issuedCouponIds) {
        if (!issuedCouponIds.isEmpty()) {
            List<IssuedCoupon> coupons = issuedCouponRepository.findAllById(issuedCouponIds);
            payment.useCoupons(coupons);
        }
    }
}
