package com.example.newsandroid.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.TYPE_ETHERNET
import android.net.ConnectivityManager.TYPE_WIFI
import android.net.ConnectivityManager.TYPE_MOBILE
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.newsandroid.NewsApplication
import com.example.newsandroid.models.Article
import com.example.newsandroid.models.NewsResponse
import com.example.newsandroid.repository.NewsRepository
import com.example.newsandroid.utils.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
    app: Application,
    private val newsRepository: NewsRepository
) : AndroidViewModel(app) {
    private val _breakingNews = MutableLiveData<Resource<NewsResponse>>()
    val breakingNews: LiveData<Resource<NewsResponse>> get() = _breakingNews
    var breakingNewsPage = 1
    private var breakingNewsResponse: NewsResponse? = null

    private val _searchNews = MutableLiveData<Resource<NewsResponse>>()
    val searchNews: LiveData<Resource<NewsResponse>> get() = _searchNews
    private var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null

    val savedNews = newsRepository.getSavedNews()

    init {
        getBreakingNews("us")
    }

    fun getBreakingNews(countryCode: String) {
        safeNewsCall(_breakingNews) {
            val res = newsRepository.getBreakingNews(countryCode, breakingNewsPage)
            _breakingNews.postValue(handleBreakingNewsResponse(res))
        }
    }

    private fun handleBreakingNewsResponse(res: Response<NewsResponse>): Resource<NewsResponse> {
        if (res.isSuccessful) {
            res.body()?.let {
                breakingNewsPage++
                if (breakingNewsResponse == null) {
                    breakingNewsResponse = it
                } else {
                    breakingNewsResponse?.articles?.addAll(it.articles)
                }
                return Resource.Success(breakingNewsResponse ?: it)
            }
        }
        return Resource.Error(res.message())
    }

    fun searchNews(searchQuery: String) {
        safeNewsCall(_searchNews) {
            val res = newsRepository.searchNews(searchQuery, searchNewsPage)
            _searchNews.postValue(handleSearchNewsResponse(res))
        }
    }

    private fun handleSearchNewsResponse(res: Response<NewsResponse>): Resource<NewsResponse> {
        if (res.isSuccessful) {
            res.body()?.let {
                searchNewsPage++;
                if (searchNewsResponse == null) {
                    searchNewsResponse = it
                } else {
                    searchNewsResponse?.articles?.addAll(it.articles)
                }
                return Resource.Success(searchNewsResponse ?: it)
            }
        }
        return Resource.Error(res.message())
    }

    fun saveArticle(article: Article) {
        viewModelScope.launch {
            newsRepository.upsert(article)
        }
    }

    fun deleteArticle(article: Article) {
        viewModelScope.launch {
            newsRepository.deleteArticle(article)
        }
    }

    private fun safeNewsCall(
        news: MutableLiveData<Resource<NewsResponse>>,
        cb: suspend () -> Unit
    ) {
        viewModelScope.launch {
            news.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    cb()
                } else {
                    news.postValue(Resource.Error("No internet connection"))
                }
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> news.postValue(Resource.Error("Network failure"))
                    else -> news.postValue(Resource.Error("Conversion error"))
                }
            }
        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }
}