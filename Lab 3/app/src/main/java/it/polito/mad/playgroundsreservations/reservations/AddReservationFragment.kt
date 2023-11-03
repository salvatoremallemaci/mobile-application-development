package it.polito.mad.playgroundsreservations.reservations

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.room.util.foreignKeyCheck
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.Playground
import it.polito.mad.playgroundsreservations.database.Reservation
import it.polito.mad.playgroundsreservations.database.Sports
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

class AddReservationFragment : Fragment(R.layout.add_reservation_fragment) {
    private val args by navArgs<AddReservationFragmentArgs>()
    private val reservationsViewModel by viewModels<ReservationsViewModel>()
    val zoneId = ZoneId.of("UTC+02:00")
    var playgroundList = mutableListOf<Playground>()
    var arrayOccupated = mutableListOf<String>()
    var duration = mutableListOf<String>()
    var aus = 0
    var hours = mutableListOf<String>()
    var myReservation = Reservation(
        userId = 1,
        playgroundId = 0,
        sport = Sports.VOLLEYBALL,
        time = ZonedDateTime.of(2023, 5, 26, 14, 0, 0, 0, zoneId),
        duration = Duration.ofHours(1),
        rentingEquipment = false
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.add_reservation_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reservations = reservationsViewModel.getUserReservations(1)
        val playgrounds = reservationsViewModel.playgrounds

        // ACTIVITY TITLE
        activity?.title = activity?.resources?.getString(R.string.add_reservation)

        playgrounds.observe(viewLifecycleOwner) {
            it.forEach { p ->
                playgroundList.add(p)
            }

            val adapter =
                activity?.let {
                    ArrayAdapter(
                        it.applicationContext,
                        android.R.layout.simple_spinner_dropdown_item,
                        playgroundList.map { p -> p.name }.toTypedArray()
                    )
                }

            val spinner = view.findViewById<Spinner>(R.id.playgroundList)
            spinner.adapter = adapter
            val spinnerHour = view.findViewById<Spinner>(R.id.spinnerDuration)
            val spinnerDuration = view.findViewById<Spinner>(R.id.spinnerDuration2)

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem = parent.getItemAtPosition(position).toString()
                    hours.removeAll(hours)
                    val playgroundId = playgroundList.get(position).id
                    myReservation.playgroundId = playgroundId
                    myReservation.sport = playgroundList.get(position).sport
                    reservations.observe(viewLifecycleOwner) {
                        it.forEach { r ->
                            if (r.playgroundId == playgroundId &&
                                r.time.year == args.dateOfReservation.split("-")[0].toInt() &&
                                r.time.month.value == args.dateOfReservation.split("-")[1].toInt() &&
                                r.time.dayOfMonth == args.dateOfReservation.split("-")[2].toInt()
                            ) {
                                // same day, same field
                                arrayOccupated.add(r.time.hour.toString() + ":00");
                                aus = r.time.hour
                                for (i in 1 until r.duration.toHours()) {
                                    arrayOccupated.add((aus + 1).toString() + ":00")
                                    aus = aus + 1
                                }
                                Log.d("C", arrayOccupated.toString())
                            }
                        }

                        for (hour in 8..24) {
                            hours.add("$hour:00")
                        }
                        hours.removeAll(arrayOccupated);
                        Log.d("listaFinale", hours.toString())

                        val adapterHours =
                            activity?.let {
                                ArrayAdapter(
                                    it.applicationContext,
                                    android.R.layout.simple_spinner_dropdown_item,
                                    hours.toTypedArray()
                                )
                            }
                        spinnerHour.adapter = adapterHours
                        spinnerHour.setSelection(0)

                        val durationAdapter =
                            activity?.let {
                                ArrayAdapter(
                                    it.applicationContext,
                                    android.R.layout.simple_spinner_dropdown_item,
                                    duration
                                )
                            }
                        spinnerDuration.adapter = durationAdapter
                        var i = 0
                        var oraTotale = 0
                        var esci = false;
                        spinnerHour.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    val selectedItem = parent.getItemAtPosition(position).toString()
                                    myReservation.time = ZonedDateTime.of(
                                        args.dateOfReservation.split("-")[0].toInt(),
                                        args.dateOfReservation.split("-")[1].toInt(),
                                        args.dateOfReservation.split("-")[2].toInt(), 14, 0, 0, 0, zoneId)
                                    myReservation.time =
                                        myReservation.time.withHour(selectedItem.split(':')[0].toInt())
                                    oraTotale = selectedItem.split(':')[0].toInt()
                                    Log.d("Ora totale: ", oraTotale.toString());
                                    esci = false
                                    i = 0;
                                    while (i != 4 && oraTotale < 24 && !esci) {
                                        i = i + 1
                                        if (parent.getItemAtPosition((position + i)).toString()
                                                .split(":")[0].toInt() == oraTotale + 1
                                        ) {
                                            oraTotale = oraTotale + 1
                                        } else
                                            esci = true;
                                    }
                                    if (i == 4)
                                        i = 3;
                                    Log.d("BOH", i.toString());
                                    duration.removeAll(duration)
                                    for (j in 1..i)
                                        duration.add(j.toString() + " h")
                                    if (durationAdapter != null) {
                                        durationAdapter.notifyDataSetChanged()
                                    }


                                }

                                override fun onNothingSelected(parent: AdapterView<*>) {

                                }


                            }
                        spinnerDuration.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    val selectedItem = parent.getItemAtPosition(position).toString()
                                    myReservation.duration =
                                        Duration.ofHours(selectedItem.split(' ')[0].toLong())
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {
                                    TODO("Not yet implemented")
                                }

                            }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }


        }

        view.findViewById<TextView>(R.id.viewHour).text = args.dateOfReservation
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_save_edit_reservation, menu)
        super.onCreateOptionsMenu(menu!!, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController= view?.findNavController()
        var action = AddReservationFragmentDirections.actionAddReservationFragmentToCalendarFragment()
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.save_edit_reservation -> {
                // var spinnerHourValue= view?.findViewById<Spinner>(R.id.spinnerViewHours);
                // var spinnerDurationValue=view?.findViewById<Spinner>(R.id.spinnerDuration);
                var chkEquipment=view?.findViewById<CheckBox>(R.id.rentingEquipment);

                if (chkEquipment != null) {
                    myReservation.rentingEquipment=myReservation.rentingEquipment.not()
                }
                Log.d("AO", myReservation.toString())
                reservationsViewModel.save(myReservation)

                navController?.navigate(action)

            }
        }
        return super.onOptionsItemSelected(item)
    }
}