package it.polito.mad.playgroundsreservations

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import it.polito.mad.playgroundsreservations.reservations.ReservationsActivity
import it.polito.mad.playgroundsreservations.reservations.ViewModel


class Global {
    companion object {
        var userId: String? = null
        var fullName: String? = null
    }
}

class MainActivity: AppCompatActivity() {
    private val viewModel by viewModels<ViewModel>()

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {   // Successfully signed in
                Global.userId = user.uid
                    Global.fullName = user.displayName
                viewModel.createUserIfNotExists(user.uid, user.displayName)

                val intent = Intent(this, ReservationsActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.no_anim)
                finish()
            }
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            if (response != null)
                Log.d("LOGIN ERROR", response.error.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            Global.userId = user.uid
            Global.fullName = user.displayName

            val intent = Intent(this, ReservationsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.no_anim)
            finish()
        }

        val playgroundsAvailabilityButton = findViewById<Button>(R.id.playgroundsAvailabilityButton)
        playgroundsAvailabilityButton.setOnClickListener {
            val intent = Intent(this, ReservationsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.no_anim)
        }

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            // Choose authentication providers
            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build()
            )

            val customLayout = AuthMethodPickerLayout.Builder(R.layout.login_layout)
                .setEmailButtonId(R.id.loginEmail)
                .setGoogleButtonId(R.id.loginGoogle)
                .build()

            // Create and launch sign-in intent
            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.volleyball_court)
                .setTheme(R.style.Theme_PlaygroundsReservations)
                .setAuthMethodPickerLayout(customLayout)
                .build()

            signInLauncher.launch(signInIntent)
        }
    }
}