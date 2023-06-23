# Ch10. 간단한 게시판 프로젝트 개선
## Ch10-01. 추상화를 통한 게시판 프로젝트 개선 - 1
### 추상화 crud
- CRUDInterface
```
public interface CRUDInterface<DTO> {
    DTO create(DTO dto);

    Optional<DTO> read(Long id);

    DTO update(DTO dto);

    void delete(Long id);

    Api<List<DTO>> list(Pageable pageable);
}
```
- <A> CRUDAbstractService
```
/**
 * dto -> entity -> dto
 */
public abstract class CRUDAbstractService<DTO, ENTITY> implements CRUDInterface<DTO>{

    @Autowired(required = false)
    private Converter<DTO, ENTITY> converter;

    @Autowired(required = false)
    private JpaRepository<ENTITY, Long> jpaRepository;

    @Override
    public DTO create(DTO dto) {

        // dto -> entity
        var entity = converter.toEntity(dto);
        // entity -> save
        jpaRepository.save(entity);
        // save -> dto
        var returnDto = converter.toDTO(entity);
        return null;
    }

    @Override
    public Optional<DTO> read(Long id) {
        var optionalEntity = jpaRepository.findById(id);
        var dto = optionalEntity.map(converter::toDTO).orElseGet(() -> null);
        return Optional.ofNullable(dto);
    }

    @Override
    public DTO update(DTO dto) {
        var entity = converter.toEntity(dto);
        jpaRepository.save(entity);
        var returnDto = converter.toDTO(entity);
        return returnDto;
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Api<List<DTO>> list(Pageable pageable) {
        var list = jpaRepository.findAll(pageable);
        var pagination = Pagination.builder()
                .page(list.getNumber())
                .size(list.getSize())
                .currentElements(list.getNumberOfElements())
                .totalElements(list.getTotalElements())
                .totalPage(list.getTotalPages())
                .build();

        var dtoList = list.stream()
                .map(converter::toDTO)
                .collect(toList());

        var response = Api.<List<DTO>>builder()
                .body(dtoList)
                .pagination(pagination)
                .build();
        return response;
    }
}
```
- <I> Converter
```
public interface Converter<DTO, ENTITY> {
    DTO toDTO(ENTITY entity);

    ENTITY toEntity(DTO dto);
}
```

## Ch10-02. 추상화를 통한 게시판 프로젝트 개선 - 2
### crud.CRUDAbstractApiController
```
public abstract class CRUDAbstractApiController<DTO, ENTITY> implements CRUDInterface<DTO>{
    @Autowired(required = false)
    private CRUDAbstractService<DTO, ENTITY> crudAbstractService;

    @PostMapping("")
    @Override
    public DTO create(@Valid @RequestBody DTO dto) {
        return crudAbstractService.create(dto);
    }

    @GetMapping("/id/{id}")
    @Override
    public Optional<DTO> read(@PathVariable Long id) {
        return crudAbstractService.read(id);
    }

    @PutMapping("")
    @Override
    public DTO update(@Valid @RequestBody DTO dto) {
        return crudAbstractService.update(dto);
    }

    @DeleteMapping("")
    @Override
    public void delete(@PathVariable Long id) {
        crudAbstractService.delete(id);
    }

    @GetMapping("/all")
    @Override
    public Api<List<DTO>> list(@PageableDefault Pageable pageable) {
        return crudAbstractService.list(pageable);
    }
}
```
### reply
```
- model/ReplyDto
@~
public class ReplyDto {
    private Long id;
    private Long postId;
    private String userName;
    private String password;
    private String status;
    private String title;
    private String content;
    private LocalDateTime repliedAt;
}
- service/Replyconverter
@Service
@RequiredArgsConstructor
public class ReplyConverter implements Converter<ReplyDto, ReplyEntity> {
    private final PostRepository postRepository;
    @Override
    public ReplyDto toDTO(ReplyEntity replyEntity) {
        return ReplyDto.builder()
                .id(replyEntity.getId())
                .postId(replyEntity.getPost().getId())
                .userName(replyEntity.getUserName())
                .password(replyEntity.getPassword())
                .status(replyEntity.getStatus())
                .title(replyEntity.getTitle())
                .content(replyEntity.getContent())
                .repliedAt(replyEntity.getRepliedAt())
                .build();
    }

    @Override
    public ReplyEntity toEntity(ReplyDto replyDto) {
        var postEntity = postRepository.findById(replyDto.getPostId());
        return ReplyEntity.builder()
                .id(replyDto.getId()) // null save | !null update
                .post(postEntity.orElseGet(()->null))
                .userName(replyDto.getUserName())
                .password(replyDto.getPassword())
                .status((replyDto.getStatus() != null ? replyDto.getStatus() : "REGISTERED"))
                .title(replyDto.getTitle())
                .content(replyDto.getContent())
                .repliedAt((replyDto.getRepliedAt() != null) ? replyDto.getRepliedAt() : LocalDateTime.now())
                .build();
    }
}
- controller/ReplyApiController
@RestController
@RequestMapping("/api/reply")
@RequiredArgsConstructor
public class ReplyApiController extends CRUDAbstractApiController<ReplyDto, ReplyEntity> {
}
- service/ReplyService
@Transactional
@Service
@RequiredArgsConstructor
public class ReplyService extends CRUDAbstractService<ReplyDto, ReplyEntity> {
}
```
> ReplyConverter, ReplyService 자동주입(제네릭)