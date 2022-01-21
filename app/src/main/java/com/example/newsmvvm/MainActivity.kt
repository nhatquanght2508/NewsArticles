package com.example.newsmvvm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.newsmvvm.databinding.ActivityMainBinding
import com.example.newsmvvm.db.ArticleDatabase
import com.example.newsmvvm.repository.NewsRepository
import com.example.newsmvvm.ui.NewsViewModel
import com.example.newsmvvm.ui.NewsViewModelProviderFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: NewsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val newsRepository = NewsRepository(ArticleDatabase(this))
        val viewModelProviderFactory = NewsViewModelProviderFactory(application,newsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)
        val navController = findNavController(R.id.newsNavHostFragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.breakingNewsFragment,
                R.id.searchNewsFragment,
                R.id.savedNewsFragment
            )
        )
        setupActionBarWithNavController(this, navController, appBarConfiguration)
        binding.bottomNavView.setupWithNavController(navController)
    }
}