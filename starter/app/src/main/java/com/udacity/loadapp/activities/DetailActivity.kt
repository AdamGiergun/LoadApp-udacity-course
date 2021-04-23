package com.udacity.loadapp.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.udacity.loadapp.viewmodels.DetailViewModel
import com.udacity.loadapp.viewmodels.DetailViewModelFactory
import com.udacity.loadapp.R
import com.udacity.loadapp.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityDetailBinding.inflate(layoutInflater).run {
            setContentView(root)
            toolbar.title = getString(R.string.download_details)
            setSupportActionBar(toolbar)
            val detailViewModel by viewModels<DetailViewModel> { DetailViewModelFactory(application, intent)  }
            detailViewModel.download?.let { contentDetail.download = it }
        }
    }
}