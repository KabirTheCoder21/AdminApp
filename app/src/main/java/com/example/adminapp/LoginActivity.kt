package com.example.adminapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import app.rive.runtime.kotlin.core.Rive
import com.example.adminapp.databinding.ActivityLoginBinding
import com.example.adminapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val stateMachineName = "Login Machine"
    private lateinit var firebaseAuth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = DataBindingUtil.setContentView(this,R.layout.activity_login)
        binding =ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        Rive.init(this)

        binding.emailLogin.setOnFocusChangeListener { view, b ->
            if (b){
                binding.loginCharacter.controller.setBooleanState(stateMachineName,"isChecking",true)
            }else{
                binding.loginCharacter.controller.setBooleanState(stateMachineName,"isChecking",false)

            }
        }
        binding.emailLogin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                try {
                    binding.loginCharacter.controller.setNumberState(
                        stateMachineName,
                        "numLook",
                        p0!!.length.toFloat()
                    )
                }
                catch (e: Exception){

                }
            }

        })

        binding.passwordlogin.setOnFocusChangeListener { view, b ->
            if (b){
                binding.loginCharacter.controller.setBooleanState(stateMachineName,"isHandsUp",true)
            }else{
                binding.loginCharacter.controller.setBooleanState(stateMachineName,"isHandsUp",false)

            }
        }


        binding.button.setOnClickListener {

//            binding.passwordlogin.clearFocus()

                val mail = binding.emailLogin.text.toString()
                val pass = binding.passwordlogin.text.toString()
//                val confirmPass=binding.
                if(mail.isNotEmpty() && pass.isNotEmpty() ){

                        firebaseAuth.signInWithEmailAndPassword(mail,pass).addOnCompleteListener {
                            if (it.isSuccessful){
                                binding.loginCharacter.controller.fireState(
                                    stateMachineName,
                                    "trigSuccess"
                                );
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            else{
                                Toast.makeText(this,it.exception.toString(), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                else{
                    Toast.makeText(this,"Enter All fields",Toast.LENGTH_SHORT).show()
                    Toast.makeText(this,"pura daalo",Toast.LENGTH_SHORT).show()
                }

            }

        binding.signUpact.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    override fun onBackPressed() {
        finish()
    }
}