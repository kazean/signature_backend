# Ch02. Spring Batch 적용
# Ch02-01-01. Spring Batch 도메인 용어 익히기
## 배치 도메인
- Job
- JobInstance
- JobParameter
- JobExecution
- Step
- StepExecution
- ExecutionContext
- JobLauncher
- ItemReader/Processor/Writer
### Job
- 전체 배치 프로세스를 캡슐화한 도메인
- 단순히 Step 인스턴스를 위한 컨테이너
- Job의 구성
> - Job의 이름
> - Step 정의 및 순서
> - 작업을 다시 시작할 수 있는지 여부
### JobInstance
- Job의 논리적 실행 단위를 나타내는 도메인
- 하나의 Job이 여러개의 JobInstance를 가짐
- 구성요소
> - Job 이름
> - 식별 파라미터
- BATCH_JOB_INSTANCE 테이블에 저장
### JobParameters
- Job을 실행할때 함께 사용되는 파라미터 도메인
- 하나의 Job에 존재할 수 있는 여러개의 JobInstancefmf rnqns
- BATCH_JOB_EXECUTION_PARAMS 저장
### JobExeuction
- Job의 단일 실행에 대한 도메인
- Job 실행중에 실제로 일어난 일에 대한 기본 저장 메커니즘
- BATCH_JOB_EXECUTION 저장
### Step
- 배치 작업의 독립적이고, 순차적인 단계를 캡슐화한 도메인
- 하나의 Job은 한 개 이상의 Step을 가짐
### StepExecution
- Step의 단일 실행에 대한 도메인
- BATCH_STEP_EXECUTION
### ExecutionContext
- Batch의 세션 역할을 하는 도메인
- Job, Step의 상태를 가진다
- Key-Value 구조
- Batch_JOB_EXECUTION_CONTEXT, STEP_JOB_EXECUTION_CONTEXT 저장
### JobRepsitory
- Job 저장소 도메인
- 배치의 상태를 DB에 저장함으로써 다양한 기능에 활용할 수 있음
### JobLauncher
- Job을 실행 시키는 도메인
### ItemReader/Processor/Writer
## 정리하기
- Batch Stereotypes <그림>


---------------------------------------------------------------------------------------------------------------------------
# Ch02-01-02. Spring Batch 도메인 용어 익히기 - 적용
## batch-campus
- build.gradle
```gradle
dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-batch'
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	compileOnly 'org.projectlombok:lombok'
	runtimeOnly 'com.h2database:h2'
	runtimeOnly 'com.mysql:mysql-connector-j'
	annotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
	testImplementation 'org.springframework.batch:spring-batch-test'
}
```
> spring-boot-starter-batch, data-jpa, lombok, h2, mysql
- application.yaml
```yaml
spring:
  batch:
    jdbc:
      initialize-schema: always
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
#    url: jdbc:mysql://localhost:3306/spring_batch
    url: jdbc:mysql://localhost:3306/spring_batch?userSSL=false&useUnicode=true&PublicKeyRetrieval=true
    username: root
    password: root1234!!
```
> spring.batch.initialize-schema: always  
> spring.datasource
- JobConfiguration
```java
@Slf4j
@Configuration
public class JobConfiguration {

    @Bean
    public Job job(JobRepository jobRepository, Step step) {
        return new JobBuilder("job", jobRepository)
                .start(step)
                .build();
    }

    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("step", jobRepository)
                .tasklet((a, b) -> {
                    log.info("step 실패");
//                    throw new IllegalStateException("상태가 잘못되었습니다.");
                    return RepeatStatus.FINISHED;
                }, platformTransactionManager)
                .build();
    }
}
```
> @Bean Job, Step: JobBuilder, StepBuilder
- cf, BatchAutoConfiguration
> @Bean JobLauncherApplicationRunner, DataSourceInitializerConfiguration, SpringBootBatchConfiguration  
> Job 실행, Database Schema, jobRepository


---------------------------------------------------------------------------------------------------------------------------
# Ch02-02. Job
## Job 이란?
- 전체 배치 프로세스를 캡슐화한 도메인
- 단순히 Step 인스턴스를 위한 컨테이너
- Job의 구성
> - Job의 이름
> - Step 정의 및 순서
> - 작업을 다시 시작할 수 있는지 여부
```java
public interface Job {

	String getName();

	/**
	 * Flag to indicate if this job can be restarted, at least in principle.
	 * @return true if this job can be restarted after a failure. Defaults to
	 * {@code true}.
	 */
	default boolean isRestartable() {
		return true;
	}

	/**
	 * Run the {@link JobExecution} and update the meta information, such as status and
	 * statistics, as necessary. This method should not throw any exceptions for failed
	 * execution. Clients should be careful to inspect the {@link JobExecution} status to
	 * determine success or failure.
	 * @param execution a {@link JobExecution}
	 */
	void execute(JobExecution execution);

	@Nullable
	default JobParametersIncrementer getJobParametersIncrementer() {
		return null;
	}

	default JobParametersValidator getJobParametersValidator() {
		return new DefaultJobParametersValidator();
	}

}
```
> isRestartable, execute
### Job 클래스 구조
- Job > AbstractJob / GroupWareJob
> AbstractJob > SimpleJob/ FlowJob
> > `Template Method 패턴`
- SimpleJob
```
- f
steps: List<Step>
- m
setSteps(List<Step>): void
addStep(Step): void
doExecute(JobExecution) void
...
```
### SimpleJobBuilder 
> `extends JobBuilderHelper<SimpleJobBuilder>`  
> `abstract class JobBuilderHelper<B extends JobBuilderHelper<B>>`
### Restartability
- Default 설정으론 Job은 실패하면 재시작할 수 있음
- SimpleJobBuilder#preventRestart 를 설정하면 재시작할 수 없음
- 비지니스 성격상 판단에 맡김
### JobParameterIncrementer
- 시퀀스에서 다음 JobParameters 객체를 얻기 위한 인터페이스
- 주로 잡 파라미터의 변경없이 Job을 반복해서 실행하기 위해 사용
> RunIdIncrementer
### JobParameterValidator
- 입력 받은 잡 파라미터 검증
> DefaultJobParameterValidator
### JobExecutionListener
- 스프링 배치 생명주기 중 Job 실행 전/후 로직을 추가할 수 있는 기능 제공
- 주의
> jobExecution.getStatus() == BatchStatus.COMPLETED/FAILED 두 가지 다 정의해야 함


---------------------------------------------------------------------------------------------------------------------------
# Ch02-03-01. Step
## Step 이란?
- 배치 작업의 독립적이고, 순차적인 단계를 캡슐화한 도메인
## Step 클래스 다이어그램
- Step
> AbstractStep
> > JobStep/ TaskletStep/ FlowStep/ PartitionStep
## Tasklet, Chunk-oriented
```java
new StepBuilder("step2", jobRepository)
  .chunk(10, transactionManager)
```
### Tasklet 단점
> 처리해야할 개수는 많은데 하나씩 커밋
## Chunk-oriented Processing
- Spring Batch는 일반적으로 `Chunk-oriented` 스타일을 사용
- 읽은 항목의 수가 커밋 간격과 같으면 ItemWriter 가 전체 청크를 기록한 다음 트랜잭션을 커밋함
- Tasklet > ChunkOrientedTasklet<I>
## Commit Interval
- ChunkSize는 커밋 간격을 의미
> new StepBuilder(~).chunk(chunSize: Int, transactionManager)
> > 성능적 이점, Chunk 내에서 롤백
## Step Restart
- allowStartIfComplete: Step이 성공해도 재시작 허용
- startLimit: Step 시작 제한수
> new StepBuilder(~).allowStartIfComplete(true).startLimit(3)
## Skip
- 한 번의 실패가 Job, Step을 멈추게함
> .falutTolerant().skipLimit(10).skip(ClassCastException.class)  
>   .noSkip(IllegalStateException.class)
### SkipPolicy
- @FunctionalInterface boolean shoudSkip(Throwable t, long skipCount)
## Retry
- 특정 에러의 경우 다시 시도하면 성공하는 케이스가 있을 경우, 재시도함으로써 회복탄력성을 가짐
> .retry(~Exception.class)
### RetryPolicy
> 이미 많은 구현체 ex) CircuitBreakerRetryPolicy
## Rollback
- 롤백 유무 구성
> noRollback(~Exception.class)
## Intercepting Step
- Step은 다양한 Listener를 제공
> StepExecutionListener/ ChunkListener/ SkipListener/ ItemReaderListener / ...
## Late Binding
- 어플리케이션 구동 시점이 아닌 빈의 실행 시점에 적용
> @JobScope: Step  
> @StepScope: Tasklet, Item 3총사
## Sequentail Flow / Conditional Flow
```
new JobBuilder(~)
  .start(step1)
    .on(*).to(step2)
  .from(step1)
    .on("FAILED").to(step3)
```
### Flow 속성
> on(String): ExitStatus의 반환물과 match  
> to(Step): on 조건에 만족하면 Step으로 이동  
> from: 이전에 등록한 단계로 돌아가서 새 경로를 시작
### Flow Stop
- Completed/ Failed/ Stopped
```
new JobBuilder(~)
  .start(step1)
    .on(*).to(step2)
  .from(step1)
    .on("FAILED").end()/.fail().stopAndRestart(step2)
```
> completed/fail/restart


---------------------------------------------------------------------------------------------------------------------------
# Ch02-03-02. Step - 기본적용
- code1: Tasklet, allowStartIfComplete/startLimit
```java
@Slf4j
@Configuration
public class JobConfiguration {
    
  @Bean
  public Job job(JobRepository jobRepository, Step step) {
    return new JobBuilder("job-chunk", jobRepository)
      .start(step)
      .build();
  }
  
  // allowStartIfComplete
  @Bean
  public Step step(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
    return new StepBuilder("step", jobRepository)
      .tasklet((StepContribution contribution, ChunkContext chunkContext) -> {
        log.info("step 실행");
        return RepeatStatus.FINISHED;
      }, platformTransactionManager)
      .allowStartIfComplete(true)
      .startLimit(5)
      .build();
  }
}
```
> 기본 Step  
> allowStartIfComplete, startLimit: default = 0
- code2 - skip,skipLimit/ noRollback/ retry,retryLimit
```java
@Slf4j
@Configuration
public class JobConfiguration {

  @Bean
  public Job job(JobRepository jobRepository, Step step) {
    return new JobBuilder("job-chunk", jobRepository)
      .start(step)
      .build();
  }

  @Bean
  public Step step(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {

    ItemReader<Integer> itemReader = new ItemReader<>() {
      private int count = 0;

      @Override
      public Integer read() {
        count++;
        log.info("Read {}", count);
        if (count == 20)
          return null;
//                if (count >= 15)
//                    throw new IllegalStateException("예외가 발생했어요.");
        return count;
      }
    };

    ItemProcessor<Integer, Integer> itemProcessor = new ItemProcessor<>() {
      @Override
      public Integer process(Integer item) throws Exception {
        if (item == 15) {
          throw new IllegalStateException();
        }
        return item;
      }
    };

    return new StepBuilder("step", jobRepository)
//                .tasklet(tasklet, platformTransactionManager)
      .<Integer, Integer>chunk(10, platformTransactionManager)
      .reader(itemReader)
      .processor(itemProcessor)
      .writer(read -> {})
      .faultTolerant()
//                .skip(IllegalStateException.class)
//                .skipLimit(5)
//                .skipPolicy((t, skipCount) -> t instanceof IllegalStateException && skipCount < 5)
//                .noRollback(IllegalStateException.class)
      .retry(IllegalStateException.class)
      .retryLimit(5)
      .build();
  }

}
```
- code3 - `@JobScope`, `@Value(<SPEL>)`
```java
@Slf4j
@Configuration
public class JobConfiguration {

  @Bean
  public Job job(JobRepository jobRepository, Step step) {
    return new JobBuilder("job-chunk", jobRepository)
      .start(step)
      .build();
  }

  @Bean
  @JobScope
  public Step step(JobRepository jobRepository,
      PlatformTransactionManager platformTransactionManager,
      @Value("#{jobParameters['name']}") String name
    ) {
      log.info("name: {}", name);
      return new StepBuilder("step", jobRepository)
        .tasklet((a, b) -> {
          return RepeatStatus.FINISHED;
        }, platformTransactionManager)
        .build();
  }
}
```
> @JobScope, @StepScope > ItemReader 등


---------------------------------------------------------------------------------------------------------------------------
# Ch02-03-03. Step - Flow 적용
- Code
```java
@Configuration
public class FlowConfiguration {

  @Bean
  public Job flowJob(
    JobRepository jobRepository,
    Step step1,
    Step step2,
    Step step3
  ) {
      /*return new JobBuilder("flowJob", jobRepository)
              .start(step1)
//                    .on("*").to(step2)
//                .from(step1)
//                    .on("FAILED").to(step3)
//                    .on("FAILED").end()
//                    .on("FAILED").fail()
              .end()
              .build();*/
    return new JobBuilder("flowJob", jobRepository)
      .start(step1)
        .on("COMPLETED").stopAndRestart(step2)
      .end()
      .build();
  }

  @Bean
  public Step step1(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
    return new StepBuilder("step1", jobRepository)
      .tasklet((a, b) -> {
        log.info("step1 실행");
        return null;
      }, platformTransactionManager)
      .build();
  }

  @Bean
  public Step step2(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
      return new StepBuilder("step2", jobRepository)
        .tasklet((a, b) -> {
          log.info("step2 실행");
          return null;
        }, platformTransactionManager)
        .build();
  }

  @Bean
  public Step step3(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
      return new StepBuilder("step3", jobRepository)
        .tasklet((a, b) -> {
          log.info("step3 실행");
          return null;
        }, platformTransactionManager)
        .build();
  }
}
```
> organize
```
new JobBuilder("flowJob", jobRepository)
  .start(step)
    .on(pattern:String).to(step2)
  .from(step)
    .on(pattern:String).to(step3)/end()/fail()

- .from().on("FAILED").to(step3) 
> 실패의 경우 step3 를 실행
> jobExe: COMPLETED
> Step: step1: ABANDONED, step3: COMPLETED

- .from().on("FAILED").fail()
> jobExe: FAILED, Step: FAILED

- .from().on("FAILED").end()
> jobExe: COMPLETED, Step: FAILED
> > end(), fail()은 jobExecution 상태값

- .start(step1)
    .on("COMPLETED").stopAndRestart(step2)
> run 1 : 
> > JobExe: STOPPED, Step1: COMPLETED
> run2 :
> > JobExe: COMPLTED, Step1: COMPLETED Step2: COMPLETED
```

---------------------------------------------------------------------------------------------------------------------------
# Ch02-04-02. ItemReader - File
## ItemReaderInterface
```java
@FunctionalInterface
public interface ItemReader<T> {

	/**
	 * Reads a piece of input data and advance to the next one. Implementations
	 * <strong>must</strong> return <code>null</code> at the end of the input data set. In
	 * a transactional setting, caller might get the same item twice from successive calls
	 * (or otherwise), if the first call was in a transaction that rolled back.
	 * @throws ParseException if there is a problem parsing the current record (but the
	 * next one may still be valid)
	 * @throws NonTransientResourceException if there is a fatal exception in the
	 * underlying resource. After throwing this exception implementations should endeavour
	 * to return null from subsequent calls to read.
	 * @throws UnexpectedInputException if there is an uncategorised problem with the
	 * input data. Assume potentially transient, so subsequent calls to read might
	 * succeed.
	 * @throws Exception if an there is a non-specific error.
	 * @return T the item to be processed or {@code null} if the data source is exhausted
	 */
	@Nullable
	T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException;

}
```
> return null 일 경우 정상 process
## 입력받기
- File
- Database
- HTTP API
- Message Queue
- ETC
## `Flat File`
1. 파일을 연다
2. 내용은 세번째 줄부터 읽어나간다
3. ','로 데이터를 구분해서 객체에 매핑한다.
### `FlatFileItemReader`
- example Code
```java
```
- LineMapper<T>
> LineTokenizer, FieldSetMapper<T>
#### FlatItemReader 속성
```
property    Type        Des
comments    String[]    주석
encoding    String
lineMapper  LineMapper  라인 Mapping Class
linesToSkip int         처음 몇번째 Skip
recordSeparatorPolicy RecordSeparatorPolicy record 구분자 default ','
resource  Resource
skippedLinesCallback  LineCallbackHandler
strict  boolean
```
## JSON File
### JsomItemReader
- example Code
```java
@Bean
public JsonItemReader<User> jsonItemReader() {
  return new JsonItemReaderBuilder<User>()
    .name("tradeJsonItemReader")
    .resource(new ClassPathResource("users.json"))
    .jsonObjectReader(new JacksonJsonObjectReader<>(User.class))
    .build()
}
```
### JSONObjectReader<T>
> `JacksonJsonObjectReader<T>`, `GsonJsonObjectReader<T>`

## 데이터베이스 읽기
- Paging
> JpaPagingItemReader  
> JdbcPagingItemReader
- Cursor
> JpaCursorItemReader  
> JdbcCursorItemReader/ StoredProcedureItemReader
> > (단점) Connection을 물고 있음
### JpaPagingItemReader
- `JpaPagingItemReaderBuilder<T>`
- example Code
```java
@Bean
public ItemReader<User> jpaPagingItemReader(
  EntityMAnagerFactory entityManagerFactory
){
  return new JpaPagingItemReaderbuilder<User>()
    .name("jpaPagingItemReader")
    .entityManagerFactory(entityManagerFactory)
    .pageSize(3)
    .quertyString("SELECT u FROM User u Order by u.ud")
    .build()
}
```
- JpaPagingItemreaderBuilder 속성들
### JpaCursorItemReader
- pageSize가 없다
- JpaCursorItemReaderBuilder


# Ch02-04-02. ItemReader - File
- build.gradle
> implementation 'com.fasterxml.jackson.core:jackson-databind:2.15.2'  
> ObjectMapper
- Code
```java
@Configuration
public class ItemReaderJobConfiguration {

    @Bean
    public Job job(
            JobRepository jobRepository,
            Step step
    ) {
        return new JobBuilder("itemReaderJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }

    @Bean
    public Step step(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager,
//            ItemReader<User> flatFileItemReader
//            ItemReader<User> fixedLengthFlatFileItemReader
            ItemReader<User> jsonItemReader
    ) {
        return new StepBuilder("step", jobRepository)
                .<User, User>chunk(2, platformTransactionManager)
//                .reader(flatFileItemReader)
//                .reader(fixedLengthFlatFileItemReader)
                .reader(jsonItemReader)
                .writer(System.out::println)
                .build();
    }

    @Bean
    public FlatFileItemReader<User> flatFileItemReader() {
        return new FlatFileItemReaderBuilder<User>()
                .name("flatFileItemReader")
                .resource(new ClassPathResource("users.txt"))
                .linesToSkip(2)
                .delimited().delimiter("|") // default ','
                .names("name", "age", "region", "telephone")
                .targetType(User.class)
                .strict(true) // default: true, false > 읽지안고 정상적으로 종료
                .build();
    }

    // 옛날 방식이지만 종종 사용됨
    @Bean
    public FlatFileItemReader<User> fixedLengthFlatFileItemReader() {
        return new FlatFileItemReaderBuilder<User>()
                .name("fixedLengthFlatFileItemReader")
                .resource(new ClassPathResource("usersFixedLength.txt"))
                .linesToSkip(2)
                .fixedLength()
                .columns(new Range[]{new Range(1,2), new Range(3,4), new Range(5,6), new Range(7,19)})
                .names("name", "age", "region", "telephone")
                .targetType(User.class)
                .strict(true) // default: true, false > 읽지안고 정상적으로 종료
                .build();
    }

    @Bean
    public ItemReader<User> jsonItemReader() {
        return new JsonItemReaderBuilder<User>()
                .name("jsonItemReader")
                .resource(new ClassPathResource("users.json"))
                .jsonObjectReader(new JacksonJsonObjectReader<>(User.class))
                .build();
    }
}
```
- File: FlatFileItemReder > FlatFileItemReaderBuilder
> name, resource(new ClassPathResource("useres.txt")), linesToSkip(int)  
> delimited().delimiter(","), names(String... field) 
> or fixedLength(), columns(new Ragne[]{ ~ }) 
> targetType(~.class), strict(true): false 경우 읽지않고 정상진행
- Json File: JsonItemReader > JsonItemReaderBuilder
> name(), resource(), .jsonObjectReader(new JacksonJsonObjectReader<>(~.class)/Gson~).build()


---------------------------------------------------------------------------------------------------------------------------
# Ch02-04-03. ItemReader - Database
- Code
```java
@Configuration
public class ItemReaderJobConfiguration {

    @Bean
    public Job job(
            JobRepository jobRepository,
            Step step
    ) {
        return new JobBuilder("itemReaderJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }

    @Bean
    public Step step(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager,
//            ItemReader<User> jpaPagingItemReader
            ItemReader<User> jpaCursorItemReader
    ) {
        return new StepBuilder("step", jobRepository)
                .<User, User>chunk(2, platformTransactionManager)
                .reader(jpaCursorItemReader)
                .writer(System.out::println)
                .build();
    }

    @Bean
    public ItemReader<User> jpaPagingItemReader(
            EntityManagerFactory entityManagerFactory
    ) {
        return new JpaPagingItemReaderBuilder<User>()
                .name("jpaPagingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(3)
                .queryString("SELECT u From User u Order By u.id")
                .build();
    }

    @Bean
    public ItemReader<User> jpaCursorItemReader(
            EntityManagerFactory entityManagerFactory
    ) {
        return new JpaCursorItemReaderBuilder<User>()
                .name("jpaCursorItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT u From User u Order By u.id")
                .build();
    }
}
```
- Database: JpaPagingItemReader, JpaCursorItemReader
> JpaPaging/CursorItemReaderBuilder, JpaCursorItemReaderBuilder  
> name(), entityManagerFactory(), queryString(), build()


---------------------------------------------------------------------------------------------------------------------------
# Ch02-05-01. ItemWriter
---------------------------------------------------------------------------------------------------------------------------
# Ch02-05-02. ItemWriter - 적용
---------------------------------------------------------------------------------------------------------------------------
# Ch02-06. ItemProcessor


---------------------------------------------------------------------------------------------------------------------------
# Ch02-07-01. 확장을 통한 성능 개선
---------------------------------------------------------------------------------------------------------------------------
# Ch02-07-02. 확장을 통한 성능 개선 - Multi-threaded, Parallel 
---------------------------------------------------------------------------------------------------------------------------
# Ch02-07-03. 확장을 통한 성능 개선 - Partitioning