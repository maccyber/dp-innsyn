package no.nav.dagpenger.innsyn

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import no.nav.dagpenger.oauth2.CachedOauth2Client
import no.nav.dagpenger.oauth2.OAuth2Config

internal object Configuration {
    const val appName = "dp-innsyn"
    private val localProperties = ConfigurationMap(
        mapOf(
            "KAFKA_CONSUMER_GROUP_ID" to "dp-innsyn-v1",
            "KAFKA_RAPID_TOPIC" to "private-dagpenger-behov-v2",
            "KAFKA_RESET_POLICY" to "earliest",
            "HTTP_PORT" to "8080",
            "KAFKA_BROKERS" to "localhost:9092"
        )
    )

    private val defaultProperties = ConfigurationMap(
        mapOf(
            "KAFKA_CONSUMER_GROUP_ID" to "dp-innsyn-v1",
            "KAFKA_RAPID_TOPIC" to "teamdagpenger.rapid.v1",
            "KAFKA_RESET_POLICY" to "latest",
            "KAFKA_EXTRA_TOPIC" to "teamdagpenger.journalforing.v1,teamdagpenger.arena.oppgave.v1,teamarenanais.gg-arena-vedtak-dagpenger-v2-q1",
            "HTTP_PORT" to "8080"
        )
    )

    private val prodProperties = ConfigurationMap(
        "KAFKA_EXTRA_TOPIC" to "teamdagpenger.journalforing.v1,teamdagpenger.arena.oppgave.v1,teamarenanais.gg-arena-vedtak-dagpenger-v2-p",
    )

    val properties by lazy {
        val envProperties = systemProperties() overriding EnvironmentVariables()
        when (envProperties.getOrNull(Key("NAIS_CLUSTER_NAME", stringType))) {
            null -> envProperties overriding localProperties
            "prod-gcp" -> envProperties overriding prodProperties overriding defaultProperties
            else -> envProperties overriding defaultProperties
        }
    }

    val dpProxyUrl by lazy { properties[Key("DP_PROXY_URL", stringType)] }
    val dpProxyScope by lazy { properties[Key("DP_PROXY_SCOPE", stringType)] }

    val dpProxyTokenProvider by lazy {
        val azureAd = OAuth2Config.AzureAd(properties)
        CachedOauth2Client(
            tokenEndpointUrl = azureAd.tokenEndpointUrl,
            authType = azureAd.clientSecret(),
        )
    }

    fun asMap(): Map<String, String> = properties.list().reversed().fold(emptyMap()) { map, pair ->
        map + pair.second
    }
}
