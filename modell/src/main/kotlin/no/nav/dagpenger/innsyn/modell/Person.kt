package no.nav.dagpenger.innsyn.modell

internal class Person private constructor(
    private val fnr: String,
    private val søknader: MutableList<Søknad>
) {
    constructor(fnr: String) : this(fnr, mutableListOf())

    fun harSøknadUnderBehandling() = søknader.any { !it.harVedtak() }

    fun håndter(søknad: Søknad) {
        søknader.add(søknad)
    }

    fun håndter(vedtak: Vedtak) {
        søknader.forEach { it.håndter(vedtak) }
    }
}