package aegis.server.domain.member.service;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import org.junit.jupiter.api.Test;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.MemberRecordSource;
import aegis.server.domain.member.dto.response.AdminMemberRecordPageResponse;
import aegis.server.helper.IntegrationTestWithoutTransactional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("postgres")
class MemberRecordServicePostgresTest extends IntegrationTestWithoutTransactional {

    @Autowired
    MemberRecordService memberRecordService;

    @Autowired
    DataSource dataSource;

    @Test
    void 학기별_회원기록_조회는_null_필터_조건에서도_postgres에서_정상_동작한다() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertEquals("PostgreSQL", connection.getMetaData().getDatabaseProductName());
        }

        // given
        Member member = createMember();
        memberRecordService.createMemberRecordIfAbsent(
                member.getId(), YearSemester.YEAR_SEMESTER_2026_1, MemberRecordSource.BACKFILL_PAYMENT);

        // when
        AdminMemberRecordPageResponse response =
                assertDoesNotThrow(() -> memberRecordService.getMemberRecordsByYearSemester(
                        YearSemester.YEAR_SEMESTER_2026_1, 0, 50, null, null, null));

        // then
        assertTrue(response.content().stream().anyMatch(item -> item.memberId().equals(member.getId())));
    }

    @Test
    void 학기별_회원기록_조회는_keyword_필터_조건에서도_postgres에서_정상_동작한다() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertEquals("PostgreSQL", connection.getMetaData().getDatabaseProductName());
        }

        // given
        Member member = createMember();
        memberRecordService.createMemberRecordIfAbsent(
                member.getId(), YearSemester.YEAR_SEMESTER_2026_1, MemberRecordSource.BACKFILL_PAYMENT);

        // when
        AdminMemberRecordPageResponse response =
                assertDoesNotThrow(() -> memberRecordService.getMemberRecordsByYearSemester(
                        YearSemester.YEAR_SEMESTER_2026_1, 0, 50, "테스트사용자이름", null, "name,asc"));

        // then
        assertEquals(1, response.content().size());
        assertEquals(member.getName(), response.content().getFirst().snapshotName());
    }
}
