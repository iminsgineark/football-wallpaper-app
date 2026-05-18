package com.meritshot.footballwallpaper.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.meritshot.footballwallpaper.data.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    // ──────────────────────────────────────────────
    // Categories
    // ──────────────────────────────────────────────

    fun getCategoriesFlow(): Flow<List<Category>> = callbackFlow {
        val listener = firestore.collection("categories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(Category::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addCategory(name: String, iconEmoji: String): Result<Unit> {
        return try {
            val data = mapOf("name" to name, "icon_emoji" to iconEmoji)
            firestore.collection("categories").add(data).await()
            Result.Success(Unit)
        } catch (e: Exception) { Result.Error(e) }
    }

    suspend fun deleteCategory(categoryId: String): Result<Unit> {
        return try {
            firestore.collection("categories").document(categoryId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) { Result.Error(e) }
    }

    // ──────────────────────────────────────────────
    // Subcategories
    // ──────────────────────────────────────────────

    fun getSubcategoriesFlow(categoryId: String): Flow<List<Subcategory>> = callbackFlow {
        val listener = firestore.collection("subcategories")
            .whereEqualTo("category_id", categoryId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(Subcategory::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addSubcategory(categoryId: String, name: String): Result<Unit> {
        return try {
            val data = mapOf("category_id" to categoryId, "name" to name)
            firestore.collection("subcategories").add(data).await()
            Result.Success(Unit)
        } catch (e: Exception) { Result.Error(e) }
    }

    // ──────────────────────────────────────────────
    // Wallpapers
    // ──────────────────────────────────────────────

    fun getWallpapersFlow(): Flow<List<Wallpaper>> = callbackFlow {
        val listener = firestore.collection("wallpapers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(Wallpaper::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun getWallpapersByCategoryFlow(categoryId: String): Flow<List<Wallpaper>> = callbackFlow {
        val listener = firestore.collection("wallpapers")
            .whereEqualTo("category_id", categoryId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(Wallpaper::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun getWallpapersBySubcategoryFlow(subcategoryId: String): Flow<List<Wallpaper>> = callbackFlow {
        val listener = firestore.collection("wallpapers")
            .whereEqualTo("subcategory_id", subcategoryId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(Wallpaper::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getWallpaperById(wallpaperId: String): Result<Wallpaper> {
        return try {
            val doc = firestore.collection("wallpapers").document(wallpaperId).get().await()
            val wallpaper = doc.toObject(Wallpaper::class.java)?.copy(id = doc.id)
                ?: throw Exception("Wallpaper not found")
            Result.Success(wallpaper)
        } catch (e: Exception) { Result.Error(e) }
    }

    // ──────────────────────────────────────────────
    // Add Wallpaper via External URL (No Storage needed)
    // ──────────────────────────────────────────────

    suspend fun addWallpaperWithUrl(
        imageUrl: String,
        title: String,
        categoryId: String,
        subcategoryId: String,
        tags: List<String>
    ): Result<Unit> {
        return try {
            val wallpaperData = mapOf(
                "title" to title,
                "image_url" to imageUrl,
                "category_id" to categoryId,
                "subcategory_id" to subcategoryId,
                "tags" to tags,
                "created_at" to Timestamp.now()
            )
            firestore.collection("wallpapers").add(wallpaperData).await()
            Result.Success(Unit)
        } catch (e: Exception) { Result.Error(e) }
    }

    // ──────────────────────────────────────────────
    // Bulk Add (Admin)
    // ──────────────────────────────────────────────

    suspend fun bulkAddWallpapers(
        wallpapers: List<WallpaperData>,
        categoryId: String,
        subcategoryId: String = ""
    ): Result<Int> {
        return try {
            val batch = firestore.batch()
            wallpapers.forEach { wall ->
                val docRef = firestore.collection("wallpapers").document()
                val data = mapOf(
                    "title" to wall.title,
                    "image_url" to wall.imageUrl,
                    "category_id" to categoryId,
                    "subcategory_id" to subcategoryId,
                    "tags" to wall.tags,
                    "created_at" to Timestamp.now()
                )
                batch.set(docRef, data)
            }
            batch.commit().await()
            Result.Success(wallpapers.size)
        } catch (e: Exception) { Result.Error(e) }
    }

    data class WallpaperData(val title: String, val imageUrl: String, val tags: List<String>)

    suspend fun updateWallpaper(
        wallpaperId: String,
        title: String,
        tags: List<String>
    ): Result<Unit> {
        return try {
            firestore.collection("wallpapers").document(wallpaperId)
                .update(mapOf("title" to title, "tags" to tags)).await()
            Result.Success(Unit)
        } catch (e: Exception) { Result.Error(e) }
    }

    suspend fun deleteWallpaper(wallpaper: Wallpaper): Result<Unit> {
        return try {
            // Delete from Firestore
            firestore.collection("wallpapers").document(wallpaper.id).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) { Result.Error(e) }
    }

    // ──────────────────────────────────────────────
    // Favorites
    // ──────────────────────────────────────────────

    fun getFavoritesFlow(): Flow<List<Favorite>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run { trySend(emptyList()); close(); return@callbackFlow }
        val listener = firestore.collection("favorites")
            .whereEqualTo("user_id", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(Favorite::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addFavorite(wallpaperId: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("Not logged in")
            val data = mapOf("user_id" to uid, "wallpaper_id" to wallpaperId)
            firestore.collection("favorites").add(data).await()
            Result.Success(Unit)
        } catch (e: Exception) { Result.Error(e) }
    }

    suspend fun removeFavorite(favoriteId: String): Result<Unit> {
        return try {
            firestore.collection("favorites").document(favoriteId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) { Result.Error(e) }
    }

    suspend fun isFavorite(wallpaperId: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val query = firestore.collection("favorites")
            .whereEqualTo("user_id", uid)
            .whereEqualTo("wallpaper_id", wallpaperId)
            .get().await()
        return !query.isEmpty
    }

    suspend fun getFavoriteId(wallpaperId: String): String? {
        val uid = auth.currentUser?.uid ?: return null
        val query = firestore.collection("favorites")
            .whereEqualTo("user_id", uid)
            .whereEqualTo("wallpaper_id", wallpaperId)
            .get().await()
        return query.documents.firstOrNull()?.id
    }

    /** Get full wallpapers for a list of favorites */
    suspend fun getFavoriteWallpapers(favorites: List<Favorite>): List<Wallpaper> {
        if (favorites.isEmpty()) return emptyList()
        return favorites.mapNotNull { fav ->
            try {
                val doc = firestore.collection("wallpapers").document(fav.wallpaperId).get().await()
                doc.toObject(Wallpaper::class.java)?.copy(id = doc.id)
            } catch (_: Exception) { null }
        }
    }
}
