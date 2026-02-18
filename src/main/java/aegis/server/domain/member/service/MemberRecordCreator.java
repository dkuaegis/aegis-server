package aegis.server.domain.member.service;

import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.MemberRecord;
import aegis.server.domain.member.domain.MemberRecordSource;
import aegis.server.domain.member.repository.MemberRecordRepository;

@Service
@RequiredArgsConstructor
public class MemberRecordCreator {

    private final MemberRecordRepository memberRecordRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean createIfAbsent(
            Member member,
            YearSemester yearSemester,
            MemberRecordSource recordSource,
            Long paymentId,
            LocalDateTime paymentCompletedAt) {
        if (memberRecordRepository.existsByMemberIdAndYearSemester(member.getId(), yearSemester)) {
            return false;
        }

        MemberRecord memberRecord =
                MemberRecord.create(member, yearSemester, recordSource, paymentId, paymentCompletedAt);

        try {
            memberRecordRepository.save(memberRecord);
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }
}
