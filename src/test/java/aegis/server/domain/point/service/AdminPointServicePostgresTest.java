package aegis.server.domain.point.service;

import java.math.BigDecimal;
import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.domain.PointTransaction;
import aegis.server.domain.point.domain.PointTransactionType;
import aegis.server.domain.point.dto.response.AdminPointLedgerPageResponse;
import aegis.server.domain.point.repository.PointAccountRepository;
import aegis.server.domain.point.repository.PointTransactionRepository;
import aegis.server.helper.IntegrationTestWithoutTransactional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("postgres")
class AdminPointServicePostgresTest extends IntegrationTestWithoutTransactional {

    @Autowired
    AdminPointService adminPointService;

    @Autowired
    PointAccountRepository pointAccountRepository;

    @Autowired
    PointTransactionRepository pointTransactionRepository;

    @Test
    void 통합_원장_조회는_null_필터_조건에서도_postgres에서_정상_동작한다() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertEquals("PostgreSQL", connection.getMetaData().getDatabaseProductName());
        }

        // given
        Member member = createMember();
        PointAccount account = pointAccountRepository.save(PointAccount.create(member));
        account.add(BigDecimal.valueOf(100));
        pointAccountRepository.save(account);
        pointTransactionRepository.save(
                PointTransaction.create(account, PointTransactionType.EARN, BigDecimal.valueOf(100), "테스트 적립"));

        // when
        AdminPointLedgerPageResponse response =
                assertDoesNotThrow(() -> adminPointService.getLedger(0, 50, null, null, null, null));

        // then
        assertTrue(response.content().stream().anyMatch(item -> item.memberId().equals(member.getId())));
    }

    @Autowired
    DataSource dataSource;
}
