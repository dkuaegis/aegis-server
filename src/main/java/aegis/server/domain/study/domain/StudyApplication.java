package aegis.server.domain.study.domain;

import jakarta.persistence.*;

import lombok.*;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.member.domain.Member;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"study_id", "member_id"}))
@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_application_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private StudyApplicationStatus status;

    @Column(columnDefinition = "text")
    private String applicationReason;

    public static StudyApplication create(Study study, Member member, String applicationReason) {
        return StudyApplication.builder()
                .study(study)
                .member(member)
                .applicationReason(applicationReason)
                .status(StudyApplicationStatus.PENDING)
                .build();
    }

    public void validateApplicationUpdatable() {
        if (this.status == StudyApplicationStatus.APPROVED) {
            throw new CustomException(ErrorCode.STUDY_APPLICATION_ALREADY_APPROVED);
        }
    }

    public void updateApplicationReason(String applicationReason) {
        this.applicationReason = applicationReason;
    }

    public void approve() {
        this.status = StudyApplicationStatus.APPROVED;
    }

    public void reject() {
        this.status = StudyApplicationStatus.REJECTED;
    }
}
