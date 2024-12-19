# ch08. 간단한 게시판 만들기
- [1. 간단한 게시판 시스템 설계](#ch08-01-간단한-게시판-시스템-설계)
- [2. 간단한 게시판 테이블 설계](#ch08-02-간단한-게시판-테이블-설계)
- [3. 간단한 게시판 - Entity 개발](#ch08-03-간단한-게시판-개발하기-entity-개발)
- [4. 간단한 게시판 - API End Point 개발 - 1](#ch08-04-간단한-게시판-개발하기-api-end-point-개발---1)
- [5. 간단한 게시판 - API End Point 개발 - 2](#ch08-05-간단한-게시판-개발하기-api-end-point-개발---2)
- [6. 간단한 게시판 - API End Point 개발 - 3](#ch08-06-간단한-게시판-개발하기-api-end-point-개발---3)
- [7. 간단한 게시판 - JPA 연관관계 설정 - 1](#ch08-07-간단한-게시판-개발하기-jpa-연관관계-설정하기---1)
- [8. 간단한 게시판 - JPA 연관관계 설정 - 2](#ch08-08-간단한-게시판-개발하기-jpa-연관관계-설정하기---2)


--------------------------------------------------------------------------------------------------------------------------------
# ch08-01. 간단한 게시판 시스템 설계
## 프로젝트 생성
- Project: simple-board
> - Gradle - Groovy, JDK11, com.example.simple-board
> - Dependencies
> > Spring web, Lombok, JPA, Mysql, validation
> - Mysql(Database): simple_board schema

## 실습 (simple-baord)
```yml
spring:
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
    hibernate:
      ddl-auto: validate
  datasource:
    url: jdbc:mysql://localhost:3306/simple_board?userSSL=false&useUnicode=true&PublicKeyRetrieval=true
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: root1234!!
logging:
  level:
    org.hibernate.SQL: DEBUG
#    org.hibernate.orm.jdbc.bind: TRACE
#    org.hibernate.type.descriptor.sql: TRACE
    org.hibernate.type.BasicTypeRegistry: WARN
```


--------------------------------------------------------------------------------------------------------------------------------
# ch08-02. 간단한 게시판 테이블 설계
- ERD 그리기
> - board(게시판), post(게시글), reply(댓글) 테이블
> >  board -< post -< reply
## 실습 
```sql
-- -----------------------------------------------------
-- Schema simple_board
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `simple_board` DEFAULT CHARACTER SET utf8 ;
USE `simple_board` ;

-- -----------------------------------------------------
-- Table `simple_board`.`board`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `simple_board`.`board` (
  `id` BIGINT(32) NOT NULL AUTO_INCREMENT COMMENT 'PK',
  `board_name` VARCHAR(100) NOT NULL,
  `status` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `simple_board`.`post`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `simple_board`.`post` (
  `id` BIGINT(32) NOT NULL AUTO_INCREMENT,
  `board_id` BIGINT(32) NOT NULL,
  `user_name` VARCHAR(50) NOT NULL,
  `password` VARCHAR(4) NOT NULL,
  `email` VARCHAR(100) NOT NULL,
  `status` VARCHAR(50) NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `content` TEXT NULL,
  `posted_at` DATETIME NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `simple_board`.`reply`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `simple_board`.`reply` (
  `id` BIGINT(32) NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT(32) NOT NULL,
  `user_name` VARCHAR(50) NOT NULL,
  `password` VARCHAR(4) NOT NULL,
  `status` VARCHAR(50) NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `content` TEXT NOT NULL,
  `replied_at` DATETIME NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;
```
> - File > Export > `Forward Enginess SQL CREATE Script` 
> > FK/Index 사용하지 않음 (강제약)


--------------------------------------------------------------------------------------------------------------------------------
# ch08-03. 간단한 게시판 개발하기 Entity 개발
- Entity 개발
## 실습
```java
package com.example.simpleboard.board.db;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity(name = "board")
public class BoardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String boardName;
    private String status;
    @OneToMany(mappedBy = "board")
    @Where(clause = "status = 'REGISTERED'")
    @org.hibernate.annotations.OrderBy(clause = "id desc")
    @Builder.Default
    private List<PostEntity> postList = new ArrayList<>();
}
public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
}

package com.example.simpleboard.post.db;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity(name = "post")
public class PostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JsonIgnore
    @ToString.Exclude
    private BoardEntity board;  // board + _id
    private String userName;
    private String password;
    private String email;
    private String status;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;
    private LocalDateTime postedAt;
    @OneToMany(mappedBy = "post")
    @Builder.Default
    private List<ReplyEntity> replyList = new ArrayList<>();
}
public interface PostRepository extends JpaRepository<PostEntity, Long> {
}

package com.example.simpleboard.reply.db;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity(name = "reply")
public class ReplyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @ToString.Exclude
    @JsonIgnore
    private PostEntity post;
    private String userName;
    private String password;
    private String status;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;
    private LocalDateTime repliedAt;
}
public interface ReplyRepository extends JpaRepository<ReplyEntity, Long> {
}
```
> - `@Column(columnDefinition = "TEXT")`
> > DB Type: TEXT - Java Type:String


--------------------------------------------------------------------------------------------------------------------------------
# ch08-04. 간단한 게시판 개발하기 API End Point 개발 - 1
- 게시판, 게시글 API 개발

## 실습
```java
package com.example.simpleboard.board.*;
@Slf4j
@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class BoardApiController {
    private final BoardService boardService;

    @PostMapping("")
    public BoardDto create(@Valid @RequestBody BoardRequest boardRequest) {
        return boardService.create(boardRequest);
    }
}

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BoardRequest {
    @NotBlank
    private String boardName;
}

@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardConverter boardConverter;

    public BoardDto create(BoardRequest boardRequest) {
        BoardEntity boardEntity = BoardEntity.builder()
                .boardName(boardRequest.getBoardName())
                .status("REGISTERED")
                .build();
        BoardEntity saveEntity = boardRepository.save(boardEntity);
        return boardConverter.toDto(saveEntity);
    }
}

package com.example.simpleboard.post.*;
@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostApiController {
    private final PostService postService;
    @PostMapping("")
    public PostEntity create(@Valid @RequestBody PostRequest postRequest) {
        return postService.create(postRequest);
    }
}

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PostRequest {
    private Long boardId = 1L;
    @NotBlank
    private String userName;
    @NotBlank
    @Size(min = 4, max = 4)
    private String password;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String title;
    @NotBlank
    private String content;
}

@Transactional
@Service
@RequiredArgsConstructor
public class PostService {
    public PostEntity create(PostRequest postRequest) {
        BoardEntity boardEntity = boardRepository.findById(postRequest.getBoardId()).get(); // 임시고정

        PostEntity postEntity = PostEntity.builder()
                .boardId(1L)    // 임시고정
                .userName(postRequest.getUserName())
                .password(postRequest.getPassword())
                .email(postRequest.getEmail())
                .status("REGISTERED")
                .title(postRequest.getTitle())
                .content(postRequest.getContent())
                .postedAt(LocalDateTime.now())
                .build();
        return postRepository.save(postEntity);
    }
}
```

## 실습
- Talent API
> - 게시판 생성: Q&A
> - 게시글 생성: 
```json
{
	"user_name": "홍길동",
	// ...
}
```



--------------------------------------------------------------------------------------------------------------------------------
# ch08-05. 간단한 게시판 개발하기 API End Point 개발 - 2
- 게시판 전체보기, 열람, 삭제 API 개발

## 실습
```java
public class PostApiController {
    // ~
		@PostMapping("/view")
    public PostEntity view(@Valid @RequestBody PostViewRequest postViewRequest) {
        return postService.view(postViewRequest);
    }

    @GetMapping("/all")
    public Api<List<PostEntity>> list(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return postService.all(pageable);
    }

    @PostMapping("/delete")
    public void delete(@Valid @RequestBody PostViewRequest postViewRequest) {
        postService.delete(postViewRequest);
    }
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PostViewRequest {
    @NotNull
    private Long postId;
    @NotEmpty
    private String password;
}

public class PostService {
    private final PostRepository postRepository;

    public PostEntity create(PostRequest postRequest) {
        PostEntity postEntity = PostEntity.builder()
                .boardId(1L)    // 임시고정
                .userName(postRequest.getUserName())
                .password(postRequest.getPassword())
                .email(postRequest.getEmail())
                .status("REGISTERED")
                .title(postRequest.getTitle())
                .content(postRequest.getContent())
                .postedAt(LocalDateTime.now())
                .build();
        return postRepository.save(postEntity);
    }

    /**
     * 1. 게시글이 있는가?
     * 2. 비밀번호가 맞는가?
     */
    public PostEntity view(PostViewRequest postViewRequest) {
        return postRepository.findFirstByIdAndStatusOrderByIdDesc(postViewRequest.getPostId(), "REGISTERED")
                .map(it -> {
                    // entity 존재
                    if (!it.getPassword().equals(postViewRequest.getPassword())) {
                        String format = "패스워드가 맞지 않습니다 %s vs %s";
                        throw new RuntimeException(String.format(format, it.getPassword(), postViewRequest.getPassword()));
                    }
                    return it;
                }).orElseThrow(() -> {
                    return new RuntimeException("해당 게시글이 존재 하지 않습니다 : " + postViewRequest.getPostId());
                });
    }

    public List<PostEntity> all() {
        return postRepository.findAll();
    }


    public void delete(PostViewRequest postViewRequest) {
        postRepository.findById(postViewRequest.getPostId())
                .map(it -> {
                    if (it.getPassword().equals(postViewRequest.getPostId())) {
                        String format = "패스워드가 맞지 않습니다 %s vs %s";
                        throw new RuntimeException(String.format(format, it.getPassword(), postViewRequest.getPassword()));
                    }
                    it.setStatus("UNREGISTERED");
                    postRepository.save(it);
                    return it;
                }).orElseThrow(() -> {
                    return new RuntimeException("해당 게시글이 존재 하지 않습니다 : " + postViewRequest.getPostId());
                });
    }
}

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    // select * from post where id = ? and status = ? order by id desc limit1
    public Optional<PostEntity> findFirstByIdAndStatusOrderByIdDesc(Long id, String status);
}
```

## 실습
- Talent API
> - 전체 게시글 / 열람 / 삭제


--------------------------------------------------------------------------------------------------------------------------------
# ch08-06. 간단한 게시판 개발하기 API End Point 개발 - 3
- 댓글 API 개발

## 실습
```java
package com.example.simpleboard.reply.controller;
@RestController
@RequestMapping("/api/reply")
@RequiredArgsConstructor
public class ReplyApiController {
    private final ReplyService replyService;

    @PostMapping("")
    public ReplyEntity create(@RequestBody @Valid ReplyRequest replyRequest) {
        return replyService.create(replyRequest);
    }
}

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReplyRequest {
    @NotNull
    private Long postId;
    @NotBlank
    private String userName;
    @NotBlank
    @Size(min = 4, max = 4)
    private String password;
    @NotBlank
    private String title;
    @NotBlank
    private String content;
}

@Transactional
@Service
@RequiredArgsConstructor
public class ReplyService {
    private final ReplyRepository replyRepository;

    public ReplyEntity create(ReplyRequest replyRequest) {
        ReplyEntity entity = ReplyEntity.builder()
                .postId(replyRequest.getPostId())
                .userName(replyRequest.getUserName())
                .password(replyRequest.getPassword())
                .status("REGISTERED")
                .title(replyRequest.getTitle())
                .content(replyRequest.getContent())
                .repliedAt(LocalDateTime.now())
                .build();

        return replyRepository.save(entity);
    }

    public List<ReplyEntity> findAllByPostId(Long postId) {
        return replyRepository.findAllByPostIdAndStatusOrderByIdDesc(postId, "REGISTERED");
    }
}

public interface ReplyRepository extends JpaRepository<ReplyEntity, Long> {
    // select * from reply where post_id = ? and status = ? order by id desc
    public List<ReplyEntity> findAllByPostIdAndStatusOrderByIdDesc(Long id, String status);
}

public class PostService {
	private final ReplyService replyService;
	// ~
	/**
     * 1. 게시글이 있는가?
     * 2. 비밀번호가 맞는가?
     * @param postViewRequest
     * @return
     */
    public PostEntity view(PostViewRequest postViewRequest) {
        return postRepository.findFirstByIdAndStatusOrderByIdDesc(postViewRequest.getPostId(), "REGISTERED")
                .map(it -> {
                    // entity 존재
                    if (!it.getPassword().equals(postViewRequest.getPassword())) {
                        String format = "패스워드가 맞지 않습니다 %s vs %s";
                        throw new RuntimeException(String.format(format, it.getPassword(), postViewRequest.getPassword()));
                    }

										//답변글도 같이 적용
										List<ReplyEntity> replyList = replyService.findAllByPostId(it.getId());
										it.setReplyList(replyList);

                    return it;
                }).orElseThrow(() -> {
                    return new RuntimeException("해당 게시글이 존재 하지 않습니다 : " + postViewRequest.getPostId());
                });
    }
}

public class PostEntity {
    // ~
		@Transient
		private List<ReplyEntity> replyList = new ArrayList<>();

}
```
## 실습2
- Talent API
> - 답변작성(2) > 게시글 열람




--------------------------------------------------------------------------------------------------------------------------------
# ch08-07. 간단한 게시판 개발하기 JPA 연관관계 설정하기 - 1
- Board -< Post JPA 연관관계 설정하기

## 실습
```java
public class BoardEntity {
    // ~
	@OneToMany(mappedBy = "board")
    @Where(clause = "status = 'REGISTERED'")
    @org.hibernate.annotations.OrderBy(clause = "id desc")
    @Builder.Default
    private List<PostEntity> postList = new ArrayList<>();
}

public class PostEntity {
    // ~
	@ManyToOne
    @JsonIgnore
    @ToString.Exclude
    private BoardEntity board;  // board + _id

}

public class PostRequest {
    private Long boardId = 1L;
    // ~
}
public class PostService {
	public PostEntity create(PostRequest postRequest) {
        BoardEntity boardEntity = boardRepository.findById(postRequest.getBoardId()).get(); // 임시고정

        PostEntity postEntity = PostEntity.builder()
                .board(boardEntity)
                .userName(postRequest.getUserName())
                .password(postRequest.getPassword())
                .email(postRequest.getEmail())
                .status("REGISTERED")
                .title(postRequest.getTitle())
                .content(postRequest.getContent())
                .postedAt(LocalDateTime.now())
                .build();
        return postRepository.save(postEntity);
    }
}

public class BoardApiController {
    // BoardDto
	public BoardDto create(BoardRequest boardRequest) {
        BoardEntity boardEntity = BoardEntity.builder()
                .boardName(boardRequest.getBoardName())
                .status("REGISTERED")
                .build();
        BoardEntity saveEntity = boardRepository.save(boardEntity);
        return boardConverter.toDto(saveEntity);
    }

	@GetMapping("/id/{id}")
    public BoardDto view(@PathVariable Long id) {
        BoardDto boardDto = boardService.view(id);
        return boardDto;
    }
}

public class BoardService {
	private final BoardConverter boardConverter;

	public BoardDto create(BoardRequest boardRequest) {
        BoardEntity boardEntity = BoardEntity.builder()
                .boardName(boardRequest.getBoardName())
                .status("REGISTERED")
                .build();
        BoardEntity saveEntity = boardRepository.save(boardEntity);
		// 추가
        return boardConverter.toDto(saveEntity);
    }

    public BoardDto view(Long id) {
        BoardEntity boardEntity = boardRepository.findById(id).get();
		// 추가
        return boardConverter.toDto(boardEntity);
    }
}

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BoardDto {
    private Long id;

    private String boardName;
    private String status;
    private List<PostDto> postList = List.of();
}
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PostDto {
    private Long id;
    private Long boardId;
    private String userName;
    private String password;
    private String email;
    private String status;
    private String title;
    private String content;
    private LocalDateTime postedAt;
}

@Service
@RequiredArgsConstructor
public class BoardConverter {
    private final PostConverter postConverter;

    public BoardDto toDto(BoardEntity boardEntity) {
        List<PostDto> postList = boardEntity.getPostList()
                .stream()
                .map(postConverter::toDto)
                .collect(toList());

        return BoardDto.builder()
                .id(boardEntity.getId())
                .boardName(boardEntity.getBoardName())
                .status(boardEntity.getStatus())
                .postList(postList)
                .build();
    }
}

@Service
public class PostConverter {

    public PostDto toDto(PostEntity postEntity) {
        return PostDto.builder()
                .id(postEntity.getId())
                .userName(postEntity.getUserName())
                .password(postEntity.getPassword())
                .status(postEntity.getStatus())
                .email(postEntity.getEmail())
                .title(postEntity.getTitle())
                .content(postEntity.getContent())
                .postedAt(postEntity.getPostedAt())
                .boardId(postEntity.getBoard().getId())
                .build();
    }
}
```
> postList - board `루프순환` 방지를 위한 `@JsonIgnore` - (view), `ToString.Exclude` - (log)

## 실습 - 2
- Talent API
> - 게시판 열람

## 정리
- JPA 연관관계 설정
> - `@OneToMany(mappedBy = "<Entity_Name>")`
> - `@ManyToOne`
- JPA 연관 결과 무한반복 방지
> - @JsonIgnore: 결과출력시
> - @ToString.Exclude: log출력시
> > 결과는 Dto를 만들어서 내린다


--------------------------------------------------------------------------------------------------------------------------------
# ch08-08. 간단한 게시판 개발하기 JPA 연관관계 설정하기 - 2
- JPA 연관관계 설정 
> post -< reply

## 실습
```java
public class PostEntity {
	// ~
	@OneToMany(mappedBy = "post")
	@Builder.Default
	private List<ReplyEntity> replyList = new ArrayList<>();
}

public class ReplyEntity {
	// ~
	@ManyToOne
	@ToString.Exclude
	@JsonIgnore
	private PostEntity post;
}

public class ReplyService {
	private final PostRepository postRepository;

	public ReplyEntity create(ReplyRequest replyRequest) {
        Optional<PostEntity> postEntityOptional = postRepository.findById(replyRequest.getPostId());
        if (postEntityOptional.isEmpty()) {
            throw new RuntimeException("해당 게시물이 존재하지 않습니다 : " + replyRequest.getPostId());
        }

        ReplyEntity entity = ReplyEntity.builder()
				// post 연관 설정
                .post(postEntityOptional.get())
                .userName(replyRequest.getUserName())
                .password(replyRequest.getPassword())
                .status("REGISTERED")
                .title(replyRequest.getTitle())
                .content(replyRequest.getContent())
                .repliedAt(LocalDateTime.now())
                .build();

        return replyRepository.save(entity);
    }
}

public class PostService {
    /**
     * 1. 게시글이 있는가?
     * 2. 비밀번호가 맞는가?
     * @param postViewRequest
     * @return
     */
    public PostEntity view(PostViewRequest postViewRequest) {
        return postRepository.findFirstByIdAndStatusOrderByIdDesc(postViewRequest.getPostId(), "REGISTERED")
                .map(it -> {
                    // entity 존재
                    if (!it.getPassword().equals(postViewRequest.getPassword())) {
                        String format = "패스워드가 맞지 않습니다 %s vs %s";
                        throw new RuntimeException(String.format(format, it.getPassword(), postViewRequest.getPassword()));
                    }

                    // 답변글도 같이 적용
//                    List<ReplyEntity> replyList = replyService.findAllByPostId(it.getId());
//                    it.setReplyList(replyList);

                    return it;
                }).orElseThrow(() -> {
                    return new RuntimeException("해당 게시글이 존재 하지 않습니다 : " + postViewRequest.getPostId());
                });
    }
}

public class BoardEntity {
    @OneToMany(mappedBy = "board")
    @Where(clause = "status = 'REGISTERED'")
    @org.hibernate.annotations.OrderBy(clause = "id desc")
    @Builder.Default
    private List<PostEntity> postList = new ArrayList<>();
}

// 페이징 API 스펙
package com.example.simpleboard.board.common;
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Api <T>{
    private T body;
    private Pagination pagination;
}

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pagination {
    private Integer page;
    private Integer size;
    private Integer currentElements;
    private Integer totalPage;
    private Long totalElements;
}

public class PostApiController {
    @GetMapping("/all")
    public Api<List<PostEntity>> list(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return postService.all(pageable);
    }
}

public class PostService {
	public Api<List<PostEntity>> all(Pageable pageable) {
        Page<PostEntity> list = postRepository.findAll(pageable);

        Pagination pagination = Pagination.builder()
                .page(list.getNumber())
                .size(list.getSize())
                .currentElements(list.getNumberOfElements())
                .totalElements(list.getTotalElements())
                .totalPage(list.getTotalPages())
                .build();

        Api<List<PostEntity>> response = Api.<List<PostEntity>>builder()
                .body(list.toList())
                .pagination(pagination)
                .build();
        return response;
    }
}
```
> @Where, @OrderBy, @Builder.Default

## 실습2
- Talent API
> - 게시판 열람
> - 게시글 열람(all)
```
- Query Parameters
page: 0
size: 5
``` 

## 정리
- `@Builder.Default` 
> - Entity 연관관계시 Builder Pattern List 초기화 
> > Builder패턴시 NoArgsConstructor시 빈 컬렉션 초기화를 위해
- Entity 정렬 및 조건
> - @Where(clause = "status = 'REGISTERED'")
> - @org.hibernate.annotations.OrderBy(clause = "id desc")
- JPA 페이징 `Pageable`
> - @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable: Request
> - `~Repository.findAll(pageable): Page<~Entity>`
> > list.getNumber() > page, list.getSize(): size  
> > list.getNumberOfElements(): CurrnetElements  
> > list.getTotalElements(), list.getTotalPages()
