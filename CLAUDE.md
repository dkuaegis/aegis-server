# 핵심 규칙

- 아래 규칙은 그 어떤 규칙보다 우선하며 항상 지켜야 합니다.
- 코드를 수정하고 반드시 테스트코드를 작성하거나 실행하세요.

# 아키텍처

- 기본적인 Clean Architecture 의 Layered Architecture 를 따르세요.
- DDD 를 적용하여 도메인 중심의 설계를 지향하세요.
- 이벤트 기반 아키텍처를 통해 도메인 간의 결합도를 낮추세요.

# 정적 팩토리 메서드

- 객체를 생성할 때 가능한 정적 팩토리 메서드를 사용하세요.
  - create: 엔티티를 생성할 때 사용합니다.
  - of: 매개변수로부터 객체를 생성할 때 사용합니다.
  - from: 엔티티로 부터 DTO 를 생성할 때 사용합니다.

# Controller

- 200대 응답이 아닌 경우 빈 `@Content` 를 반환하도록 Swagger 문서에 명시하세요.
- 200 Status OK만 사용하는 것이 아니라 적절한 HTTP 상태 코드를 사용하세요.
- DTO 내용을 파싱하지 말고 그대로 Service 레이어로 전달하세요.
- 관리자용 API 는 `/admin` 경로를 사용하세요.

# Service

- 메서드가 아닌 클래스 단에 `@Transactional(readOnly = true)` 를 사용하세요.
- 변경을 수반하는 메서드에만 `@Transactional` 을 사용하세요.
- 절대 Entity 를 반환하지 말고 DTO 를 반환하세요.
- DTO 생성은 Service 레이어에서만 수행하세요.

# Repository

- N+1 문제가 발생할 수 있는지 항상 확인하고, 이를 위해 Fetch Join 이나 Entity Graph 를 사용하세요.
- 단 무조건적으로 Fetch Join 을 사용하지 마세요.
  - 테스트를 통해 문제가 발생하는지 확인하고, 필요한 경우에만 사용하세요.
- 가능한 경우 반드시 쿼리 단에서 필터링을 실행하세요.
- `@Param` 어노테이션을 사용하지 마세요.

# Entity

- DDD 의 Rich Domain Model 을 지향하세요.
- Entity 는 다른 레이어에 의존하지 않도록 하세요.
- 아래 어노테이션을 사용하여 Entity 클래스를 정의하세요.
  - `@Entity`
  - `@Getter`
  - `@Builder(access = AccessLevel.PRIVATE)`
  - `@AllArgsConstructor(access = AccessLevel.PRIVATE)`
  - `@NoArgsConstructor(access = AccessLevel.PROTECTED)`
- id 필드에는 아래 어노테이션을 사용하세요.
  - `@Id`
  - `@GeneratedValue(strategy = GenerationType.IDENTITY)`
  - `@Column(name = "소문자클래스명_id")`
    - `@Colume(name = "member_id")`
- 논리적으로 유일해야 하는 필드의 집합의 경우 `@UniqueConstraint` 를 사용하세요.
- 필요한 경우 인덱스를 추가하여 성능을 최적화하세요.

# DTO

- 가능하다면 record 를 사용하여 DTO 를 정의하세요.
- DTO 의 정적 팩토리 메서드에서는 별도의 어노테이션 대신 그냥 `new` 키워드를 사용하세요.
- request, response 패키지를 구분하여 DTO 패키지를 구성하세요.
  - internal 패키지는 별도의 지시가 없는 한 사용하지 마세요.
- 요청 DTO 는 클래스명 마지막에 `Request` 를 붙이고, 응답 DTO 는 마지막에 `Response` 를 붙이세요.
  - `CouponCreateRequest`, `PaymentStatusResponse`

# Test Code

- 코드를 수정하고 반드시 테스트코드를 작성하세요.
- 본 프로젝트의 테스트코드는 조금 독특하므로 테스트코드 컨벤션은 다른 코드를 참조하세요.
- 코드 구현을 마치고 항상 전체 테스트 코드를 실행하여, 시스템에 문제가 없는지 확인하세요.
- 필요한 경우 ReflectionTestUtils 을 사용하여 private 필드에 접근하세요.

# 배포 환경

- 본 시스템은 단일 VM, 단일 컨테이너 환경에서 실행됩니다.
  분산 환경에서 발생하는 문제는 고려하지 않습니다.
- 동아리 내부 서비스이므로 다운타임이 발생해도 큰 문제가 되지 않습니다.

# 하위 호환

- 현재 시점은 방학이며, 다음 학기에 사용할 시스템을 구축하고 있습니다.
- 따라서 현재 시스템과의 하위 호환성은 고려하지 않습니다.
