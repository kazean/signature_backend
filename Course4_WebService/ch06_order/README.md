# Ch06. 실전 프로젝트 5: 상품 주문 개발
- [1. User Order 데이터베이스 설계](#ch06-01-user-order-데이터베이스-설계)
- [2. User Order 서비스 로직 개발 - 1](#ch06-02-user-order-서비스-로직-개발---1)
- [3. User Order 서비스 로직 개발 - 2](#ch06-03-user-order-서비스-로직-개발---2)
- [4. User Order 서비스 로직 개발 - 3](#ch06-04-user-order-서비스-로직-개발---3)
- [5. User Order 서비스 로직 개발 - 4](#ch06-05-user-order-서비스-로직-개발---4)


--------------------------------------------------------------------------------------------------------------------------------
# Ch06-01. User Order 데이터베이스 설계
- 주문과 상품메뉴 테이블 관계, N:M
- user_order < user_order_menu > store_menu, N:M 테이블 Map
## 실습(Mysql, service:db)
### user_order, user_order_menu
```sql
CREATE TABLE IF NOT EXISTS `delivery`.`user_order` (
  `id` BIGINT(32) NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT(32) NOT NULL,
  `status` VARCHAR(50) NOT NULL,
  `amount` DECIMAL(11,4) NOT NULL,
  `ordered_at` DATETIME NULL,
  `accepted_at` DATETIME NULL,
  `cooking_started_at` DATETIME NULL,
  `delivery_started_at` DATETIME NULL,
  `received_at` DATETIME NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id` ASC) VISIBLEs
)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `delivery`.`user_order_menu` (
  `id` BIGINT(32) NOT NULL AUTO_INCREMENT,
  `user_order_id` BIGINT(32) NOT NULL,
  `store_menu_id` BIGINT(32) NOT NULL,
  `status` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_order_id` ASC) VISIBLE,
  INDEX `idx_menu_id` (`store_menu_id` ASC) VISIBLE
)x`
ENGINE = InnoDB;
```
> user_order, user_order_menu Table


--------------------------------------------------------------------------------------------------------------------------------
# Ch06-02. User Order 서비스 로직 개발 - 1
- UserOrder, UserOrderMenu domain 만들기
## 실습(service:db)
```java
// UserOrder
package org.delivery.db.userorder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Entity
@Table(name = "user_order")
public class UserOrderEntity extends BaseEntity {
    @Column(nullable = false)
    private Long userId;
    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private UserOrderStatus status;
    @Column(precision = 11, scale = 4, nullable = false)
    private BigDecimal amount;
    private LocalDateTime orderedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime cookingStartedAt;
    private LocalDateTime deliveryStartedAt;
    private LocalDateTime receivedAt;
}

package org.delivery.db.userorder.enums;

@AllArgsConstructor
public enum UserOrderStatus {
    REGISTERED("등록"),
    UNREGISTERED("해지"),
    ;
    private String description;
}

package org.delivery.db.userorder;

public interface UserOrderRepository extends JpaRepository<UserOrderEntity, Long> {
}


// UserOrderMenu
package org.delivery.db.userordermenu;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Entity
@Table(name = "user_order_menu")
public class UserOrderMenuEntity extends BaseEntity {
    @Column(nullable = false)
    private Long userOrderId;
    @Column(nullable = false)
    private Long storeMenuId;
    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private UserOrderMenuStatus status;
}

package org.delivery.db.userordermenu.enums;

@AllArgsConstructor
public enum UserOrderMenuStatus {
    REGISTERED("등록"),
    UNREGISTERED("해지");
    private String description;
}

package org.delivery.db.userordermenu;

public interface UserOrderMenuRepository extends JpaRepository<UserOrderMenuEntity, Long> {
}
```
> - UserOrder
> > - 
> - UserOrderMenu
> > -

--------------------------------------------------------------------------------------------------------------------------------
# Ch06-03. User Order 서비스 로직 개발 - 2
## 실습 (Mysql, service:db, api)
### DB
- 주문 로직(user_order, user_order_menu, store_menu)
```sql
-- 사용자 주문에 대한 쿼리로직
SELECT * FROM delivery.user_order;

INSERT INTO `delivery`.`user_order` (`user_id`, `status`, `amount`) VALUES ('1', 'REGISTERED', '8000');

SELECT * FROM delivery.user_order_menu;

INSERT INTO `delivery`.`user_order_menu` (`user_order_id`, `store_menu_id`, `status`) VALUES ('1', '1', 'REGISTERED');
INSERT INTO `delivery`.`user_order_menu` (`user_order_id`, `store_menu_id`, `status`) VALUES ('1', '2', 'REGISTERED');

select * from user_order where user_id = 1;
select * from user_order_menu where user_order_id = 1;
select * from store_menu where id in (1,2);

select 
	u.name,
    sm.name,
    sm.amount,
    uo.amount
from user as u
join user_order as uo on u.id = uo.user_id
join user_order_menu as uom on uom.user_order_id = uo.id
join store_menu as sm on sm.id = uom.store_menu_id
where u.id = 1
;
```
### code
- user_order_menu 주문내역 조회
- user_order 주문, 주문내역, 특정 상태값 조회
```java
// UserOrderMenu
package org.delivery.api.domain.userordermenu.service;

@RequiredArgsConstructor
@Service
public class UserOrderMenuService {
    private final UserOrderMenuRepository userOrderMenuRepository;

    public List<UserOrderMenuEntity> getUserOrderMenu(Long userOrderId) {
        var list = userOrderMenuRepository.findAllByUserOrderIdAndStatus(userOrderId, UserOrderMenuStatus.REGISTERED);
        return list;
    }
}


package org.delivery.db.userordermenu;

public interface UserOrderMenuRepository extends JpaRepository<UserOrderMenuEntity, Long> {
    List<UserOrderMenuEntity> findAllByUserOrderIdAndStatus(Long userOrderId, UserOrderMenuStatus status);
}

// UserOrder
package org.delivery.db.userorder;

public interface UserOrderRepository extends JpaRepository<UserOrderEntity, Long> {
    // 특정 유저의 모든 주문
		// select * from user_order where user_id = ? and status = ? order by id desc
    List<UserOrderEntity> findAllByUserIdAndStatusOrderByIdDesc(Long userId, UserOrderStatus status);

		// 특정 유저의 주문 중 특정 상태값들
		// select * from user_order where user_id = ? and status in (?, ...) order by id desc 
		List<UserOrderEntity> findAllByUserIdAndStatusInOrderByIdDesc(Long userId, List<UserOrderStatus> status);

    // 특정 주문
    Optional<UserOrderEntity> findAllByIdAndStatusAndUserId(Long id, UserOrderStatus status, Long userId);
}

package org.delivery.db.userordermenu.enums;

@AllArgsConstructor
public enum UserOrderStatus {
    REGISTERED("등록"),
    UNREGISTERED("해지"),
    // 주문
    ORDER("주문"),
    ACCEPT("확인"),
    COOKING("요리중"),
    DELIVERY("배달중"),
    RECEIVE("완료")
    ;
    private String description;
}


package org.delivery.api.domain.userorder.service;

@RequiredArgsConstructor
@Service
public class UserOrderService {
    private final UserOrderRepository userOrderRepository;

		// 특정 유저의 모든 주문
    public UserOrderEntity getUserOrderWithThrow(
            Long id, Long userId
    ) {
        return userOrderRepository.findAllByIdAndStatusAndUserId(id, UserOrderStatus.REGISTERED, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT));
    }

    public List<UserOrderEntity> getUserOrderList(Long userId) {
        var list = userOrderRepository.findAllByUserIdAndStatusOrderByIdDesc(userId, UserOrderStatus.REGISTERED);
        return list;
    }

		// 특정 유저의 주문중 특정 상태값들
		public List<UserOrderEntity> getUserOrderList(Long userId, List<UserOrderStatus> statusList) {
        var list = userOrderRepository.findAllByUserIdAndStatusInOrderByIdDesc(userId, statusList);
        return list;
    }

    // 주문
		public UserOrderEntity order(UserOrderEntity userOrderEntity) {
        return Optional.ofNullable(userOrderEntity)
                .map(it -> {
                    it.setStatus(UserOrderStatus.ORDER);
                    it.setOrderedAt(LocalDateTime.now());
                    return userOrderRepository.save(it);
                })
                .orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT));
    }

    // 상태 변경
		public UserOrderEntity setStatus(UserOrderEntity userOrderEntity, UserOrderStatus status) {
        userOrderEntity.setStatus(status);
        return userOrderRepository.save(userOrderEntity);
    }

    // 주문 확인
    public UserOrderEntity accept(UserOrderEntity userOrderEntity) {
        userOrderEntity.setAcceptedAt(LocalDateTime.now());
        return setStatus(userOrderEntity, UserOrderStatus.ACCEPT);
    }

    // 조리 시작
    public UserOrderEntity cooking(UserOrderEntity userOrderEntity) {
        userOrderEntity.setCookingStartedAt(LocalDateTime.now());
        return setStatus(userOrderEntity, UserOrderStatus.COOKING);
    }

    // 배달 시작
    public UserOrderEntity delivery(UserOrderEntity userOrderEntity) {
        userOrderEntity.setDeliveryStartedAt(LocalDateTime.now());
        return setStatus(userOrderEntity, UserOrderStatus.DELIVERY);
    }

    // 배달 완료
    public UserOrderEntity receive(UserOrderEntity userOrderEntity) {
        userOrderEntity.setReceivedAt(LocalDateTime.now());
        return setStatus(userOrderEntity, UserOrderStatus.RECEIVE);
    }

		// 현재 진행중인 내역
    public List<UserOrderEntity> current(Long userId) {
        return getUserOrderList(
                userId,
                List.of(
                        UserOrderStatus.ORDER,
                        UserOrderStatus.COOKING,
                        UserOrderStatus.DELIVERY,
                        UserOrderStatus.ACCEPT
                )
        );
    }

    // 과거 주문한 내역
    public List<UserOrderEntity> history(Long userId) {
        return getUserOrderList(
                userId,
                List.of(
                        UserOrderStatus.RECEIVE
                )
        );
    }
}
```
## 실행
- Mysql: 주문 데이터 생성
> - user_order, user_order_menu


--------------------------------------------------------------------------------------------------------------------------------
# Ch06-04. User Order 서비스 로직 개발 - 3
- 사용자 주문 로직 개발
## 실습 (service:api)
- user_order, user_order_menu 주문내역 로직 구성하기
```java
package org.delivery.api.domain.userorder.controller.model;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOrderRequest {
    // 주문
    // 특정 사용자가, 특정 메뉴를 주문
    // 특정 사용자 = 로그인된 세션에 들어있는 사용자
    // 특정 메뉴 id
    @NotNull
    private List<Long> storeMenuIdList;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOrderResponse {
    private Long id;
    private UserOrderStatus status;
    private BigDecimal amount;
    private LocalDateTime orderedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime cookingStartedAt;
    private LocalDateTime deliveryStartedAt;
    private LocalDateTime receivedAt;
}



package org.delivery.api.domain.userorder.controller;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user-order")
public class UserOrderApiController {

    private final UserOrderBusiness userOrderBusiness;

    // 사용자 주문
    @PostMapping("")
    public Api<UserOrderResponse> userOrder(
            @Valid @RequestBody
            Api<UserOrderRequest> userOrderRequest,
            @Parameter(hidden = true)
            @UserSession
            User user
            ) {
        var response = userOrderBusiness.userOrder(user, userOrderRequest.getBody());
        return Api.OK(response);
    }
}


package org.delivery.api.domain.userorder.business;

@RequiredArgsConstructor
@Business
public class UserOrderBusiness {
	private final UserOrderService userOrderService;
	private final UserOrderMenuService userOrderMenuService;
	private final StoreMenuService storeMenuService;

	private final UserOrderConverter userOrderConverter;
	private final UserOrderMenuConverter userOrderMenuConverter;
	// 1. 사용자, 메뉴 id
	// 2. userOrder 생성
	// 3. userOrderMenu 생성
	// 4. 응답 생성
	public UserOrderResponse userOrder(User user, UserOrderRequest body) {
        List<StoreMenuEntity> storeMenuEntityList = body.getStoreMenuIdList().stream()
                .map(it -> storeMenuService.getStoreMenuWithThrow(it))
                .collect(toList());

				// converter 구성
        UserOrderEntity userOrderEntity = userOrderConverter.toEntity(user, storeMenuEntityList);
        // 주문
        UserOrderEntity newUserOrderEntity = userOrderService.order(userOrderEntity);
        // 매핑
        List<UserOrderMenuEntity> userOrderMenuEntityList = storeMenuEntityList.stream()
                .map(it -> {
                    // menu + user order
										// converter 구성
                    UserOrderMenuEntity userOrderMenuEntity = userOrderMenuConverter.toEntity(newUserOrderEntity, it);
                    return userOrderMenuEntity;
                })
                .collect(toList());
        // 주문내역 기록 남기기
        userOrderMenuEntityList.forEach(it ->{
            userOrderMenuService.order(it);
        });

        // response
        return userOrderConverter.toResponse(newUserOrderEntity);
    }
}

package org.delivery.api.domain.userorder.converter;

@Converter
public class UserOrderConverter {
    public UserOrderEntity toEntity(
            User user,
            List<StoreMenuEntity> storeMenuEntityList
    ) {
        BigDecimal totalAmount = storeMenuEntityList.stream()
                .map(it -> it.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return UserOrderEntity.builder()
                .userId(user.getId())
                .amount(totalAmount)
                .build();
    }

    public UserOrderResponse toResponse(UserOrderEntity userOrderEntity) {
        return UserOrderResponse.builder()
                .id(userOrderEntity.getId())
                .status(userOrderEntity.getStatus())
                .amount(userOrderEntity.getAmount())
                .orderedAt(userOrderEntity.getOrderedAt())
                .acceptedAt(userOrderEntity.getAcceptedAt())
                .cookingStartedAt(userOrderEntity.getCookingStartedAt())
                .deliveryStartedAt(userOrderEntity.getDeliveryStartedAt())
                .receivedAt(userOrderEntity.getReceivedAt())
                .build();
    }
}

@RequiredArgsConstructor
@Service
public class UserOrderMenuService {
    // ~

    public UserOrderMenuEntity order(UserOrderMenuEntity userOrderMenuEntity) {
        return Optional.ofNullable(userOrderMenuEntity)
                .map(it -> {
                    it.setStatus(UserOrderMenuStatus.REGISTERED);
                    return userOrderMenuRepository.save(it);
                })
                .orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT));

    }
}

package org.delivery.api.domain.userordermenu.converter;

@RequiredArgsConstructor
@Converter
public class UserOrderMenuConverter {

    public UserOrderMenuEntity toEntity(
            UserOrderEntity userOrderEntity,
            StoreMenuEntity storeMenuEntity
    ) {
        return UserOrderMenuEntity.builder()
                .userOrderId(userOrderEntity.getId())
                .storeMenuId(storeMenuEntity.getId())
                .build();
    }
}

```
## 실행
- Swagger 
> - "/api/user-order"

--------------------------------------------------------------------------------------------------------------------------------
# Ch06-05. User Order 서비스 로직 개발 - 4
- 현재 or 과거 주문내역
## 실습 (service:api)
- UserOrder 현재, 과거, 주문 1건에 대한 내역
> - UserOrderDetailResponse: UserOrderResponse, StoreResponse, List: StoreMenuResponse
```java
@RequestMapping("/api/user-order")
public class UserOrderApiController {
	// ~
	// 현재 진행중인 주문건
	@GetMapping("/current")
	public Api<List<UserOrderDetailResponse>> current(
					@Parameter(hidden = true)
					@UserSession User user
	) {
			var response = userOrderBusiness.current(user);
			return Api.OK(response);
	}

	// 과거 주문 내역
	@GetMapping("/history")
	public Api<List<UserOrderDetailResponse>> history(
					@Parameter(hidden = true)
					@UserSession User user
	) {
			var response = userOrderBusiness.history(user);
			return Api.OK(response);
	}

	// 주문 1 건에 대한 내역
	@GetMapping("/id/{orderId}")
	public Api<UserOrderDetailResponse> read(
					@Parameter(hidden = true)
					@UserSession User user,
					@PathVariable Long orderId
	) {
			var response = userOrderBusiness.read(user, orderId);
			return Api.OK(response);
	}
}

package org.delivery.api.domain.userorder.controller.model;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOrderDetailResponse {
    private UserOrderResponse userOrderResponse;
    private StoreResponse storeResponse;
    private List<StoreMenuResponse> storeMenuResponseList;
}

public class UserOrderBusiness {
	// ~
	private final StoreService storeService;
	private final StoreMenuConverter storeMenuConverter;
	private final StoreConverter storeConverter;

	public List<UserOrderDetailResponse> current(User user) {
		var userOrderEntityList = userOrderService.current(user.getId());
		// 주문 1건씩 처리
		var userOrderDetailResponseList = userOrderEntityList.stream()
			.map(it -> {
				// 사용자 주문 메뉴
				var userOrderMenuEntityList = userOrderMenuService.getUserOrderMenu(it.getId());
				var storeMenuEntityList = userOrderMenuEntityList.stream()
					.map(userOrderMenuEntity -> {
						var storeMenuEntity = storeMenuService.getStoreMenuWithThrow(userOrderMenuEntity.getStoreMenuId());
						return storeMenuEntity;
					}).collect(toList());

				// 사용자가 주문한 스토어 TODO 리팩토링 필요
				var storeEntity = storeService.getStoreWithThrow(storeMenuEntityList.stream().findFirst().get().getStoreId());

				return UserOrderDetailResponse.builder()
					.userOrderResponse(userOrderConverter.toResponse(it))
					.storeMenuResponseList(storeMenuConverter.toResponse(storeMenuEntityList))
					.storeResponse(storeConverter.toResponse(storeEntity))
					.build();
			}).collect(toList());

			return userOrderDetailResponseList;
	}

	public List<UserOrderDetailResponse> history(User user) {
		var userOrderEntityList = userOrderService.history(user.getId());
		// 주문 1건씩 처리
		var userOrderDetailResponseList = userOrderEntityList.stream()
			.map(it -> {
				// 사용자 주문 메뉴
				var userOrderMenuEntityList = userOrderMenuService.getUserOrderMenu(it.getId());
				var storeMenuEntityList = userOrderMenuEntityList.stream()
					.map(userOrderMenuEntity -> {
						var storeMenuEntity = storeMenuService.getStoreMenuWithThrow(userOrderMenuEntity.getStoreMenuId());
						return storeMenuEntity;
					}).collect(toList());

				// 사용자가 주문한 스토어 TODO 리팩토링 필요
				var storeEntity = storeService.getStoreWithThrow(storeMenuEntityList.stream().findFirst().get().getStoreId());

				return UserOrderDetailResponse.builder()
					.userOrderResponse(userOrderConverter.toResponse(it))
					.storeMenuResponseList(storeMenuConverter.toResponse(storeMenuEntityList))
					.storeResponse(storeConverter.toResponse(storeEntity))
					.build();
			}).collect(toList());

		return userOrderDetailResponseList;
	}

	public UserOrderDetailResponse read(User user, Long orderId) {
		UserOrderEntity userOrderEntity = userOrderService.getUserOrderWithOutStatusWithThrow(orderId, user.getId());
		var userOrderMenuEntityList = userOrderMenuService.getUserOrderMenu(userOrderEntity.getId());
		var storeMenuEntityList = userOrderMenuEntityList.stream()
			.map(userOrderMenuEntity -> {
				var storeMenuEntity = storeMenuService.getStoreMenuWithThrow(userOrderMenuEntity.getStoreMenuId());
				return storeMenuEntity;
			}).collect(toList());

		// 사용자가 주문한 스토어 TODO 리팩토링 필요
		var storeEntity = storeService.getStoreWithThrow(storeMenuEntityList.stream().findFirst().get().getStoreId());

		return UserOrderDetailResponse.builder()
			.userOrderResponse(userOrderConverter.toResponse(userOrderEntity))
			.storeMenuResponseList(storeMenuConverter.toResponse(storeMenuEntityList))
			.storeResponse(storeConverter.toResponse(storeEntity))
			.build();
	}
}

public class StoreMenuConverter {
	// ~
	
	public List<StoreMenuResponse> toResponse(
					List<StoreMenuEntity> list
	) {
			return list.stream()
							.map(it -> {
									return toResponse(it);
							}).collect(toList());
	}
}

public class UserOrderService {
	// 상태값 없이 조회
	public UserOrderEntity getUserOrderWithOutStatusWithThrow(Long id, Long userId) {
			return userOrderRepository.findAllByIdAndUserId(id, userId)
							.orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT));
	}
}
```
## 실행
- Swagger 
> - "/api/user-order"
> > - "/current"
> > - "/id/{orderId}"