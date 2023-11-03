package it.polito.mad.playgroundsreservations.reservations

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.reservations.favorite_playgrounds.ChoosePlaygroundFromProfile

class MyCourtsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_courts_activity)

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val fragment = ChoosePlaygroundFromProfile()
        val tag = "FavoritePlaygrounds"
        val containerViewId = R.id.fragmentContainerViewMyCourts
        fragmentTransaction.replace(containerViewId, fragment, tag).commit()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.no_anim, R.anim.fade_out)
    }
}