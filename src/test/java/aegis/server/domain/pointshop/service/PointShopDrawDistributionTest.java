package aegis.server.domain.pointshop.service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.repository.PointAccountRepository;
import aegis.server.domain.pointshop.domain.PointShopItem;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

class PointShopDrawDistributionTest extends IntegrationTest {

    @Autowired
    PointShopService pointShopService;

    @Autowired
    PointAccountRepository pointAccountRepository;

    @Test
    void _200회_뽑기_분포가_가중치와_유의하게_일치한다() {
        // given
        Member member = createMember();
        UserDetails userDetails = createUserDetails(member);

        PointAccount account = pointAccountRepository.save(PointAccount.create(member));
        int draws = 200;
        int costPerDraw = 100;
        account.add(BigDecimal.valueOf((long) draws * costPerDraw));

        // when: 200회 뽑기 수행 후 결과 집계
        Map<PointShopItem, Integer> counts = new EnumMap<>(PointShopItem.class);
        for (int i = 0; i < draws; i++) {
            PointShopItem item = pointShopService.draw(userDetails).item();
            counts.merge(item, 1, Integer::sum);
        }

        // then: 각 아이템 빈도가 기대값의 3σ 범위 내에 있는지 검증 + 최종 결과 출력
        int totalWeight = 0;
        for (PointShopItem item : PointShopItem.values()) totalWeight += item.getWeight();

        StringBuilder sb = new StringBuilder();
        sb.append("\n==== PointShop Draw Distribution (" + draws + " draws) ====/\n");
        sb.append(String.format("totalWeight=%d\n", totalWeight));

        double chiSquare = 0.0;

        for (PointShopItem item : PointShopItem.values()) {
            int observed = counts.getOrDefault(item, 0);
            double p = (double) item.getWeight() / (double) totalWeight;
            double expected = draws * p;
            double sd = Math.sqrt(draws * p * (1.0 - p));
            double z = sd == 0 ? 0 : (observed - expected) / sd;

            long lower = (long) Math.max(0, Math.floor(expected - 3.0 * sd));
            long upper = (long) Math.ceil(expected + 3.0 * sd);

            // 출력용 라인
            sb.append(String.format(
                    "%-24s weight=%4d p=%6.3f expected=%6.2f observed=%3d z=%6.2f range=[%d,%d]\n",
                    item.name(), item.getWeight(), p, expected, observed, z, lower, upper));

            // 검증: 3σ 범위 체크
            assertTrue(
                    observed >= lower && observed <= upper,
                    () -> String.format(
                            "[%s] 관측=%d, 기대=%.2f, 허용범위=[%d,%d] (p=%.3f)",
                            item.name(), observed, expected, lower, upper, p));

            // χ² 통계 누적(리포팅 용도)
            if (expected > 0) {
                chiSquare += Math.pow(observed - expected, 2) / expected;
            }
        }

        sb.append(String.format("chiSquare=%.3f df=%d\n", chiSquare, PointShopItem.values().length - 1));
        System.out.println(sb);
    }
}
