package com.havistudio.android.greekconstitution.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.havistudio.android.greekconstitution.data.local.entity.Article
import com.havistudio.android.greekconstitution.data.local.entity.Bookmark
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(bookmark: Bookmark): Long

    @Query("DELETE FROM bookmarks WHERE articleId = :articleId")
    suspend fun removeByArticle(articleId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE articleId = :articleId)")
    fun observeIsBookmarked(articleId: Int): Flow<Boolean>

    @Query(
        """
        SELECT a.* FROM articles a
        INNER JOIN bookmarks b ON b.articleId = a.id
        ORDER BY b.createdAt DESC
        """
    )
    fun observeBookmarkedArticles(): Flow<List<Article>>
}
