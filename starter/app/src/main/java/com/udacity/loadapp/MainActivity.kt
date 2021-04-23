package com.udacity.loadapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.udacity.loadapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainViewModel: MainViewModel by viewModels()

        ActivityMainBinding.inflate(layoutInflater).run {
            lifecycleOwner = this@MainActivity
            setContentView(root)
            setSupportActionBar(toolbar)
            contentMain.viewModel = mainViewModel
        }

        mainViewModel.showInfo.observe(this) {
            if (it) Toast.makeText(this, mainViewModel.infoId, Toast.LENGTH_SHORT).show()
        }
    }
}