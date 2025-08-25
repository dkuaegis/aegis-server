package aegis.server.domain.activity.domain;

import java.math.BigDecimal;

import jakarta.persistence.*;

import lombok.*;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.common.domain.YearSemester;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "year_semester"}))
@Getter
@Builder(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Activity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private YearSemester yearSemester;

    @Column(precision = 10, scale = 0)
    private BigDecimal pointAmount;

    public static Activity create(String name, BigDecimal pointAmount) {
        assertPositiveAmount(pointAmount);
        return Activity.builder()
                .name(name)
                .yearSemester(CURRENT_YEAR_SEMESTER)
                .pointAmount(pointAmount)
                .build();
    }

    public void update(String name, BigDecimal pointAmount) {
        assertPositiveAmount(pointAmount);
        this.name = name;
        this.pointAmount = pointAmount;
    }

    private static void assertPositiveAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new CustomException(ErrorCode.POINT_ACTION_AMOUNT_NOT_POSITIVE);
        }
    }
}
