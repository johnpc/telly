package com.johncorser.telly.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.johncorser.telly.features.epg.db.EpgSourceDao
import com.johncorser.telly.features.epg.db.EpgSourceEntity
import com.johncorser.telly.features.epg.db.ProgramDao
import com.johncorser.telly.features.epg.db.ProgramEntity
import com.johncorser.telly.features.history.db.WatchHistoryDao
import com.johncorser.telly.features.history.db.WatchHistoryEntity
import com.johncorser.telly.features.mylist.db.MyListDao
import com.johncorser.telly.features.mylist.db.MyListEntity
import com.johncorser.telly.features.playlist.db.ChannelDao
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.playlist.db.PlaylistDao
import com.johncorser.telly.features.playlist.db.PlaylistEntity
import com.johncorser.telly.features.reminders.db.ReminderDao
import com.johncorser.telly.features.reminders.db.ReminderEntity
import com.johncorser.telly.features.search.db.SearchDao

/** The single app database; schema JSON is exported to app/schemas. */
@Database(
    entities = [
        PlaylistEntity::class, ChannelEntity::class, ProgramEntity::class,
        WatchHistoryEntity::class, EpgSourceEntity::class, ReminderEntity::class,
        MyListEntity::class,
    ],
    version = 6,
    exportSchema = true,
)
abstract class TellyDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao

    abstract fun channelDao(): ChannelDao

    abstract fun programDao(): ProgramDao

    abstract fun epgSourceDao(): EpgSourceDao

    abstract fun searchDao(): SearchDao

    abstract fun watchHistoryDao(): WatchHistoryDao

    abstract fun reminderDao(): ReminderDao

    /** v6: My-list saved programmes (migration in MyListMigration). */
    abstract fun myListDao(): MyListDao

    companion object {
        /** v2 adds the programme `<sub-title>` column (round3 P0 item 1). */
        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE programs ADD COLUMN subTitle TEXT")
                }
            }

        /** v3 adds the watch_history table behind the History card. */
        val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `watch_history` " +
                            "(`channelKey` TEXT NOT NULL, `watchedAtMs` INTEGER NOT NULL, " +
                            "PRIMARY KEY(`channelKey`))",
                    )
                }
            }

        /** v4 adds custom EPG sources (Settings -> EPG -> EPG sources). */
        val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `epg_sources` " +
                            "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`playlistUrl` TEXT NOT NULL, `url` TEXT NOT NULL, " +
                            "`addedAtMs` INTEGER NOT NULL)",
                    )
                    db.execSQL(
                        "CREATE UNIQUE INDEX IF NOT EXISTS `index_epg_sources_playlistUrl_url` " +
                            "ON `epg_sources` (`playlistUrl`, `url`)",
                    )
                }
            }
    }
}
