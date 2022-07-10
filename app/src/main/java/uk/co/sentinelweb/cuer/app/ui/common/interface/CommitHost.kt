package uk.co.sentinelweb.cuer.app.ui.common.inteface

interface CommitHost {
    fun isReady(ready: Boolean)
}

class EmptyCommitHost : CommitHost {
    override fun isReady(ready: Boolean) = Unit
}