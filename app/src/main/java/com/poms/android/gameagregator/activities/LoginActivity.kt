package com.poms.android.gameagregator.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.poms.android.gameagregator.R
import com.poms.android.gameagregator.databinding.LoginActivityBinding
import com.poms.android.gameagregator.ui.login.LoginFragment

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginActivityBinding

    /**
     * Function launched when the activity is started
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Loads the information checking if the screen is new or is reloading
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.login_activity, LoginFragment.newInstance())
                .commitNow()
        }
    }

    /**
     * Function called on start
     */
    override fun onStart() {
        super.onStart()

        // Check if the user is logged to go to the Main activity instead
        if (FirebaseAuth.getInstance().currentUser != null) {
            val myIntent = Intent(this, MainActivity::class.java)
            startActivity(myIntent)
            finish()
        }
    }
}