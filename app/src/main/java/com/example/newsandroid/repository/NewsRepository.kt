package com.example.newsandroid.repository

import androidx.lifecycle.LiveData
import com.example.newsandroid.api.RetrofitInstance
import com.example.newsandroid.db.ArticleDatabase
import com.example.newsandroid.models.Article
import com.example.newsandroid.models.NewsResponse
import retrofit2.Response

class NewsRepository(
    private val db: ArticleDatabase
) {
    suspend fun getBreakingNews(countryCode: String, page: Int): Response<NewsResponse> {
        return RetrofitInstance.api.getBreakingNews(countryCode, page)
    }

    suspend fun searchNews(searchQuery: String, page: Int): Response<NewsResponse> {
        return RetrofitInstance.api.searchForNews(searchQuery, page)
    }

    suspend fun upsert(article: Article): Long {
        return db.getArticleDao().upsertArticle(article)
    }

    fun getSavedNews(): LiveData<List<Article>> {
        return db.getArticleDao().getArticles()
    }

    suspend fun deleteArticle(article: Article) {
        db.getArticleDao().deleteArticle(article)
    }
}