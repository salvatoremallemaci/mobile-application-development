package it.polito.mad.playgroundsreservations.reservations

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import it.polito.mad.playgroundsreservations.Global
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.Playground
import it.polito.mad.playgroundsreservations.database.PlaygroundRating
import it.polito.mad.playgroundsreservations.database.Reservation
import it.polito.mad.playgroundsreservations.database.Sport
import it.polito.mad.playgroundsreservations.profile.SpinnerFragment
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Date

class AddReservationFragment: Fragment(R.layout.add_reservation_fragment) {

    private val args by navArgs<AddReservationFragmentArgs>()
    private val viewModel by viewModels<ViewModel>()
    val zoneId: ZoneId = ZoneId.of("UTC+02:00")
    var playgroundList = mutableListOf<Playground>()
    var arrayOccupated = mutableListOf<String>()
    var durationsList = mutableListOf<String>()
    var aus = 0
    var hours = mutableListOf<String>()

    object MyReservation {
        var playgroundId = ""
        var sport = Sport.VOLLEYBALL
        var time: ZonedDateTime = ZonedDateTime.of(2023, 5, 26, 14, 0, 0, 0, zoneId)
        var duration: Duration = Duration.ofHours(1)
        var rentingEquipment = false
    }

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

        val reservations = viewModel.getUserReservations(Global.userId!!)
        val playgrounds = viewModel.playgrounds

        val loading = view.findViewById<FragmentContainerView>(R.id.loadingAddReservationFragment)
        val fragmentManager = childFragmentManager
        fragmentManager.beginTransaction().replace(R.id.loadingAddReservationFragment, SpinnerFragment()).commit()
        loading.visibility = VISIBLE

        playgroundList.removeAll(playgroundList)
        val sharedPreferences = requireContext().getSharedPreferences("AddPref", Context.MODE_PRIVATE)
        val navController = view.findNavController()
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


            val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
            ratingBar.setIsIndicator(true)

            val seeRatingButton = view.findViewById<Button>(R.id.btnSeeRatings)
            val noRatingsTextView = view.findViewById<TextView>(R.id.noRatingsTextView)

            val image = view.findViewById<ImageView>(R.id.reservationImage)
            val sportIcon = view.findViewById<ImageView>(R.id.sportNameIcon)
            val sportName = view.findViewById<TextView>(R.id.sportName)

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
                    hours.removeAll(hours)
                    val playground = playgroundList[position]
                    MyReservation.playgroundId = playground.id
                    MyReservation.sport = playgroundList[position].sport

                    val editor = sharedPreferences.edit()
                    editor.putString("playgroundSelected", playground.id)
                    editor.apply()
                    // MyReservation.playgroundId = sharedPreferences.getString("playgroundSelected", "").toString()


                    var totalRating = 0.0f
                    val ratingBarValue = viewModel.getRatingsByPlaygroundIdFragment(playground.id)
                    ratingBarValue.observe(viewLifecycleOwner) { ratingPlaygroundsList ->
                        if (ratingPlaygroundsList.isEmpty()){
                            ratingBar.rating = (0.0).toFloat()
                        } else {
                            ratingPlaygroundsList.forEach { r ->
                                totalRating += r?.rating!!
                            }
                            ratingBar.rating = totalRating / ratingPlaygroundsList.size
                        }
                    }

                    when (MyReservation.sport) {
                        Sport.TENNIS ->  {
                            image.setImageResource(R.drawable.tennis_court)
                            sportIcon.setImageResource(R.drawable.tennis_ball)
                            sportName.setText(R.string.sport_tennis)
                        }
                        Sport.FOOTBALL -> {
                            image.setImageResource(R.drawable.football_pitch)
                            sportIcon.setImageResource(R.drawable.football_ball)
                            sportName.setText(R.string.sport_football)
                        }
                        Sport.GOLF -> {
                            image.setImageResource(R.drawable.golf_field)
                            sportIcon.setImageResource(R.drawable.golf_ball)
                            sportName.setText(R.string.sport_golf)
                        }
                        Sport.VOLLEYBALL -> {
                            image.setImageResource(R.drawable.volleyball_court)
                            sportIcon.setImageResource(R.drawable.volleyball_ball)
                            sportName.setText(R.string.sport_volleyball)
                        }
                        Sport.BASKETBALL -> {
                            image.setImageResource(R.drawable.basketball_court)
                            sportIcon.setImageResource(R.drawable.basketball_ball)
                            sportName.setText(R.string.sport_basketball)
                        }
                    }

                    reservations.observe(viewLifecycleOwner) {
                        arrayOccupated.removeAll(arrayOccupated)
                        val formatter = SimpleDateFormat("yyyy-MM-dd")
                        val formatterDate=formatter.format(Date.from(Instant.now()))
                        if (args.dateOfReservation==formatterDate) {
                           val ora= Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                            for(i in 8..ora) {
                                    arrayOccupated.add(i.toString()+":00")
                            }
                        }
                        it.forEach { r ->
                            if (r.playgroundId.path.split('/')[1] == playground.id &&
                                r.time.year == args.dateOfReservation.split("-")[0].toInt() &&
                                r.time.month.value == args.dateOfReservation.split("-")[1].toInt() &&
                                r.time.dayOfMonth == args.dateOfReservation.split("-")[2].toInt()
                            ) {
                                // same day, same field
                                arrayOccupated.add(r.time.hour.toString() + ":00")
                                aus = r.time.hour
                                for (i in 1 until r.duration.toHours()) {
                                    arrayOccupated.add((aus + 1).toString() + ":00")
                                    aus += 1
                                }
                                Log.d("C", arrayOccupated.toString())
                            }
                        }

                        for (hour in 8..24) {
                            hours.add("$hour:00")
                        }
                        hours.removeAll(arrayOccupated)
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
                                    durationsList
                                )
                            }
                        spinnerDuration.adapter = durationAdapter
                        var i: Int
                        var oraTotale: Int
                        var esci: Boolean
                        spinnerHour.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    val selectedItem = parent.getItemAtPosition(position).toString()
                                    MyReservation.time = ZonedDateTime.of(
                                        args.dateOfReservation.split("-")[0].toInt(),
                                        args.dateOfReservation.split("-")[1].toInt(),
                                        args.dateOfReservation.split("-")[2].toInt(), 14, 0, 0, 0, zoneId)
                                    MyReservation.time =
                                        MyReservation.time.withHour(selectedItem.split(':')[0].toInt())
                                    oraTotale = selectedItem.split(':')[0].toInt()
                                    Log.d("Ora totale: ", oraTotale.toString())
                                    esci = false
                                    i = 0
                                    while (i != 4 && oraTotale < 24 && !esci) {
                                        i += 1
                                        if (parent.getItemAtPosition((position + i)).toString()
                                                .split(":")[0].toInt() == oraTotale + 1
                                        ) {
                                            oraTotale += 1
                                        } else
                                            esci = true
                                    }
                                    if (i == 4)
                                        i = 3
                                    durationsList.removeAll(durationsList)
                                    for (j in 1..i)
                                        durationsList.add("$j h")
                                    durationAdapter?.notifyDataSetChanged()
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
                                    MyReservation.duration =
                                        Duration.ofHours(selectedItem.split(' ')[0].toLong())
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {
                                    TODO("Not yet implemented")
                                }

                            }
                    }

                    ratingBarValue.observe(viewLifecycleOwner) { ratingPlaygroundsList ->
                        loading.visibility = GONE
                        val ratingSinglePlaygroundList = mutableListOf<PlaygroundRating>()
                        ratingPlaygroundsList.forEach { r ->
                            if (r?.playgroundId?.id == playground.id) {
                                ratingSinglePlaygroundList.add(r)
                            }
                        }
                        if (ratingSinglePlaygroundList.isEmpty()){
                            seeRatingButton.visibility = GONE
                            noRatingsTextView.visibility = VISIBLE
                        } else {
                            noRatingsTextView.visibility = GONE
                            seeRatingButton.visibility = VISIBLE
                            seeRatingButton.setOnClickListener {
                                val action = AddReservationFragmentDirections.actionAddReservationFragmentToSeeRatings(playground.id)
                                navController.navigate(action)
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
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController= view?.findNavController()
        val action = AddReservationFragmentDirections.actionAddReservationFragmentToCalendarFragment()
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.save_edit_reservation -> {
                // var spinnerHourValue= view?.findViewById<Spinner>(R.id.spinnerViewHours);
                // var spinnerDurationValue=view?.findViewById<Spinner>(R.id.spinnerDuration);
                val chkEquipment=view?.findViewById<CheckBox>(R.id.rentingEquipment)

                if (chkEquipment != null) {
                    MyReservation.rentingEquipment=chkEquipment.isChecked
                }

                val newReservation = Reservation(
                    "",
                    viewModel.getUserReference(Global.userId!!),
                    viewModel.getPlaygroundReference(MyReservation.playgroundId),
                    MyReservation.sport,
                    MyReservation.time,
                    MyReservation.duration
                )

                viewModel.saveReservation(newReservation)

                navController?.navigate(action)

            }
        }
        return super.onOptionsItemSelected(item)
    }
}