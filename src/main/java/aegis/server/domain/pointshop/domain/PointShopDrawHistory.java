package aegis.server.domain.pointshop.domain;

import jakarta.persistence.*;

import lombok.*;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.point.domain.PointTransaction;

@Entity
@Table(
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_point_shop_draw_history_point_transaction",
                    columnNames = "point_transaction_id")
        })
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
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "fk_point_shop_draw_history_member"))
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointShopItem item;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "point_transaction_id",
            foreignKey = @ForeignKey(name = "fk_point_shop_draw_history_point_transaction"))
    private PointTransaction pointTransaction;

    public static PointShopDrawHistory create(Member member, PointShopItem item, PointTransaction pointTransaction) {
        return PointShopDrawHistory.builder()
                .member(member)
                .item(item)
                .pointTransaction(pointTransaction)
                .build();
    }
}
