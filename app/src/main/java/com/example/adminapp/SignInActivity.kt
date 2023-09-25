package com.example.adminapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import app.rive.runtime.kotlin.core.Rive
import com.example.adminapp.databinding.ActivitySignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

//import com.google.android.gms.auth.api.signin.GoogleSignInAccount


class SignInActivity : AppCompatActivity() {

    private lateinit var binding:ActivitySignInBinding
    private val stateMachineName = "Login Machine"
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var auth:FirebaseAuth

    private lateinit var gso:GoogleSignInOptions
    private lateinit var gsc:GoogleSignInClient
    private lateinit var user : FirebaseUser
    private lateinit var progressBar:ProgressBar

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Rive.init(this)

        firebaseAuth = FirebaseAuth.getInstance()
        auth= Firebase.auth
        progressBar=binding.progressBar

      // user = FirebaseAuth.getInstance().currentUser!!

//        val webClientId = resources.getString(R.string.default_web_client_id)
        gso=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken("992337267708-gtqpoj2pm4ijpukvcknrmg749lhu6fuj.apps.googleusercontent.com").requestEmail().build()

        gsc=GoogleSignIn.getClient(this,gso)

        //Google imge buttonset
        binding.btnGoogle.setOnClickListener {
           progressBar.visibility = View.VISIBLE
            val signInclient=gsc.signInIntent
            startActivityForResult(signInclient,100)
        }

        

        binding.emailSignin.setOnFocusChangeListener { view, b ->
            if (b){
                binding.loginCharacterS.controller.setBooleanState(stateMachineName,"isChecking",true)
            }else{
                binding.loginCharacterS.controller.setBooleanState(stateMachineName,"isChecking",false)

            }
        }

        binding.emailSignin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                try {
                    binding.loginCharacterS.controller.setNumberState(
                        stateMachineName,
                        "numLook",
                        p0!!.length.toFloat()
                    )
                }
                catch (e: Exception){

                }
            }

        })





        binding.passwordSign.setOnFocusChangeListener { view, b ->
            if (b){
                binding.loginCharacterS.controller.setBooleanState(stateMachineName,"isHandsUp",true)
            }else{
                binding.loginCharacterS.controller.setBooleanState(stateMachineName,"isHandsUp",false)

            }
        }
        binding.passwordSignconfirm.setOnFocusChangeListener { view, b ->
            if (b){
                binding.loginCharacterS.controller.setBooleanState(stateMachineName,"isHandsUp",true)
            }else{
                binding.loginCharacterS.controller.setBooleanState(stateMachineName,"isHandsUp",false)

            }
        }




        binding.btnSignIn.setOnClickListener {

//            binding.passwordSign.clearFocus()

            val mail = binding.emailSignin.text.toString()
            val pass = binding.passwordSign.text.toString()
            val confirmPass=binding.passwordSignconfirm.text.toString()

            if (mail.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (confirmPass == pass) {
                    // Check if the email is already in use
                    firebaseAuth.fetchSignInMethodsForEmail(mail)
                        .addOnCompleteListener { checkTask ->
                            if (checkTask.isSuccessful) {
                                val result = checkTask.result
                                if (result?.signInMethods?.isEmpty() == true) {
                                    // No user with this email address exists, you can proceed with user registration
                                    firebaseAuth.createUserWithEmailAndPassword(mail, pass)
                                        .addOnCompleteListener { registrationTask ->
                                            if (registrationTask.isSuccessful) {
                                                // Registration was successful
                                                binding.loginCharacterS.controller.fireState(
                                                    stateMachineName,
                                                    "trigSuccess"
                                                )
                                                val intent = Intent(this, MainActivity::class.java)
                                                startActivity(intent)
                                                finish()

                                                binding.emailSignin.text?.clear()
                                                binding.passwordSign.text?.clear()
                                                binding.passwordSignconfirm.text?.clear()
                                            } else {
                                                // Registration failed
                                                Toast.makeText(this, "Registration failed: " + registrationTask.exception?.message, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } else {
                                    // A user with this email address already exists, display an error message to the user
                                    Toast.makeText(this, "A user with this email already exists", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // An error occurred while checking the email
                                val exception = checkTask.exception
                                Toast.makeText(this, "Error: " + exception?.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Password and confirm password do not match", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "Shi daalo", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Enter credentials", Toast.LENGTH_SHORT).show()
                binding.loginCharacterS.controller.fireState(stateMachineName, "trigFail")
            }

//                else{
//                    Toast.makeText(this,"Enter credentials",Toast.LENGTH_SHORT).show()
//                    binding.loginCharacterS.controller.fireState(stateMachineName, "trigFail");
//                }
            }
        binding.btnloginAct.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }


    }



    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            reload()
        }
    }

    private fun reload() {
        val intent = Intent(this,MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==100&&resultCode== RESULT_OK)
        {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)

                if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            val credentials = GoogleAuthProvider.getCredential(account?.idToken, null)

            auth.signInWithCredential(credentials).addOnCompleteListener { signInTask ->
                if (signInTask.isSuccessful) {
                    // Successful sign-in
                    progressBar.visibility = View.GONE
                    Log.d("GoogleSignIn", "Sign-in successful")
                    Toast.makeText(this, "Sign-in successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                } else {
                    // Sign-in failed
                    progressBar.visibility = View.GONE
                    Log.e("GoogleSignIn", "Sign-in failed: ${signInTask.exception}")
                    Toast.makeText(this, "Sign-in failed: " + signInTask.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Handle task failure
            Log.e("GoogleSignIn", "Google Sign-In task failed")
                    progressBar.visibility = View.GONE
            Toast.makeText(this, "Google Sign-In task failed", Toast.LENGTH_SHORT).show()
        }
    }
        else{
            progressBar.visibility = View.GONE
            Log.e("GoogleSignIn", "Google Sign-In result failed")
            Toast.makeText(this, "Google Sign-In result failed", Toast.LENGTH_SHORT).show()
        }
    }

    /*private val launcher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result->
        if (result.resultCode == Activity.RESULT_OK) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            val credentials = GoogleAuthProvider.getCredential(account?.idToken, null)

            auth.signInWithCredential(credentials).addOnCompleteListener { signInTask ->
                if (signInTask.isSuccessful) {
                    // Successful sign-in
                    Log.d("GoogleSignIn", "Sign-in successful")
                    Toast.makeText(this, "Sign-in successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    // Sign-in failed
                    Log.e("GoogleSignIn", "Sign-in failed: ${signInTask.exception}")
                    Toast.makeText(this, "Sign-in failed: " + signInTask.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Handle task failure
            Log.e("GoogleSignIn", "Google Sign-In task failed")
            Toast.makeText(this, "Google Sign-In task failed", Toast.LENGTH_SHORT).show()
        }
    } else {
        // Handle result failure
        Log.e("GoogleSignIn", "Google Sign-In result failed")
        Toast.makeText(this, "Google Sign-In result failed", Toast.LENGTH_SHORT).show()
    }

    }*/
}