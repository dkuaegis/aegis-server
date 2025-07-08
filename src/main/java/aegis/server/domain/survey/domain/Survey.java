package aegis.server.domain.survey.domain;

import jakarta.persistence.*;

import lombok.*;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.member.domain.Member;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Survey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "survey_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private AcquisitionType acquisitionType;

    @Column(length = 1000)
    private String joinReason;

    public static Survey create(Member member, AcquisitionType acquisitionType, String joinReason) {
        return Survey.builder()
                .member(member)
                .acquisitionType(acquisitionType)
                .joinReason(joinReason)
                .build();
    }

    public void update(AcquisitionType acquisitionType, String joinReason) {
        this.acquisitionType = acquisitionType;
        this.joinReason = joinReason;
    }
}
