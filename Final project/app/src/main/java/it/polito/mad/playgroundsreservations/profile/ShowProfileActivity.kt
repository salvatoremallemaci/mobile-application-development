package it.polito.mad.playgroundsreservations.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import it.polito.mad.playgroundsreservations.Global
import it.polito.mad.playgroundsreservations.MainActivity
import it.polito.mad.playgroundsreservations.R
import it.polito.mad.playgroundsreservations.database.Gender
import it.polito.mad.playgroundsreservations.database.Sport
import it.polito.mad.playgroundsreservations.reservations.MyCourtsActivity
import it.polito.mad.playgroundsreservations.reservations.ViewModel
import it.polito.mad.playgroundsreservations.reservations.YourFriendsActivity
import java.io.InputStream

class ShowProfileActivity: AppCompatActivity() {
    private lateinit var nameView: TextView
    private lateinit var ageView: TextView
    private lateinit var bioView: TextView
    private lateinit var genderView: TextView
    private lateinit var phoneView: TextView
    private lateinit var locationView: TextView
    private lateinit var ratingBarView: RatingBar
    private lateinit var userProfileImageView: ImageView

    private lateinit var basketballRow: TableRow
    private lateinit var volleyballRow: TableRow
    private lateinit var tennisRow: TableRow
    private lateinit var golfRow: TableRow
    private lateinit var footballRow: TableRow

    private lateinit var basketballRatingBar: RatingBar
    private lateinit var volleyballRatingBar: RatingBar
    private lateinit var tennisRatingBar: RatingBar
    private lateinit var golfRatingBar: RatingBar
    private lateinit var footballRatingBar: RatingBar

    private lateinit var logoutButton: Button
    private lateinit var favoriteCourtsButton: Button
    private lateinit var yourFriendsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

        logoutButton = findViewById(R.id.logout_button)
        favoriteCourtsButton = findViewById(R.id.favoriteCourtsButton)
        yourFriendsButton = findViewById(R.id.friendsListButton)

        if (intent.getStringExtra("reservationCreatorId") != null) {
            this.title = resources?.getString(R.string.reservation_creator_profile)
            favoriteCourtsButton.visibility = GONE
            yourFriendsButton.visibility = GONE
            logoutButton.visibility = GONE
        } else if (intent.getStringExtra("friendId") != null) {
            this.title = resources?.getString(R.string.contact_profile)
            favoriteCourtsButton.visibility = GONE
            yourFriendsButton.visibility = GONE
            logoutButton.visibility = GONE
        } else {
            this.title = resources?.getString(R.string.user_profile)
            favoriteCourtsButton.visibility = VISIBLE
            yourFriendsButton.visibility = VISIBLE
            logoutButton.visibility = VISIBLE
        }

        nameView = findViewById(R.id.textFullName)
        ageView = findViewById(R.id.textAge)
        bioView = findViewById(R.id.textBio)
        genderView = findViewById(R.id.textGender)
        phoneView = findViewById(R.id.textPhone)
        locationView = findViewById(R.id.textLocation)
        ratingBarView = findViewById(R.id.ratingBar)
        userProfileImageView = findViewById(R.id.imageUserProfile)
        ratingBarView.setIsIndicator(true)

        basketballRow = findViewById(R.id.basketballRow)
        volleyballRow = findViewById(R.id.volleyballRow)
        tennisRow = findViewById(R.id.tennisRow)
        golfRow = findViewById(R.id.golfRow)
        footballRow = findViewById(R.id.footballRow)

        basketballRatingBar = findViewById(R.id.basketballRowRatingBar)
        basketballRatingBar.setIsIndicator(true)

        volleyballRatingBar = findViewById(R.id.volleyballRowRatingBar)
        volleyballRatingBar.setIsIndicator(true)

        tennisRatingBar = findViewById(R.id.tennisRowRatingBar)
        tennisRatingBar.setIsIndicator(true)

        golfRatingBar = findViewById(R.id.golfRowRatingBar)
        golfRatingBar.setIsIndicator(true)

        footballRatingBar = findViewById(R.id.footballRowRatingBar)
        footballRatingBar.setIsIndicator(true)

        favoriteCourtsButton.setOnClickListener {
            val intent = Intent(this, MyCourtsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.no_anim)
        }

        yourFriendsButton.setOnClickListener {
            val intent = Intent(this, YourFriendsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.no_anim)
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Global.userId = null

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.no_anim)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        val viewModel by viewModels<ViewModel>()

        val loading = findViewById<FragmentContainerView>(R.id.loadingShowProfileFragment)
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
            .replace(R.id.loadingShowProfileFragment, SpinnerFragment()).commit()
        loading.visibility = VISIBLE

        val userId: String = if (intent.getStringExtra("reservationCreatorId") != null) {
            intent.getStringExtra("reservationCreatorId")!!
        } else if (intent.getStringExtra("friendId") != null) {
            intent.getStringExtra("friendId")!!
        } else {
            Global.userId!!
        }

        // show image profile fragment
        val data = Bundle()
        data.putString("userId", userId)

        val transaction = fragmentManager.beginTransaction()
        val imageProfileFragment = ImageProfileFragment()
        imageProfileFragment.arguments = data

        transaction
            .replace(R.id.imageProfileFragment, imageProfileFragment)
            .commit()

        viewModel.getUserInfo(userId).observe(this) { user ->
            if (user != null) {
                loading.visibility = GONE
                nameView.text = user.fullName
                bioView.text = user.bio
                genderView.text = when (user.gender) {
                    Gender.MALE -> resources.getString(R.string.genderMale)
                    Gender.FEMALE -> resources.getString(R.string.genderFemale)
                    Gender.OTHER -> resources.getString(R.string.genderOther)
                    null -> ""
                }

                if (user.age != null) {
                    ageView.text = resources.getString(R.string.years, user.age)
                } else {
                    ageView.text = resources.getString(R.string.age)
                }

                phoneView.text = user.phone
                locationView.text = user.location
                ratingBarView.rating = user.rating

                if (user.mySports.contains(Sport.BASKETBALL)) {
                    basketballRow.visibility = VISIBLE
                    basketballRatingBar.visibility = VISIBLE
                    basketballRatingBar.rating = user.mySports[Sport.BASKETBALL]!!
                } else {
                    basketballRow.visibility = GONE
                    basketballRatingBar.visibility = GONE
                }
                if (user.mySports.contains(Sport.VOLLEYBALL)) {
                    volleyballRow.visibility = VISIBLE
                    volleyballRatingBar.visibility = VISIBLE
                    volleyballRatingBar.rating = user.mySports[Sport.VOLLEYBALL]!!
                } else {
                    volleyballRow.visibility = GONE
                    volleyballRatingBar.visibility = GONE
                }
                if (user.mySports.contains(Sport.TENNIS)) {
                    tennisRow.visibility = VISIBLE
                    tennisRatingBar.visibility = VISIBLE
                    tennisRatingBar.rating = user.mySports[Sport.TENNIS]!!
                } else {
                    tennisRow.visibility = GONE
                    tennisRatingBar.visibility = GONE
                }
                if (user.mySports.contains(Sport.GOLF)) {
                    golfRow.visibility = VISIBLE
                    golfRatingBar.visibility = VISIBLE
                    golfRatingBar.rating = user.mySports[Sport.GOLF]!!
                } else {
                    golfRow.visibility = GONE
                    golfRatingBar.visibility = GONE
                }
                if (user.mySports.contains(Sport.FOOTBALL)) {
                    footballRow.visibility = VISIBLE
                    footballRatingBar.visibility = VISIBLE
                    footballRatingBar.rating = user.mySports[Sport.FOOTBALL]!!
                } else {
                    footballRow.visibility = GONE
                    footballRatingBar.visibility = GONE
                }

                val storageReference = Firebase.storage.reference.child("profileImages/${user.id}")

                Glide.with(this)
                    .load(storageReference)
                    .placeholder(R.drawable.user_profile)
                    .into(userProfileImageView)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.no_anim, R.anim.fade_out)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val userId: String = if (intent.getStringExtra("reservationCreatorId") != null) {
            intent.getStringExtra("reservationCreatorId")!!
        } else if (intent.getStringExtra("friendId") != null) {
            intent.getStringExtra("friendId")!!
        } else {
            Global.userId!!
        }
        if (userId == Global.userId) {
            // Inflate the menu to use in the action bar
            val inflater = menuInflater
            inflater.inflate(R.menu.menu, menu)
            return super.onCreateOptionsMenu(menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.modify_profile -> {
                val intent = Intent(this, EditProfileActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.no_anim)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

// class to download images from the Firebase storage
@GlideModule
class MyAppGlideModule: AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        // Register FirebaseImageLoader to handle StorageReference
        registry.append(
            StorageReference::class.java, InputStream::class.java,
            FirebaseImageLoader.Factory()
        )
    }
}