package it.polito.mad.playgroundsreservations.reservations

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.app.ActivityCompat.invalidateOptionsMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.Playground
import it.polito.mad.playgroundsreservations.database.Reservation
import it.polito.mad.playgroundsreservations.database.Sports
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ShowReservationFragment: Fragment(R.layout.show_reservation_fragment) {
    private val args by navArgs<ShowReservationFragmentArgs>()
    private val reservationsViewModel by viewModels<ReservationsViewModel>()
    private val zoneId = ZoneId.of("UTC+02:00")
    private var myReservation = Reservation(
        userId = 0,
        playgroundId = 0,
        sport = Sports.VOLLEYBALL,
        time = ZonedDateTime.of(2023, 5, 26, 14, 0, 0, 0, zoneId),
        duration = Duration.ofHours(1)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.show_reservation_fragment, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val reservationsViewModel by viewModels<ReservationsViewModel>()
        val reservations = reservationsViewModel.getUserReservations(1)
        val playgrounds = reservationsViewModel.playgrounds

        // ACTIVITY TITLE
        activity?.title = activity?.resources?.getString(R.string.reservation)

        var myPlayground = Playground(
            id = 0,
            name = "temp",
            address = "",
            sport = Sports.VOLLEYBALL
        )

        reservations.observe(viewLifecycleOwner) { reservationsList ->
                reservationsList.forEach { r ->
                    if (r.id == args.reservationId) {
                        myReservation = r

                        // invalidate the menu to see whether editing and canceling is still possible
                        invalidateOptionsMenu(activity)
                    }
                }

            playgrounds.observe(viewLifecycleOwner) { playgroundsList ->
                playgroundsList.forEach { p ->
                    if (p.id == myReservation.playgroundId) {
                         myPlayground = p
                    }
                }

                val sportName = when (myReservation.sport) {
                    Sports.BASKETBALL -> resources.getString(R.string.sport_basketball)
                    Sports.TENNIS -> resources.getString(R.string.sport_tennis)
                    Sports.FOOTBALL -> resources.getString(R.string.sport_football)
                    Sports.VOLLEYBALL -> resources.getString(R.string.sport_volleyball)
                    Sports.GOLF -> resources.getString(R.string.sport_golf)
                }

                val btnRateCourt = view.findViewById<Button>(R.id.btnRateCourt)
                // display rate court button only for past reservations and if not already rated
                val previousRatingForReservation = reservationsViewModel.getRatingByReservation(myReservation.id)

                previousRatingForReservation.observe(viewLifecycleOwner) { rating ->
                    if (myReservation.time.plus(myReservation.duration).isBefore(Instant.now().atZone(myReservation.time.zone))
                        && rating == null
                    ) {
                        btnRateCourt.setOnClickListener {
                            val navController = view.findNavController()
                            val action = ShowReservationFragmentDirections.actionShowReservationFragmentToRatingPlaygrounds(myReservation.playgroundId,myReservation.id)
                            navController.navigate(action)
                        }
                    } else {
                        btnRateCourt.visibility = GONE
                    }
                }

                val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
                ratingBar.setIsIndicator(true)

                val rating = reservationsViewModel.getPlaygroundAverageRating(myPlayground.id)
                rating.observe(viewLifecycleOwner) {
                    ratingBar.rating = it.toFloat()
                }

                view.findViewById<TextView>(R.id.playgroundName).text = myPlayground.name
                view.findViewById<TextView>(R.id.sportName).text = sportName

                view.findViewById<TextView>(R.id.timeInfo).text = myReservation.time.toLocalDate().format(DateTimeFormatter.ofLocalizedDate((FormatStyle.FULL))) + " - " +
                        myReservation.time.toLocalTime().format(DateTimeFormatter.ofLocalizedTime((FormatStyle.SHORT)))


                view.findViewById<TextView>(R.id.durationInfo).text = resources.getString(R.string.duration) + ": "+ myReservation.duration.toHours().toString() + "h"

                if (myReservation.rentingEquipment) {
                    view.findViewById<ImageView>(R.id.rentingEquipmentIconTrue).visibility = VISIBLE
                    view.findViewById<ImageView>(R.id.rentingEquipmentIconFalse).visibility = GONE
                } else {
                    view.findViewById<ImageView>(R.id.rentingEquipmentIconTrue).visibility = GONE
                    view.findViewById<ImageView>(R.id.rentingEquipmentIconFalse).visibility = VISIBLE
                }
                val image = view.findViewById<ImageView>(R.id.reservationImage)
                val sportIcon = view.findViewById<ImageView>(R.id.sportNameIcon)

                when (myReservation.sport) {
                    Sports.TENNIS -> { image.setImageResource(R.drawable.tennis_court); sportIcon.setImageResource(R.drawable.tennis_ball) }
                    Sports.BASKETBALL -> { image.setImageResource(R.drawable.basketball_court); sportIcon.setImageResource(R.drawable.basketball_ball) }
                    Sports.FOOTBALL -> { image.setImageResource(R.drawable.football_pitch); sportIcon.setImageResource(R.drawable.football_ball) }
                    Sports.VOLLEYBALL -> { image.setImageResource(R.drawable.volleyball_court); sportIcon.setImageResource(R.drawable.volleyball_ball) }
                    Sports.GOLF -> { image.setImageResource(R.drawable.golf_field); sportIcon.setImageResource(R.drawable.golf_ball) }
                }
            }
        }
     }

   @Deprecated("Deprecated in Java")
   override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
       super.onCreateOptionsMenu(menu, inflater)
       inflater.inflate(R.menu.menu_edit_reservation, menu)

       val editMenuItem = menu.findItem(R.id.editReservation)
       val cancelMenuItem = menu.findItem(R.id.cancelReservation)

       // don't allow editing and canceling for past reservations
       if (myReservation.time.isBefore(Instant.now().atZone(myReservation.time.zone))) {
           editMenuItem.isEnabled = false
           editMenuItem.isVisible = false
           cancelMenuItem.isEnabled = false
           cancelMenuItem.isVisible = false
       }
   }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController= view?.findNavController()
        var action=ShowReservationFragmentDirections.actionShowReservationFragmentToCalendarFragment()
        // Handle item selection
        return when (item.itemId) {
            R.id.cancelReservation -> {
                AlertDialog.Builder(context)
                    .setTitle(resources.getString(R.string.delete_reservation_title))
                    .setMessage(resources.getString(R.string.delete_reservation_message)) // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(
                        resources.getString(R.string.delete_reservation_delete_button)
                    ) { _, _ ->
                        val fragmentManager = requireFragmentManager()
                        fragmentManager.popBackStack()
                        // Continue with delete operation
                        reservationsViewModel.deleteReservation(myReservation)
                        navController?.navigate(action)
                    } // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(R.string.delete_reservation_cancel_button, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
                true
            }
            R.id.editReservation -> {
                if (navController != null) {
                    action = ShowReservationFragmentDirections.actionShowReservationFragmentToEditReservationFragment(myReservation.id)
                    navController.navigate(action)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}