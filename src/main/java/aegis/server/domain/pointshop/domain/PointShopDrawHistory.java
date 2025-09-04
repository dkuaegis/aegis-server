package aegis.server.domain.pointshop.domain;

import jakarta.persistence.*;

import lombok.*;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.point.domain.PointTransaction;

@Entity
@Table(
        indexes =
                @Index(
                        name = "idx_point_shop_draw_history_member_id_id",
                        columnList = "member_id, point_shop_draw_history_id"))
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointShopDrawHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_shop_draw_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointShopItem item;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_transaction_id")
    private PointTransaction pointTransaction;

    public static PointShopDrawHistory create(Member member, PointShopItem item, PointTransaction pointTransaction) {
        return PointShopDrawHistory.builder()
                .member(member)
                .item(item)
                .pointTransaction(pointTransaction)
                .build();
    }
}
