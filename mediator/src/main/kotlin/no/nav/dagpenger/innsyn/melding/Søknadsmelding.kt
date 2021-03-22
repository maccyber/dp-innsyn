package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.helse.rapids_rivers.JsonMessage

internal class Søknadsmelding(packet: JsonMessage) : Innsendingsmelding(packet) {
    private val søknadsid = packet["brukerBehandlingId"].asText()
    internal val søknad get() = Søknad(søknadsid, oppgaver)
}
