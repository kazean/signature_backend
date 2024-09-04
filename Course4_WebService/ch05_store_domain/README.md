# Ch05. 실전 프로젝트 4: 스토어 도메인 개발
- [1. Store 데이터베이스 개발](#ch05-01-store-데이터-베이스-개발)
- [2. Store 서비스 로직 개발](#ch05-02-store-서비스-로직-개발)
- [3. Store Menu 데이터베이스 설계](#ch05-03-store-menu-데이터-베이스-설계)
- [4. Store Menu 서비스 로직 개발](#ch05-04-store-menu-서비스-로직-개발)


--------------------------------------------------------------------------------------------------------------------------------
# Ch05-01. Store 데이터 베이스 개발
- Store 데이터베이스 개발
- Store JPA 설정

## 실습 (service:db) 
- Store Schema
```sql
CREATE TABLE IF NOT EXISTS `delivery`.`store` (
  `id` BIGINT(32) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `address` VARCHAR(150) NOT NULL,
  `status` VARCHAR(50) NULL,
  `category` VARCHAR(50) NULL,
  `star` DOUBLE NULL DEFAULT 0,
  `thumbnail_url` VARCHAR(200) NULL,
  `minimum_amount` DECIMAL(11,4) NULL,
  `minimum_delivery_amount` DECIMAL(11,4) NULL,
  `phone_number` VARCHAR(20) NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;
```

- code
```java
package org.delivery.db.store.enums;
@AllArgsConstructor
public enum StoreStatus {
    REGISTER("등록"),
    UNREGISTERED("해지"),
    ;
    private String description;
}

@AllArgsConstructor
public enum StoreCategory {
    // 중식
    CHINES_FOOD("중식", "중식"),
    // 양식
    WESTERN_FOOD("양식", "양식"),
    // 한식
    KOREAN_FOOD("한식", "한식"),
    // 일식
    JAPANESE_FOOD("일식", "일식"),
    // 치킨
    CHICKEN("치킨", "치킨"),
    // 피자
    PIZZA("피자", "피자"),
    // 햄버거
    HAMBURGER("햄버거", "햄버거"),
    // 커피
    COFFEE_TEA("커피&차", "커피&차"),
    ;
    private String display;
    private String description;
}


package org.delivery.db.store;
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Entity
@Table(name = "store")
public class StoreEntity extends BaseEntity {
    @Column(length = 100, nullable = false)
    private String name;
    @Column(length = 150, nullable = false)
    private String address;
    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private StoreStatus status;
    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private StoreCategory category;
    private double star;
    @Column(length = 200, nullable = false)
    private String thumbnailUrl;
    @Column(precision = 11, scale = 4, nullable = false)
    private BigDecimal minimumAmount;
    @Column(precision = 11, scale = 4, nullable = false)
    private BigDecimal minimumDeliveryAmount;
    @Column(length = 20)
    private String phoneNumber;
}

package org.delivery.db.store;
public interface StoreRepository extends JpaRepository<StoreEntity, Long> {
    // 특정 유효한 스토어
    // select * from store where id = ? and status = 'REGISTERED' order by id desc limit 1
    Optional<StoreEntity> findFirstByIdAndStatusOrderByIdDesc(Long id, StoreStatus status);
    // 유효한 스토어 리스트
    // select * from store where status - ? order by id desc
    List<StoreEntity> findAllByStatusOrderByIdDesc(StoreStatus status);

    // 유효한 특정 카테고리의 스토어 리스트
    List<StoreEntity> findAllByStatusAndCategoryOrderByStarDesc(StoreStatus status, StoreCategory storeCategory);
}
```
- cf, account 삭제


--------------------------------------------------------------------------------------------------------------------------------
# Ch05-02. Store 서비스 로직 개발
- 상점 검색, 등록

## 실습 (service: api)
- StoreConroller
> - "/open-api/store/" "/register"
> - "/api/store/search"
```java
package org.delivery.api.domain.store.service;
@RequiredArgsConstructor
@Service
public class StoreService {
    private final StoreRepository storeRepository;

    // 유효한 스토어 가져오기
    public StoreEntity getStoreWithThrow(Long id) {
        var entity = storeRepository.findFirstByIdAndStatusOrderByIdDesc(id, StoreStatus.REGISTERED);
        return entity.orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT));
    }
    // 스토어 등록
    public StoreEntity register(StoreEntity storeEntity) {
        return Optional.ofNullable(storeEntity)
                .map(it -> {
                    it.setStar(0);
                    it.setStatus(StoreStatus.REGISTERED);
                    // TODO 등록일시 추가하기
                    return storeRepository.save(it);
                })
                .orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT));
    }

    // 카테고리로 스토어 검색
    public List<StoreEntity> searchByCategory(StoreCategory storeCategory) {
        var list = storeRepository.findAllByStatusAndCategoryOrderByStarDesc(StoreStatus.REGISTERED, storeCategory);
        return list;
    }

    // 전체 스토어
    public List<StoreEntity> registerStore() {
        var list = storeRepository.findAllByStatusOrderByIdDesc(StoreStatus.REGISTERED);
        return list;
    }
}

package org.delivery.api.domain.store.controller.model;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreRegisterRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String address;
    @NotNull
    private StoreCategory storeCategory;
    @NotBlank
    private String thumbnailUrl;
    @NotNull
    private BigDecimal minimumAmount;
    @NotNull
    private BigDecimal minimumDeliveryAmount;
    @NotBlank
    private String phoneNumber;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreResponse {
    private Long id;
    private String name;
    private String address;
    private StoreStatus status;
    private StoreCategory category;
    private double star;
    private String thumbnailUrl;
    private BigDecimal minimumAmount;
    private BigDecimal minimumDeliveryAmount;
    private String phoneNumber;
}

package org.delivery.api.domain.store.converter;
@Converter
public class StoreConverter {
    public StoreEntity toEntity(StoreRegisterRequest request) {
        return Optional.ofNullable(request)
                .map(it -> {
                    return StoreEntity.builder()
                            .name(it.getName())
                            .address(it.getAddress())
                            .category(it.getStoreCategory())
                            .minimumAmount(it.getMinimumAmount())
                            .minimumDeliveryAmount(it.getMinimumDeliveryAmount())
                            .thumbnailUrl(it.getThumbnailUrl())
                            .phoneNumber(it.getPhoneNumber())
                            .build();
                })
                .orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT));
    }

    public StoreResponse toResponse(StoreEntity entity) {
        return Optional.ofNullable(entity)
                .map(it -> {
                    return StoreResponse.builder()
                            .id(it.getId())
                            .name(it.getName())
                            .status(it.getStatus())
                            .category(it.getCategory())
                            .address(it.getAddress())
                            .minimumAmount(it.getMinimumAmount())
                            .minimumDeliveryAmount(it.getMinimumDeliveryAmount())
                            .thumbnailUrl(it.getThumbnailUrl())
                            .phoneNumber(it.getPhoneNumber())
                            .star(it.getStar())
                            .build();
                })
                .orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT));
    }
}

package org.delivery.api.domain.store.business;
@Business
public class StoreBusiness {
    private final StoreService storeService;
    private final StoreConverter storeConverter;

    public StoreResponse register(StoreRegisterRequest request) {
        // req -> entity > response
        var entity = storeConverter.toEntity(request);
        var newEntity = storeService.register(entity);
        var response = storeConverter.toResponse(newEntity);
        return response;
    }

    public List<StoreResponse> searchCategory(StoreCategory storeCategory) {
        var storeList = storeService.searchByCategory(storeCategory);
        return storeList.stream()
                .map(storeConverter::toResponse)
                .collect(toList());
    }
}

package org.delivery.api.domain.store.controller;
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/store")
public class StoreApiController {
    private final StoreBusiness storeBusiness;

    @GetMapping("/search")
    public Api<List<StoreResponse>> search(
        @RequestParam(required = false)
            StoreCategory storeCategory
    ) {
        var response = storeBusiness.searchCategory(storeCategory);
        return Api.OK(response);
    }
}

@RequiredArgsConstructor
@RestController
@RequestMapping("/open-api/store")
public class StoreOpenApiController {
    private final StoreBusiness storeBusiness;

    @PostMapping("/register")
    public Api<StoreResponse> register(
            @Valid
            @RequestBody
            Api<StoreRegisterRequest> request
    ) {
        var response = storeBusiness.register(request.getBody());
        return Api.OK(response);
    }
}
```
## 싧습
- Swagger UI
> - /open-api/store/register
> > - 스타개미 강남점/COFFEE_TEA/url/3000/0211112222
> > - ED 커피 강남점/~
> - /api/store/search
> - DB 확인


--------------------------------------------------------------------------------------------------------------------------------
# Ch05-03. Store Menu 데이터 베이스 설계
- Store Menu Table
- 가맹점이 가지고 있는 메뉴, 주문

## 실습 (service: db)
- Store Menu Schema >- Store
```sql
CREATE TABLE IF NOT EXISTS `delivery`.`store_menu` (
  `id` BIGINT(32) NOT NULL AUTO_INCREMENT,
  `store_id` BIGINT(32) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `amount` DECIMAL(11,4) NOT NULL,
  `status` VARCHAR(50) NOT NULL,
  `thumbnail_url` VARCHAR(200) NOT NULL,
  `like_count` INT NULL DEFAULT 0,
  `sequence` INT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
)
ENGINE = InnoDB;
```
> - 외례키 설정안함
> - 인덱스 설정안함

- code
> - 유효한 메뉴 체크
> - 특정 가게의 메뉴리스트 가져오기
```java
package org.delivery.db.storemenu;
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Entity
@Table(name = "store_menu")
public class StoreMenuEntity extends BaseEntity {
    @Column(nullable = false)
    private Long storeId;
    @Column(length = 100, nullable = false)
    private String name;
    @Column(precision = 11, scale = 4, nullable = false)
    private BigDecimal amount;
    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private StoreMenuStatus status;
    @Column(length = 200, nullable = false)
    private String thumbnailUrl;
    private int likeCount;
    private int sequence;
}

package org.delivery.db.storemenu.enums;
@AllArgsConstructor
public enum StoreMenuStatus {
    REGISTERED("등록"),
    UNREGISTERED("해지")
    ;
    private String description;
}

package org.delivery.db.storemenu;
public interface StoreMenuRepository extends JpaRepository<StoreMenuEntity, Long> {
    // 유효한 메뉴 체크
    Optional<StoreMenuEntity> findFirstByIdAndStatusOrderByIdDesc(Long id, StoreMenuStatus status);

    // 특정 가게의 메뉴 가져오기
    List<StoreMenuEntity> findAllByStoreIdAndStatusOrderBySequenceDesc(Long storeId, StoreMenuStatus status);
}
```
> - @Columm
> > - precision = 소수점 포함한 전체 자리수
> > - scale = 소수의 자리수

## 실행
- ApiApplication Run


--------------------------------------------------------------------------------------------------------------------------------
# Ch05-04. Store Menu 서비스 로직 개발
- 유효 메뉴/리스트 서비스 로직 개발
## 실습 (service: api)
- controller 
> - api "/search" open "/register"
```java
package org.delivery.api.domain.storemenu.service;
@RequiredArgsConstructor
@Service
public class StoreMenuService {
    private final StoreMenuRepository storeMenuRepository;

    public StoreMenuEntity register(
            StoreMenuEntity storeMenuEntity
    ) {
        return Optional.ofNullable(storeMenuEntity)
                .map(it -> {
                    it.setStatus(StoreMenuStatus.REGISTERED);
                    return storeMenuRepository.save(it);
                })
                .orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT));
    }

    public StoreMenuEntity getStoreMenuWithThrow(Long id) {
        var entity = storeMenuRepository.findFirstByIdAndStatusOrderByIdDesc(id, StoreMenuStatus.REGISTERED);
        return entity.orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT));
    }

    public List<StoreMenuEntity> getStoreMenuByStoreId(Long storeId) {
        var list = storeMenuRepository.findAllByStoreIdAndStatusOrderBySequenceDesc(storeId, StoreMenuStatus.REGISTERED);
        return list;
    }
}

package org.delivery.api.domain.storemenu.controller.model;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreMenuRegisterRequest {
    @NotNull
    private Long storeId;
    @NotBlank
    private String name;
    @NotNull
    private BigDecimal amount;
    @NotBlank
    private String thumbnailUrl;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreMenuResponse {
    private Long id;
    private Long storeId;
    private String name;
    private BigDecimal amount;
    private StoreMenuStatus status;
    private String thumbnailUrl;
    private int likeCount;
    private int sequence;
}

package org.delivery.api.domain.storemenu.converter;
@Converter
public class StoreMenuConverter {
    public StoreMenuEntity toEntity(StoreMenuRegisterRequest request) {
        return Optional.ofNullable(request)
                .map(it -> {
                    return StoreMenuEntity.builder()
                            .storeId(it.getStoreId())
                            .name(it.getName())
                            .amount(it.getAmount())
                            .thumbnailUrl(it.getThumbnailUrl())
                            .status(StoreMenuStatus.REGISTERED)
                            .build();
                })
                .orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT));
    }

    public StoreMenuResponse toResponse(StoreMenuEntity entity) {
        return Optional.ofNullable(entity)
                .map(it -> {
                    return StoreMenuResponse.builder()
                            .id(it.getId())
                            .storeId(it.getStoreId())
                            .name(it.getName())
                            .amount(it.getAmount())
                            .status(it.getStatus())
                            .thumbnailUrl(it.getThumbnailUrl())
                            .likeCount(it.getLikeCount())
                            .sequence(it.getSequence())
                            .build();
                })
                .orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT));
    }

    public List<StoreMenuResponse> toResponse(
            List<StoreMenuEntity> list
    ) {
        return list.stream()
                .map(it -> {
                    return toResponse(it);
                }).collect(toList());
    }
}

package org.delivery.api.domain.storemenu.business;
@RequiredArgsConstructor
@Business
public class StoreMenuBusiness {
    private final StoreMenuService storeMenuService;
    private final StoreMenuConverter storeMenuConverter;

    public StoreMenuResponse register(StoreMenuRegisterRequest request) {
        // req > entity > save > response
        var entity = storeMenuConverter.toEntity(request);
        var newEntity = storeMenuService.register(entity);
        var response = storeMenuConverter.toResponse(newEntity);
        return response;
    }

    public List<StoreMenuResponse> search(Long storeId) {
        var list = storeMenuService.getStoreMenuByStoreId(storeId);
        return list.stream()
                .map(storeMenuConverter::toResponse)
                .collect(toList());
    }
}

package org.delivery.api.domain.storemenu.controller;
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/store-menu")
public class StoreMenuApiController {
    private final StoreMenuBusiness storeMenuBusiness;

    @GetMapping("/search")
    public Api<List<StoreMenuResponse>> search(
            @RequestParam Long storeId
    ) {
        var response = storeMenuBusiness.search(storeId);
        return Api.OK(response);
    }
}

@RequiredArgsConstructor
@RestController
@RequestMapping("/open-api/store-menu")
public class StoreMenuOpenApiController {
    private final StoreMenuBusiness storeMenuBusiness;

    @PostMapping("/register")
    public Api<StoreMenuResponse> register(
            @Valid
            @RequestBody
            Api<StoreMenuRegisterRequest> request
    ) {
        var req = request.getBody();
        var response = storeMenuBusiness.register(req);
        return Api.OK(response);
    }
}
```
## 실행
- Swagger
> - /open-api/store-menu/register
> > - store_id:1/아이스 아메리카노/url/~
> > - store_id:1/아이스 카페라떼/url/~
> > - store_id:1/카페라떼/url/UNREGISTER
> - /api/store-menu/search