package uk.co.sentinelweb.cuer.app.ui.common.resources

class DefaultStringDecoder : StringDecoder {
    override fun get(res: StringResource): String = res.default

    override fun get(res: StringResource, params: List<String>): String = res.default.run {
        params.foldIndexed(res.default) { index, acc, s -> acc.replaceFirst("%1\$s", s) }
    }
}