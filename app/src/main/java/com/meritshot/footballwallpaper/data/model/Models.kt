package com.meritshot.footballwallpaper.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

// ──────────────────────────────────────────────
// User model
// ──────────────────────────────────────────────
data class User(
    @DocumentId val id: String = "",
    val email: String = "",
    val role: String = "user",
    val admin: Boolean = false // Matches "admin" field in Firestore
) {
    @get:Exclude // Tells Firebase to ignore this helper property
    val isAdmin: Boolean get() = role == "admin" || admin
}

// ──────────────────────────────────────────────
// Category model
// ──────────────────────────────────────────────
data class Category(
    @DocumentId val id: String = "",
    val name: String = "",
    @get:PropertyName("icon_emoji") @set:PropertyName("icon_emoji")
    var iconEmoji: String = "⚽"
)

// ──────────────────────────────────────────────
// Subcategory model
// ──────────────────────────────────────────────
data class Subcategory(
    @DocumentId val id: String = "",
    @get:PropertyName("category_id") @set:PropertyName("category_id")
    var categoryId: String = "",
    val name: String = ""
)

// ──────────────────────────────────────────────
// Wallpaper model
// ──────────────────────────────────────────────
data class Wallpaper(
    @DocumentId val id: String = "",
    val title: String = "",
    @get:PropertyName("image_url") @set:PropertyName("image_url")
    var imageUrl: String = "",
    @get:PropertyName("category_id") @set:PropertyName("category_id")
    var categoryId: String = "",
    @get:PropertyName("subcategory_id") @set:PropertyName("subcategory_id")
    var subcategoryId: String = "",
    val tags: List<String> = emptyList(),
    @get:PropertyName("created_at") @set:PropertyName("created_at")
    var createdAt: Timestamp? = null
)

// ──────────────────────────────────────────────
// Favorite model
// ──────────────────────────────────────────────
data class Favorite(
    @DocumentId val id: String = "",
    @get:PropertyName("user_id") @set:PropertyName("user_id")
    var userId: String = "",
    @get:PropertyName("wallpaper_id") @set:PropertyName("wallpaper_id")
    var wallpaperId: String = ""
)
