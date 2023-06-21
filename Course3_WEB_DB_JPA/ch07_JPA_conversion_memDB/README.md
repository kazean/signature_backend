# Ch07. JPA로의 변환
## Ch07-01. Memory DB를 MySQL로 변환 - 01
memorydb > jpa  
db: book_store

## Ch07-02. Memory DB를 MySQL로 변환 - 02
### QueryMethod
#### UserRepository
```
public interface UserRepository extends JpaRepository<UserEntity, Long> {
//    public List<UserEntity> findAllScoreGreaterThanEqual(int score);
    public List<UserEntity> findAllByScoreGreaterThanEqual(int score);

    public List<UserEntity> findAllByScoreBetween(int min, int max);

    @Query(value = "select * from user as u where u.score >= :min and u.score <= :max",
        nativeQuery = true)
    public List<UserEntity> score(@Param("min") int min, @Param("max") int max);
}
```