package it.polito.mad.playgroundsreservations.reservations

import android.os.Bundle
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
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import it.polito.mad.playgroundsreservations.Global
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.Playground
import it.polito.mad.playgroundsreservations.database.Reservation
import it.polito.mad.playgroundsreservations.database.Sport
import it.polito.mad.playgroundsreservations.profile.SpinnerFragment
import java.time.Duration

class EditReservationFragment : Fragment(R.layout.edit_reservation_fragment) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.edit_reservation_fragment, container, false)
    }

    private val args by navArgs<EditReservationFragmentArgs>()
    private val viewModel by viewModels<ViewModel>()
    private var aus = 0
    private var hours = mutableListOf<String>()
    var durationsList = mutableListOf<String>()

    lateinit var myReservation: Reservation
    private lateinit var myPlayground: Playground

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel by viewModels<ViewModel>()
        val reservations = viewModel.getUserReservations(Global.userId!!)
        val playgrounds = viewModel.playgrounds
        val arrayOccupated = mutableListOf<String>()

        val loading = view.findViewById<FragmentContainerView>(R.id.loadingEditReservationFragment)
        val fragmentManager = childFragmentManager
        fragmentManager.beginTransaction().replace(R.id.loadingEditReservationFragment, SpinnerFragment()).commit()
        loading.visibility = View.VISIBLE

        // ACTIVITY TITLE
        activity?.title = activity?.resources?.getString(R.string.modify_reservation)

        reservations.observe(viewLifecycleOwner) {
            it.forEach { r ->
                if (r.id == args.resId) {
                    myReservation = r
                }
            }
            it.forEach { r ->
                if (r.time.year == myReservation.time.year && r.time.month == myReservation.time.month && r.time.dayOfMonth == myReservation.time.dayOfMonth && r.id != myReservation.id) {
                    arrayOccupated.add(r.time.hour.toString() + ":00")
                    aus = r.time.hour
                    for (i in 1 until r.duration.toHours()) {
                        arrayOccupated.add((aus + 1).toString() + ":00")
                        aus += 1
                    }
                }
            }

            playgrounds.observe(viewLifecycleOwner) {
                it.forEach { p ->
                    if (p.id == myReservation.playgroundId.id) {
                        myPlayground = p
                        loading.visibility = View.GONE
                    }
                }

                for (hour in 8..24) {
                    hours.add("$hour:00")
                }
                hours.removeAll(arrayOccupated)

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
                            durationsList
                        )
                    }

                val sportName = when (myReservation.sport) {
                    Sport.BASKETBALL -> resources.getString(R.string.sport_basketball)
                    Sport.TENNIS -> resources.getString(R.string.sport_tennis)
                    Sport.FOOTBALL -> resources.getString(R.string.sport_football)
                    Sport.VOLLEYBALL -> resources.getString(R.string.sport_volleyball)
                    Sport.GOLF -> resources.getString(R.string.sport_golf)
                }

                val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
                ratingBar.setIsIndicator(true)

                var totalRating = 0.0f
                val ratingBarValue = viewModel.getRatingsByPlaygroundIdFragment(myPlayground.id)
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

                view.findViewById<TextView>(R.id.sportName).text = sportName
                view.findViewById<TextView>(R.id.playgroundName).text = myPlayground.name
                view.findViewById<CheckBox>(R.id.rentingEquipment).isChecked =
                    myReservation.rentingEquipment
                view.findViewById<CheckBox>(R.id.rentingEquipment).isChecked =
                    myReservation.rentingEquipment

                val image = view.findViewById<ImageView>(R.id.reservationImage)
                val sportIcon = view.findViewById<ImageView>(R.id.sportNameIcon)

                when (myReservation.sport) {
                    Sport.TENNIS -> {
                        image.setImageResource(R.drawable.tennis_court)
                        sportIcon.setImageResource(R.drawable.tennis_ball)
                    }
                    Sport.BASKETBALL -> {
                        image.setImageResource(R.drawable.basketball_court)
                        sportIcon.setImageResource(R.drawable.basketball_ball)
                    }
                    Sport.FOOTBALL -> {
                        image.setImageResource(R.drawable.football_pitch)
                        sportIcon.setImageResource(R.drawable.football_ball)
                    }
                    Sport.VOLLEYBALL -> {
                        image.setImageResource(R.drawable.volleyball_court)
                        sportIcon.setImageResource(R.drawable.volleyball_ball)
                    }
                    Sport.GOLF -> {
                        image.setImageResource(R.drawable.golf_field)
                        sportIcon.setImageResource(R.drawable.golf_ball)
                    }
                }

                val spinner = view.findViewById<Spinner>(R.id.spinnerViewHours)
                spinner.adapter = adapter
                spinner.setSelection(0)
                for (j in 0..hours.size) {
                    if (hours[j].split(":")[0] == myReservation.time.hour.toString()) {
                        spinner.setSelection((j))
                        break
                    }
                }

                val spinnerDuration = view.findViewById<Spinner>(R.id.spinnerDuration)
                for (j in 1..myReservation.duration.toHours().toInt())
                    durationsList.add("$j h")

                val selId: Int = if (myReservation.duration.toHours().toInt() == 1) 0
                else if (myReservation.duration.toHours().toInt() == 2) 1
                else 2

                spinnerDuration.adapter = durationAdapter
                spinnerDuration.setSelection(selId)

                var i: Int
                var oraTotale: Int
                var esci: Boolean
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedItem = parent.getItemAtPosition(position).toString()
                        myReservation.time =
                            myReservation.time.withHour(selectedItem.split(':')[0].toInt())

                        oraTotale = selectedItem.split(':')[0].toInt()
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
                            myReservation.duration =
                                Duration.ofHours(selectedItem.split(' ')[0].toLong())
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
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = view?.findNavController()

        when (item.itemId) {
            R.id.save_edit_reservation -> {
                val chkEquipment = view?.findViewById<CheckBox>(R.id.rentingEquipment)

                if (chkEquipment != null) {
                    myReservation.rentingEquipment = chkEquipment.isChecked
                }

                viewModel.updateReservation(myReservation)

                navController?.popBackStack()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}