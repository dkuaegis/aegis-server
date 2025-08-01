package aegis.server.domain.study.domain;

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
public class StudyMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private StudyRole role;

    public static StudyMember create(Study study, Member member, StudyRole role) {
        return StudyMember.builder().study(study).member(member).role(role).build();
    }

    public void validateMemberIsInstructor() {
        if (!this.role.equals(StudyRole.INSTRUCTOR)) {
            throw new CustomException(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR);
        }
    }
}
