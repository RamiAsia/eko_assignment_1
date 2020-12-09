package com.ramiasia.ekoapp1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ramiasia.ekoapp1.ui.TimeFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, TimeFragment.newInstance())
                .commitNow()
        }
    }
}