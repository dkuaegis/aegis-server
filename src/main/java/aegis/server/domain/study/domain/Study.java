package aegis.server.domain.study.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import org.hibernate.annotations.DynamicUpdate;

import lombok.*;

import aegis.server.domain.common.converter.StringListJsonConverter;
import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.common.domain.YearSemester;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

@Entity
@DynamicUpdate
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

    @Builder.Default
    private int currentParticipants = 0;

    private String schedule;

    @Builder.Default
    @Getter(AccessLevel.NONE)
    @Column(columnDefinition = "text")
    @Convert(converter = StringListJsonConverter.class)
    private List<String> curricula = new ArrayList<>();

    @Builder.Default
    @Getter(AccessLevel.NONE)
    @Column(columnDefinition = "text")
    @Convert(converter = StringListJsonConverter.class)
    private List<String> qualifications = new ArrayList<>();

    public static Study create(
            String title,
            StudyCategory category,
            StudyLevel level,
            String description,
            StudyRecruitmentMethod recruitmentMethod,
            int maxParticipants,
            String schedule,
            List<String> curricula,
            List<String> qualifications) {
        return Study.builder()
                .yearSemester(CURRENT_YEAR_SEMESTER)
                .title(title)
                .category(category)
                .level(level)
                .description(description)
                .recruitmentMethod(recruitmentMethod)
                .maxParticipants(maxParticipants)
                .schedule(schedule)
                .curricula(new ArrayList<>(curricula))
                .qualifications(new ArrayList<>(qualifications))
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
            List<String> curricula,
            List<String> qualifications) {
        // 대기 중인 신청서가 처리되지 못한 채 남을 수 있으므로 모집 방식은 개설 후 변경할 수 없습니다.
        if (this.recruitmentMethod != recruitmentMethod) {
            throw new CustomException(ErrorCode.STUDY_RECRUITMENT_METHOD_IMMUTABLE);
        }
        // maxParticipants가 0이면 무제한으로 간주합니다.
        if (maxParticipants != 0 && maxParticipants < this.currentParticipants) {
            throw new CustomException(ErrorCode.STUDY_CAPACITY_BELOW_PARTICIPANTS);
        }
        this.title = title;
        this.category = category;
        this.level = level;
        this.description = description;
        this.maxParticipants = maxParticipants;
        this.schedule = schedule;
        this.curricula = new ArrayList<>(curricula);
        this.qualifications = new ArrayList<>(qualifications);
    }

    public void increaseCurrentParticipant() {
        // maxParticipants가 0이면 무제한으로 간주하여 정원 체크를 건너뜁니다.
        if (this.maxParticipants != 0 && this.currentParticipants >= this.maxParticipants) {
            throw new CustomException(ErrorCode.STUDY_FULL);
        }
        this.currentParticipants++;
    }
}
