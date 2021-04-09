package no.nav.dagpenger.innsyn

import no.nav.dagpenger.innsyn.Dagpenger.søknadOppgave
import no.nav.dagpenger.innsyn.Dagpenger.vedleggOppgave
import no.nav.dagpenger.innsyn.Dagpenger.vedtakOppgave
import no.nav.dagpenger.innsyn.db.PostgresPersonRepository
import no.nav.dagpenger.innsyn.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.innsyn.modell.Behandlingskjede
import no.nav.dagpenger.innsyn.modell.BehandlingskjedeId
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.serde.PersonJsonBuilder
import no.nav.dagpenger.innsyn.modell.serde.PersonVisitor
import no.nav.dagpenger.innsyn.tjenester.EttersendingMottak
import no.nav.dagpenger.innsyn.tjenester.SøknadMottak
import no.nav.dagpenger.innsyn.tjenester.VedtakMottak
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class E2ESøknadOgVedtakTest {
    private val rapid = TestRapid()
    private val personRepository = PostgresPersonRepository()
    private val personMediator = PersonMediator(personRepository)
    private val søknadAsJson by lazy { javaClass.getResource("/søknadsinnsending.json").readText() }
    private val ettersendingAsJson by lazy { javaClass.getResource("/ettersending.json").readText() }
    private val vedtakAsJson by lazy { javaClass.getResource("/vedtak.json").readText() }

    init {
        SøknadMottak(rapid, personMediator)
        EttersendingMottak(rapid, personMediator)
        VedtakMottak(rapid, personMediator)
    }

    @Test
    fun `skal kunne motta søknad og vedtak`() {
        withMigratedDb {
            rapid.sendTestMessage(søknadAsJson)
            with(PersonInspektør(person)) {
                assertEquals(1, behandlingskjeder)
                assertEquals(1, vedtakOppgaver)
                assertEquals(3, uferdigeOppgaver)
                assertEquals(1, ferdigeOppgaver)
            }

            rapid.sendTestMessage(ettersendingAsJson)
            with(PersonInspektør(person)) {
                assertEquals(1, vedtakOppgaver)
                assertEquals(2, vedleggOppgaver)
                assertEquals(2, uferdigeOppgaver)
                assertEquals(2, ferdigeOppgaver)
            }

            rapid.sendTestMessage(vedtakAsJson)
            with(PersonInspektør(person)) {
                assertEquals(1, vedtakOppgaver)
                assertEquals(1, søknadOppgaver)
                assertEquals(2, vedleggOppgaver)
                assertEquals(1, uferdigeOppgaver)
                assertEquals(3, ferdigeOppgaver)
            }

            println(PersonJsonBuilder(person).resultat().toPrettyString())
        }
    }

    private val person get() = personRepository.person("10108099999")

    private class PersonInspektør(person: Person) : PersonVisitor {
        var uferdigeOppgaver = 0
        var ferdigeOppgaver = 0
        var søknadOppgaver = 0
        var vedleggOppgaver = 0
        var vedtakOppgaver = 0
        var behandlingskjeder = 0

        init {
            person.accept(this)
        }

        override fun preVisit(behandlingskjede: Behandlingskjede, id: BehandlingskjedeId) {
            behandlingskjeder++
        }

        override fun preVisit(
            oppgave: Oppgave,
            id: Oppgave.OppgaveId,
            beskrivelse: String,
            opprettet: LocalDateTime,
            tilstand: Oppgave.OppgaveTilstand
        ) {
            when (tilstand) {
                Oppgave.OppgaveTilstand.Uferdig -> uferdigeOppgaver++
                Oppgave.OppgaveTilstand.Ferdig -> ferdigeOppgaver++
            }

            when (id.type) {
                søknadOppgave -> søknadOppgaver++
                vedleggOppgave -> vedleggOppgaver++
                vedtakOppgave -> vedtakOppgaver++
            }
        }
    }
}
