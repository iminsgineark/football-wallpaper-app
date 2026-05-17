package com.meritshot.footballwallpaper.data.util

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.meritshot.footballwallpaper.data.model.Category
import com.meritshot.footballwallpaper.data.model.Subcategory
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSeeder @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun scanAndSeedAssets(context: Context) {
        val prefs = context.getSharedPreferences("fw_prefs", Context.MODE_PRIVATE)
        // VERSION 6: This will force a complete fresh sync of your folders
        val currentSeedVersion = 6
        if (prefs.getInt("seed_version", 0) >= currentSeedVersion) return

        try {
            Log.d("DataSeeder", ">>> STARTING FRESH FOLDER SYNC (v6) <<<")
            
            // 1. WIPE OLD DATA (To avoid confusion with old tests)
            val collections = listOf("wallpapers", "categories", "subcategories")
            for (col in collections) {
                val docs = firestore.collection(col).get().await()
                if (!docs.isEmpty) {
                    val batch = firestore.batch()
                    docs.forEach { batch.delete(it.reference) }
                    batch.commit().await()
                    Log.d("DataSeeder", "Cleared old $col")
                }
            }

            val assetManager = context.assets
            val rootPath = "wallpapers/Football WallPapers"
            val rootFolders = assetManager.list(rootPath)
            
            if (rootFolders.isNullOrEmpty()) {
                Log.e("DataSeeder", "No folders found in assets/$rootPath. Did you paste them correctly?")
                return
            }

            for (categoryName in rootFolders) {
                Log.d("DataSeeder", "Processing Category: $categoryName")
                val categoryId = createCategory(categoryName)
                
                val items = assetManager.list("$rootPath/$categoryName") ?: continue
                for (item in items) {
                    val subPath = "$rootPath/$categoryName/$item"
                    val subItems = assetManager.list(subPath)

                    // If it's a subfolder (like Cristiano Ronaldo)
                    if (!subItems.isNullOrEmpty() && !item.contains(".")) {
                        Log.d("DataSeeder", "  Found Subcategory: $item")
                        val subcategoryId = createSubcategory(categoryId, item)
                        
                        val batch = firestore.batch()
                        var count = 0
                        for (img in subItems) {
                            if (isImage(img)) {
                                val wallRef = firestore.collection("wallpapers").document()
                                batch.set(wallRef, mapOf(
                                    "title" to "${item} Wallpaper",
                                    "image_url" to "asset://$subPath/$img",
                                    "category_id" to categoryId,
                                    "subcategory_id" to subcategoryId,
                                    "tags" to listOf(categoryName.lowercase(), item.lowercase()),
                                    "created_at" to Timestamp.now()
                                ))
                                count++
                            }
                        }
                        if (count > 0) {
                            batch.commit().await()
                            Log.d("DataSeeder", "    Added $count images to $item")
                        }
                    } 
                    // If it's a direct image in the category (like Clubs/logo.jpg)
                    else if (isImage(item)) {
                        val wallRef = firestore.collection("wallpapers").document()
                        firestore.collection("wallpapers").add(mapOf(
                            "title" to item.substringBeforeLast("."),
                            "image_url" to "asset://$subPath",
                            "category_id" to categoryId,
                            "tags" to listOf(categoryName.lowercase()),
                            "created_at" to Timestamp.now()
                        )).await()
                        Log.d("DataSeeder", "  Added direct image: $item")
                    }
                }
            }

            prefs.edit().putInt("seed_version", currentSeedVersion).apply()
            Log.d("DataSeeder", ">>> FOLDER SYNC COMPLETED SUCCESSFULLY <<<")

        } catch (e: Exception) {
            Log.e("DataSeeder", "Sync Error: ${e.message}")
            e.printStackTrace()
        }
    }

    private suspend fun createCategory(name: String): String {
        val ref = firestore.collection("categories").document()
        val emoji = when {
            name.contains("Player", true) -> "⭐"
            name.contains("Club", true) -> "🏟️"
            name.contains("National", true) -> "🌍"
            else -> "⚽"
        }
        ref.set(mapOf("name" to name, "icon_emoji" to emoji)).await()
        return ref.id
    }

    private suspend fun createSubcategory(catId: String, name: String): String {
        val ref = firestore.collection("subcategories").document()
        ref.set(mapOf("category_id" to catId, "name" to name)).await()
        return ref.id
    }

    private fun isImage(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")
    }
}
