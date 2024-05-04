package uk.co.sentinelweb.cuer.app.service.remote.player

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator

// todo maybe emulate MediaSessionManager
interface Session {
    val id: OrchestratorContract.Identifier<GUID>
    fun start()
    fun stop()
}

class PlayerSession(
    override val id: OrchestratorContract.Identifier<GUID>,
    val item: PlaylistItemDomain
) : Session {

    override fun start() {

    }

    override fun stop() {

    }

}

class PlayerSessionManager(
    private val guidCreator: GuidCreator,
) {
    private val sessions: MutableList<PlayerSession> = mutableListOf()

    fun createSession(item: PlaylistItemDomain): PlayerSession {
        return PlayerSession(guidCreator.create().toIdentifier(LOCAL), item)
            .apply { sessions.add(this) }
    }

    fun getSessionCount(): Int {
        return sessions.size
    }

    fun getSession(index: Int): PlayerSession {
        return sessions[index]
    }

    fun getSession(id: OrchestratorContract.Identifier<GUID>): PlayerSession {
        return sessions.first { it.id == id }
    }

    fun clearSessions() {
        sessions.forEach { it.stop() }
        sessions.clear()
    }
}