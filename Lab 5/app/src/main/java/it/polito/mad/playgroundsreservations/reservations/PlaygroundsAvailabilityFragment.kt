package it.polito.mad.playgroundsreservations.reservations

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.GONE
import androidx.recyclerview.widget.RecyclerView.VISIBLE
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.kizitonwose.calendar.core.Week
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.yearMonth
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekCalendarView
import com.kizitonwose.calendar.view.WeekDayBinder
import com.kizitonwose.calendar.view.WeekHeaderFooterBinder
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.Playground
import it.polito.mad.playgroundsreservations.database.Reservation
import it.polito.mad.playgroundsreservations.database.Sport
import it.polito.mad.playgroundsreservations.profile.SpinnerFragment
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

class PlaygroundsAvailabilityFragment: Fragment(R.layout.fragment_playgrounds_availability), AdapterView.OnItemSelectedListener {

    private val sports = Sport.values()
    private lateinit var weekCalendarView: WeekCalendarView
    private var selectedSport = MutableLiveData(Sport.TENNIS)
    private var selectedDate = MutableLiveData(LocalDate.now())
    private lateinit var reservedPlaygrounds: LiveData<Map<Reservation, Playground>>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel by viewModels<ViewModel>()
        // NEW RESERVATION BUTTON
        val button = view.findViewById<Button>(R.id.reserve_new_playground_button)

        val loading = view.findViewById<FragmentContainerView>(R.id.loadingPlaygroundAvailabilityFragment)
        val fragmentManager = childFragmentManager
        fragmentManager.beginTransaction().replace(R.id.loadingPlaygroundAvailabilityFragment, SpinnerFragment()).commit()
        loading.visibility = VISIBLE

        // ACTIVITY TITLE
        activity?.title = activity?.resources?.getString(R.string.playgrounds_availability)

        // SPINNER
        val spinner = view.findViewById<Spinner>(R.id.spinner)
        activity?.let { activity ->
            ArrayAdapter(
                activity.applicationContext,
                R.layout.spinner_item,
                sports.map {
                    resources.getString(
                        when (it) {
                            Sport.TENNIS -> R.string.sport_tennis
                            Sport.BASKETBALL -> R.string.sport_basketball
                            Sport.FOOTBALL -> R.string.sport_football
                            Sport.VOLLEYBALL -> R.string.sport_volleyball
                            Sport.GOLF -> R.string.sport_golf
                        }
                    )
                }
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
        }
        spinner.onItemSelectedListener = this

        // CALENDAR
        weekCalendarView = view.findViewById(R.id.week_calendar_view)

        val currentDate = LocalDate.now()
        val currentMonth = YearMonth.now()
        val startDate = currentMonth.minusMonths(12).atStartOfMonth()
        val endDate = currentMonth.plusMonths(12).atEndOfMonth()
        val daysOfWeek = daysOfWeek()

        // change month title
        weekCalendarView.weekScrollListener = { newWeek ->
            val monthHeaderTextView = view.findViewById<TextView>(R.id.month_header_text)
            val formatter = DateTimeFormatter.ofPattern("MMM uuuu", Locale.getDefault())
            monthHeaderTextView.text = newWeek.days[0].date.yearMonth.format(formatter)
        }

        // handles the newly selected date
        weekCalendarView.dayBinder = object: WeekDayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, data: WeekDay) {
                container.day = data
                container.textView.text = data.date.dayOfMonth.toString()

                if (data.date == selectedDate.value) {
                    container.textView.setTextColor(Color.WHITE)
                    container.textView.setBackgroundResource(R.drawable.rounded_shape)
                    if (data.date.isBefore(LocalDate.now())){
                        button.visibility = GONE
                    } else {
                        button.visibility = VISIBLE
                    }

                } else {
                    container.textView.setTextColor(Color.BLACK)
                    container.textView.background = null
                }
            }
        }

        // shows the days names
        weekCalendarView.weekHeaderBinder = object: WeekHeaderFooterBinder<WeekViewContainer> {
            override fun create(view: View) = WeekViewContainer(view)
            override fun bind(container: WeekViewContainer, data: Week) {

                // Remember that the header is reused so this will be called for each month.
                // However, the first day of the week will not change so no need to bind
                // the same view every time it is reused.
                if (container.titlesContainer.tag == null) {
                    container.titlesContainer.tag = data.days
                    container.titlesContainer.children.map { it as TextView }
                        .forEachIndexed { index, textView ->
                            val dayOfWeek = daysOfWeek[index]
                            val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                            textView.text = title
                            // In the code above, we use the same `daysOfWeek` list
                            // that was created when we set up the calendar.
                            // However, we can also get the `daysOfWeek` list from the month data:
                            // val daysOfWeek = data.weekDays.first().map { it.date.dayOfWeek }
                            // Alternatively, you can get the value for this specific index:
                            // val dayOfWeek = data.weekDays.first()[index].date.dayOfWeek
                        }
                }
            }
        }

        weekCalendarView.setup(startDate, endDate, daysOfWeek.first())
        weekCalendarView.scrollToWeek(currentDate)

        // LIST OF RESERVED PLAYGROUNDS
        val recyclerView = view.findViewById<RecyclerView>(R.id.reserved_playgrounds)
        recyclerView.layoutManager = LinearLayoutManager(activity?.applicationContext)

        selectedSport.observe(viewLifecycleOwner) { sport ->
            reservedPlaygrounds = viewModel.getReservedPlaygrounds(sport)

            reservedPlaygrounds.observe(viewLifecycleOwner) { reservedPlaygroundsMap ->
                selectedDate.observe(viewLifecycleOwner) { selectedDateValue ->
                    loading.visibility = GONE
                    val displayedReservedPlaygrounds = reservedPlaygroundsMap.filter {
                        it.key.time.toLocalDate() == selectedDateValue
                    }
                    recyclerView.adapter = MyAdapter(displayedReservedPlaygrounds)
                }
            }
        }

        val navController = view.findNavController()
        button.setOnClickListener {
            val action = PlaygroundsAvailabilityFragmentDirections.actionPlaygroundsAvailabilityFragmentToAddReservationFragment(selectedDate.value.toString())
            navController.navigate(action)
        }
    }

    // SPINNER METHODS
    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        // val selected = parent.getItemAtPosition(pos) as String
        selectedSport.postValue(sports[pos])
    }

    override fun onNothingSelected(parent: AdapterView<*>) { }

    // CALENDAR CLASSES
    inner class DayViewContainer(view: View): ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
        lateinit var day: WeekDay

        init {
            view.setOnClickListener {
                // show date clicked
                val currentSelection = selectedDate.value
                selectedDate.postValue(day.date)
                weekCalendarView.notifyDateChanged(day.date)
                if (currentSelection != null) {
                    weekCalendarView.notifyDateChanged(currentSelection)
                }
            }
        }
    }

    class WeekViewContainer(view: View) : ViewContainer(view) {
        // Alternatively, you can add an ID to the container layout and use findViewById()
        val titlesContainer = view as ViewGroup
    }

    // RECYCLER VIEW CLASSES
    class ItemViewHolder(private val view: View): ViewHolder(view) {
        private val titleTextView = view.findViewById<TextView>(R.id.reservation_box_title)
        private val durationTextView = view.findViewById<TextView>(R.id.reservation_box_duration)
        private val playgroundTextView = view.findViewById<TextView>(R.id.reservation_box_playground)

        fun bind(rp: Pair<Reservation, Playground>) {
            titleTextView.text = view.context.getString(
                R.string.reservation_box_title,
                rp.first.time.toLocalTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
            )
            if (rp.first.duration.toHours().toInt() == 1) {
                durationTextView.text = view.context.getString(R.string.reservation_box_duration_single_hour, rp.first.duration.toHours())
            } else if (rp.first.duration.toHours().toInt() == 2 || rp.first.duration.toHours().toInt() == 3) {
                durationTextView.text = view.context.getString(R.string.reservation_box_duration, rp.first.duration.toHours())
            }

            playgroundTextView.text = view.context.getString(R.string.reservation_box_playground_name, rp.second.name)
        }
    }

    class MyAdapter(
        private val reservedPlaygrounds: Map<Reservation, Playground>
        ): RecyclerView.Adapter<ItemViewHolder>() {
        private val reservedPlaygroundsList = reservedPlaygrounds.toList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(viewType, parent, false)
            return ItemViewHolder(v)
        }

        override fun getItemCount(): Int {
            return reservedPlaygrounds.size
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            holder.bind(reservedPlaygroundsList[position])
        }

        override fun getItemViewType(position: Int): Int {
            return R.layout.reservation_box
        }
    }
}