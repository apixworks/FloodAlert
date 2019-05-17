package com.example.floodalert

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.gigamole.library.PulseView

class ViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        val messageString = intent.extras?.getString("message")
        val message = findViewById<TextView>(R.id.message)

        val pulseView: PulseView = findViewById(R.id.pv)
        pulseView.startPulse()
        message.setText(messageString)
    }
}
