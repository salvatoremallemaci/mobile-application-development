package it.polito.mad.playgroundsreservations.reservations

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.reservations.invite_friends.InviteFriendsFromProfile

class YourFriendsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.your_friends_activity)

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val fragment = InviteFriendsFromProfile()
        val tag = "YourFriendsFragment"
        val containerViewId = R.id.fragmentContainerViewYourFriends
        fragmentTransaction.replace(containerViewId, fragment, tag).commit()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.no_anim, R.anim.fade_out)
    }
}