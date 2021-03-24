package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Mangelbrev
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.FerdigOppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PersonTest {
    @Test
    fun `Person får søknad under behandling etter ny søknad`() {
        Person("ident").also { person ->
            person.håndter(søknad("id1", manglerVedtakOppgave()))
            assertTrue(person.harUferdigeOppgaverAv(vedtakOppgave))
        }
    }

    @Test
    fun `Søknad uten vedlegg og deretter innsendte vedlegg også vedtak`() {
        Person("ident").also { person ->
            person.håndter(søknad("id", manglerVedleggOppgave("vedleggA") + manglerVedtakOppgave()))
            assertTrue(person.harUferdigeOppgaverAv(vedtakOppgave))
            assertTrue(person.harUferdigeOppgaverAv(vedleggOppgave))

            person.håndter(ettersending("id", ferdigVedleggOppgave("vedleggB")))
            assertTrue(person.harUferdigeOppgaverAv(vedleggOppgave))

            person.håndter(ettersending("id", ferdigVedleggOppgave("vedleggA")))
            assertTrue(person.harUferdigeOppgaverAv(vedtakOppgave))
            assertFalse(person.harUferdigeOppgaverAv(vedleggOppgave))

            person.håndter(vedtak("id"))
            assertFalse(person.harUferdigeOppgaverAv(vedtakOppgave))
        }
    }

    @Test
    fun `Person skal kunne motta mangelbrev`() {
        Person("ident").also { person ->
            person.håndter(søknad("id", manglerVedtakOppgave()))
            person.håndter(mangelbrev("id"))
            assertTrue(person.finnUferdigeOppgaverAv(mangelbrevOppgave).isNotEmpty())
        }
    }

    private fun søknad(id: String, oppgaver: List<Oppgave>) = Søknad(id, oppgaver)
    private fun ettersending(id: String, oppgaver: List<Oppgave>) = Ettersending(id, oppgaver)
    private fun vedtak(søknadId: String) = Vedtak("1", søknadId, listOf(FerdigOppgave("vedtak", vedtakOppgave)))
    private fun manglerVedtakOppgave() = listOf(Oppgave("vedtak", vedtakOppgave))
    private fun manglerVedleggOppgave(navn: String) = listOf(Oppgave(navn, vedleggOppgave))
    private fun ferdigVedleggOppgave(navn: String) = listOf(FerdigOppgave(navn, vedleggOppgave))
    private fun mangelbrev(søknadId: String) = Mangelbrev("id", søknadId, listOf(Oppgave("", mangelbrevOppgave)))

    val vedtakOppgave = OppgaveType("testOppgave")
    val vedleggOppgave = OppgaveType("testOppgave")
    val mangelbrevOppgave = OppgaveType("testOppgave")
}