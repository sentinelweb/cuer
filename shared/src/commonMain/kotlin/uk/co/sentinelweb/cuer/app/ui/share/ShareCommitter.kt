package uk.co.sentinelweb.cuer.app.ui.share

import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain

interface ShareCommitter {
    suspend fun commit(afterCommit: AfterCommit)
    interface AfterCommit {
        suspend fun onCommit(type: ObjectTypeDomain, data: List<*>)
    }
}