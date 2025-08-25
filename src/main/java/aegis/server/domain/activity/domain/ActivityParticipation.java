package aegis.server.domain.activity.domain;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.member.domain.Member;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "member_id"}))
@Getter
@Builder(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class ActivityParticipation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_participation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public static ActivityParticipation create(Activity activity, Member member) {
        return ActivityParticipation.builder().activity(activity).member(member).build();
    }
}
