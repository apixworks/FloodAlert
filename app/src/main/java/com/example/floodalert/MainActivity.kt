package com.example.floodalert

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val displayMetrics = DisplayMetrics()
//        windowManager.defaultDisplay.getMetrics(displayMetrics)
//        var width = displayMetrics.widthPixels
//        var height = displayMetrics.heightPixels
//
//        var params : LinearLayout.LayoutParams = LinearLayout.LayoutParams(
//            width/2,height/2
//        )
    }
}
