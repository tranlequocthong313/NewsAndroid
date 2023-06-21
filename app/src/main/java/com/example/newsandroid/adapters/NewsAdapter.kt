package com.example.newsandroid.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsandroid.databinding.ItemArticlePreviewBinding
import com.example.newsandroid.models.Article

object DiffItemCallback : DiffUtil.ItemCallback<Article>() {
    override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem.url == newItem.url
    }

    override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem == newItem
    }
}

class ArticleViewHolder(private val binding: ItemArticlePreviewBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun inflateFrom(parent: ViewGroup): ArticleViewHolder {
            return ArticleViewHolder(
                ItemArticlePreviewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    fun bind(article: Article, onClick: ((Article) -> Unit)) {
        binding.root.apply {
            Glide.with(this).load(article.urlToImage).into(binding.ivArticleImage)
            binding.tvSource.text = article.source?.name
            binding.tvTitle.text = article.title
            binding.tvDescription.text = article.description
            binding.tvPublishedAt.text = article.publishedAt
            setOnClickListener {
                onClick(article)
            }
        }
    }
}

class NewsAdapter(private val onClick: ((Article) -> Unit)) :
    ListAdapter<Article, ArticleViewHolder>(DiffItemCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder.inflateFrom(parent)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }
}