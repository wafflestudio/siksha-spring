package siksha.wafflestudio.api.common

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import siksha.wafflestudio.core.domain.auth.social.data.SocialProvider

@Component
class SocialProviderConverter : Converter<String, SocialProvider> {
    override fun convert(source: String): SocialProvider {
        return SocialProvider.valueOf(source.uppercase())
    }
}
