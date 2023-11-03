package it.polito.mad.playgroundsreservations.database

import androidx.room.TypeConverter
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class TypeConverters {
    @TypeConverter
    fun zonedDateTimeFromString(value: String?): ZonedDateTime? {
        return value?.let { ZonedDateTime.parse(value) }
    }

    @TypeConverter
    fun zonedDateTimeToString(zonedDateTime: ZonedDateTime?): String? {
        val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
        return zonedDateTime?.format(formatter)
    }

    @TypeConverter
    fun durationFromLong(value: Long?): Duration? {
        return value?.let { Duration.ofMillis(it) }
    }

    @TypeConverter
    fun durationToLong(duration: Duration?): Long? {
        return duration?.toMillis()
    }
}