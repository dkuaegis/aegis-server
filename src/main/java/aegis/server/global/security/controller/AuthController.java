package aegis.server.global.security.controller;

import aegis.server.domain.member.domain.Student;
import aegis.server.domain.member.repository.StudentRepository;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;

    // TODO: 전면 리팩토링 필요
    @GetMapping("/auth/check")
    public ResponseEntity<AuthCheckResponse> check(@LoginUser UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Student student = studentRepository.findByMemberIdInCurrentYearSemester(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));
        Optional<Payment> optionalPayment = paymentRepository.findByStudentInCurrentYearSemester(student);

        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            return ResponseEntity.status(HttpStatus.OK).body(new AuthCheckResponse(payment.getStatus()));
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(new AuthCheckResponse(PaymentStatus.PENDING));
        }
    }
}
