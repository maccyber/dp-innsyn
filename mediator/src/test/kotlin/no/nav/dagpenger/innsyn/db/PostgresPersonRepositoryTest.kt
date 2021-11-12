package no.nav.dagpenger.innsyn.db

import no.nav.dagpenger.innsyn.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.hendelser.Innsending
import no.nav.dagpenger.innsyn.modell.hendelser.Innsending.Vedlegg
import no.nav.dagpenger.innsyn.modell.hendelser.Innsending.Vedlegg.Status.LastetOpp
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.serde.PersonVisitor
import no.nav.dagpenger.innsyn.modell.serde.SøknadVisitor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

internal class PostgresPersonRepositoryTest {
    private val repository = PostgresPersonRepository()

    @Test
    fun `skal lagre og finne person`() {
        withMigratedDb {
            val person = repository.person("123")
            val søknad = Søknad(
                "id",
                "journalpostId",
                "NAV01",
                Søknad.SøknadsType.NySøknad,
                Kanal.Digital,
                LocalDateTime.now(),
                listOf(
                    Vedlegg("123", "123", LastetOpp)
                ),
                "tittel"
            )

            person.håndter(søknad)
            repository.lagre(person)

            repository.person(person.fnr).also {
                with(PersonInspektør(it)) {
                    assertEquals(1, søknader)
                    assertEquals(1, vedlegg)
                }
            }

            repository.hentSøknaderFor(person.fnr).also {
                assertSøknadEquals(søknad, it.first())
            }
        }
    }

    @Test
    fun `skal lagre og finne person med papirsøknad`() {
        withMigratedDb {
            val person = repository.person("123")

            person.håndter(
                Søknad(
                    null,
                    "journalpostId",
                    "NAV01",
                    Søknad.SøknadsType.NySøknad,
                    Kanal.Digital,
                    LocalDateTime.now(),
                    emptyList(),
                    "tittel"
                )
            )
            repository.lagre(person)

            repository.person(person.fnr).also {
                with(PersonInspektør(it)) {
                    assertEquals(1, søknader)
                    assertEquals(0, vedlegg)
                }
            }
        }
    }

    @Test
    fun `skal finne papirsøknad for person`() {
        withMigratedDb {
            val person = repository.person("123")
            val søknad = Søknad(
                null,
                "journalpostId",
                "NAV01",
                Søknad.SøknadsType.NySøknad,
                Kanal.Papir,
                LocalDateTime.now(),
                emptyList(),
                "tittel"
            )
            person.håndter(søknad)
            repository.lagre(person)
            repository.hentSøknaderFor(person.fnr, LocalDate.now().minusDays(5), LocalDate.now().plusDays(5)).also {
                assertFalse(it.isEmpty())
                assertSøknadEquals(søknad, it.first())
            }
        }
    }

    private fun assertSøknadEquals(expected: Søknad, result: Søknad) {
        val expInspektør = SøknadInspektør(expected)
        val resultInspektør = SøknadInspektør(result)
        assertEquals(
            expInspektør.datoInnsendt.truncatedTo(ChronoUnit.SECONDS),
            resultInspektør.datoInnsendt.truncatedTo(ChronoUnit.SECONDS)
        )
        assertEquals(expInspektør.journalpostId, resultInspektør.journalpostId)
        assertEquals(expInspektør.kanal, resultInspektør.kanal)
        assertEquals(expInspektør.skjemaKode, resultInspektør.skjemaKode)
        assertEquals(expInspektør.søknadId, resultInspektør.søknadId)
        assertEquals(expInspektør.søknadsType, resultInspektør.søknadsType)
        assertEquals(expInspektør.tittel, resultInspektør.tittel)
        assertEquals(expInspektør.antallVedlegg, resultInspektør.antallVedlegg)
    }

    private class PersonInspektør(person: Person) : PersonVisitor {
        var søknader = 0
        var vedlegg = 0

        init {
            person.accept(this)
        }

        override fun visitSøknad(
            søknadId: String?,
            journalpostId: String,
            skjemaKode: String?,
            søknadsType: Søknad.SøknadsType,
            kanal: Kanal,
            datoInnsendt: LocalDateTime,
            tittel: String?
        ) {
            søknader++
        }

        override fun visitVedlegg(skjemaNummer: String, navn: String, status: Vedlegg.Status) {
            vedlegg++
        }
    }

    private class SøknadInspektør(val søknad: Søknad) : SøknadVisitor {
        var søknadId: String? = null
        var skjemaKode: String? = null
        var tittel: String? = null
        var antallVedlegg = 0

        lateinit var journalpostId: String
        lateinit var søknadsType: Søknad.SøknadsType
        lateinit var kanal: Kanal
        lateinit var datoInnsendt: LocalDateTime

        init {
            søknad.accept(this)
        }

        override fun visitSøknad(
            søknadId: String?,
            journalpostId: String,
            skjemaKode: String?,
            søknadsType: Søknad.SøknadsType,
            kanal: Kanal,
            datoInnsendt: LocalDateTime,
            tittel: String?
        ) {
            this.søknadId = søknadId
            this.skjemaKode = skjemaKode
            this.journalpostId = journalpostId
            this.søknadsType = søknadsType
            this.kanal = kanal
            this.datoInnsendt = datoInnsendt
            this.tittel = tittel
        }

        override fun visitVedlegg(
            skjemaNummer: String,
            navn: String,
            status: Innsending.Vedlegg.Status
        ) {
            antallVedlegg++
        }
    }
}
