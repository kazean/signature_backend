# Ch04. Memory DataBase CRUD
## CH04-01. Memory Database CRUD 적용해보기 - 1
@YearMonth Annotation Validator 적용해보기 - birthDayYearMonth
## CH04-02. Memory Database CRUD 적용해보기 - 2
- <I> Repository, DataRepository, <A> SimpleDataRepository
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
## CH04-02. Memory Database CRUD 적용해보기 - 4
## CH04-02. Memory Database CRUD 적용해보기 - 5