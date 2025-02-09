package aegis.server.domain.timetable.domain;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.common.domain.YearSemester;
import jakarta.persistence.*;
import lombok.*;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Timetable extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "timetable_id")
    private Long id;

    private String hashedOidcId;

    private String hashedIdentifier;

    @Enumerated(EnumType.STRING)
    private YearSemester yearSemester;

    @Column(length = 5000)
    private String jsonData;

    public static Timetable create(String hashedOidcId, String hashedIdentifier, String jsonData) {
        return Timetable.builder()
                .hashedOidcId(hashedOidcId)
                .hashedIdentifier(hashedIdentifier)
                .yearSemester(CURRENT_YEAR_SEMESTER)
                .jsonData(jsonData)
                .build();
    }

    public void updateHashedIdentifier(String hashedIdentifier) {
        this.hashedIdentifier = hashedIdentifier;
    }

    public void updateJsonData(String jsonData) {
        this.jsonData = jsonData;
    }
}
