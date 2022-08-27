package uk.co.sentinelweb.cuer.db.adapter

import com.squareup.sqldelight.ColumnAdapter
import kotlinx.datetime.Instant

class InstantAdapter: ColumnAdapter<Instant, String> {
    override fun decode(databaseValue: String) = Instant.parse(databaseValue)
    override fun encode(value: Instant) = value.toString()
}