package com.example.floodalert

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.floodalert.models.UserResponse
import com.example.floodalert.utils.LoginApi
import com.google.gson.Gson
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class LoginActivity : AppCompatActivity() {

    lateinit var dialog: AlertDialog
    private lateinit var serverURL: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        serverURL = getString(R.string.serverURL)

        loginBtn.setOnClickListener{
            val emailString = email.text.toString()
            val passwordString= password.text.toString()
            if(emailString.isNotEmpty() and passwordString.isNotEmpty()){
                if(verifyAvailableNetwork(this@LoginActivity)){
                    login(emailString,passwordString)
                }else{
                    Toast.makeText(this@LoginActivity,"Please check Connection!", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this@LoginActivity,"Please fill all fields!", Toast.LENGTH_SHORT).show()
            }
        }

        sign_up_txt.setOnClickListener{
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun login(email:String, password:String){
        Log.i("EditText values: ",email+" "+password)
        dialog = SpotsDialog.Builder()
            .setContext(this@LoginActivity)
            .setCancelable(false)
            .setTheme(R.style.CustomDialog)
            .build()
            .apply {
                show()
            }

        val gson = Gson()

        val retrofit = Retrofit.Builder()
            .baseUrl(serverURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(LoginApi::class.java)

        val call = api.loginUser(email,password)

        call.enqueue(object : Callback<UserResponse> {

            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                Log.i("ResponseString", gson.toJson(response.body()))
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i("onSuccess", gson.toJson(response.body()))
                        parseRegisterData(gson.toJson(response.body()))
                        dialog.hide()
                    } else {
                        Log.i(
                            "onEmptyResponse",
                            "Returned empty response")
                        dialog.hide()
                        Toast.makeText(this@LoginActivity,"Login Unsuccessful!",Toast.LENGTH_SHORT).show()
                    }
                }else {
                    Log.i(
                        "onEmptyResponse",
                        "Returned empty response")
                    Toast.makeText(this@LoginActivity,"Login Unsuccessful!",Toast.LENGTH_SHORT).show()
                    dialog.hide()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity,"Login Unsuccessful!",Toast.LENGTH_SHORT).show()
                dialog.hide()
            }
        })
    }

    private fun parseRegisterData(response: String){
        try {
            val jsonObject = JSONObject(response)
            if(jsonObject.getBoolean("status")){
                email.setText("")
                password.setText("")
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
            }else{
                Toast.makeText(this@LoginActivity,"Login Unsuccessful!",Toast.LENGTH_SHORT).show()
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
