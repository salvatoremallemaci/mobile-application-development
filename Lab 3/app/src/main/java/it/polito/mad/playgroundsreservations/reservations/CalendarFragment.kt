package it.polito.mad.playgroundsreservations.reservations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stacktips.view.CalendarListener
import com.stacktips.view.CustomCalendarView
import com.stacktips.view.DayDecorator
import com.stacktips.view.DayView
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.Playground
import it.polito.mad.playgroundsreservations.database.Reservation
import it.polito.mad.playgroundsreservations.database.Sports
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Calendar
import java.util.Date
import java.util.Locale


private val zoneId = ZoneId.systemDefault()
private var tappedDay = MutableLiveData(Instant.now().atZone(zoneId).toLocalDate())

class CalendarFragment: Fragment(R.layout.calendar_fragment) {

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.calendar_fragment, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = view.findNavController()

        val reservationsViewModel by viewModels<ReservationsViewModel>()
        val reservations = reservationsViewModel.getUserReservations(1)
        val playgrounds = reservationsViewModel.playgrounds

        // ACTIVITY TITLE
        activity?.title = activity?.resources?.getString(R.string.my_reservations)

        //Initialize CustomCalendarView from layout
        val calendarView = view.findViewById<View>(R.id.calendar_view) as CustomCalendarView
        //Initialize calendar with date
        val currentCalendar = Calendar.getInstance(Locale.getDefault())
        val decorators: MutableList<DayDecorator> = ArrayList()

        reservations.observe(viewLifecycleOwner) {
            decorators.add(DisabledColorDecorator(it))
            calendarView.decorators = decorators
            calendarView.refreshCalendar(currentCalendar)
        }

        //Show Monday as first date of week
        calendarView.firstDayOfWeek = Calendar.MONDAY

        //Handling custom calendar events
        calendarView.setCalendarListener(object : CalendarListener {
            override fun onDateSelected(date: Date?) {
                if (date != null)
                    tappedDay.postValue(date.toInstant().atZone(zoneId).toLocalDate())
            }

            override fun onMonthChanged(date: Date?) { }
        })

        val recyclerView = view.findViewById<RecyclerView>(R.id.listOfReservations)

        tappedDay.observe(viewLifecycleOwner) { tappedDayDate ->
            reservations.observe(viewLifecycleOwner) {
                val displayedReservations = it.filter { r ->
                    val reservationLocalDate = r.time.withZoneSameInstant(zoneId).toLocalDate()
                    reservationLocalDate.isEqual(tappedDayDate)
                }
                playgrounds.observe(viewLifecycleOwner) {
                    recyclerView.adapter = MyAdapter(displayedReservations, it, navController)
                }
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(activity?.applicationContext)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_switch_view_calendar, menu)
        super.onCreateOptionsMenu(menu!!, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController= view?.findNavController()
        var action = CalendarFragmentDirections.actionCalendarFragmentToPlaygroundsAvailabilityFragment()
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.switch_calendar -> {
                navController?.navigate(action)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    abstract class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        abstract fun bind(r: Reservation?, pos: Int, onTap: (Int) -> Unit)
        abstract fun unbind()
    }

    class ItemViewHolder(private val view: View, private val playgrounds: List<Playground>): MyViewHolder(view) {
        private val titleTextView = view.findViewById<TextView>(R.id.personal_reservation_box_title)
        private val sportTextView = view.findViewById<TextView>(R.id.personal_reservation_box_sport)
        private val durationTextView = view.findViewById<TextView>(R.id.personal_reservation_box_duration)
        private val playgroundTextView = view.findViewById<TextView>(R.id.personal_reservation_box_playground)

        override fun bind(r: Reservation?, pos: Int, onTap: (Int) -> Unit) {
            r!! // abstract class requires nullable reservation, but for this class a reservation has to be passed
            titleTextView.text = view.context.getString(
                R.string.personal_reservation_box_title,
                r.time.toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                r.time.toLocalTime())

            val sportText = when (r.sport) {
                Sports.TENNIS -> R.string.sport_tennis
                Sports.BASKETBALL -> R.string.sport_basketball
                Sports.FOOTBALL -> R.string.sport_football
                Sports.VOLLEYBALL -> R.string.sport_volleyball
                Sports.GOLF -> R.string.sport_golf
            }
            sportTextView.text = view.context.getString(sportText)

            durationTextView.text = view.context.getString(R.string.reservation_box_duration, r.duration.toHours())
            playgroundTextView.text = view.context.getString(
                R.string.reservation_box_playground_name,
                playgrounds.find { it.id == r.playgroundId }?.name ?: ""
            )

            super.itemView.setOnClickListener { onTap(pos) }
        }

        override fun unbind() {
            super.itemView.setOnClickListener(null)
        }
    }

    class DefaultViewHolder(private val view: View, val nav: NavController): MyViewHolder(view) {
        override fun bind(r: Reservation?, pos: Int, onTap: (Int) -> Unit) {
            val plusTextView = view.findViewById<TextView>(R.id.reservation_box_plus)
            plusTextView.setOnClickListener {
                // TODO navigate
                val action = CalendarFragmentDirections.actionCalendarFragmentToAddReservationFragment(
                    tappedDay.value.toString())
                nav.navigate(action)
            }
        }

        override fun unbind() {
            super.itemView.setOnClickListener(null)
        }

    }

    class MyAdapter(
        private val reservations: List<Reservation>,
        private val playgrounds: List<Playground>,
        private val navController: NavController
        ): RecyclerView.Adapter<MyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(viewType, parent, false)
            return if (viewType == R.layout.personal_reservation_add_box) DefaultViewHolder(v, navController) else ItemViewHolder(v, playgrounds)
        }

        override fun getItemCount(): Int {
            return reservations.size + 1
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            if (position == reservations.size)
                holder.bind(null, position) { }   // passes values that will not be used
            else holder.bind(reservations[position], position) {
                val action = CalendarFragmentDirections.actionCalendarFragmentToShowReservationFragment(reservations[position].id)
                navController.navigate(action)
            }
        }

        override fun onViewRecycled(holder: MyViewHolder) {
            holder.unbind()
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == reservations.size) R.layout.personal_reservation_add_box else R.layout.personal_reservation_box
        }
    }
}

private class DisabledColorDecorator(val reservations: List<Reservation>) : DayDecorator {
    private val zoneId = ZoneId.systemDefault()

    override fun decorate(dayView: DayView) {
        var occurrences = 0
        var previousSport = Sports.BASKETBALL
        val sortedReservations = reservations.sortedBy { it.time } // sorted by day of reservation

        for (r in sortedReservations.indices) { // index iteration, so I can have access to previous index
            val reservationDate = sortedReservations[r].time.withZoneSameInstant(zoneId).toLocalDate()
            val dayViewDate = dayView.date.toInstant().atZone(zoneId).toLocalDate()

            if (r>0) // only after first iteration
                previousSport = sortedReservations[r-1].sport

            if(reservationDate.isEqual(dayViewDate)) {
                occurrences++
                if (occurrences > 1) {
                    if (sortedReservations[r].sport != previousSport)
                        dayView.setBackgroundResource(R.color.MULTIPLE_COLOR)
                    occurrences--
                }
                else if (sortedReservations[r].sport == Sports.TENNIS) {
                    dayView.setBackgroundResource(R.color.TENNIS_COLOR)
                } else if (sortedReservations[r].sport == Sports.BASKETBALL) {
                    dayView.setBackgroundResource(R.color.BASKETBALL_COLOR)
                } else if (sortedReservations[r].sport == Sports.FOOTBALL) {
                    dayView.setBackgroundResource(R.color.FOOTBALL_COLOR)
                } else if (sortedReservations[r].sport == Sports.VOLLEYBALL) {
                    dayView.setBackgroundResource(R.color.VOLLEYBALL_COLOR)
                } else if (sortedReservations[r].sport == Sports.GOLF) {
                    dayView.setBackgroundResource(R.color.GOLF_COLOR)
                }
            }
        }
    }
}
