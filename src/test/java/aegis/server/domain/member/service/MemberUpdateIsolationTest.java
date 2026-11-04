package aegis.server.domain.member.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.ProfileIcon;
import aegis.server.domain.member.domain.Role;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.helper.IntegrationTestWithoutTransactional;

import static org.junit.jupiter.api.Assertions.*;

class MemberUpdateIsolationTest extends IntegrationTestWithoutTransactional {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PlatformTransactionManager transactionManager;

    /** 회비 납부로 승격되는 사이에 프로필을 저장해도 승격이 유실되면 납부 회원이 GUEST로 남습니다. */
    @Test
    void 프로필_아이콘_저장이_겹쳐도_승격이_유지된다() {
        // given
        Member saved = createMember();
        saved.demoteToGuest();
        Long memberId = memberRepository.save(saved).getId();
        TransactionTemplate outer = new TransactionTemplate(transactionManager);
        TransactionTemplate inner = new TransactionTemplate(transactionManager);
        inner.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // when
        outer.executeWithoutResult(status -> {
            Member stale = memberRepository.findById(memberId).orElseThrow();
            inner.executeWithoutResult(promotion -> {
                Member fresh = memberRepository.findById(memberId).orElseThrow();
                fresh.promoteToUser();
            });
            stale.updateProfileIcon(ProfileIcon.PYTHON);
        });

        // then
        Member result = memberRepository.findById(memberId).orElseThrow();
        assertEquals(Role.USER, result.getRole());
        assertEquals(ProfileIcon.PYTHON, result.getProfileIcon());
    }
}
