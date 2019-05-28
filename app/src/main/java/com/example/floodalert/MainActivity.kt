package com.example.floodalert

import am.appwise.components.ni.ConnectionCallback
import am.appwise.components.ni.NoInternetDialog
import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.View
import android.widget.*
import com.example.floodalert.Utils.SharedPreference
import com.example.floodalert.Utils.WeatherApi
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
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
    lateinit var noInternetDialog: NoInternetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val lowerLayout = findViewById<ConstraintLayout>(R.id.lowerLayout)
        val temperature = findViewById<TextView>(R.id.temperature)
        val day = findViewById<TextView>(R.id.day)
        val date = findViewById<TextView>(R.id.date)
//        val condition = findViewById<TextView>(R.id.condition)
        val notificationSwitch = findViewById<Switch>(R.id.notificationSwitch)
//        val weatherImage = findViewById<ImageView>(R.id.weatherImage)
        val swipeLayout = findViewById<SwipeRefreshLayout>(R.id.swipeLayout)
        val sharedPreference = SharedPreference(this)

        val builder = NoInternetDialog.Builder(this)
        builder.setBgGradientStart(resources.getColor(R.color.colorPrimary)) // Start color for background gradient
        builder.setBgGradientCenter(resources.getColor(R.color.colorPrimary)) // Center color for background gradient
        builder.setBgGradientEnd(resources.getColor(R.color.colorPrimary)) // End color for background gradient
//        builder.setBgGradientOrientation() // Background gradient orientation (possible values see below)
//        builder.setDialogRadius() // Set custom radius for background gradient
//        builder.setTitleTypeface() // Set custom typeface for title text
//        builder.setMessageTypeface() // Set custom typeface for message text
        builder.setButtonColor(resources.getColor(R.color.colorAccent)) // Set custom color for dialog buttons
        builder.setButtonTextColor(resources.getColor(R.color.colorWhite)) // Set custom text color for dialog buttons
        builder.setButtonIconsColor(resources.getColor(R.color.colorWhite)) // Set custom color for icons of dialog buttons
//        builder.setWifiLoaderColor() // Set custom color for wifi loader
//        builder.setConnectionCallback({ hasActiveConnection: Boolean -> getResponse() }) // Set a Callback for network status
        builder.setCancelable(false) // Set cancelable status for dialog
        noInternetDialog = builder.build()

        if(verifyAvailableNetwork(this)){
            getResponse()
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

        swipeLayout.setOnRefreshListener{
            Log.d("MainActivity","Imefika")
            getResponse()
        }

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

    override fun onDestroy() {
        super.onDestroy()
        noInternetDialog.onDestroy()
    }

    private fun getResponse() {
        dialog = SpotsDialog.Builder()
            .setContext(this@MainActivity)
            .setCancelable(false)
            .setTheme(R.style.CustomDialog)
            .build()
            .apply {
                show()
            }
        lowerLayout.visibility = View.INVISIBLE
        weatherImage.visibility = View.INVISIBLE
        condition.visibility = View.INVISIBLE

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

                        connectionLayout.visibility  = View.INVISIBLE
                        lowerLayout.visibility = View.VISIBLE
                        weatherImage.visibility = View.VISIBLE
                        condition.visibility = View.VISIBLE

                        dialog.dismiss()
                        swipeLayout.isRefreshing = false

                    } else {
                        Log.i(
                            "onEmptyResponse",
                            "Returned empty response")
                        connectionLayout.visibility  = View.VISIBLE
                        dialog.dismiss()
                        swipeLayout.isRefreshing = false
                    }
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                connectionLayout.visibility  = View.VISIBLE
                dialog.dismiss()
                swipeLayout.isRefreshing = false
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
