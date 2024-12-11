# Ch04. Memory DataBase CRUD
- [1. Memory Database CRUD 적용해보기 - 1](#ch04-01-memory-database-crud-적용해보기---1)
- [2. Memory Database CRUD 적용해보기 - 2](#ch04-02-memory-database-crud-적용해보기---2)
- [3. Memory Database CRUD 적용해보기 - 3](#ch04-03-memory-database-crud-적용해보기---3)
- [4. Memory Database CRUD 적용해보기 - 4](#ch04-04-memory-database-crud-적용해보기---4)
- [5. Memory Database CRUD 적용해보기 - 5](#ch04-05-memory-database-crud-적용해보기---5)


--------------------------------------------------------------------------------------------------------------------------------
# ch04-01. Memory Database CRUD 적용해보기 - 1
- [과제] @YearMonth Annotation Validator 적용해보기 - birthDayYearMonth
> `Custom Annotation @interface + @Contraint(validatedBy = { <.class> })`
> > `<.class> impl ContraintValidator<A, T>`

## 실습 (validation)
```java
package com.example.validation.annotation;
@Constraint(validatedBy = { YearMonthValidator.class })
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@NotBlank
public @interface YearMonth {
    String message() default "year month 양식에 맞지 않습니다 ex) 2023-01";

    String pattern() default "yyyyMM";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}

package com.example.validation.validator;
@Slf4j
public class YearMonthValidator implements ConstraintValidator<YearMonth, String> {
    private String pattern;

    @Override
    public void initialize(YearMonth constraintAnnotation) {
        this.pattern = constraintAnnotation.pattern();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
//        "2023-01-01T13:00:00" yyyy-MM-ddTHH:mm:ss
//        "2023-01"

//        yyyy MM dd

        String reValue = value + "01";
        String rePattern = pattern + "dd";
        try {
            LocalDate date = LocalDate.parse(reValue, DateTimeFormatter.ofPattern(rePattern));
            log.info("date: {}", date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```


--------------------------------------------------------------------------------------------------------------------------------
# ch04-02. Memory Database CRUD 적용해보기 - 2
- <I> Repository<T, ID>, DataRepository<T, ID> - CRUD, <A> SimpleDataRepository impl DataRepository
- <I> PrimaryKey - getId, setId(), <A> Entity - Long
## 실습 
### <I> DataRepository
```java
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


--------------------------------------------------------------------------------------------------------------------------------
# ch04-03. Memory Database CRUD 적용해보기 - 3
## 실습 
### user/config, controller, service, db, model/UserEntity - name, score
### UserApiController
```java
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
```java
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


--------------------------------------------------------------------------------------------------------------------------------
# ch04-04. Memory Database CRUD 적용해보기 - 4
## 실습 
### UserApiController
```java
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


--------------------------------------------------------------------------------------------------------------------------------
# CH04-05. Memory Database CRUD 적용해보기 - 5
70점 이상 User 검색
## 실습 
### UserApiController
```java
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
