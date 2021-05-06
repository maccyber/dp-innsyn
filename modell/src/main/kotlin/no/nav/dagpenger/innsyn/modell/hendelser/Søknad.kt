package no.nav.dagpenger.innsyn.modell.hendelser

class Søknad(
    internal val søknadId: String,
    internal val journalpostId: String,
    oppgaver: Set<Oppgave>
) : Innsending(søknadId, oppgaver)
