package aegis.server.domain.timetable.domain;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

@Entity
@Table(
        indexes = {
                @Index(name = "idx_timetable_identifier_year_semester", columnList = "identifier, year_semester")
        }
)
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Timetable extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "timetable_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String identifier;

    @Enumerated(EnumType.STRING)
    private YearSemester yearSemester;

    @Column(length = 5000)
    private String jsonData;

    public static Timetable create(Member member, String identifier, String jsonData) {
        return Timetable.builder()
                .member(member)
                .identifier(identifier)
                .yearSemester(CURRENT_YEAR_SEMESTER)
                .jsonData(jsonData)
                .build();
    }

    public void updateIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void updateJsonData(String jsonData) {
        this.jsonData = jsonData;
    }
}