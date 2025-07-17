package aegis.server.domain.activity.domain;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.common.domain.YearSemester;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "year_semester"}))
@Getter
@Builder(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Activity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private YearSemester yearSemester;

    @Builder.Default
    private Boolean isActive = false;

    public static Activity create(String name) {
        return Activity.builder().name(name).yearSemester(CURRENT_YEAR_SEMESTER).build();
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void updateName(String name) {
        this.name = name;
    }
}
