package aegis.server.domain.payment.service;

import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.coupon.repository.IssuedCouponRepository;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Student;
import aegis.server.domain.member.repository.StudentRepository;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.dto.request.PaymentRequest;
import aegis.server.domain.payment.dto.response.PaymentStatusResponse;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.domain.payment.repository.TransactionRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final IssuedCouponRepository issuedCouponRepository;
    private final StudentRepository studentRepository;

    public PaymentStatusResponse checkPaymentStatus(UserDetails userDetails) {
        Student student = studentRepository.findByMemberIdInCurrentYearSemester(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        Payment payment = paymentRepository.findByStudentInCurrentYearSemester(student)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        BigDecimal currentDepositAmount = transactionRepository.sumAmountByDepositorName(payment.getExpectedDepositorName());

        return PaymentStatusResponse.from(payment, currentDepositAmount);
    }

    @Transactional
    public void createOrUpdatePendingPayment(PaymentRequest request, UserDetails userDetails) {
        Student student = studentRepository.findByMemberIdInCurrentYearSemester(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        Payment payment = paymentRepository.findByStudentInCurrentYearSemester(student)
                .orElseGet(() -> createNewPayment(student));

        validatePaymentStatus(payment);
        applyCouponsIfPresent(payment, request.getIssuedCouponIds());
    }

    private Payment createNewPayment(Student student) {
        Payment payment = Payment.of(student);
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
        if (!issuedCouponIds.isEmpty()) {
            Member member = payment.getStudent().getMember();
            List<IssuedCoupon> validIssuedCoupons = new ArrayList<>();
            for (Long issuedCouponId : issuedCouponIds) {
                IssuedCoupon issuedCoupon = issuedCouponRepository.findByIdAndMember(issuedCouponId, member)
                        .orElseThrow(() -> new CustomException(ErrorCode.ISSUED_COUPON_NOT_FOUND_FOR_MEMBER));
                validIssuedCoupons.add(issuedCoupon);
            }
            payment.applyCoupons(validIssuedCoupons);
        }
    }
}
