# Ch04. Memory DataBase CRUD
## CH04-01. Memory Database CRUD 적용해보기 - 1
@YearMonth Annotation Validator 적용해보기 - birthDayYearMonth
## CH04-02. Memory Database CRUD 적용해보기 - 2
- <I> Repository<T, ID>, DataRepository<T, ID> - CRUD, <A> SimpleDataRepository impl DataRepository
- <I> PrimaryKey - getId, setId(), <A> Entity - Long
### <I> DataRepository
```
public interface DataRepository<T, ID> extends Repository<T, ID> {
    //    create, create
    T save(T data);

    //    read
    Optional<T> findById(ID id);

    List<T> findAll();

    //    delete
    void delete(ID id);
}

```
### <A> SimpleDataRepository
CRUD 구현

## CH04-02. Memory Database CRUD 적용해보기 - 3
### user/config, controller, service, db, model/UserEntity - name, score
### UserApiController
```
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApiController {
    private final UserService userService;

    @PutMapping("")
    public UserEntity create(@RequestBody UserEntity userEntity) {
        return userService.save(userEntity);
    }

    @GetMapping("/all")
    public List<UserEntity> findAll() {
        return userService.findAll();
    }
}
```
### UserService
```
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserEntity save(UserEntity user) {
        // save
        return userRepository.save(user);
    }

    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }
}
```
### UserRepository - extends SimpleDataRepository

## CH04-02. Memory Database CRUD 적용해보기 - 4
### UserApiController
```
@DeleteMapping("/id/{id}")
public void delete(@PathVariable Long id) {
    userService.delete(id);
}

@GetMapping("/id/{id}")
public UserEntity findOne(@PathVariable Long id) {
    Optional<UserEntity> response = userService.findById(id);
    return response.get();
}
```

## CH04-03. Memory Database CRUD 적용해보기 - 5
70점 이상 User 검색
### UserApiController
```
@GetMapping("/score")
public List<UserEntity> filterScore(@RequestParam int score) {
    return userService.filetScore(score);
}
```
### UserService
### USerRepository
```
public List<UserEntity> findAllScoreGraterThen(int score) {
    return this.findAll().stream()
            .filter(it -> {
                return it.getScore() >= score;
            }).collect(toList());
}
```
### BookEntity - name, category, amount, BookRepository, BookService, BookApiController
("") create, ("/all") findAll(), delete/findOne
