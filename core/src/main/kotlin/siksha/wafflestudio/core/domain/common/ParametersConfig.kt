package siksha.wafflestudio.core.domain.common

import aws.sdk.kotlin.services.ssm.SsmClient
import aws.sdk.kotlin.services.ssm.model.GetParameterRequest
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class ParametersConfig : EnvironmentAware, BeanFactoryPostProcessor {
    private lateinit var env: Environment

    override fun setEnvironment(environment: Environment) {
        env = environment
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val ssmClient = SsmClient { region = "ap-northeast-2" }

        val ssmName =
            when {
                "prod" in env.activeProfiles -> "/siksha/shared_config/prod"
                "dev" in env.activeProfiles -> "/siksha/shared_config/dev"
                else -> return
            }

        val getParameterRequest =
            GetParameterRequest {
                name = ssmName
                withDecryption = true
            }
        val parametersString =
            runBlocking {
                ssmClient.getParameter(getParameterRequest).parameter?.value
            }
        checkNotNull(parametersString).split("\n").forEach {
            val (paramKey, paramValue) = it.split("=", limit = 2)
            System.setProperty(paramKey, paramValue)
        }
    }
}
