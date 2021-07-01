package no.nav.dagpenger.innsyn.tjenester

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall.VedtakMottak")

internal class VedtakAvsluttetMottak(
    rapidsConnection: RapidsConnection,
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("table", "SIAMO.VEDTAK") }
            validate {
                it.requireKey(
                    "tokens",
                    "after",
                    "tokens.FODSELSNR",
                    "after.VEDTAK_ID",
                    "after.SAK_ID",
                )
            }
            validate { it.requireValue("before.VEDTAKSTATUSKODE", "IVERK") }
            validate { it.requireValue("after.VEDTAKSTATUSKODE", "AVSLU") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val fnr = packet["tokens"]["FODSELSNR"].asText()
        val vedtakId = packet["after"]["VEDTAK_ID"].asText()
        val sakId = packet["after"]["SAK_ID"].asText()

        withLoggingContext(
            "fagsakId" to sakId,
            "vedtakId" to vedtakId
        ) {
            logg.info { "Mottok vedtak som gikk fra IVERK til AVLSU" }
            sikkerlogg.info { "Mottok nytt vedtak for person $fnr: ${packet.toJson()}" }
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        logg.error { problems }
        sikkerlogg.error { problems.toExtendedReport() }
    }
}
