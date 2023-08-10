# Ch06. 실전 프로젝트 5: 상품 주문 개발
# Ch06-01. User Order 데이터베이스 설계
주문과 상품메뉴 테이블 관계, N:M
## user_order 주문 테이블, store_menu
- user_order < user_order_menu > store_menu, N:M 테이블 Map
```
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
> user_order, user_order_menu


# Ch06-02. User Order 서비스 로직 개발 - 1
## UserOrder, UserOrderMenu domain 만들기
- Entity, Repository, Status
- UserOrderMenuRepository
```
List<UserOrderMenuEntity> findAllByUserOrderIdAndStatus(Long userOrderId, UserOrderMenuStatus status);
```


# Ch06-03. User Order 서비스 로직 개발 - 2
## DB
```
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
## UserOrderMenu
- UserOrderMenuService, UserOrderRepository
```
- UserOrderMenuService
	public List<UserOrderMenuEntity> getUserOrderMenu(Long userOrderId) {
		var list = userOrderMenuRepository.findAllByUserOrderIdAndStatus(userOrderId, UserOrderMenuStatus.REGISTERED);
		return list;
	}

- UserOrderRepository
	// 특정 유저의 모든 주문
	List<UserOrderEntity> findAllByUserIdAndStatusOrderByIdDesc(Long userId, UserOrderStatus status);

	List<UserOrderEntity> findAllByUserIdAndStatusInOrderByIdDesc(Long userId, List<UserOrderStatus> status);
	// 특정 주문
	Optional<UserOrderEntity> findAllByIdAndStatusAndUserId(Long id, UserOrderStatus status, Long userId);
```
- UserOrderService
```
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

	public List<UserOrderEntity> getUserOrderList(Long userId, List<UserOrderStatus> statusList) {
		var list = userOrderRepository.findAllByUserIdAndStatusInOrderByIdDesc(userId, statusList);
		return list;
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
```


# Ch06-04. User Order 서비스 로직 개발 - 3
- UserOrder Controller, Request, Response, Business, converter
```
- UserOrderRequest
	// 주문
	// 특정 사용자가, 특정 메뉴를 주문
	// 특정 메뉴 id
	private List<Long> storeMenuIdList;

- UserOrderApiController("/api/user-order")
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

- UserOrderBusiness
	// 1. 사용자, 메뉴 id
	// 2. userOrder 생성
	// 3. userOrderMenu 생성
	// 4. 응답 생성
	public UserOrderResponse userOrder(User user, UserOrderRequest body) {
			List<StoreMenuEntity> storeMenuEntityList = body.getStoreMenuIdList().stream()
							.map(it -> storeMenuService.getStoreMenuWithThrow(it))
							.collect(toList());

			UserOrderEntity userOrderEntity = userOrderConverter.toEntity(user, storeMenuEntityList);
			// 주문
			UserOrderEntity newUserOrderEntity = userOrderService.order(userOrderEntity);
			// 매핑
			List<UserOrderMenuEntity> userOrderMenuEntityList = storeMenuEntityList.stream()
							.map(it -> {
									// menu + user order
									UserOrderMenuEntity userOrderMenuEntity = userOrderMenuConverter.toEntity(newUserOrderEntity, it);
									return userOrderMenuEntity;
							})
							.collect(toList());

			userOrderMenuEntityList.forEach(it ->{
					userOrderMenuService.order(it);
			});

			// response
			return userOrderConverter.toResponse(newUserOrderEntity);
	}

- UserOrderConverter
	public UserEntity toEntity(UserRegisterRequest request) {
        return Optional.ofNullable(request)
                .map(it -> {
                    return UserEntity.builder()
                            .name(request.getName())
                            .email(request.getEmail())
                            .password(request.getPassword())
                            .address(request.getAddress())
                            .build();
                })
                .orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT, "UserRegisterRequest Null"));
    }

    public UserResponse toResponse(UserEntity userEntity) {
        return Optional.ofNullable(userEntity)
                .map(it ->{
                    return UserResponse.builder()
                            .id(userEntity.getId())
                            .name(userEntity.getName())
                            .status(userEntity.getStatus())
                            .email(userEntity.getEmail())
                            .address(userEntity.getAddress())
                            .registeredAt(userEntity.getRegisteredAt())
                            .unregisteredAt(userEntity.getUnregisteredAt())
                            .lastLoginAt(userEntity.getLastLoginAt())
                            .build();
                })
                .orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT, "UserRegisterRequest Null"));
    }
	
- UserPrderResponse
	private Long id;
    private UserOrderStatus status;
    private BigDecimal amount;
    private LocalDateTime orderedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime cookingStartedAt;
    private LocalDateTime deliveryStartedAt;
    private LocalDateTime receivedAt;
```
> Controller: userOrder(user, userOrderRequest)  
Business: userOrder(user, userOrderRequest) // UserOrder, UserOrderMenu order(): Service
>> User와 StoreMenu id List를 받아 UserOrder, UserOrderMenu order()

- UserOrderMenu
```
- UserOrderMenuService
	public List<UserOrderMenuEntity> getUserOrderMenu(Long userOrderId) {
        var list = userOrderMenuRepository.findAllByUserOrderIdAndStatus(userOrderId, UserOrderMenuStatus.REGISTERED);
        return list;
    }

    public UserOrderMenuEntity order(UserOrderMenuEntity userOrderMenuEntity) {
        return Optional.ofNullable(userOrderMenuEntity)
                .map(it -> {
                    it.setStatus(UserOrderMenuStatus.REGISTERED);
                    return userOrderMenuRepository.save(it);
                })
                .orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT));

    }
```
> UserOrder 


# Ch06-05. User Order 서비스 로직 개발 - 4
- 현재 or 과거 주문내역
- UserOrderApiController
```
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
```
> @UserSession User current, history, read()  
user의 user_order, user_order_menu, store_menu, store

- UserOrderBusiness
```
- UserOrderBusiness
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

- UserOrderDetailResponse
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOrderDetailResponse {
    private UserOrderResponse userOrderResponse;
    private StoreResponse storeResponse;
    private List<StoreMenuResponse> storeMenuResponseList;
}

- StoreMenuConverter
	public List<StoreMenuResponse> toResponse(
					List<StoreMenuEntity> list
	) {
			return list.stream()
							.map(it -> {
									return toResponse(it);
							}).collect(toList());
	}

- UserOrderService
	public UserOrderEntity getUserOrderWithOutStatusWithThrow(Long id, Long userId) {
			return userOrderRepository.findAllByIdAndUserId(id, userId)
							.orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT));
	}
```
> user > userOrderService.current(user.getId()) : userOrderEntityList > userOrderDetailResponse  
userOrderMenuService.getUserOrderMenu(it.getId()) : userOrderMenuEntityList  
userOrderMenuEntityList.stream() > storeMenuService.getStoreMenuWithThrow(userOrderMenuEntity.getStoreMenuId()) : storeMenuEntity > StoreMenuEntityList  
storeService.getStoreWithThrow(storeMenuEntityList.stream().findFirst().get().getStoreId()) : StoreEntity