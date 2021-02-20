package com.zomatunes.zomatunes.ui.download

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.zomatunes.zomatunes.R
import kotlinx.android.synthetic.main.activity_swipe.*

class SwipeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swipe)

        image_play.setOnClickListener {
            Toast.makeText(this , "play clicked " , Toast.LENGTH_SHORT).show()
        }
    }
}