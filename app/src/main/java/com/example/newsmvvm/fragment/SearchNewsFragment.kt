package com.example.newsmvvm.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsmvvm.MainActivity
import com.example.newsmvvm.R
import com.example.newsmvvm.adapters.NewsAdapter
import com.example.newsmvvm.databinding.FragmentSearchNewsBinding
import com.example.newsmvvm.ui.NewsViewModel
import com.example.newsmvvm.util.Common
import com.example.newsmvvm.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchNewsFragment : Fragment(R.layout.fragment_search_news) {
    lateinit var viewModel: NewsViewModel
    private lateinit var binding: FragmentSearchNewsBinding
    lateinit var newsAdapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel
        setupRecycleView()
        setupSearchingBar()
        viewModel.searchingNews.observe(
            viewLifecycleOwner, { response ->
                when (response) {
                    is Resource.Success -> {
                        binding.paginationProgressBar.hide()
                        response.data?.let { newsResponse ->
                            newsAdapter.differ.submitList(newsResponse.articles.toList())
                            val totalPages = newsResponse.totalResults / Common.QUERY_PAGE_SIZE + 2
                            isLastPage = viewModel.searchingNewsPage == totalPages
                            if (isLastPage) {
                                binding.rvSearchingNews.setPadding(0, 0, 0, 0)
                            }
                        }
                    }
                    is Resource.Error -> {
                        binding.paginationProgressBar.hide()
                        isLoading = false
                        response.message?.let { newsMessage ->
                            Toast.makeText(
                                activity,
                                "An error occurred: $newsMessage",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    is Resource.Loading -> {
                        binding.paginationProgressBar.show()
                        isLoading = true
                    }
                }
            })
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false
    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount
            val isNotLoadingAndNotLastPage = !isLastPage && !isLoading
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Common.QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem
                    && isTotalMoreThanVisible && isNotAtBeginning && isScrolling
            if (shouldPaginate) {
                viewModel.getSearchingNews(binding.edtSearch.text.toString())
                isScrolling = false
            } else {
                binding.rvSearchingNews.setPadding(
                    0, 0, 0, 0
                )
            }
        }
    }

    private fun setupSearchingBar() {
        var job: Job? = null
        binding.edtSearch.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(Common.SEARCHING_NEWS_TIME_DELAY)
                editable?.let {
                    if (editable.toString().isNotEmpty()) {
                        viewModel.getSearchingNews(editable.toString())
                    }
                }
            }
        }
    }

    private fun setupRecycleView() {
        newsAdapter = NewsAdapter()
        binding.rvSearchingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollListener)
        }
        newsAdapter.setOnItemClickArticle {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_searchNewsFragment_to_articleFragment,
                bundle
            )
        }
    }

}