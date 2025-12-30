package siksha.wafflestudio.core.infrastructure.config

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort
import org.springframework.transaction.PlatformTransactionManager
import siksha.wafflestudio.core.domain.user.data.AlarmType
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.dto.DailyMenuAlarm
import siksha.wafflestudio.core.domain.user.repository.UserRepository

private const val USER_BATCH_SIZE = 500

@Configuration
@EnableBatchProcessing
class AlarmBatchConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    @Bean
    fun dailyMenuAlarmJob(dailyMenuAlarmStep: Step): Job =
        JobBuilder("dailyMenuAlarmJob", jobRepository)
            .start(dailyMenuAlarmStep)
            .build()

    @Bean
    fun dailyMenuAlarmStep(
        @Qualifier("dailyAlarmUserReader")
        reader: ItemReader<User>,
        @Qualifier("dailyAlarmProcessor")
        processor: ItemProcessor<User, DailyMenuAlarm>,
        writer: ItemWriter<DailyMenuAlarm>,
    ): Step =
        StepBuilder("dailyMenuAlarmStep", jobRepository)
            .chunk<User, DailyMenuAlarm>(USER_BATCH_SIZE, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build()

    @Bean
    fun dailyAlarmUserReader(userRepository: UserRepository): RepositoryItemReader<User> =
        RepositoryItemReaderBuilder<User>()
            .name("dailyAlarmUserReader")
            .repository(userRepository)
            .methodName("findAllByAlarmType")
            .arguments(listOf(AlarmType.DAILY))
            .pageSize(USER_BATCH_SIZE)
            .sorts(mapOf("id" to Sort.Direction.ASC))
            .build()

    @Bean
    fun everyMealAlarmJob(everyMealAlarmStep: Step): Job =
        JobBuilder("everyMealAlarmJob", jobRepository)
            .start(everyMealAlarmStep)
            .build()

    @Bean
    fun everyMealAlarmStep(
        @Qualifier("everyMealAlarmUserReader")
        reader: ItemReader<User>,
        @Qualifier("everyMealAlarmProcessor")
        processor: ItemProcessor<User, DailyMenuAlarm>,
        writer: ItemWriter<DailyMenuAlarm>,
    ): Step =
        StepBuilder("everyMealAlarmStep", jobRepository)
            .chunk<User, DailyMenuAlarm>(USER_BATCH_SIZE, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build()

    @Bean
    fun everyMealAlarmUserReader(userRepository: UserRepository): RepositoryItemReader<User> =
        RepositoryItemReaderBuilder<User>()
            .name("everyMealAlarmUserReader")
            .repository(userRepository)
            .methodName("findAllByAlarmType")
            .arguments(listOf(AlarmType.EVERY_MEAL))
            .pageSize(USER_BATCH_SIZE)
            .sorts(mapOf("id" to Sort.Direction.ASC))
            .build()
}
