package no.nav.dagpenger.innsyn

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.basic
import io.ktor.features.CallLogging
import io.ktor.request.document
import io.ktor.routing.routing
import mu.KotlinLogging
import org.slf4j.event.Level

private val logger = KotlinLogging.logger { }
private val sikkerLogg = KotlinLogging.logger("tjenestekall")

internal fun Application.innsynApi(
    personRepository: PersonRepository
    /*jwkProvider: JwkProvider,
    issuer: String,
    clientId: String*/
) {
    install(CallLogging) {
        level = Level.DEBUG
        filter { call ->
            !setOf(
                "isalive",
                "isready",
                "metrics"
            ).contains(call.request.document())
        }
    }
    /*install(Authentication) {
        jwt {
            verifier(jwkProvider, issuer)
            realm = appName
            validate { credentials ->
                try {
                    requireNotNull(credentials.payload.audience) {
                        "Auth: Missing audience in token"
                    }
                    require(credentials.payload.audience.contains(clientId)) {
                        "Auth: Valid audience not found in claims"
                    }
                    JWTPrincipal(credentials.payload)
                } catch (e: Throwable) {
                    logger.error(e) { "JWT validerte ikke" }
                    null
                }
            }
        }
    }*/
    install(Authentication) {
        basic {
            realm = "ktor"
            validate { credentials ->
                UserIdPrincipal(credentials.name)
            }
        }
    }

    routing {
    }
}
