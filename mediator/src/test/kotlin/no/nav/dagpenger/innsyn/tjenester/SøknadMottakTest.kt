package no.nav.dagpenger.innsyn.tjenester

import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.CollectorRegistry.defaultRegistry
import no.nav.dagpenger.innsyn.PersonMediator
import no.nav.dagpenger.innsyn.melding.PapirSøknadsMelding
import no.nav.dagpenger.innsyn.melding.Søknadsmelding
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class SøknadMottakTest {
    private val testRapid = TestRapid()
    private val personMediator = mockk<PersonMediator>(relaxed = true)

    init {
        SøknadMottak(testRapid, personMediator)
    }

    @BeforeEach
    internal fun setUp() {
        testRapid.reset()
    }

    @Test
    fun `vi kan motta søknader`() {
        testRapid.sendTestMessage(søknadJson)
        verify { personMediator.håndter(any<Søknad>(), any<Søknadsmelding>()) }
        confirmVerified(personMediator)

        defaultRegistry.getSampleValue(
            "dagpenger_mottak_forsinkelse_sum",
            "type" to "soknad"
        ).also {
            assertTrue(syntheticDelaySeconds <= it)
        }
        defaultRegistry.getSampleValue(
            "dagpenger_mottak_forsinkelse_count",
            "type" to "soknad"
        ).also {
            assertTrue(1.0 <= it)
        }
    }

    @Test
    fun `vi kan motta papirsøknad`() {
        testRapid.sendTestMessage(papirsøknadJson)
        verify { personMediator.håndter(any<Søknad>(), any<PapirSøknadsMelding>()) }
        confirmVerified(personMediator)
    }
}

private fun CollectorRegistry.getSampleValue(name: String, vararg labels: Pair<String, String>) =
    labels.unzip().let { (labelNames, labelValues) ->
        getSampleValue(
            name,
            labelNames.toTypedArray(), labelValues.toTypedArray()
        )
    }

private const val syntheticDelaySeconds: Long = 5

@Language("JSON")
private val søknadJson = """{
  "@event_name": "innsending_mottatt",
  "@opprettet": "${LocalDateTime.now()}",
  "fødselsnummer": "123",
  "journalpostId": "123",
  "skjemaKode": "NAV 03-102.23",
  "tittel": "Tittel",
  "type": "NySøknad",
  "datoRegistrert": "${LocalDateTime.now().minusSeconds(syntheticDelaySeconds)}",
  "søknadsData": {
    "brukerBehandlingId": "123",
    "vedlegg": [],
    "skjemaNummer": "NAV12"
  }
}
""".trimIndent()

@Language("JSON")
private val papirsøknadJson = """{
  "@id": "123",
  "@opprettet": "2021-01-01T01:01:01.000001",
  "journalpostId": "12455",
  "datoRegistrert": "2021-01-01T01:01:01.000001",
  "skjemaKode": "NAV 03-102.23",
  "tittel": "Tittel",
  "type": "NySøknad",
  "fødselsnummer": "11111111111",
  "aktørId": "1234455",
  "søknadsData": {},
  "@event_name": "innsending_mottatt",
  "system_read_count": 0
}
""".trimIndent()
