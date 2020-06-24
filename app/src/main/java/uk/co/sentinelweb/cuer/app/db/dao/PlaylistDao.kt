package uk.co.sentinelweb.cuer.app.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistAndItems
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistEntity

@Dao
interface PlaylistDao {

    @Transaction
    @Query("SELECT * FROM playlist")
    fun getPlaylists(): List<PlaylistAndItems>

    @Insert
    fun insertAll(medias: List<PlaylistEntity>)

    @Insert
    fun insert(playlist: PlaylistEntity)

}