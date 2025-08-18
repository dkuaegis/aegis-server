package aegis.server.domain.study.domain;

import jakarta.persistence.*;

import lombok.*;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.common.domain.YearSemester;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Study extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private YearSemester yearSemester;

    private String title;

    @Enumerated(EnumType.STRING)
    private StudyCategory category;

    @Enumerated(EnumType.STRING)
    private StudyLevel level;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    private StudyRecruitmentMethod recruitmentMethod;

    private int maxParticipants;

    private int currentParticipants = 0;

    private String schedule;

    @Column(columnDefinition = "text")
    private String curricula;

    @Column(columnDefinition = "text")
    private String qualifications;

    public static Study create(
            String title,
            StudyCategory category,
            StudyLevel level,
            String description,
            StudyRecruitmentMethod recruitmentMethod,
            int maxParticipants,
            String schedule,
            String curricula,
            String qualifications) {
        return Study.builder()
                .yearSemester(CURRENT_YEAR_SEMESTER)
                .title(title)
                .category(category)
                .level(level)
                .description(description)
                .recruitmentMethod(recruitmentMethod)
                .maxParticipants(maxParticipants)
                .schedule(schedule)
                .curricula(curricula)
                .qualifications(qualifications)
                .build();
    }

    public void update(
            String title,
            StudyCategory category,
            StudyLevel level,
            String description,
            StudyRecruitmentMethod recruitmentMethod,
            int maxParticipants,
            String schedule,
            String curricula,
            String qualifications) {
        this.title = title;
        this.category = category;
        this.level = level;
        this.description = description;
        this.recruitmentMethod = recruitmentMethod;
        this.maxParticipants = maxParticipants;
        this.schedule = schedule;
        this.curricula = curricula;
        this.qualifications = qualifications;
    }

    public void increaseCurrentParticipant() {
        if (this.currentParticipants >= this.maxParticipants) {
            throw new CustomException(ErrorCode.STUDY_FULL);
        }
        this.currentParticipants++;
    }
}
