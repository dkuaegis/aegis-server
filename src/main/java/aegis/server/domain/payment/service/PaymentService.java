package aegis.server.domain.payment.service;

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
    private final MemberRepository memberRepository;
    private final IssuedCouponRepository issuedCouponRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public void createPendingPayment(PaymentRequest request, SessionUser sessionUser) {
        Member member = memberRepository.findById(sessionUser.getId()).orElseThrow();

        List<Payment> payments = paymentRepository.findAllByMemberAndCurrentSemester(member, CURRENT_SEMESTER);
        for (Payment payment : payments) {
            if (payment.getStatus().equals(PaymentStatus.COMPLETED)) {
                throw new IllegalStateException("완료된 결제 정보가 존재합니다.");
            } else if (payment.getStatus().equals(PaymentStatus.OVERPAID)) {
                throw new IllegalStateException("초과입금된 결제 정보가 존재합니다.");
            }
            if (payment.getStatus().equals(PaymentStatus.PENDING)) {
                payment.cancel();
            }
        }

        Payment payment = Payment.of(member);
        if (!request.getIssuedCouponIds().isEmpty()) {
            payment.useCoupons(
                    issuedCouponRepository.findAllById(request.getIssuedCouponIds())
            );
        }

        paymentRepository.save(payment);
    }

    public PaymentStatusResponse checkPaymentStatus(SessionUser sessionUser) {
        Member member = memberRepository.findById(sessionUser.getId()).orElseThrow();
        Long currentDepositAmount = transactionRepository.sumAmountByDepositorName(Payment.expectedDepositorName(member)).orElse(0L);

        Payment payment = paymentRepository.findByMemberAndCurrentSemester(member, CURRENT_SEMESTER)
                .orElseThrow();

        return PaymentStatusResponse.from(payment, BigDecimal.valueOf(currentDepositAmount));
    }
}
