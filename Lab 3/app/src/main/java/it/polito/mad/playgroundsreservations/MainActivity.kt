package it.polito.mad.playgroundsreservations

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import it.polito.mad.playgroundsreservations.database.Database
import it.polito.mad.playgroundsreservations.profile.ShowProfileActivity
import it.polito.mad.playgroundsreservations.reservations.ReservationsActivity

class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonReservations = findViewById<Button>(R.id.homeButtonReservations)
        buttonReservations.setOnClickListener {
            val intent = Intent(this, ReservationsActivity::class.java)
            startActivity(intent)
        }

        val buttonProfile = findViewById<Button>(R.id.homeButtonProfile)
        buttonProfile.setOnClickListener {
            val intent = Intent(this, ShowProfileActivity::class.java)
            startActivity(intent)
        }
    }
}