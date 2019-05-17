package com.example.floodalert

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.example.floodalert.Utils.SharedPreference
import com.example.floodalert.Utils.WeatherApi
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import retrofit2.converter.scalars.ScalarsConverterFactory
import com.example.floodalert.models.WeatherResponse
import com.google.gson.Gson
import dmax.dialog.SpotsDialog
import retrofit2.Retrofit
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.round


class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"
    val TOPIC_GLOBAL = "global"
    val JSONURL = "http://api.openweathermap.org/data/2.5/"

    lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val lowerLayout = findViewById<ConstraintLayout>(R.id.lowerLayout)
        val temperature = findViewById<TextView>(R.id.temperature)
        val day = findViewById<TextView>(R.id.day)
        val date = findViewById<TextView>(R.id.date)
        val condition = findViewById<TextView>(R.id.condition)
        val notificationSwitch = findViewById<Switch>(R.id.notificationSwitch)
        val weatherImage = findViewById<ImageView>(R.id.weatherImage)
        val sharedPreference = SharedPreference(this)

        dialog = SpotsDialog.Builder()
            .setContext(this@MainActivity)
            .setCancelable(false)
            .setTheme(R.style.CustomDialog)
            .build()
            .apply {
                show()
            }

        if (sharedPreference.getValueInt("notificationState") == 0) {
            notificationSwitch.setChecked(false)
        }else{
            notificationSwitch.setChecked(true)
        }

        temperature.setText("18" + 0x00B0.toChar())
        day.setText(android.text.format.DateFormat.format("EEEE", Date()))
        date.setText(android.text.format.DateFormat.format("MMMM dd", Date()) as String)

        notificationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_GLOBAL)
                    .addOnCompleteListener { task ->
                        var msg = "Notification set!"
                        sharedPreference.save("notificationState", 1)
                        if (!task.isSuccessful) {
                            msg = "Notification failed!"
                            sharedPreference.save("notificationState", 0)
                        }
                        Log.d(TAG, msg)
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    }
            else
                FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_GLOBAL)
                    .addOnCompleteListener { task ->
                        var msg = "Notification removed!"
                        sharedPreference.save("notificationState", 0)
                        if (!task.isSuccessful) {
                            msg = "Notification not removed!"
                            sharedPreference.save("notificationState", 1)
                        }
                        Log.d(TAG, msg)
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    }
        }

        getResponse()

//        notificationSwitch.setOnClickListener (object : View.OnClickListener {
//            override fun onClick(v: View?) {
//                if(verifyAvailableNetwork(this@MainActivity)){
//
//                }
//            }
//        })
//
//        notificationSwitch.setOnTouchListener(object : View.OnTouchListener {
//            override fun onTouch(v: View, m: MotionEvent): Boolean {
//                // Perform tasks here
//                if(!verifyAvailableNetwork(this@MainActivity)){
//                    notificationSwitch.setChecked(false)
//                    Toast.makeText(baseContext,"Please turn your Internet!",Toast.LENGTH_SHORT).show()
//                }else{
//                    if(notificationSwitch.isChecked)
//                        notificationSwitch.setChecked(false)
//                    else
//                        notificationSwitch.setChecked(true)
//                }
//                return true
//            }
//        })
    }

    private fun getResponse() {

        val gson = Gson()

        val retrofit = Retrofit.Builder()
            .baseUrl(JSONURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(WeatherApi::class.java!!)

        val call = api.currentWeatherData

        call.enqueue(object : Callback<WeatherResponse> {

            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                Log.i("ResponseString", gson.toJson(response.body()))
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i("onSuccess", gson.toJson(response.body()))

                        val weatherResponse = response.body()

                        if(weatherResponse?.weather!!.get(0).icon!! in arrayOf("02d","02n","03d","03n")) {
                            weatherImage.setImageResource(R.drawable.partly_cloudy)
                            condition.setText("Partly Cloud")
                        }else if(weatherResponse.weather.get(0).icon!! in arrayOf("04d","04n")) {
                            weatherImage.setImageResource(R.drawable.cloudy_weather)
                            condition.setText("Cloudy")
                        }else if(weatherResponse.weather.get(0).icon!! in arrayOf("01d","01n")) {
                            weatherImage.setImageResource(R.drawable.clear_day)
                            condition.setText("Clear Day")
                        }else if(weatherResponse.weather.get(0).icon!! in arrayOf("10d","10n","13d","09d")) {
                            weatherImage.setImageResource(R.drawable.rainy_weather)
                            condition.setText("Rainy")
                        }
                        temperature.setText(kelvinToCelsius(weatherResponse.main!!.temp))

                        lowerLayout.visibility = View.VISIBLE
                        weatherImage.visibility = View.VISIBLE
                        condition.visibility = View.VISIBLE

                        dialog.dismiss()

                    } else {
                        Log.i(
                            "onEmptyResponse",
                            "Returned empty response")
                        dialog.dismiss()
                    }
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                dialog.dismiss()
            }
        })
    }

    fun kelvinToCelsius(temp: Float):String {
        val celsius = temp - 273.15
        return round(celsius).toInt().toString() + 0x00B0.toChar()
    }

    fun verifyAvailableNetwork(activity: AppCompatActivity): Boolean {
        val connectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}
