package com.example.newsandroid.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.newsandroid.models.Article

@Dao
interface ArticleDao {
    // replace current existed article in db
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    // insert and update in one method
    suspend fun upsertArticle(article: Article): Long

    @Delete
    suspend fun deleteArticle(article: Article)

    @Query("SELECT * FROM articles")
    fun getArticles(): LiveData<List<Article>>
}