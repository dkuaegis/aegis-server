package aegis.server.domain.pointshop.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointShopItem {
    ENERGY_DRINK(420),
    COFFEE_LOW(330),
    CLUB_DUES_DISCOUNT_COUPON(150),
    COFFEE_HIGH(75),
    CHICKEN(25);

    private final Integer weight;
}
