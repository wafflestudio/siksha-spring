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
    private val everyMealAlarmJob: Job,
) {
    // 오전 7시 30분 실행
    @Scheduled(cron = "0 30 7 * * *")
    fun dailyAlarm() {
        jobLauncher.run(
            dailyMenuAlarmJob,
            JobParametersBuilder()
                .addLong("runAt", System.currentTimeMillis())
                .toJobParameters(),
        )
    }

    // 오전 7시 30분 실행
    @Scheduled(cron = "0 30 7 * * *")
    fun breakfastAlarm() {
        runEveryMealJob("BR")
    }

    // 오전 10시 30분 실행
    @Scheduled(cron = "0 30 10 * * *")
    fun lunchAlarm() {
        runEveryMealJob("LU")
    }

    // 오후 4시 30분 실행
    @Scheduled(cron = "0 30 16 * * *")
    fun dinnerAlarm() {
        runEveryMealJob("DN")
    }

    private fun runEveryMealJob(type: String) {
        jobLauncher.run(
            everyMealAlarmJob,
            JobParametersBuilder()
                .addString("type", type)
                .addLong("runAt", System.currentTimeMillis())
                .toJobParameters(),
        )
    }
}
