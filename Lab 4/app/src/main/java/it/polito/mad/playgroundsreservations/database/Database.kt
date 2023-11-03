package it.polito.mad.playgroundsreservations.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

@androidx.room.Database(
    entities = [Reservation::class, Playground::class, PlaygroundRating::class, User::class],
    version = 12,
    exportSchema = true
)
@androidx.room.TypeConverters(TypeConverters::class)
abstract class Database: RoomDatabase() {
    abstract fun reservationsDao(): ReservationsDao
    abstract fun userDao(): UserDao
    abstract fun playgroundsDao(): PlaygroundsDao
    abstract fun playgroundRatingsDao(): PlaygroundRatingsDao

    companion object {
        @Volatile
        private var INSTANCE: Database? = null

        fun getDatabase(context: Context): Database =
            (INSTANCE ?:
            synchronized(this) {
                val i = INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    Database::class.java,
                    "database")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = i
                INSTANCE
            })!!
    }
}