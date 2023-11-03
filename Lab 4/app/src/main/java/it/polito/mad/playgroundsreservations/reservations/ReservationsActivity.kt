package it.polito.mad.playgroundsreservations.reservations

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.Reservation
import it.polito.mad.playgroundsreservations.database.Sports
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class ReservationsActivity: AppCompatActivity() {
    private val reservationsViewModel by viewModels<ReservationsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservations)

        // create sample reservations if the database is empty
        if (reservationsViewModel.dbUpdated) {
            reservationsViewModel.playgrounds.observe(this) { playgroundsList ->
                val zoneId = ZoneId.of("UTC+02:00")
                val now = Instant.now().atZone(zoneId)
                val pastNow = Instant.now().atZone(zoneId).minusDays(1)
                val futureNow = Instant.now().atZone(zoneId).plusDays(1)

                val reservationFOOTBALL = Reservation(
                    userId = 1,
                    playgroundId = playgroundsList.find { it.sport == Sports.FOOTBALL }?.id ?: 0,
                    sport = Sports.FOOTBALL,
                    time = ZonedDateTime.of(2023, 5, 2, 12, 0, 0, 0, zoneId),
                    duration = Duration.ofHours(1),
                    rentingEquipment = true
                )
                val reservationBASKETBALL = Reservation(
                    userId = 1,
                    playgroundId = playgroundsList.find { it.sport == Sports.BASKETBALL }?.id ?: 0,
                    sport = Sports.BASKETBALL,
                    time = ZonedDateTime.of(now.year, now.monthValue, now.dayOfMonth, now.hour, 0, 0, 0, zoneId),
                    duration = Duration.ofHours(2),
                    rentingEquipment = true
                )
                val reservationGOLF = Reservation(
                    userId = 1,
                    playgroundId = playgroundsList.find { it.sport == Sports.GOLF }?.id ?: 0,
                    sport = Sports.GOLF,
                    time = ZonedDateTime.of(2023, 5, 10, 23, 0, 0, 0, zoneId),
                    duration = Duration.ofHours(1),
                    rentingEquipment = false
                )
                val reservationTENNIS = Reservation(
                    userId = 1,
                    playgroundId = playgroundsList.find { it.sport == Sports.TENNIS }?.id ?: 0,
                    sport = Sports.TENNIS,
                    time = ZonedDateTime.of(2023, 5, 22, 8, 0, 0, 0, zoneId),
                    duration = Duration.ofHours(3),
                    rentingEquipment = false
                )
                val reservationVOLLEYBALL = Reservation(
                    userId = 1,
                    playgroundId = playgroundsList.find { it.sport == Sports.VOLLEYBALL }?.id ?: 0,
                    sport = Sports.VOLLEYBALL,
                    time = ZonedDateTime.of(2023, 5, 26, 22, 0, 0, 0, zoneId),
                    duration = Duration.ofHours(1),
                    rentingEquipment = false
                )
                val reservationSameDay = Reservation(
                    userId = 1,
                    playgroundId = playgroundsList.find { it.sport == Sports.GOLF }?.id ?: 0,
                    sport = Sports.GOLF,
                    time = ZonedDateTime.of(2023, 5, 22, 14, 0, 0, 0, zoneId),
                    duration = Duration.ofHours(2),
                    rentingEquipment = true
                )

                val reservationSameDaySameSport = Reservation(
                    userId = 1,
                    playgroundId = playgroundsList.find { it.sport == Sports.VOLLEYBALL }?.id ?: 0,
                    sport = Sports.VOLLEYBALL,
                    time = ZonedDateTime.of(2023, 5, 26, 14, 0, 0, 0, zoneId),
                    duration = Duration.ofHours(3),
                    rentingEquipment = false
                )
                val reservationTENNISPast = Reservation(
                    userId = 1,
                    playgroundId = playgroundsList.find { it.sport == Sports.TENNIS }?.id ?: 0,
                    sport = Sports.TENNIS,
                    time = ZonedDateTime.of(pastNow.year, pastNow.monthValue, pastNow.dayOfMonth, pastNow.hour, 0, 0, 0, zoneId),
                    duration = Duration.ofHours(2),
                    rentingEquipment = true
                )
                val reservationFOOTBALLFuture = Reservation(
                    userId = 1,
                    playgroundId = playgroundsList.find { it.sport == Sports.FOOTBALL }?.id ?: 0,
                    sport = Sports.FOOTBALL,
                    time = ZonedDateTime.of(futureNow.year, futureNow.monthValue, futureNow.dayOfMonth, futureNow.hour, 0, 0, 0, zoneId),
                    duration = Duration.ofHours(2),
                    rentingEquipment = true
                )

                val reservations = listOf(
                    reservationFOOTBALL,
                    reservationBASKETBALL,
                    reservationGOLF,
                    reservationTENNIS,
                    reservationVOLLEYBALL,
                    reservationSameDay,
                    reservationSameDaySameSport,
                    reservationTENNISPast,
                    reservationFOOTBALLFuture
                )
                reservations.forEach { reservation ->
                    reservationsViewModel.saveReservation(reservation)
                }
            }
        }
    }

}
