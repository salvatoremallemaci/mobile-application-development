package it.polito.mad.playgroundsreservations.profile

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import it.polito.mad.playgroundsreservations.Global
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.Sport
import it.polito.mad.playgroundsreservations.reservations.ViewModel

class SelectSportsActivity : AppCompatActivity() {
    private lateinit var tennisCb: CheckBox
    private lateinit var basketballCb: CheckBox
    private lateinit var footballCb: CheckBox
    private lateinit var volleyballCb: CheckBox
    private lateinit var golfCb: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_sports)
        this.title = resources?.getString(R.string.my_sports)

        tennisCb = findViewById(R.id.checkBoxTennis)
        basketballCb = findViewById(R.id.checkBoxBasketball)
        footballCb = findViewById(R.id.checkBoxFootball)
        volleyballCb = findViewById(R.id.checkBoxVolleyball)
        golfCb = findViewById(R.id.checkBoxGolf)

        val viewModel by viewModels<ViewModel>()

        val userInfo = viewModel.getUserInfo(Global.userId!!)

        userInfo.observe(this) { user ->
            if (user != null) {
                tennisCb.isChecked = user.selectedSports.contains(Sport.TENNIS) == true
                basketballCb.isChecked = user.selectedSports.contains(Sport.BASKETBALL) == true
                footballCb.isChecked = user.selectedSports.contains(Sport.FOOTBALL) == true
                volleyballCb.isChecked = user.selectedSports.contains(Sport.VOLLEYBALL) == true
                golfCb.isChecked = user.selectedSports.contains(Sport.GOLF) == true

                val saveButton = findViewById<Button>(R.id.saveSportsButton)
                saveButton.setOnClickListener {
                    user.selectedSports = mutableSetOf()

                    if (tennisCb.isChecked)
                        user.selectedSports.add(Sport.TENNIS)
                    if (basketballCb.isChecked)
                        user.selectedSports.add(Sport.BASKETBALL)
                    if (footballCb.isChecked)
                        user.selectedSports.add(Sport.FOOTBALL)
                    if (volleyballCb.isChecked)
                        user.selectedSports.add(Sport.VOLLEYBALL)
                    if (golfCb.isChecked)
                        user.selectedSports.add(Sport.GOLF)

                    viewModel.updateUserInfo(user)

                    finish()
                    overridePendingTransition(R.anim.no_anim, R.anim.fade_out)
                }
            }
        }
    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("super.onBackPressed()", "androidx.appcompat.app.AppCompatActivity")
    )
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.no_anim, R.anim.fade_out)
    }
}