package siksha.wafflestudio.config

import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.Ordered
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.EnumerablePropertySource
import org.springframework.core.env.PropertySource

class StartupEnvironmentDiagnosticsPostProcessor : EnvironmentPostProcessor, Ordered {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun postProcessEnvironment(
        environment: ConfigurableEnvironment,
        application: SpringApplication,
    ) {
        val activeProfiles = environment.activeProfiles.toList().ifEmpty { listOf("default") }
        val vaultSource = environment.propertySources.firstOrNull { it.name == OCI_VAULT_PROPERTY_SOURCE_NAME }
        val resolvedDatasourceUrl = environment.getProperty(DATASOURCE_URL_KEY)
        val resolvedDatasourceUsername = environment.getProperty(DATASOURCE_USERNAME_KEY)
        val resolvedDatasourcePassword = environment.getProperty(DATASOURCE_PASSWORD_KEY)

        emitInfo(
            "Startup environment diagnostics: activeProfiles=$activeProfiles, " +
                "ociVaultSecretIdsPresent=${environment.getProperty("oci.vault.secret-ids").isNullOrBlank().not()}, " +
                "ociVaultRegion=${environment.getProperty("oci.vault.region")}, " +
                "ociAuthType=${environment.getProperty("oci.auth.type", "auto")}, " +
                "vaultPropertySourcePresent=${vaultSource != null}",
        )

        emitInfo(
            "Datasource diagnostics: resolvedUrl=${resolvedDatasourceUrl ?: "<missing>"}, " +
                "usernamePresent=${resolvedDatasourceUsername.isNullOrBlank().not()}, " +
                "passwordPresent=${resolvedDatasourcePassword.isNullOrBlank().not()}, " +
                "urlPropertySources=${propertySourcesContaining(environment, DATASOURCE_URL_KEY)}, " +
                "usernamePropertySources=${propertySourcesContaining(environment, DATASOURCE_USERNAME_KEY)}, " +
                "passwordPropertySources=${propertySourcesContaining(environment, DATASOURCE_PASSWORD_KEY)}",
        )

        if (vaultSource is EnumerablePropertySource<*>) {
            val vaultKeys = vaultSource.propertyNames
                .filter { key ->
                    key.startsWith("spring.datasource.") ||
                        key == "jwt.secret-key" ||
                        key.startsWith("siksha.oauth.") ||
                        key.startsWith("slack.")
                }.sorted()

            emitInfo(
                "OCI vault property source diagnostics: keyCount=${vaultSource.propertyNames.size}, " +
                    "relevantKeys=$vaultKeys",
            )

            emitInfo(
                "OCI vault datasource key presence: " +
                    "url=${vaultKeys.contains(DATASOURCE_URL_KEY)}, " +
                    "username=${vaultKeys.contains(DATASOURCE_USERNAME_KEY)}, " +
                    "password=${vaultKeys.contains(DATASOURCE_PASSWORD_KEY)}",
            )
        } else {
            emitWarn(
                "OCI vault property source '$OCI_VAULT_PROPERTY_SOURCE_NAME' is missing or not enumerable. " +
                    "Datasource URL is currently '${resolvedDatasourceUrl ?: "<missing>"}'.",
            )
        }

        if (resolvedDatasourceUrl.isNullOrBlank()) {
            emitError(
                "Datasource URL is still missing after environment post-processing. " +
                    "activeProfiles=$activeProfiles, " +
                    "vaultPropertySourcePresent=${vaultSource != null}, " +
                    "urlPropertySources=${propertySourcesContaining(environment, DATASOURCE_URL_KEY)}",
            )
        }
    }

    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE

    private fun propertySourcesContaining(
        environment: ConfigurableEnvironment,
        key: String,
    ): List<String> = environment.propertySources
        .filter { source ->
            when (source) {
                is EnumerablePropertySource<*> -> source.propertyNames.contains(key)
                else -> source.getProperty(key) != null
            }
        }.map(PropertySource<*>::getName)

    private fun emitInfo(message: String) {
        log.info(message)
        emitStderr("INFO", message)
    }

    private fun emitWarn(message: String) {
        log.warn(message)
        emitStderr("WARN", message)
    }

    private fun emitError(message: String) {
        log.error(message)
        emitStderr("ERROR", message)
    }

    private fun emitStderr(
        level: String,
        message: String,
    ) {
        System.err.println("[startup-diagnostics][$level] $message")
        System.err.flush()
    }

    companion object {
        private const val OCI_VAULT_PROPERTY_SOURCE_NAME = "oci-vault-secrets"
        private const val DATASOURCE_URL_KEY = "spring.datasource.url"
        private const val DATASOURCE_USERNAME_KEY = "spring.datasource.username"
        private const val DATASOURCE_PASSWORD_KEY = "spring.datasource.password"
    }
}
