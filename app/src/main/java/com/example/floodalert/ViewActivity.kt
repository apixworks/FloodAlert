package com.example.floodalert

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.gigamole.library.PulseView

class ViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        val pulseView: PulseView = findViewById(R.id.pv)
        pulseView.startPulse()
    }
}
