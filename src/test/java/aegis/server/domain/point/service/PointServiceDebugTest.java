package aegis.server.domain.point.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.repository.PointAccountRepository;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

class PointServiceDebugTest extends IntegrationTest {

    @Autowired
    PointAccountRepository pointAccountRepository;

    @Autowired
    PointService pointService;

    @Test
    void debug_포인트_계정_생성_및_조회() {
        PointAccount pointAccount = PointAccount.create(createMember());
        System.out.println(pointAccount.getBalance());
        pointAccountRepository.save(pointAccount);
        pointAccount.add(BigDecimal.ONE);
        System.out.println(pointAccount.getBalance());
    }
}
