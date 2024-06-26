# Ch03. 정산 시스템 만들기
- [03-01. 요구사항 분석](#ch03-01-요구사항-분석)
- [03-02. API 호출 이력 배치 만들기](#ch03-02-api-호출-이력-배치-만들기)
- [03-03. API 호출 이력 배치 확장하기](#ch03-03-api-호출-이력-배치-확장하기)
- [03-04. 일일 정산 배치 만들기](#ch03-04-일일-정산-배치-만들기)
- [03-05. 주간 정산 배치 만들기](#ch03-05-주간-정산-배치-만들기)


---------------------------------------------------------------------------------------------------------------------------
# Ch03-01. 요구사항 분석
- 고객이 유로 API를 사용
- 서비스팀은 유료 API 사용 이력을 남긴다
- 유료 API 사용 이력을 파일로 정산팀에 전달한다.
- 정산팀은 1일 단위로 정산한다
- 매주 금요일 1주일치 1일 정산을 집계해서 DB에 저장하고 고객사에 이메일을 보낸다.
## TODO
- API 호출 이력 파일 만드는 배치
- 일 단위 정산 배치
- 주 단위 정산 배치


---------------------------------------------------------------------------------------------------------------------------
# Ch03-02. API 호출 이력 배치 만들기
- 유료 API 사용(ApiOrder) 만들고 출력하기
> API 호출 이력 파일 만드는 배치, totalCount: 10000 (Reader) & Processor(ApiOrder) & Writer(_api_orders.csv)
## 실습 - batch-campus
- com.fastcampus.batchcampus
- build.gradle
```gradle
    implementation 'org.springframework.boot:spring-boot-starter-batch'
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	implementation 'com.fasterxml.jackson.core:jackson-databind:2.15.2'
	compileOnly 'org.projectlombok:lombok'
	runtimeOnly 'com.h2database:h2'
	runtimeOnly 'com.mysql:mysql-connector-j'
```
```java
// batch.generator
@Configuration
@RequiredArgsConstructor
public class ApiOrderGenerateJobConfiguration {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    @Bean
    public Job apiOrderGenerateJob(Step apiOrderGenerateStep) {
        return new JobBuilder("apiOrderGenerateJob", jobRepository)
                .start(stapiOrderGenerateStep)
                .incrementer(new RunIdIncrementer())
                .validator(
                        new DefaultJobParametersValidator(
                                new String[]{"totalCount", "targetDate"}, new String[0]
                        )
                )
                .build();
    }

    @Bean
    public Step apiOrderGenerateStep(
            ApiOrderGenerateReader apiOrderGenerateReader,
            ApiOrderGenerateProcessor apiOrderGenerateProcessor
    ) {
        return new StepBuilder(("apiOrderGenerateStep"), jobRepository)
                .<Boolean, ApiOrder>chunk(10000, platformTransactionManager)
                .reader(apiOrderGenerateReader)
                .processor(apiOrderGenerateProcessor)
                .writer(apiOrderGenerateWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<ApiOrder> apiOrderGenerateWriter(
            @Value("#{jobParameters['targetDate']}") String targetDate
    ) {
        final String fileName = targetDate + "_api_orders.csv";
        return new FlatFileItemWriterBuilder<ApiOrder>()
                .name("apiOrderGenerateWriter")
                .resource(new PathResource("src/main/resources/datas/" + fileName))
                .delimited()
                .names("id", "customerId", "url", "state", "createdAt")
                .headerCallback(writer -> writer.write("id, customerId, url, state, createdAt"))
                .build();
    }
}

@Component
@StepScope
public class ApiOrderGenerateReader implements ItemReader {
    private Long totalCount;
    private AtomicLong current;

    public ApiOrderGenerateReader(
            @Value("#{jobParameters['totalCount']}") String totalCount
    ) {
        this.totalCount = Long.valueOf(totalCount);
        this.current = new AtomicLong(0);
    }

    @Override
    public Boolean read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (current.incrementAndGet() > totalCount) {
            return null;
        }
        return true;
    }
}

@Component
public class ApiOrderGenerateProcessor implements ItemProcessor<Boolean, ApiOrder> {
    private final List<Long> customerIds = LongStream.range(0, 20).boxed().toList();
    private final List<ServicePolicy> servicePolicies = Arrays.stream(ServicePolicy.values()).toList();
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public ApiOrder process(Boolean item) throws Exception {
        final Long randomCustomerId = customerIds.get(random.nextInt(customerIds.size()));
        final ServicePolicy randomServicePolicy = servicePolicies.get(random.nextInt(servicePolicies.size()));
        final ApiOrder.State randomState = random.nextInt(5) % 5 == 1 ? ApiOrder.State.FAIL : ApiOrder.State.SUCCESS;

        return new ApiOrder(
                UUID.randomUUID().toString(),
                randomCustomerId,
                randomServicePolicy.getUrl(),
                randomState,
                LocalDateTime.now().format(dateTimeFormatter)
        );
    }
}

// domain
@Data
@NoArgsConstructor
public class ApiOrder {
    public String id;
    public Long customerId;
    private String url;
    private State state;
    private String createdAt;

    public enum State {
        SUCCESS,
        FAIL
    }

    public ApiOrder(String id, Long customerId, String url, State state, String createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.url = url;
        this.state = state;
        this.createdAt = createdAt;
    }
}

@Getter
public enum ServicePolicy {
    A(1L, "/fastcampus/services/a", 10),
    B(2L, "/fastcampus/services/b", 10),
    // ~
    Z(26L, "/fastcampus/services/z", 19);

    private final long id;
    private final String url;
    private final Integer fee;

    ServicePolicy(long id, String url, Integer fee) {
        this.id = id;
        this.url = url;
        this.fee = fee;
    }
}
```
> organize
```
# ApiOrderGenerateJobConfiguration
##Job
- JobBuilder
  .valitor(new DefaultJobParametersValidator(String[] requiredKeys, String[] optionalKeys))
##Step
## ItemReader: FlatFileItermWriter
- FlatFileItemWriterBuilder
## ItemReader
- Boolean read()
> totalCount 만큼 읽기  
> null 이면 stop
## ItemProcessor<Boolean, ApiOrder>
> ApiOrder 생성
> `ThreadLocalRandom` nextInt(int Range)
## ItemWriter
- FlatFileItemWriterbuilder<ApiOrder>
> ApiOrder 쓰기
```
> > Program Arguments
> > - totalCount=10000 targetDate=20230701


---------------------------------------------------------------------------------------------------------------------------
# Ch03-03. API 호출 이력 배치 확장하기
Partitioning을 활용해서 ApiOrder.csv 일주일치 만들기
## 실습 - batch-campus
```java
// batch.generator
@Configuration
@RequiredArgsConstructor
public class ApiOrderGeneratePartitionJobConfiguration {
    /**
     * STEP [Master Step]
     * Work Step1, Work Step2, ...7
     *
     */
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    @Bean
    public Job apiOrderGenerateJob(Step managerStep) {
        return new JobBuilder("apiOrderGenerateJob", jobRepository)
                .start(managerStep)
                .incrementer(new RunIdIncrementer())
                .validator(
                        new DefaultJobParametersValidator(
                                new String[]{"totalCount", "targetDate"}, new String[0]
                        )
                )
                .build();
    }

    @Bean
    @JobScope
    public Step managerStep(
        PartitionHandler partitionHandler,
        @Value("#{jobParameters['targetDate']}") String targetDate,
        Step apiOrderGenerateStep
    ) {
        return new StepBuilder("managerStep", jobRepository)
                .partitioner("delegateStep", getPartitioner(targetDate))
                .step(apiOrderGenerateStep)
                .partitionHandler(partitionHandler)
                .build();
    }

    // 매니저 스텝이 워커 스텝을 어떻게 다룰지 정의
    @Bean
    public PartitionHandler partitionHandler(Step apiOrderGenerateStep) {
        final TaskExecutorPartitionHandler taskExecutorPartitionHandler = new TaskExecutorPartitionHandler();
        taskExecutorPartitionHandler.setStep(apiOrderGenerateStep);
        taskExecutorPartitionHandler.setGridSize(7);
        taskExecutorPartitionHandler.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return taskExecutorPartitionHandler;
    }

    // 워커 스텝을 위해서 StepExecution 을 생성하는 인터페이스
    Partitioner getPartitioner(String targetDate) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        final LocalDate date = LocalDate.parse(targetDate, formatter);
        return x -> {
            final Map<String, ExecutionContext> result = new HashMap<>();

            IntStream.range(0, 7)
                    .forEach(it -> {
                        ExecutionContext value = new ExecutionContext();
                        value.putString("targetDate", date.minusDays(it).format(formatter));
                        result.put("partition" + it, value);
                    });

            return result;
        };
    }

    @Bean
    public Step apiOrderGenerateStep(
            ApiOrderGenerateReader apiOrderGenerateReader,
            ApiOrderGenerateProcessor apiOrderGenerateProcessor
    ) {
        return new StepBuilder(("apiOrderGenerateStep"), jobRepository)
                .<Boolean, ApiOrder>chunk(10000, platformTransactionManager)
                .reader(apiOrderGenerateReader)
                .processor(apiOrderGenerateProcessor)
                .writer(apiOrderGenerateWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<ApiOrder> apiOrderGenerateWriter(
            @Value("#{stepExecutionContext['targetDate']}") String targetDate
    ) {
        final String fileName = targetDate + "_api_orders.csv";
        return new FlatFileItemWriterBuilder<ApiOrder>()
                .name("apiOrderGenerateWriter")
                .resource(new PathResource("src/main/resources/datas/" + fileName))
                .delimited()
                .names("id", "customerId", "url", "state", "createdAt")
                .headerCallback(writer -> writer.write("id, customerId, url, state, createdAt"))
                .build();
    }
}
```
> organize
```
# ManagerStep
- Step(partitionHandler, Step apiOrderGenerateStep)
  .partitioner()  // 나누기
  .step(apiOrderGenerateStep)
  .partitionHandler(partitionHandler) // step, gridSize, taskExecutor: 비동기
# PartitionHandler
  .setStep/setGridSize/setTaskExecutor
# Partitioner
  Map<String, ExecutonContext>: <파티션명, ExecutionContext>
# ItemWriter
  (@Value("#{stepExecutionContext['targetDate']}" String targetDate))
```
> > Program Arguments
> > - totalCount=10000 targetDate=20230707


---------------------------------------------------------------------------------------------------------------------------
# Ch03-04. 일일 정산 배치 만들기
일 csv 읽어서 Key(customerId, serviceId)로 나누고 serviceId별 KeyAndCount로 저장
- 첫번째 스탭은 파일의 고객 + 서비스 별로 집계를 해서 ExecutionContext안에 넣는다
- 두번째 스탭은 집계된 ExecutionContext 데이터를 가지고 DB에 넣는다
## 실습 - batch-campus
### PreSettleDetail Code
```java
// batch.detail
public record Key(Long customerId, Long serviceId) implements Serializable {
}

public record KeyAndCount(Key key, Long count) {
}

@Configuration
@RequiredArgsConstructor
public class SettleDetailStepConfiguration {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    // 첫번째 스텝은 파일의 고객 + 서비스 별로 집계를 해서 Execution Context 안에 넣는다
    // Key(A, 1) 13번 호출, 260원이다.

    @Bean
    public Step preSettleDetailStep(
            FlatFileItemReader<ApiOrder> preSettleDetailReader,
            PreSettleDetailWriter preSettleDetailWriter,
            ExecutionContextPromotionListener promotionListener
    ) {
        return new StepBuilder("preSettleDetailStep", jobRepository)
                .<ApiOrder, Key>chunk(10000, platformTransactionManager)
                .reader(preSettleDetailReader)
                .processor(new PreSettleDetailProcessor())
                .writer(preSettleDetailWriter)
                .listener(promotionListener)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<ApiOrder> preSettleDetailReader(
        @Value("#{jobParameters['targetDate']}") String targetDate
    ) {
        final String fileName = targetDate + "_api_orders.csv";
        return new FlatFileItemReaderBuilder<ApiOrder>()
                .name("preSettleDetailReader")
                .resource(new ClassPathResource("/datas/" + fileName))
                .linesToSkip(1)
                .delimited()
                .names("id", "customerId", "url", "state", "createdAt")
                .targetType(ApiOrder.class)
                .build();
    }

    @Bean
    public ExecutionContextPromotionListener promotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"snapshots"});
        return listener;
    }
}

public class PreSettleDetailProcessor implements ItemProcessor<ApiOrder, Key> {
    @Override
    public Key process(ApiOrder item) throws Exception {
        if (item.getState() == ApiOrder.State.FAIL) {
            return null;
        }
        final Long serviceId = ServicePolicy.findByUrl(item.getUrl())
                .getId();
        return new Key(item.getCustomerId(), serviceId);
    }
}

public enum ServicePolicy {
    // ~
    public static ServicePolicy findByUrl(String url) {
        return Arrays.stream(values())
                .filter(it -> it.url.equals(url))
                .findFirst()
                .orElseThrow();
    }
}

@Component
public class PreSettleDetailWriter implements ItemWriter<Key>, StepExecutionListener {
    private StepExecution stepExecution;

    @Override
    public void write(Chunk<? extends Key> chunk) throws Exception {
        ConcurrentHashMap<Key, Long> snapshotMap = (ConcurrentHashMap<Key, Long>) stepExecution.getExecutionContext().get("snapshots");
        chunk.forEach(key -> {
            snapshotMap.compute(
                    key,
                    (k, v) -> (v == null) ? 1 : v + 1
            );
        });
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;

        final ConcurrentHashMap<Key, Long> snapshotMap = new ConcurrentHashMap<>();
        stepExecution.getExecutionContext().put("snapshots", snapshotMap);
    }

    // JOB - (Step1, Step2)
    // Step1에만 StepExecution snapshots 을 넣음
    // Step1:StepExecution -> JobExecution
    // Step2는 Step1 StepExecution 에 접근할 수 없음
}
```
> organize
```
# Step - preSettleDetailStep
(preSettleDetailReader, preSettleDetailWriter, promotionListener) // promotionListener
  // Step1에만 StepExecution snapshots 을 넣음
  // Step1:StepExecution -> JobExecution
- StepBuilder
  .reader(preSettleDetailReader)
  .processor(new PreSettleDetailProcessor())
  .writer(preSettleDetailWriter)
  .listener(promotionListener)

# FlatFileItemReader<ApiOrder> preSettleDetailOrder
(@Value("#{jobParameters['targetDate']}") String targetDate)

# ItemProcessor - PreSettleDetailProcessor<ApiOrder, Key>
  @Override
  public Key process(ApiOrder item) throws Exception {
    if (item.getState() == ApiOrder.State.FAIL) {
      return null;
    }
    final Long serviceId = ServicePolicy.findByUrl(item.getUrl())
          .getId();
    return new Key(item.getCustomerId(), serviceId);
  }
> customerId(0 ~ 19), serviceId(1 ~ 26)
> return null (filter)

# ItemWriter - PreSettleDetailWriter implements ItemWriter<Key>, StepExecutionListener
this.stepExecution = stepExecution;

@Override
public void write(Chunk<? extends Key> chunk) throws Exception 

@Override
public void beforeStep(StepExecution stepExecution)
- beforeStep: StepExecutionListener 
> ConcurrentHashMap<Key, Long> snapshotMap 
> StepExecutionContext.getExecutionContext().put("snapshots", snapshotMap)

- ItemWriter: write(Chunk<? extends Key> chunk)
> snapshotMap = stepExecution.getExecutionContext.get("snapshots")
> chunk.forEach(key -> snapshotMap.compute(key, (k, v) -> (v == null) ? 1 : v+1 ))
> > key customerId, serviceId

# ExecutionContextPromotionListener
- listener = new ExecutionContextPromotionListener
> listener.setKeys(new String[]{"snapshots"})
> > StepExecutionContext > JobExecutionContext로 올려줌
```

### SettleDetail Code
```java
// domain
@Entity
@Table(name = "settledetail")
@NoArgsConstructor
@ToString
public class SettleDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long customerId;
    private Long serviceId;
    private Long count;
    private Long fee;
    private LocalDate targetDate;

    public SettleDetail(Long customerId, Long serviceId, Long count, Long fee, LocalDate targetDate) {
        this.customerId = customerId;
        this.serviceId = serviceId;
        this.count = count;
        this.fee = fee;
        this.targetDate = targetDate;
    }
}

// batch.detail
@Configuration
@RequiredArgsConstructor
public class SettleDetailStepConfiguration {
  // 두번째 스텝은 집계된 Execution Context 데이터를 가지고 DB에 넣는다
  @Bean
  public Step settleDetailStep(
        SettleDetailReader settleDetailReader,
        SettleDetailProcessor settleDetailProcessor,
        JpaItemWriter<SettleDetail> settleDetailWriter
  ) {
    return new StepBuilder("settleDetailStep", jobRepository)
          .<KeyAndCount, SettleDetail>chunk(1000, platformTransactionManager)
          .reader(settleDetailReader)
          .processor(settleDetailProcessor)
          .writer(settleDetailWriter)
          .build();
  }

  @Bean
  public JpaItemWriter<SettleDetail> settleDetailWriter(EntityManagerFactory entityManagerFactory) {
    return new JpaItemWriterBuilder<SettleDetail>()
          .entityManagerFactory(entityManagerFactory)
          .build();
  }
}

@Component
@RequiredArgsConstructor
class SettleDetailReader implements ItemReader<KeyAndCount>, StepExecutionListener {
    private Iterator<Map.Entry<Key, Long>> iterator;

    @Override
    public KeyAndCount read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!iterator.hasNext()) {
            return null;
        }
        Map.Entry<Key, Long> map = iterator.next();
        return new KeyAndCount(map.getKey(), map.getValue());
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        final JobExecution jobExecution = stepExecution.getJobExecution();
        final Map<Key, Long> snapshots = (ConcurrentHashMap<Key, Long>) jobExecution.getExecutionContext().get("snapshots");
        iterator = snapshots.entrySet().iterator();
    }
}

@Component
public class SettleDetailProcessor implements ItemProcessor<KeyAndCount, SettleDetail>, StepExecutionListener {
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private StepExecution stepExecution;

    @Override
    public SettleDetail process(KeyAndCount item) throws Exception {
        final Key key = item.key();
        final ServicePolicy servicePolicy = ServicePolicy.findById(key.serviceId());
        final Long count = item.count();

        final String targetDate = stepExecution.getJobParameters().getString("targetDate");

        return new SettleDetail(
                key.customerId(),
                key.serviceId(),
                count,
                servicePolicy.getFee() * count,
                LocalDate.parse(targetDate, dateTimeFormatter)
        );
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }
}

// batch.support
public class DateFormatJobParametersValidator implements JobParametersValidator {
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final String[] names;

    public DateFormatJobParametersValidator(String[] names) {
        this.names = names;
    }

    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        for (String name : names) {
            validateDateFormat(parameters, name);
        }
    }

    private void validateDateFormat(JobParameters parameters, String name) throws JobParametersInvalidException {
        try {
            final String string = parameters.getString(name);
            LocalDate.parse(Objects.requireNonNull(string), dateTimeFormatter);
        } catch (Exception e) {
            throw new JobParametersInvalidException("yyyyMMdd 형식만을 지원합니다");
        }
    }
}

// batch
@Configuration
@RequiredArgsConstructor
public class SettleJobConfiguration {
  private final JobRepository jobRepository;

  @Bean
  public Job settleJob(
        Step preSettleDetailStep,
        Step settleDetailStep
  ) {
    return new JobBuilder("settleJob", jobRepository)
          .validator(new DateFormatJobParametersValidator(new String[]{"targetDate"}))
          .start(preSettleDetailStep)
          .next(settleDetailStep)
          .build();
  }
}

public record KeyAndCount(Key key, Long count) {
}

@Getter
public enum ServicePolicy {
  //~
  public static ServicePolicy findById(Long id) {
    return Arrays.stream(values())
          .filter(it -> it.id == id)
          .findFirst()
          .orElseThrow();
  }
}
```
> organize
```
# Step - settleDetailStep
(settleDetailReader, settleDetailProcessor, settleDetailWriter)

# SettleDetailReader implements ItemReader<KeyAndCount>, StepExecutionListener
private Iterator<Map.Entry<Key, Long>> iterator;
- StepExecutionListener
@Override
public void beforeStep(StepExecution stepExecution)
> iterator = ((ConcurrentHashMap<Key, Long>) jobExecution.getExecutionContext().get("snapshots")).entrySet().iterator
- ItemReader
@Override
public KeyAndCount read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
> iter.next(): Map<Key, Long>
> new KeyAndCount(map.getKey(), map.getValue())

# SettleDetailProcessor implements ItemProcessor<KeyAndCount, SettleDetail>, StepExecutionListener 
- StepExecutionListener
 @Override
public void beforeStep(StepExecution stepExecution) {
  this.stepExecution = stepExecution;
}
- ItemProcessor
@Override
public SettleDetail process(KeyAndCount item) throws Exception
> item.key: Key, ServicePolicy.findById(key.serviceId()): ServicePolicy, item.count(): Long
> stepExecution.getJobParameters().getString("targetDate")
> return new SettleDetail(...)

# JpaItemWriter - settleDetailWriter
```
> RUN @Configuration SettleJobConfiguration
> > Program Args: targetDate=20230707 


---------------------------------------------------------------------------------------------------------------------------
# Ch03-05. 주간 정산 배치 만들기
- Customer 정보를 받아 일정산 테이블 정보를 주간 정보로 합산해 SettleGroup: List > DB, Email 보낸다

## 실습 - batch-campus
### SettleJobConfiguration
```java
// batch
@Configuration
@RequiredArgsConstructor
public class SettleJobConfiguration {
    private final JobRepository jobRepository;

    // 일일 정산 배치
    // 1주일간의 데이터를 집계해서
    // 데이터베이스에 쌓고, 고객사의 Email 로 전송한다 (Fake)
    @Bean
    public Job settleJob(
            Step preSettleDetailStep,
            Step settleDetailStep
            , Step settleGroupStep
    ) {
        return new JobBuilder("settleJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .validator(new DateFormatJobParametersValidator(new String[]{"targetDate"}))
                .start(preSettleDetailStep)
                .next(settleDetailStep)
                // 주간정산하는 날이면
                .next(isFridayDecider())
                // 주간정산 실행시켜줘
                .on("COMPLETED").to(settleGroupStep)₩
                .build()
                .build();
    }

    // 매주 금요일마다 주간 정상을 한다
    public JobExecutionDecider isFridayDecider() {
        return (jobExecution, stepExecution) -> {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            final String targetDate = stepExecution.getJobParameters().getString("targetDate");
            final LocalDate date = LocalDate.parse(targetDate, formatter);

            if (date.getDayOfWeek() != DayOfWeek.FRIDAY) {
                return new FlowExecutionStatus("NOOP");
            }
            return FlowExecutionStatus.COMPLETED;
        };
    }
}
```
> JobBuilder
> start(preSettleDetailStep)  
> next(JobExecutionDecider).on("COMPLETED").to(settleGroupStep)
- cf, `JobExecutionDecider`
```java
@FunctionalInterface
public interface JobExecutionDecider {

	/**
	 * Strategy for branching an execution based on the state of an ongoing
	 * {@link JobExecution}. The return value will be used as a status to determine the
	 * next step in the job.
	 * @param jobExecution a job execution
	 * @param stepExecution the latest step execution (may be {@code null})
	 * @return the exit status code
	 */
	FlowExecutionStatus decide(JobExecution jobExecution, @Nullable StepExecution stepExecution);

}
```
> FlowExecutionState.COMPLETED/STOPPED/FAILED/UNKNOWN

### domain
```java
// domain.repository
public interface CustomerRepository {
    List<Customer> findAll(Pageable pageable);
    Customer findById(Long id);

    class Fake implements CustomerRepository {
        @Override
        public List<Customer> findAll(Pageable pageable) { /* ~ */  }

        @Override
        public Customer findById(Long id) { /* ~ */ }
    }

}

// domain.repository
public interface SettleGroupRepository extends JpaRepository<SettleGroup, Long> {
    @Query(
            value = """
                    SELECT new SettleGroup(detail.customerId, detail.serviceId, sum(detail.count), sum(detail.fee))
                    FROM SettleDetail detail
                    WHERE detail.targetDate between  :start and :end
                    AND detail.customerId = :customerId
                    GROUP BY  detail.customerId, detail.serviceId
                    """
    )
    public List<SettleGroup> findGroupByCustomerIdAndServiceId(LocalDate start, LocalDate end, Long customerId);
}

// batch.upport
public interface EmailProvider {
    void send(String emailAddress, String title, String body);

    @Slf4j
    class Fake implements EmailProvider {}
}

// domain
@Data
public class Customer {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Customer(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}

@Entity
@Getter
@NoArgsConstructor
@ToString
public class SettleGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long customerId;
    private Long serviceId;
    private Long totalCount;
    private Long totalFee;
    private LocalDateTime createdAt;

    public SettleGroup(Long customerId, Long serviceId, Long totalCount, Long totalFee) {
        this.customerId = customerId;
        this.serviceId = serviceId;
        this.totalCount = totalCount;
        this.totalFee = totalFee;
        this.createdAt = LocalDateTime.now();
    }
}
```
> JpaRepository
>> SELECT new SettleGroup(detail.~) FROM SettleDetail detail

### SettleGroupStepConfiguration
```java
// batch.group
@Configuration
@RequiredArgsConstructor
public class SettleGroupStepConfiguration {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    @Bean
    public Step settleGroupStep(
            SettleGroupItemReader settleGroupItemReader,
            SettleGroupItemProcessor settleGroupItemProcessor,
            ItemWriter<List<SettleGroup>> settleGroupItemWriter
    ) {
        return new StepBuilder("settleGroupStep", jobRepository)
                .<Customer, List<SettleGroup>>chunk(100, platformTransactionManager)
                .reader(settleGroupItemReader)
                .processor(settleGroupItemProcessor)
                .writer(settleGroupItemWriter)
                .build();
    }

    @Bean
    public ItemWriter<List<SettleGroup>> settleGroupItemWriter(
            SettleGroupItemDbWriter settleGroupItemDbWriter,
            SettleGroupItemMailWriter settleGroupItemMailWriter
    ) {
        return new CompositeItemWriter<>(
                settleGroupItemDbWriter,
                settleGroupItemMailWriter
        );
    }
}

// batch.group
@Component
public class SettleGroupItemReader implements ItemReader<Customer> {
    private final CustomerRepository customerRepository;
    private Iterator<Customer> customerIterator;
    private int pageNo = 0;

    public SettleGroupItemReader() {
        this.customerRepository = new CustomerRepository.Fake();
        this.customerIterator = Collections.emptyIterator();

    }

    @Override
    public Customer read() {
        if (customerIterator.hasNext()) {
            return customerIterator.next();
        }

        customerIterator = customerRepository.findAll(PageRequest.of(pageNo++, 10)).iterator();
        if (!customerIterator.hasNext()) {
            return null;
        }
        return customerIterator.next();
    }
}

@Component
@RequiredArgsConstructor
public class SettleGroupItemProcessor implements ItemProcessor<Customer, List<SettleGroup>>, StepExecutionListener {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final SettleGroupRepository settleGroupRepository;
    private StepExecution stepExecution;

    @Override
    public List<SettleGroup> process(Customer item) throws Exception {
        final String targetDate = stepExecution.getJobParameters().getString("targetDate");
        final LocalDate end = LocalDate.parse(targetDate, formatter);

        return settleGroupRepository.findGroupByCustomerIdAndServiceId(
                end.minusDays(6),
                end,
                item.getId()
        );
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }
}

@Component
@RequiredArgsConstructor
public class SettleGroupItemDbWriter implements ItemWriter<List<SettleGroup>> {
    private final SettleGroupRepository settleGroupRepository;

    @Override
    public void write(Chunk<? extends List<SettleGroup>> chunk) throws Exception {
        final List<SettleGroup> settleGroups = new ArrayList<>();
        chunk.forEach(settleGroups::addAll);
        settleGroupRepository.saveAll(settleGroups);
    }
}

@Component
public class SettleGroupItemMailWriter implements ItemWriter<List<SettleGroup>> {
    private final CustomerRepository customerRepository;
    private final EmailProvider emailProvider;

    public SettleGroupItemMailWriter() {
        this.customerRepository = new CustomerRepository.Fake();
        this.emailProvider = new EmailProvider.Fake();
    }

    // 유료 API 총 사용 회수, 춍 요금
    // 세부사항에 대해서 (url, 몇건, 얼마)
    @Override
    public void write(Chunk<? extends List<SettleGroup>> chunk) throws Exception {
        for (List<SettleGroup> settleGroups : chunk) {
            if(settleGroups.isEmpty())
                continue;

            final SettleGroup settleGroup = settleGroups.get(0);
            final Long customerId = settleGroup.getCustomerId();
            final Customer customer = customerRepository.findById(customerId);
            final Long totalCount = settleGroups.stream().map(SettleGroup::getTotalCount).reduce(0L, Long::sum);
            final Long totalFee = settleGroups.stream().map(SettleGroup::getTotalFee).reduce(0L, Long::sum);
            List<String> detailByService = settleGroups.stream()
                    .map(it ->
                            "\n\"%s\" - 총 사용수 : %s, 총 비용 : %s".formatted(
                                    ServicePolicy.findById(it.getServiceId()).getUrl(),
                                    it.getTotalCount(),
                                    it.getTotalFee()
                            )
                    ).toList();

            final String body = """
                    안녕하세요. %s 고객님. 사용하신 유료 API 과금안내 드립니다.
                    총 %s건을 사용하셨으며, %s원의 비용이 발생했습니다.
                    세부내역은 다음과 같습니다. 감사합니다
                    %s
                    """.formatted(
                    customer.getName(),
                    totalCount,
                    totalFee,
                    detailByService
            );

            emailProvider.send(customer.getEmail(), "유로 API 과금 안내", body);
        }
    }
}
```
> - organize
```
- SettleGroupItemReader
> 고객정보를 받는다
> * ItemReader return null 일때까지 진행
- SettleGroupItemProcessor 
> 고객정보를 기반으로 SettleDetail테이블에서 조건 startDate, endDate, customerId과 customerId, serviceId 기준으로 Group By
> List<SettleGroup> Return
- CompositeItemWriter
> new CompositeItemWriter<>(writer...)
> SettleGroupItemDbWriter
> > settleGroupRepository.saveAll(settleGroup)
> SettleGroupItemMailWriter
```
> - Run @Configuration SettleGroupStepConfiguration
> > > SettleDetail에 20230701 ~ 20230707까지 데이터를 다 넣은 다음에 
> > > - Program Args: targetDate=20230707 