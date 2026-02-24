package aegis.server.domain.payment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;

public interface PaymentQueryRepository {

    Page<Payment> searchAdminPayments(
            YearSemester yearSemester, PaymentStatus status, String memberKeyword, Pageable pageable);
}
