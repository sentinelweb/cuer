package uk.co.sentinelweb.cuer.app.db.backup.version

import androidx.annotation.VisibleForTesting
//import uk.co.sentinelweb.cuer.app.db.backup.version.v1.V1Mapper
//import uk.co.sentinelweb.cuer.app.db.backup.version.v1.V1Parser
import uk.co.sentinelweb.cuer.app.db.backup.version.v2.V2Parser
import uk.co.sentinelweb.cuer.app.db.backup.version.v2.V3Parser
import java.util.*

class ParserFactory {

    fun create(data: String): Parser {
        val version = getVersion(data)
        return when (version) {
            //1 -> V1Parser(V1Mapper())
            2 -> V2Parser()
            3 -> V3Parser()
            else -> throw InvalidPropertiesFormatException("Can't get parser for version :(")
        }
    }

    @VisibleForTesting
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