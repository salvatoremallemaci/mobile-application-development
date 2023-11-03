package it.polito.mad.playgroundsreservations.reservations

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import it.polito.mad.playgroundsreservations.R

class NotLoggedReservationsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.not_logged_activity_reservations)

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val fragment = PlaygroundsAvailabilityFragment()
        val tag = "PlaygroundsAvailabilityFragment"
        val containerViewId = R.id.fragmentContainerViewNotLogged
        fragmentTransaction.replace(containerViewId, fragment, tag).commit()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.no_anim, R.anim.fade_out)
    }
}