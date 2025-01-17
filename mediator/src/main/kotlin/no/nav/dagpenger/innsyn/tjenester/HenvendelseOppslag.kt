package no.nav.dagpenger.innsyn.tjenester

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.DefaultRequest
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import no.nav.dagpenger.innsyn.tjenester.ettersending.MinimalEttersendingDto
import no.nav.dagpenger.innsyn.tjenester.ettersending.toInternal
import no.nav.dagpenger.innsyn.tjenester.paabegynt.Påbegynt
import no.nav.dagpenger.innsyn.tjenester.paabegynt.toInternal
import no.nav.dagpenger.ktor.client.metrics.PrometheusMetrics
import java.time.ZonedDateTime

internal class HenvendelseOppslag(
    private val dpProxyUrl: String,
    private val tokenProvider: () -> String,
    httpClientEngine: HttpClientEngine = CIO.create(),
    baseName: String = "dp_innsyn"
) {

    private val dpProxyClient = HttpClient(httpClientEngine) {

        install(PrometheusMetrics) {
            this.baseName = baseName
        }

        install(DefaultRequest) {
        }
        install(JsonFeature) {
            serializer = JacksonSerializer(
                jackson = jacksonMapperBuilder()
                    .addModule(JavaTimeModule())
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .build()
            )
        }
    }

    suspend fun hentEttersendelser(fnr: String): List<MinimalEttersendingDto> {
        return hentRequestMedFnrIBody<ExternalEttersending>(fnr, "$dpProxyUrl/proxy/v1/ettersendelser").toInternal()
    }

    suspend fun hentPåbegynte(fnr: String): List<Påbegynt> {
        return hentRequestMedFnrIBody<ExternalPåbegynt>(fnr, "$dpProxyUrl/proxy/v1/paabegynte").toInternal()
    }

    private suspend inline fun <reified T> hentRequestMedFnrIBody(fnr: String, requestUrl: String): List<T> =
        dpProxyClient.request(requestUrl) {
            method = HttpMethod.Post
            header(HttpHeaders.Authorization, "Bearer ${tokenProvider.invoke()}")
            header(HttpHeaders.ContentType, "application/json")
            header(HttpHeaders.Accept, "application/json")
            body = mapOf("fnr" to fnr)
        }
}

data class ExternalEttersending(
    val behandlingsId: String,
    val hovedskjemaKodeverkId: String,
    val sistEndret: ZonedDateTime,
    val innsendtDato: ZonedDateTime?,
    val vedlegg: List<Vedlegg>
) {
    data class Vedlegg(val tilleggsTittel: String?, val kodeverkId: String)
}

data class ExternalPåbegynt(
    val behandlingsId: String,
    val hovedskjemaKodeverkId: String,
    val sistEndret: ZonedDateTime
)
