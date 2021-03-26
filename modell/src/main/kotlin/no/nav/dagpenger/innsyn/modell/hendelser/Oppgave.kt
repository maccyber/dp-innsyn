package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.OppgaveVisitor
import java.util.Objects

class Oppgave private constructor(
    private val id: String,
    private val oppgaveType: OppgaveType,
    private var tilstand: Tilstand
) {
    override fun equals(other: Any?): Boolean {
        return other is Oppgave && id == other.id && oppgaveType == other.oppgaveType
    }

    override fun hashCode(): Int {
        return Objects.hash(id, oppgaveType)
    }

    fun leggTilHvis(type: OppgaveType, oppgaveTilstand: OppgaveTilstand, oppgaver: MutableSet<Oppgave>) {
        if (oppgaveType == type && tilstand.kode == oppgaveTilstand) oppgaver.add(this)
    }

    internal fun accept(visitor: OppgaveVisitor) {
        visitor.preVisit(this, id, oppgaveType, tilstand.kode)
        visitor.postVisit(this, id, oppgaveType, tilstand.kode)
    }

    private interface Tilstand {
        val kode: OppgaveTilstand
    }

    private object Uferdig : Tilstand {
        override val kode = OppgaveTilstand.Uferdig
    }

    private object Ferdig : Tilstand {
        override val kode = OppgaveTilstand.Ferdig
    }

    enum class OppgaveTilstand {
        Uferdig,
        Ferdig
    }

    class OppgaveType(private val type: String) {
        fun ny(id: String) = Oppgave(id, this, Uferdig)
        fun ferdig(id: String) = Oppgave(id, this, Ferdig)

        override fun toString() = type
    }
}
