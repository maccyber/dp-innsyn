package no.nav.dagpenger.innsyn

import no.nav.dagpenger.innsyn.helpers.InMemoryPersonRepository
import no.nav.dagpenger.innsyn.tjenester.EttersendingMottak
import no.nav.dagpenger.innsyn.tjenester.SøknadMottak
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class MottaSøknadTest {
    private val rapid = TestRapid()
    private val personRepository = InMemoryPersonRepository()
    private val søknadAsJson = javaClass.getResource("/soknadsdata.json").readText()
    private val ettersendingAsJson = javaClass.getResource("/ettersending.json").readText()

    init {
        SøknadMottak(rapid, personRepository)
        EttersendingMottak(rapid, personRepository)
    }

    @Test
    fun `skal kunne motta søknad`() {
        val person = personRepository.person("10108099999")
        rapid.sendTestMessage(søknadAsJson)
        assertTrue(person.harSøknadUnderBehandling())

        rapid.sendTestMessage(ettersendingAsJson)
        assertFalse(person.harSøknadUnderBehandling())
    }
}
