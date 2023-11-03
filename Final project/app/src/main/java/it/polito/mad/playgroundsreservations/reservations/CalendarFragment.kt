package it.polito.mad.playgroundsreservations.reservations

import android.content.Intent
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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
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
import it.polito.mad.playgroundsreservations.Global
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.Playground
import it.polito.mad.playgroundsreservations.database.Reservation
import it.polito.mad.playgroundsreservations.database.Sport
import it.polito.mad.playgroundsreservations.profile.ShowProfileActivity
import it.polito.mad.playgroundsreservations.profile.SpinnerFragment
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Calendar
import java.util.Date
import java.util.Locale

val zoneId: ZoneId = ZoneId.systemDefault()
private var tappedDay = MutableLiveData(Instant.now().atZone(zoneId).toLocalDate())

class CalendarFragment: Fragment(R.layout.calendar_fragment) {
    private lateinit var navController: NavController
    val viewModel by viewModels<ViewModel>()

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

        val reservations = viewModel.userReservations
        val playgrounds = viewModel.playgrounds

        // ACTIVITY TITLE
        activity?.title = activity?.resources?.getString(R.string.my_reservations)

        // TUTORIAL
        val overlay = view.findViewById<ConstraintLayout>(R.id.calendar_fragment_overlay)
        val alreadyShownTutorial = viewModel.tutorialShown

        val loading = view.findViewById<FragmentContainerView>(R.id.loadingCalendarFragment)
        val fragmentManager = childFragmentManager
        fragmentManager.beginTransaction().replace(R.id.loadingCalendarFragment, SpinnerFragment()).commit()
        loading.visibility = VISIBLE

        alreadyShownTutorial.observe(viewLifecycleOwner) {
            if (it == true) {
                overlay.visibility = GONE
            } else {
                overlay.visibility = VISIBLE
                view.findViewById<Button>(R.id.ok_button).setOnClickListener {
                    overlay.visibility = GONE
                    viewModel.tutorialHasBeenShown()
                }
            }
        }

        //Initialize CustomCalendarView from layout
        val calendarView = view.findViewById<View>(R.id.calendar_view) as CustomCalendarView
        //Initialize calendar with date
        val currentCalendar = Calendar.getInstance(Locale.getDefault())
        val decorators: MutableList<DayDecorator> = ArrayList()

        reservations.observe(viewLifecycleOwner) {
            decorators.add(DisabledColorDecorator(it))
            calendarView.decorators = decorators
            calendarView.refreshCalendar(currentCalendar)
            loading.visibility = GONE
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

    override fun onResume() {
        super.onResume()

        // needed check if the user logs out and then presses Back,
        // as the Profile activity was terminated but not this one
        if (Global.userId == null)
            activity?.finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_switch_view_calendar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onPrepareOptionsMenu(menu: Menu) {
        val notificationMenuItem = menu.findItem(R.id.pending_invitations)
        val notificationActionView = notificationMenuItem.actionView
        val notificationCount = notificationActionView?.findViewById<TextView>(R.id.notification_count)

        notificationActionView?.setOnClickListener {
            onOptionsItemSelected(notificationMenuItem)
        }

        viewModel.nPendingInvitations.observe(viewLifecycleOwner) { n ->
            if (n > 0) {
                notificationCount?.visibility = VISIBLE
                notificationCount?.text = n.toString()
            } else {
                notificationCount?.visibility = GONE
            }
        }

        super.onPrepareOptionsMenu(menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = view?.findNavController()

        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.pending_invitations -> {
                val action = CalendarFragmentDirections.actionCalendarFragmentToPendingInvitations()
                navController?.navigate(action)
            }
            R.id.user_profile -> {
                val intent = Intent(activity?.applicationContext, ShowProfileActivity::class.java)
                startActivity(intent)
                activity?.overridePendingTransition(R.anim.fade_in, R.anim.no_anim)
            }
            R.id.switch_calendar -> {
                val action = CalendarFragmentDirections.actionCalendarFragmentToPlaygroundsAvailabilityFragment()
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
        private val sportIcon = view.findViewById<ImageView>(R.id.personal_reservation_box_sport_icon)
        override fun bind(r: Reservation?, pos: Int, onTap: (Int) -> Unit) {
            r!! // abstract class requires nullable reservation, but for this class a reservation has to be passed
            titleTextView.text = view.context.getString(
                R.string.personal_reservation_box_title,
                r.time.toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                r.time.toLocalTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)))

            val sportText = when (r.sport) {
                Sport.TENNIS -> R.string.sport_tennis
                Sport.BASKETBALL -> R.string.sport_basketball
                Sport.FOOTBALL -> R.string.sport_football
                Sport.VOLLEYBALL -> R.string.sport_volleyball
                Sport.GOLF -> R.string.sport_golf
            }

            val sportIconId = when (r.sport) {
                Sport.TENNIS -> R.drawable.tennis_ball
                Sport.BASKETBALL -> R.drawable.basketball_ball
                Sport.FOOTBALL -> R.drawable.football_ball
                Sport.VOLLEYBALL -> R.drawable.volleyball_ball
                Sport.GOLF -> R.drawable.golf_ball
            }

            sportTextView.text = view.context.getString(sportText)
            sportIcon.setImageResource(sportIconId)

            if (r.duration.toHours().toInt() == 1 ){
                durationTextView.text = view.context.getString(R.string.reservation_box_duration_single_hour, r.duration.toHours())
            } else if (r.duration.toHours().toInt() == 2 ||  r.duration.toHours().toInt() == 3){
                durationTextView.text = view.context.getString(R.string.reservation_box_duration, r.duration.toHours())
            }

            playgroundTextView.text = view.context.getString(
                R.string.reservation_box_playground_name,
                playgrounds.find { it.id == r.playgroundId.id }?.name ?: ""
            )

            super.itemView.setOnClickListener { onTap(pos) }
        }

        override fun unbind() {
            super.itemView.setOnClickListener(null)
        }
    }

    class DefaultViewHolder(private val view: View, private val nav: NavController): MyViewHolder(view) {
        override fun bind(r: Reservation?, pos: Int, onTap: (Int) -> Unit) {
            val plusTextViewLayout = view.findViewById<LinearLayout>(R.id.layout_reservation_box_plus)
            val plusTextView = view.findViewById<TextView>(R.id.reservation_box_plus)
            if (tappedDay.value?.isBefore(Instant.now().atZone(zoneId).toLocalDate()) == true) {
                // You can't add a reservation in the past
                plusTextViewLayout.visibility = GONE
                plusTextView.visibility = GONE
            } else {
                plusTextView.setOnClickListener {
                    val action = CalendarFragmentDirections.
                    actionCalendarFragmentToAddReservationFragment(
                        dateOfReservation = tappedDay.value.toString(),
                        fromPlaygroundsAvailability = false
                    )
                    nav.navigate(action)
                }
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
        var previousSport = Sport.BASKETBALL
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
                else if (sortedReservations[r].sport == Sport.TENNIS) {
                    dayView.setBackgroundResource(R.color.TENNIS_COLOR)
                } else if (sortedReservations[r].sport == Sport.BASKETBALL) {
                    dayView.setBackgroundResource(R.color.BASKETBALL_COLOR)
                } else if (sortedReservations[r].sport == Sport.FOOTBALL) {
                    dayView.setBackgroundResource(R.color.FOOTBALL_COLOR)
                } else if (sortedReservations[r].sport == Sport.VOLLEYBALL) {
                    dayView.setBackgroundResource(R.color.VOLLEYBALL_COLOR)
                } else if (sortedReservations[r].sport == Sport.GOLF) {
                    dayView.setBackgroundResource(R.color.GOLF_COLOR)
                }
            }
        }
    }
}