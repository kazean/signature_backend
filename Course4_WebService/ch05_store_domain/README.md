# Ch05. 실전 프로젝트 4: 스토어 도메인 개발
# Ch05-01. Store 데이터 베이스 개발
Store Schema
```
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
# db/store/StoreEntity, StoreRepository, enum/StoreStatus, StoreCategory
```
- StoreStatus
@AllArgsConstructor
public enum StoreStatus {
    REGISTER("등록"),
    UNREGISTERED("해지"),
    ;
    private String description;
}
- StoreCategory
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
```
> @Entity, extends JpaRepository<Storeentity, Long>


# Ch05-02. Store 서비스 로직 개발
## Store/controller/model, business, service, converter
```
- StoreService
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
- StoreBusiness
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
- StoreApiController
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
- StoreOpenApiController
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
- StoreConroller
> "/open-api/store/" "/register"  
"/api/search"  
- StoreService  
> StoreEntity getStoreWithThrow(Long id), StoreEntity register(StoreEntity storeEntity)  
List<StoreEntity> searchByCategory(StoreCategory storeCategory)  
List<StoreEntity> registerStore()  
- StoreBusiness
> StoreResponse register(StoreRegisterRequest request)  
List<StoreResponse> searchCategory(StoreCategory storeCategory)


# Ch05-03. Store Menu 데이터 베이스 설계
가맹점이 가지고 있는 메뉴, 주문
- store_menu Schema
```
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
## db/storemenu/StoreMenuEntity, StoreMenuRepository, ./enums/StoreMenuStatus


# Ch05-04. Store Menu 서비스 로직 개발
## /api/storemenu/controller/model, /business, /service, /converter
```
- service
private final StoreMenuRepository storeMenuRepository;

public StoreMenuEntity getStoreMenuWithThrow(Long id) {
    var entity = storeMenuRepository.findFirstByIdAndStatusOrderByIdDesc(id, StoreMenuStatus.REGISTERED);
    return entity.orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT));
}

public List<StoreMenuEntity> getStoreMenuByStoreId(Long storeId) {
    var list = storeMenuRepository.findAllByStoreIdAndStatusOrderBySequenceDesc(storeId, StoreMenuStatus.REGISTERED);
    return list;
}

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
- business
private final StoreMenuService storeMenuService;
private final StoreMenuConverter storeMenuConverter;

public StoreMenuResponse register(StoreMenuRegisterRequest request) {
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

- controller
@GetMapping("/search")
public Api<List<StoreMenuResponse>> search(
        @RequestParam Long storeId
) {
    var response = storeMenuBusiness.search(storeId);
    return Api.OK(response);
}
- open controller
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
```
- controller 
> api "/search" open "/register"
- business
> register(req), search(storeId)
- service
> getStoreMenuWithThrow(id), getStoreMenuByStoreId(storeId), register(entity)