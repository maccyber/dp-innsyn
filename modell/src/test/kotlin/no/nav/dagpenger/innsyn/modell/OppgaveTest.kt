package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OppgaveTest {

    @Test
    fun `En uferdig oppgave har status Uferdig`() {

        val oppgave = testOppgave.ny("1")
        assertEquals(Oppgave.Uferdig, oppgave.tilstand)
    }

    val testOppgave = OppgaveType("testOppgave")
}