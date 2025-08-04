package aegis.server.domain.point.domain;

import java.math.BigDecimal;

import jakarta.persistence.*;

import lombok.*;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.member.domain.Member;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = @Index(name = "idx_point_account_total_earned_desc", columnList = "total_earned DESC"))
public class PointAccount extends BaseEntity {

    @Id
    @Column(name = "point_account_id")
    private Long id; // memberId와 동일

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(precision = 10, scale = 0)
    private BigDecimal balance;

    @Column(precision = 10, scale = 0)
    private BigDecimal totalEarned;

    public static PointAccount create(Member member) {
        return PointAccount.builder()
                .id(member.getId())
                .member(member)
                .balance(BigDecimal.ZERO)
                .totalEarned(BigDecimal.ZERO)
                .build();
    }

    public void add(BigDecimal amount) {
        assertPositiveAmount(amount);
        this.balance = this.balance.add(amount);
        this.totalEarned = this.totalEarned.add(amount);
    }

    public void deduct(BigDecimal amount) {
        assertPositiveAmount(amount);
        if (this.balance.compareTo(amount) < 0) {
            throw new CustomException(ErrorCode.POINT_INSUFFICIENT_BALANCE);
        }
        this.balance = this.balance.subtract(amount);
    }

    private void assertPositiveAmount(BigDecimal amount) {
        if (amount.signum() <= 0) {
            throw new CustomException(ErrorCode.POINT_ACTION_AMOUNT_NOT_POSITIVE);
        }
    }
}
