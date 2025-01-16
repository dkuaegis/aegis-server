package aegis.server.domain.payment.repository;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByMemberAndCurrentSemester(Member member, String currentSemester);

    List<Payment> findAllByMemberAndCurrentSemester(Member member, String currentSemester);

    Optional<Payment> findByExpectedDepositorNameAndCurrentSemesterAndStatus(String expectedDepositorName, String currentSemester, PaymentStatus status);
}
