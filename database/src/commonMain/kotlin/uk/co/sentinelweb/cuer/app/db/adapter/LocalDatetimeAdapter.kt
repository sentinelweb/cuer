package uk.co.sentinelweb.cuer.app.db.adapter

import com.squareup.sqldelight.ColumnAdapter
import kotlinx.datetime.LocalDateTime

class LocalDatetimeAdapter: ColumnAdapter<LocalDateTime, String> {
    override fun decode(databaseValue: String) = LocalDateTime.parse(databaseValue)
    override fun encode(value: LocalDateTime) = value.toString()
}