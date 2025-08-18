package aegis.server.domain.coupon.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.coupon.domain.Coupon;
import aegis.server.domain.coupon.domain.CouponCode;
import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.coupon.dto.request.CouponCodeCreateRequest;
import aegis.server.domain.coupon.dto.request.CouponCodeUseRequest;
import aegis.server.domain.coupon.dto.request.CouponCreateRequest;
import aegis.server.domain.coupon.dto.request.CouponIssueRequest;
import aegis.server.domain.coupon.dto.response.CouponCodeResponse;
import aegis.server.domain.coupon.dto.response.CouponResponse;
import aegis.server.domain.coupon.dto.response.IssuedCouponResponse;
import aegis.server.domain.coupon.repository.CouponCodeRepository;
import aegis.server.domain.coupon.repository.CouponRepository;
import aegis.server.domain.coupon.repository.IssuedCouponRepository;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final IssuedCouponRepository issuedCouponRepository;
    private final CouponCodeRepository couponCodeRepository;
    private final MemberRepository memberRepository;

    public List<CouponResponse> findAllCoupons() {
        return couponRepository.findAll().stream().map(CouponResponse::from).toList();
    }

    @Transactional
    public CouponResponse createCoupon(CouponCreateRequest request) {
        Coupon coupon = Coupon.create(request.couponName(), request.discountAmount());

        if (couponRepository.existsByCouponNameAndDiscountAmount(coupon.getCouponName(), coupon.getDiscountAmount())) {
            throw new CustomException(ErrorCode.COUPON_ALREADY_EXISTS);
        }

        couponRepository.save(coupon);
        return CouponResponse.from(coupon);
    }

    @Transactional
    public void deleteCoupon(Long couponId) {
        try {
            couponRepository
                    .findById(couponId)
                    .ifPresentOrElse(
                            coupon -> {
                                couponRepository.delete(coupon);
                                couponRepository.flush();
                            },
                            () -> {
                                throw new CustomException(ErrorCode.COUPON_NOT_FOUND);
                            });
        } catch (DataIntegrityViolationException | InvalidDataAccessApiUsageException e) {
            throw new CustomException(ErrorCode.COUPON_ISSUED_COUPON_EXISTS);
        }
    }

    // - - -

    public List<IssuedCouponResponse> findAllIssuedCoupons() {
        return issuedCouponRepository.findAll().stream()
                .map(IssuedCouponResponse::from)
                .toList();
    }

    public List<IssuedCouponResponse> findMyAllIssuedCoupons(UserDetails userDetails) {
        Member member = memberRepository
                .findById(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return issuedCouponRepository.findAllByMemberWithCoupon(member).stream()
                .map(IssuedCouponResponse::from)
                .toList();
    }

    @Transactional
    public List<IssuedCouponResponse> createIssuedCoupon(CouponIssueRequest request) {
        Coupon coupon = couponRepository
                .findById(request.couponId())
                .orElseThrow(() -> new CustomException(ErrorCode.COUPON_NOT_FOUND));

        List<Member> members = memberRepository.findAllById(request.memberIds());

        List<IssuedCoupon> issuedCoupons =
                members.stream().map(member -> IssuedCoupon.of(coupon, member)).toList();

        issuedCouponRepository.saveAll(issuedCoupons);
        return issuedCoupons.stream().map(IssuedCouponResponse::from).toList();
    }

    @Transactional
    public void deleteIssuedCoupon(Long issuedCouponId) {
        issuedCouponRepository.findById(issuedCouponId).ifPresentOrElse(issuedCouponRepository::delete, () -> {
            throw new CustomException(ErrorCode.ISSUED_COUPON_NOT_FOUND);
        });
    }

    // - - -

    public List<CouponCodeResponse> findAllCouponCode() {
        return couponCodeRepository.findAll().stream()
                .map(CouponCodeResponse::from)
                .toList();
    }

    @Transactional
    public CouponCodeResponse createCouponCode(CouponCodeCreateRequest request) {
        Coupon coupon = couponRepository
                .findById(request.couponId())
                .orElseThrow(() -> new CustomException(ErrorCode.COUPON_NOT_FOUND));

        String code = generateUniqueCode();
        CouponCode couponCode = CouponCode.of(coupon, code);

        couponCodeRepository.save(couponCode);
        return CouponCodeResponse.from(couponCode);
    }

    @Transactional
    public CouponCodeResponse useCouponCode(UserDetails userDetails, CouponCodeUseRequest request) {
        Member member = memberRepository
                .findById(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        CouponCode couponCode = couponCodeRepository
                .findByCodeWithLock(request.code().strip())
                .orElseThrow(() -> new CustomException(ErrorCode.COUPON_CODE_NOT_FOUND));

        IssuedCoupon issuedCoupon = IssuedCoupon.of(couponCode.getCoupon(), member);
        issuedCouponRepository.save(issuedCoupon);

        couponCode.use(issuedCoupon);

        return CouponCodeResponse.from(couponCode);
    }

    @Transactional
    public void deleteCodeCoupon(Long codeCouponId) {
        couponCodeRepository.findById(codeCouponId).ifPresentOrElse(couponCodeRepository::delete, () -> {
            throw new CustomException(ErrorCode.COUPON_CODE_NOT_FOUND);
        });
    }

    private String generateUniqueCode() {
        String code;
        int maxAttempts = 100;
        int attempts = 0;
        do {
            if (attempts++ >= maxAttempts) {
                throw new CustomException(ErrorCode.COUPON_CODE_CANNOT_ISSUE_CODE);
            }
            code = CodeGenerator.generateCouponCode(8);
        } while (couponCodeRepository.existsByCode(code));
        return code;
    }
}
