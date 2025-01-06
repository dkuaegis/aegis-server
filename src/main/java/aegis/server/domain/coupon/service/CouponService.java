package aegis.server.domain.coupon.service;

import aegis.server.domain.coupon.domain.Coupon;
import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.coupon.dto.request.CouponCreateRequest;
import aegis.server.domain.coupon.dto.request.CouponIssueRequest;
import aegis.server.domain.coupon.dto.response.CouponResponse;
import aegis.server.domain.coupon.dto.response.IssuedCouponResponse;
import aegis.server.domain.coupon.repository.CouponRepository;
import aegis.server.domain.coupon.repository.IssuedCouponRepository;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final IssuedCouponRepository issuedCouponRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void createCoupon(CouponCreateRequest request) {
        Coupon coupon = Coupon.create(request.getCouponName(), BigDecimal.valueOf(request.getDiscountAmount()));

        if (couponRepository.existsCouponByCouponName(request.getCouponName())) {
            throw new IllegalArgumentException("이미 존재하는 쿠폰 이름입니다.");
        }

        couponRepository.save(coupon);
    }

    public List<CouponResponse> findAllCoupons() {
        return couponRepository.findAll().stream()
                .map(CouponResponse::from)
                .toList();
    }

    @Transactional
    public void createIssuedCoupon(CouponIssueRequest request) {
        Coupon coupon = couponRepository.findById(request.getCouponId())
                .orElseThrow();

        List<Member> members = memberRepository.findAllById(request.getMemberIds());

        List<IssuedCoupon> issuedCoupons = members.stream()
                .map(member -> IssuedCoupon.of(coupon, member))
                .toList();

        issuedCouponRepository.saveAll(issuedCoupons);
    }

    public List<IssuedCouponResponse> findAllIssuedCoupons() {
        return issuedCouponRepository.findAll().stream()
                .map(IssuedCouponResponse::from)
                .toList();
    }

    public List<IssuedCouponResponse> findMyAllValidIssuedCoupons(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow();

        return issuedCouponRepository.findByMember(member).stream()
                .filter(IssuedCoupon::getIsValid)
                .map(IssuedCouponResponse::from)
                .toList();
    }
}
