package com.example.floodalert

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_registration.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import android.content.Intent
import android.widget.Toast
import com.example.floodalert.utils.LoginApi
import org.json.JSONException


class RegistrationActivity : AppCompatActivity() {

    lateinit var dialog: AlertDialog
    private lateinit var registerURL: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        registerURL = getString(R.string.serverURL)

        registerBtn.setOnClickListener{
            val nameString = name.text.toString()
            val emailString = email.text.toString()
            val passwordString= password.text.toString()
            if(nameString.isNotEmpty() and emailString.isNotEmpty() and passwordString.isNotEmpty()){
                if(verifyAvailableNetwork(this@RegistrationActivity)){
                    register(nameString,emailString,passwordString)
                }else{
                    Toast.makeText(this@RegistrationActivity,"Please check Connection!",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this@RegistrationActivity,"Please fill all fields!",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun register(name:String, email:String, password:String){
        Log.i("EditText values: ",name+" "+email+" "+password)
        dialog = SpotsDialog.Builder()
            .setContext(this@RegistrationActivity)
            .setCancelable(false)
            .setTheme(R.style.CustomDialog)
            .build()
            .apply {
                show()
            }

        val gson = Gson()

        val retrofit = Retrofit.Builder()
            .baseUrl(registerURL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

        val api = retrofit.create(LoginApi::class.java)

        val call = api.registerUser(name,email,password)

        call.enqueue(object : Callback<String> {

            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.i("ResponseString", response.body().toString())
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i("onSuccess", gson.toJson(response.body()))
                        parseRegisterData(response.body().toString())
                        dialog.hide()
                    } else {
                        Log.i(
                            "onEmptyResponse",
                            "Returned empty response")
                        dialog.hide()
                        Toast.makeText(this@RegistrationActivity,"Registration was unsuccessful!",Toast.LENGTH_SHORT).show()
                    }
                }else {
                    Log.i(
                        "onEmptyResponse",
                        "Returned empty response")
                    Toast.makeText(this@RegistrationActivity,"Registration was unsuccessful!",Toast.LENGTH_SHORT).show()
                    dialog.hide()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@RegistrationActivity,"Registration was unsuccessful!",Toast.LENGTH_SHORT).show()
                dialog.hide()
            }
        })
    }

    private fun parseRegisterData(response: String){
        try {
            val jsonObject = JSONObject(response)
            if(jsonObject.getBoolean("status")){
                name.setText("")
                email.setText("")
                password.setText("")
                Toast.makeText(this@RegistrationActivity,"Successful Registered!",Toast.LENGTH_SHORT).show()
                val intent = Intent(this@RegistrationActivity, LoginActivity::class.java)
                startActivity(intent)
            }else{
                Toast.makeText(this@RegistrationActivity,"Registration was unsuccessful!",Toast.LENGTH_SHORT).show()
            }
        }catch (e: JSONException){
            e.printStackTrace()
        }
    }

    private fun verifyAvailableNetwork(activity: AppCompatActivity): Boolean {
        val connectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}
