package aegis.server.domain.pointshop.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointShopItem {
    ENERGY_DRINK(610),
    COFFEE_LOW(320),
    CLUB_DUES_DISCOUNT_COUPON(55),
    COFFEE_HIGH(10),
    CHICKEN(5);

    private final Integer weight;
}
