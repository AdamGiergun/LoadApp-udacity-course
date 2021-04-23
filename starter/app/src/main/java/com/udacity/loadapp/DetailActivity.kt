package com.udacity.loadapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.udacity.loadapp.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityDetailBinding.inflate(layoutInflater).run {
            setContentView(root)
            toolbar.title = getString(R.string.download_details)
            setSupportActionBar(toolbar)
            val viewModel = ViewModelProvider(this@DetailActivity, DetailViewModelFactory(application, intent)).get(DetailViewModel::class.java)
            viewModel.download?.let { contentDetail.download = it }
        }
    }
}