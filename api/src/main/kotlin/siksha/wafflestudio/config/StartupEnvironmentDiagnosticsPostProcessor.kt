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

        log.info(
            "Startup environment diagnostics: activeProfiles={}, ociVaultSecretIdsPresent={}, ociVaultRegion={}, ociAuthType={}, vaultPropertySourcePresent={}",
            activeProfiles,
            environment.getProperty("oci.vault.secret-ids").isNullOrBlank().not(),
            environment.getProperty("oci.vault.region"),
            environment.getProperty("oci.auth.type", "auto"),
            vaultSource != null,
        )

        log.info(
            "Datasource diagnostics: resolvedUrl={}, usernamePresent={}, passwordPresent={}, urlPropertySources={}, usernamePropertySources={}, passwordPropertySources={}",
            resolvedDatasourceUrl ?: "<missing>",
            resolvedDatasourceUsername.isNullOrBlank().not(),
            resolvedDatasourcePassword.isNullOrBlank().not(),
            propertySourcesContaining(environment, DATASOURCE_URL_KEY),
            propertySourcesContaining(environment, DATASOURCE_USERNAME_KEY),
            propertySourcesContaining(environment, DATASOURCE_PASSWORD_KEY),
        )

        if (vaultSource is EnumerablePropertySource<*>) {
            val vaultKeys = vaultSource.propertyNames
                .filter { key ->
                    key.startsWith("spring.datasource.") ||
                        key == "jwt.secret-key" ||
                        key.startsWith("siksha.oauth.") ||
                        key.startsWith("slack.")
                }.sorted()

            log.info(
                "OCI vault property source diagnostics: keyCount={}, relevantKeys={}",
                vaultSource.propertyNames.size,
                vaultKeys,
            )

            log.info(
                "OCI vault datasource key presence: url={}, username={}, password={}",
                vaultKeys.contains(DATASOURCE_URL_KEY),
                vaultKeys.contains(DATASOURCE_USERNAME_KEY),
                vaultKeys.contains(DATASOURCE_PASSWORD_KEY),
            )
        } else {
            log.warn(
                "OCI vault property source '{}' is missing or not enumerable. Datasource URL is currently '{}'.",
                OCI_VAULT_PROPERTY_SOURCE_NAME,
                resolvedDatasourceUrl ?: "<missing>",
            )
        }

        if (resolvedDatasourceUrl.isNullOrBlank()) {
            log.error(
                "Datasource URL is still missing after environment post-processing. activeProfiles={}, vaultPropertySourcePresent={}, urlPropertySources={}",
                activeProfiles,
                vaultSource != null,
                propertySourcesContaining(environment, DATASOURCE_URL_KEY),
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

    companion object {
        private const val OCI_VAULT_PROPERTY_SOURCE_NAME = "oci-vault-secrets"
        private const val DATASOURCE_URL_KEY = "spring.datasource.url"
        private const val DATASOURCE_USERNAME_KEY = "spring.datasource.username"
        private const val DATASOURCE_PASSWORD_KEY = "spring.datasource.password"
    }
}
