package no.nav.dagpenger.innsyn.modell.hendelser

class Søknadsprosess constructor(
    private val id: String,
    oppgaver: List<Oppgave>,
) {
    private val oppgaver = oppgaver.toMutableList()

    fun harUferdigeOppgaver() = oppgaver.any { it.status == "Uferdig" }
}
