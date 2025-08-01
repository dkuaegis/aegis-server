package aegis.server.domain.study.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.*;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.common.domain.YearSemester;

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

    private YearSemester yearSemester;

    private String title;

    @Enumerated(EnumType.STRING)
    private StudyCategory category;

    @Enumerated(EnumType.STRING)
    private StudyLevel level;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    private StudyRecruitmentMethod recruitmentMethod;

    private String maxParticipants;

    private String schedule;

    @Lob
    private String curricula;

    @Column(length = 1024)
    private String qualifications;

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private final List<StudyMember> members = new ArrayList<>();

    public static Study create(
            String title,
            StudyCategory category,
            StudyLevel level,
            String description,
            StudyRecruitmentMethod recruitmentMethod,
            String maxParticipants,
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
            String maxParticipants,
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
}
