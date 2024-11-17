package siksha.wafflestudio.core

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.testcontainers.context.ImportTestcontainers
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.testcontainers.service.connection.ServiceConnectionAutoConfiguration
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.io.ResourceLoader
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.event.BeforeTestMethodEvent
import org.testcontainers.containers.MySQLContainer
import java.nio.file.FileSystems
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.toPath

@AutoConfiguration(before = [ServiceConnectionAutoConfiguration::class])
@ImportTestcontainers
@SpringBootApplication
class TestApplication(
    private val jdbcTemplate: JdbcTemplate,
    private val resourceLoader: ResourceLoader,
) {
    companion object {
        @JvmStatic
        @ServiceConnection
        val mysql = MySQLContainer("mysql:8")
    }

    @EventListener(BeforeTestMethodEvent::class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun beforeTest() {
        val ddlDir = resourceLoader.getResource("classpath:/schema/")

        if (ddlDir.uri.scheme == "jar") {
            runCatching { FileSystems.newFileSystem(ddlDir.uri, emptyMap<String, Any>()) }
        }

        val ddls = ddlDir.uri.toPath().listDirectoryEntries("*.sql").sortedBy { it.name }

        for (ddl in ddls) {
            ddl.readText().split(";")
                .filter { it.isNotBlank() }
                .forEach { sql -> jdbcTemplate.execute(sql) }
        }

        val dmlDir = resourceLoader.getResource("classpath:/data/")

        if (dmlDir.uri.scheme == "jar") {
            runCatching { FileSystems.newFileSystem(dmlDir.uri, emptyMap<String, Any>()) }
        }

        val dmls = dmlDir.uri.toPath().listDirectoryEntries("*.sql").sortedBy { it.name }

        for (dml in dmls) {
            dml.readText().split(";")
                .filter { it.isNotBlank() }
                .forEach { sql -> jdbcTemplate.execute(sql) }
        }
    }
}
