package uk.co.sentinelweb.cuer.app.backup.version

import uk.co.sentinelweb.cuer.app.backup.version.v2.V2Parser
import uk.co.sentinelweb.cuer.app.db.backup.version.v2.V3Parser
import uk.co.sentinelweb.cuer.app.db.backup.version.v2.V4Parser

class ParserFactory {

    fun create(data: String): Parser {
        val version = getVersion(data)
        return when (version) {
            //1 -> V1Parser(V1Mapper())
            2 -> V2Parser()
            3 -> V3Parser()
            4 -> V4Parser()
            else -> throw UnsupportedOperationException("Can't get parser for version:$version")
        }
    }

    fun getVersion(data: String): Int {
        val pos = data.indexOf("version")
        if (pos in 0..100) {
            val colonPos = data.indexOf(":", startIndex = pos)
            val commaPos = data.indexOf(",", startIndex = colonPos + 1)
            return data.substring(colonPos + 1, commaPos).trim().toInt()
        } else {
            return 1
        }
    }

}