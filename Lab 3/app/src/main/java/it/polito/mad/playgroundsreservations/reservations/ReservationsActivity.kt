package it.polito.mad.playgroundsreservations.reservations

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.Playground
import it.polito.mad.playgroundsreservations.database.Reservation
import it.polito.mad.playgroundsreservations.database.Sports
import java.time.Duration
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
                    playgroundId = playgroundsList.find { it.sport == Sports.BASKETBALL }?.id
                        ?: 0,
                    sport = Sports.BASKETBALL,
                    time = ZonedDateTime.of(2023, 5, 1, 11, 0, 0, 0, zoneId),
                    duration = Duration.ofHours(2)
                )
                val reservationGOLF = Reservation(
                    userId = 1,
                    playgroundId = playgroundsList.find { it.sport == Sports.GOLF }?.id ?: 0,
                    sport = Sports.GOLF,
                    time = ZonedDateTime.of(2023, 5, 10, 23, 0, 0, 0, zoneId),
                    duration = Duration.ofHours(1)
                )
                val reservationTENNIS = Reservation(
                    userId = 1,
                    playgroundId = playgroundsList.find { it.sport == Sports.TENNIS }?.id ?: 0,
                    sport = Sports.TENNIS,
                    time = ZonedDateTime.of(2023, 5, 22, 8, 0, 0, 0, zoneId),
                    duration = Duration.ofHours(3)
                )
                val reservationVOLLEYBALL = Reservation(
                    userId = 1,
                    playgroundId = playgroundsList.find { it.sport == Sports.VOLLEYBALL }?.id
                        ?: 0,
                    sport = Sports.VOLLEYBALL,
                    time = ZonedDateTime.of(2023, 5, 26, 22, 0, 0, 0, zoneId),
                    duration = Duration.ofHours(1)
                )
                val reservationSameDay = Reservation(
                    userId = 1,
                    playgroundId = playgroundsList.find { it.sport == Sports.GOLF }?.id ?: 0,
                    sport = Sports.GOLF,
                    time = ZonedDateTime.of(2023, 5, 22, 14, 0, 0, 0, zoneId),
                    duration = Duration.ofHours(2)
                )

                val reservationSameDaySameSport = Reservation(
                    userId = 1,
                    playgroundId = playgroundsList.find { it.sport == Sports.VOLLEYBALL }?.id ?: 0,
                    sport = Sports.VOLLEYBALL,
                    time = ZonedDateTime.of(2023, 5, 26, 14, 0, 0, 0, zoneId),
                    duration = Duration.ofHours(3)
                )

                val reservations = listOf(
                    reservationFOOTBALL,
                    reservationBASKETBALL,
                    reservationGOLF,
                    reservationTENNIS,
                    reservationVOLLEYBALL,
                    reservationSameDay,
                    reservationSameDaySameSport
                )
                reservations.forEach { reservation ->
                    reservationsViewModel.save(reservation)
                }
            }
        }
    }

}
