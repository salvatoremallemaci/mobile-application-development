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
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.Playground
import it.polito.mad.playgroundsreservations.database.Reservation
import it.polito.mad.playgroundsreservations.database.Sports
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

class EditReservationFragment: Fragment(R.layout.edit_reservation_fragment) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.edit_reservation_fragment, container, false)
    }

    private val args by navArgs<EditReservationFragmentArgs>()
    private val reservationsViewModel by viewModels<ReservationsViewModel>();
    val zoneId = ZoneId.of("UTC+02:00")
    var aus = 0
    var hours = mutableListOf<String>()
    var duration = mutableListOf<String>()

    var myReservation = Reservation(
        userId = 0,
        playgroundId = 0,
        sport = Sports.VOLLEYBALL,
        time = ZonedDateTime.of(2023, 5, 26, 14, 0, 0, 0, zoneId),
        duration = Duration.ofHours(1)
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reservationsViewModel by viewModels<ReservationsViewModel>()
        val reservations = reservationsViewModel.getUserReservations(1)
        val playgrounds = reservationsViewModel.playgrounds

        val arrayOccupated = mutableListOf<String>();

        // ACTIVITY TITLE
        activity?.title = activity?.resources?.getString(R.string.modify_reservation)

        var myPlayground = Playground(
            id = 0,
            name = "temp",
            address = "",
            sport = Sports.VOLLEYBALL
        )

        reservations.observe(viewLifecycleOwner) {
            it.forEach { r ->
                if (r.id == args.resId) {
                    myReservation = r
                }
            }
            it.forEach { r ->
                if (r.time.year == myReservation.time.year && r.time.month == myReservation.time.month && r.time.dayOfMonth == myReservation.time.dayOfMonth && r.id!=myReservation.id) {
                    arrayOccupated.add(r.time.hour.toString() + ":00");
                    aus = r.time.hour
                    for (i in 1 until r.duration.toHours()) {
                        arrayOccupated.add((aus + 1).toString() + ":00")
                        aus = aus + 1
                    }
                    Log.d("C", arrayOccupated.toString())
                }
            }
            Log.d("MY_RES", myReservation.toString())

            playgrounds.observe(viewLifecycleOwner) {
                it.forEach { p ->
                    if (p.id == myReservation.playgroundId) {
                        myPlayground = p
                    }
                }

                for (hour in 8..24) {
                    hours.add("$hour:00")
                }
                hours.removeAll(arrayOccupated);
                Log.d("listaFinale", hours.toString())

                val adapter =
                    activity?.let {
                        ArrayAdapter(
                            it.applicationContext,
                            android.R.layout.simple_spinner_dropdown_item,
                            hours.toTypedArray()
                        )
                    }
                val durationAdapter =
                    activity?.let {
                        ArrayAdapter(
                            it.applicationContext,
                            android.R.layout.simple_spinner_dropdown_item,
                           duration
                        )
                    }
                val sportName = when (myReservation.sport) {
                    Sports.BASKETBALL -> resources.getString(R.string.sport_basketball)
                    Sports.TENNIS -> resources.getString(R.string.sport_tennis)
                    Sports.FOOTBALL -> resources.getString(R.string.sport_football)
                    Sports.VOLLEYBALL -> resources.getString(R.string.sport_volleyball)
                    Sports.GOLF -> resources.getString(R.string.sport_golf)
                }

                val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
                ratingBar.setIsIndicator(true)

                val rating = reservationsViewModel.getPlaygroundAverageRating(myPlayground.id)
                rating.observe(viewLifecycleOwner) {
                    ratingBar.rating = it.toFloat()
                }

                view.findViewById<TextView>(R.id.sportName).text = sportName
                view.findViewById<TextView>(R.id.playgroundName).text = myPlayground.name
                view.findViewById<CheckBox>(R.id.rentingEquipment).isChecked =
                    myReservation.rentingEquipment
                view.findViewById<CheckBox>(R.id.rentingEquipment).isChecked = myReservation.rentingEquipment

                val image = view.findViewById<ImageView>(R.id.reservationImage)
                val sportIcon = view.findViewById<ImageView>(R.id.sportNameIcon)

                if (myReservation.sport == Sports.TENNIS) {
                    image.setImageResource(R.drawable.tennis_court)
                    sportIcon.setImageResource(R.drawable.tennis_ball)
                } else if (myReservation.sport == Sports.FOOTBALL){
                    image.setImageResource(R.drawable.football_pitch)
                    sportIcon.setImageResource(R.drawable.football_ball)
                } else if (myReservation.sport == Sports.GOLF){
                    image.setImageResource(R.drawable.golf_field)
                    sportIcon.setImageResource(R.drawable.golf_ball)
                } else if (myReservation.sport == Sports.VOLLEYBALL) {
                    image.setImageResource(R.drawable.volleyball_court)
                    sportIcon.setImageResource(R.drawable.volleyball_ball)
                } else if (myReservation.sport == Sports.BASKETBALL) {
                    image.setImageResource(R.drawable.basketball_court)
                    sportIcon.setImageResource(R.drawable.basketball_ball)
                }

                val spinner = view.findViewById<Spinner>(R.id.spinnerViewHours)
                spinner.adapter = adapter
                spinner.setSelection(0)
                for(j in 0..hours.size)
                {
                    if (hours[j].split(":")[0].toString()==myReservation.time.hour.toString())
                    {
                        spinner.setSelection((j))
                        break
                    }
                }

                val spinnerDuration=view.findViewById<Spinner>(R.id.spinnerDuration)
                var selId=0;
                for(j in 1 .. myReservation.duration.toHours().toInt())
                    duration.add(j.toString()+" h")

                if(myReservation.duration.toHours().toInt()==1)
                    selId=0;
                else if (myReservation.duration.toHours().toInt()==2)
                    selId=1;
                else
                    selId=2;

                spinnerDuration.adapter=durationAdapter
                spinnerDuration.setSelection(selId)
                var a=duration.size

                var i=0
                var oraTotale=0
                var esci=false;
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedItem = parent.getItemAtPosition(position).toString()
                        myReservation.time = myReservation.time.withHour(selectedItem.split(':')[0].toInt())
                        Log.d("AAA", selectedItem.toString())
                        Log.d("BBB", myReservation.time.toString())
                        oraTotale=selectedItem.split(':')[0].toInt()
                        Log.d("Ora totale: ",oraTotale.toString());
                        esci=false
                        i=0;
                        while (i!=4 && oraTotale<24 && !esci)
                        {
                            i=i+1
                            if(parent.getItemAtPosition((position+i)).toString().split(":")[0].toInt()==oraTotale+1)
                            {
                                oraTotale=oraTotale+1
                            }
                            else
                                esci=true;
                        }
                        if(i==4)
                            i=3;
                        Log.d("BOH",i.toString());
                        duration.removeAll(duration)
                        for(j in 1 .. i)
                            duration.add(j.toString()+" h")
                        if (durationAdapter != null) {
                            durationAdapter.notifyDataSetChanged()
                        }




                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {

                    }
                }

                spinnerDuration.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedItem = parent.getItemAtPosition(position).toString()
                        myReservation.duration = Duration.ofHours(selectedItem.split(' ')[0].toLong())
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        TODO("Not yet implemented")
                    }

                }


            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_save_edit_reservation, menu)
        super.onCreateOptionsMenu(menu!!, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController= view?.findNavController()
        var action=EditReservationFragmentDirections.actionEditReservationFragmentToShowReservationFragment(myReservation.id)
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.save_edit_reservation -> {
                // var spinnerHourValue= view?.findViewById<Spinner>(R.id.spinnerViewHours);
                // var spinnerDurationValue=view?.findViewById<Spinner>(R.id.spinnerDuration);
                var chkEquipment=view?.findViewById<CheckBox>(R.id.rentingEquipment);

                if (chkEquipment != null) {
                    myReservation.rentingEquipment=chkEquipment.isChecked
                }

                reservationsViewModel.updateReservation(myReservation)

               // navController?.navigate(action)
                requireActivity().onBackPressed() // Chiudi il fragment attuale
                true

            }
            }
            return super.onOptionsItemSelected(item)
    }
}