package com.meritshot.footballwallpaper.data.util

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSeeder @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * Scans the assets/wallpapers/Football WallPapers folder and syncs it with Firestore.
     * Uses predictable IDs to prevent duplicates.
     */
    suspend fun scanAndSeedAssets(context: Context) {
        try {
            Log.d("DataSeeder", ">>> STARTING AUTOMATIC FOLDER SYNC <<<")
            
            val assetManager = context.assets
            val rootPath = "wallpapers/Football WallPapers"
            val rootFolders = assetManager.list(rootPath)
            
            if (rootFolders.isNullOrEmpty()) {
                Log.e("DataSeeder", "No folders found in assets/$rootPath")
                return
            }

            for (categoryName in rootFolders) {
                // Use the name itself as the ID to prevent duplicates
                val categoryId = "cat_${categoryName.lowercase().replace(" ", "_")}"
                ensureCategory(categoryId, categoryName)
                
                val items = assetManager.list("$rootPath/$categoryName") ?: continue
                for (item in items) {
                    val subPath = "$rootPath/$categoryName/$item"
                    val subItems = assetManager.list(subPath)

                    // If it's a subfolder (like Cristiano Ronaldo)
                    if (!subItems.isNullOrEmpty() && !item.contains(".")) {
                        val subcategoryId = "sub_${categoryId}_${item.lowercase().replace(" ", "_")}"
                        ensureSubcategory(subcategoryId, categoryId, item)
                        
                        for (img in subItems) {
                            if (isImage(img)) {
                                val wallId = "wall_${subPath.replace("/", "_")}_$img"
                                syncWallpaper(wallId, img.substringBeforeLast("."), "asset://$subPath/$img", categoryId, subcategoryId, listOf(categoryName, item))
                            }
                        }
                    } 
                    // If it's a direct image in the category
                    else if (isImage(item)) {
                        val wallId = "wall_${subPath.replace("/", "_")}"
                        syncWallpaper(wallId, item.substringBeforeLast("."), "asset://$subPath", categoryId, "", listOf(categoryName))
                    }
                }
            }
            Log.d("DataSeeder", ">>> AUTOMATIC SYNC COMPLETED <<<")
        } catch (e: Exception) {
            Log.e("DataSeeder", "Sync Error: ${e.message}")
        }
    }

    private suspend fun ensureCategory(id: String, name: String) {
        val emoji = when {
            name.contains("Player", true) -> "⭐"
            name.contains("Club", true) -> "🏟️"
            name.contains("National", true) -> "🌍"
            else -> "⚽"
        }
        firestore.collection("categories").document(id).set(mapOf(
            "name" to name,
            "icon_emoji" to emoji
        )).await()
    }

    private suspend fun ensureSubcategory(id: String, catId: String, name: String) {
        firestore.collection("subcategories").document(id).set(mapOf(
            "category_id" to catId,
            "name" to name
        )).await()
    }

    private suspend fun syncWallpaper(id: String, title: String, url: String, catId: String, subId: String, tags: List<String>) {
        firestore.collection("wallpapers").document(id).set(mapOf(
            "title" to title,
            "image_url" to url,
            "category_id" to catId,
            "subcategory_id" to subId,
            "tags" to tags.map { it.lowercase() },
            "created_at" to Timestamp.now()
        )).await()
    }

    private fun isImage(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")
    }
}
