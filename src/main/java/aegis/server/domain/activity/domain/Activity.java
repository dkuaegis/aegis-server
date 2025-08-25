package aegis.server.domain.activity.domain;

import jakarta.persistence.*;

import lombok.*;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.common.domain.YearSemester;

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

    public static Activity create(String name) {
        return Activity.builder().name(name).yearSemester(CURRENT_YEAR_SEMESTER).build();
    }

    public void updateName(String name) {
        this.name = name;
    }
}
