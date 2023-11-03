package it.polito.mad.playgroundsreservations.reservations

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.Playground
import it.polito.mad.playgroundsreservations.database.Reservation
import it.polito.mad.playgroundsreservations.database.Sports
import org.w3c.dom.Text
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime


class ShowReservationFragment: Fragment(R.layout.show_reservation_fragment) {
    private val args by navArgs<ShowReservationFragmentArgs>()
    private val reservationsViewModel by viewModels<ReservationsViewModel>();
    val zoneId = ZoneId.of("UTC+02:00")
    var myReservation = Reservation(
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

        reservations.observe(viewLifecycleOwner) {
                it.forEach { r ->
                    if (r.id == args.reservationId) {
                        myReservation = r
                    }
                }
            Log.d("MY_RES", myReservation.toString())

            playgrounds.observe(viewLifecycleOwner) {
                it.forEach { p ->
                    if (p.id == myReservation.playgroundId) {
                         myPlayground = p
                    }
                }
                val sportName = myReservation.sport.toString().replaceFirstChar { c -> c.uppercase() }
                view.findViewById<TextView>(R.id.playgroundName).text = myPlayground.name
                view.findViewById<TextView>(R.id.sportName).text = sportName
                view.findViewById<TextView>(R.id.timeInfo).text =
                    myReservation.time.year.toString() + " " +
                            myReservation.time.month.toString() + " " +
                            myReservation.time.dayOfMonth.toString()

                view.findViewById<TextView>(R.id.durationInfo).text = resources.getString(R.string.duration) + ": "+ myReservation.duration.toHours().toString() + "h"

                view.findViewById<CheckBox>(R.id.rentingEquipment).isChecked = myReservation.rentingEquipment
                val image = view.findViewById<ImageView>(R.id.reservationImage)
                if (myReservation.sport == Sports.TENNIS) {
                    image.setImageResource(R.drawable.tennis_court)
                } else if (myReservation.sport == Sports.FOOTBALL){
                    image.setImageResource(R.drawable.football_pitch)
                } else if (myReservation.sport == Sports.GOLF){
                    image.setImageResource(R.drawable.golf_field)
                } else if (myReservation.sport == Sports.VOLLEYBALL) {
                    image.setImageResource(R.drawable.volleyball_court)
                } else if (myReservation.sport == Sports.BASKETBALL) {
                    image.setImageResource(R.drawable.basketball_court)
                }
            }
        }
     }
   override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
       inflater.inflate(R.menu.menu_edit_reservation, menu)
       super.onCreateOptionsMenu(menu!!, inflater)
   }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController= view?.findNavController()
        var action=ShowReservationFragmentDirections.actionShowReservationFragmentToCalendarFragment()
        // Handle item selection
        return when (item.itemId) {
            R.id.cancelReservation -> {
                AlertDialog.Builder(context)
                    .setTitle("Cancellazione prenotazione")
                    .setMessage("Sei sicuro di voler cancellare la prenotazione?") // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(
                        "cancella"
                    ) { dialog, which ->
                        val fragmentManager = requireFragmentManager()
                        fragmentManager.popBackStack()
                        // Continue with delete operation
                        reservationsViewModel.delete(myReservation)
                        if (navController != null) {
                            navController.navigate(action)
                        }



                    } // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton("annulla", null)
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