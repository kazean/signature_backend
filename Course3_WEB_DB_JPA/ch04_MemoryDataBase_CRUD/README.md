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
## Memory Database
- Database: 데이터 저장소
- DBMS(DataBase Management System): 데이터베이스를 운영하고 관리하는 소프트웨어

### CRUD
- <I>Repository -> <I>Data Repository -> <Abstract> class>Simple Data Repository
- <I>Id -> <Abstract Class>Entity

## Project
```
com.example.memroydb
Gradle-Grooby, JDK11
Dependency: Lombok, Spring Web
```

## 실습 (memory)
```java
package com.example.memorydb.db;
public interface Repository <T, ID>{
}

public interface DataRepository<T, ID> extends Repository<T, ID> {
    //    create, create
    T save(T data);

    //    read
    Optional<T> findById(ID id);

    List<T> findAll();

    //    delete
    void delete(ID id);
}

public abstract class SimpleDataRepository<T extends Entity, ID extends Long> implements DataRepository<T, ID> {
    private List<T> dataList = new ArrayList<>();
    private static long index = 0;

    private Comparator<T> sort = new Comparator<T>() {
        @Override
        public int compare(T o1, T o2) {
            return Long.compare(o1.getId(), o2.getId());
        }
    };

//    create, update
    @Override
    public T save(T data) {
        if (Objects.isNull(data)) {
            throw new RuntimeException("Data is null");
        }
//        db에 데이터가 있는가?
        Optional<T> prevData = dataList.stream()
                .filter(it -> {
                    return it.getId().equals(data.getId());
                }).findFirst();

        if (prevData.isPresent()) {
//            기존 데이터 있는 경우
            dataList.remove(prevData.get());
            dataList.add(data);
        } else {
//            없는 경우
            index++;
            data.setId(index);
            dataList.add(data);

        }
        return data;
    }

//    read
    @Override
    public Optional<T> findById(ID id) {
        return dataList.stream()
                .filter(i -> {
                    return i.getId().equals(id);
                }).findFirst();
    }

    @Override
    public List<T> findAll() {
        return dataList.stream().sorted(sort).collect(toList());
    }

//    delete
    @Override
    public void delete(ID id) {
        Optional<T> deleteEntity = dataList.stream()
                .filter(i -> {
                    return i.getId().equals(id);
                }).findFirst();
        if (deleteEntity.isPresent()) {
            dataList.remove(deleteEntity.get());
        }
    }
}


package com.example.memorydb.entity;
public interface PrimaryKey {
    void setId(Long id);

    Long getId();
}
public abstract class Entity implements PrimaryKey {
    @Getter
    @Setter
    private Long id;

}
```


--------------------------------------------------------------------------------------------------------------------------------
# ch04-03. Memory Database CRUD 적용해보기 - 3
## CRUD
- Req/Res 실습: save/findAll

## 실습 (memory)
```java
package com.example.memorydb.user.controller;
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

package com.example.memorydb.user.service;
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

package com.example.memorydb.user.db;
@Repository
public class UserRepository extends SimpleDataRepository<UserEntity, Long> {
    public List<UserEntity> findAllScoreGraterThen(int score) {
        return this.findAll().stream()
                .filter(it -> {
                    return it.getScore() >= score;
                }).collect(toList());
    }
}


package com.example.memorydb.user.model;
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity extends Entity {

    private String name;
    private int score;

}
```
- request
```json
{
  "name": "홍길동",
  "score": 100
}
```

## 정리
- Repository 틀 만들어서 DI 사용
- Comparator


--------------------------------------------------------------------------------------------------------------------------------
# ch04-04. Memory Database CRUD 적용해보기 - 4
## CRUD
- Req/Res 실습: delete/findById
## 실습 (memory)
```java
package com.example.memorydb.user.controller;
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApiController {
    
    private final UserService userService;
    
    // ~

    @DeleteMapping("/id/{id}")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }

    @GetMapping("/id/{id}")
    public UserEntity findOne(@PathVariable Long id) {
        Optional<UserEntity> response = userService.findById(id);
        return response.get();
    }
}

public class UserService {
    // ~

    public void delete(Long id) {
        userRepository.delete(id);
    }

    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }
}
```


--------------------------------------------------------------------------------------------------------------------------------
# CH04-05. Memory Database CRUD 적용해보기 - 5
- 사용자 10명 생성 후 70점 이상 User 검색

## 실습 (memory)
```java
@Repository
public class UserRepository extends SimpleDataRepository<UserEntity, Long> {
    public List<UserEntity> findAllScoreGraterThen(int score) {
        return this.findAll().stream()
                .filter(it -> {
                    return it.getScore() >= score;
                }).collect(toList());
    }
}

public class UserService {
    public List<UserEntity> filetScore(int score) {
        return userRepository.findAllScoreGraterThen(score);
    }
}

public class UserApiController {
    // ~

    @GetMapping("/score")
    public List<UserEntity> filterScore(@RequestParam int score) {
        return userService.filetScore(score);
    }
}
```
### BookEntity CRUD ...
```java
package com.example.memorydb.book.db.entity;
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookEntity extends Entity {
    private String name;
    private String category;
    private BigDecimal amount;
}

package com.example.memorydb.book.db.repository;
@Repository
public class BookRepository extends SimpleDataRepository<BookEntity, Long> {
}


package com.example.memorydb.book.db.servie;
@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

    //    create, update
    public BookEntity create(BookEntity bookEntity) {
        return bookRepository.save(bookEntity);
    }

    //    all
    public List<BookEntity> findAll() {
        return bookRepository.findAll();
    }
//    delete
//    findOne
}

// Service, Controller
```
