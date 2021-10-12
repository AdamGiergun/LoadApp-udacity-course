package com.udacity.loadapp.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.udacity.loadapp.viewmodels.MainViewModel
import com.udacity.loadapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainViewModel: MainViewModel by viewModels()

        ActivityMainBinding.inflate(layoutInflater).run {
            activityMainBinding = this
            lifecycleOwner = this@MainActivity
            setContentView(root)
            setSupportActionBar(toolbar)
            contentMain.viewModel = mainViewModel
        }

        mainViewModel.showInfo.observe(this) {
            if (it) Toast.makeText(this, mainViewModel.infoId, Toast.LENGTH_SHORT).show()
        }
    }

    fun downloadButtonClicked(view: View) {
        activityMainBinding.contentMain.let { binding ->
            binding.customUrl.let {
                val customUrlText = it.text.toString()
                binding.viewModel?.downloadButtonClicked(customUrlText)
            }
        }
    }
}