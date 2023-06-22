# Ch08. 간단한 게시판 만들기
## CH08-01. 간단한 게시판 시스템 설계
- Project
> simple-board  
- Dependencies
> Spring web, Lombok, JPA, Mysql, validation
- RDBMS: book_store schema
### application.yaml

## CH08-02. 간단한 게시판 테이블 설계
### ERD
```
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
> FK 사용하지 않음

## CH08-03. 간단한 게시판 개발하기 Entity 개발
### board, post, reply/db, model, controller, service

## CH08-04. 간단한 게시판 개발하기 API End Point 개발 - 1
### "/api/board" Controller, Service - create
### "/api/post" Controller, Service - create

## CH08-05. 간단한 게시판 개발하기 API End Point 개발 - 2
### post - all, view, delete, PostRequest, PostViewRequest
- PostService
```
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
```
> view, delete: 게시글존재? 패스워드? 
- PostRepository
```
public interface PostRepository extends JpaRepository<PostEntity, Long> {
    // select * from post where id = ? and status = ? order by id desc limit1
    public Optional<PostEntity> findFirstByIdAndStatusOrderByIdDesc(Long id, String status);
}
```
> id, status: "REGISTERED"만 보기

## CH08-06. 간단한 게시판 개발하기 API End Point 개발 - 3
- ReplyApiController, ReplyService, ReplyRepository - create
- ReplyRequest
- PostService - view(PostViewRequest postViewRequest), PostEntity - List<ReplyEntity> replyList
> ReplyList 추가  
@Transient List<ReplyEntity> replyList

## CH08-07. 간단한 게시판 개발하기 JPA 연관관계 설정하기 - 1
Board - Post JPA 연관관계 설정하기
- BoardEntity
```
@OneToMany(mappedBy = "board")
private List<PostEntity> postList = List.of();
```
- PostEntity
```
@ManyToOne
@JsonIgnore
@ToString.Exclude
private BoardEntity board;  // board + _id
```
> postList - board `루프순환` 방지를 위한 `@JsonIgnore` - (view), `ToString.Exclude` - (log)

### Entity 대신 DTO 사용 및 Converter
- BoardDto, BoardConverter
```
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
```
- PostDto, PostConverter
- BoardApiController, BoardService - BoardConverter

## CH08-08. 간단한 게시판 개발하기 JPA 연관관계 설정하기 - 2
### Post - Reply / PostEntity, ReplyEntity
Board 조회시 게시글 
```
- Post
@OneToMany(mappedBy = "post")
@Builder.Default
private List<ReplyEntity> replyList = new ArrayList<>();

- Reply
@ManyToOne
@ToString.Exclude
@JsonIgnore
private PostEntity post;
```
> OneToMany(mappedBy = "post"), @ManyToOne  
@JsonIgnore, @ToString.Exclude

### REGISTERED - BoardEntity, PostEntity
```
@OneToMany(mappedBy = "board")
@Where(clause = "status = 'REGISTERED'")
@org.hibernate.annotations.OrderBy(clause = "id desc")
@Builder.Default
private List<PostEntity> postList = new ArrayList<>();
```
> @Where(clause = "col = 'val'")  
@Builder.Default builder() 시 해당값 디폴드 값주기

### Index - Page / Api, Pagination
- Api, Pagination 
```
@~
public class Api <T>{
    private T body;
    private Pagination pagination;
}

@~
public class Pagination {
    private Integer page;
    private Integer size;
    private Integer currentElements;
    private Integer totalPage;
    private Long totalElements;
}
```
- PostApiController, PostService
```
- Controller
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

@GetMapping("/all")
public Api<List<PostEntity>> list(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
    return postService.all(pageable);
}

- Service
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
```
> @PageableDefault(page, size, sort, direction), Pageable  
list.getNumber(): 현재 페이지  
list.getSize(): 요청 size 개수
list.getNumberOfElements()/getTotalElements/getTotalPages