package aegis.server.domain.member.service;

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.hibernate.exception.ConstraintViolationException;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.MemberRecord;
import aegis.server.domain.member.domain.MemberRecordSource;
import aegis.server.domain.member.repository.MemberRecordRepository;

@Service
@RequiredArgsConstructor
public class MemberRecordCreator {

    private static final String MEMBER_RECORD_UNIQUE_CONSTRAINT = "uk_member_record_member_year_semester";

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
            if (isMemberRecordDuplicateConstraintViolation(e)) {
                return false;
            }
            throw e;
        }
    }

    private boolean isMemberRecordDuplicateConstraintViolation(DataIntegrityViolationException exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof ConstraintViolationException constraintViolationException) {
                String constraintName = constraintViolationException.getConstraintName();
                if (constraintName != null
                        && MEMBER_RECORD_UNIQUE_CONSTRAINT.equals(constraintName.toLowerCase(Locale.ROOT))) {
                    return true;
                }
            }
            current = current.getCause();
        }

        Throwable mostSpecificCause = exception.getMostSpecificCause();
        if (mostSpecificCause == null || mostSpecificCause.getMessage() == null) {
            return false;
        }

        return mostSpecificCause.getMessage().toLowerCase(Locale.ROOT).contains(MEMBER_RECORD_UNIQUE_CONSTRAINT);
    }
}
