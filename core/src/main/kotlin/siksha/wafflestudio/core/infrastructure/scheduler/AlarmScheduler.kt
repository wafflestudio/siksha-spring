package siksha.wafflestudio.core.infrastructure.scheduler

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class AlarmScheduler(
    private val jobLauncher: JobLauncher,
    private val dailyMenuAlarmJob: Job,
) {
    // 오전 7시 30분 실행
    @Scheduled(cron = "0 30 7 * * *")
    fun morningAlarm() {
        jobLauncher.run(
            dailyMenuAlarmJob,
            JobParametersBuilder()
                .addLong("runAt", System.currentTimeMillis())
                .toJobParameters(),
        )
    }
}
