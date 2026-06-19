package com.educode.app.data.repository

import com.educode.app.domain.models.*
import com.educode.app.domain.repository.ShopRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseShopRepository(
    private val firestore: FirebaseFirestore
) : ShopRepository {

    override suspend fun getShopItems(): Result<List<ShopItem>> {
        return try {
            val result = firestore.collection("shop_items")
                .get()
                .await()
            val items = result.toObjects(ShopItem::class.java)
            
            if (items.isEmpty()) {
                // Seed some default items if collection is empty
                seedDefaultItems()
                return getShopItems()
            }
            
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun seedDefaultItems() {
        val defaultItems = listOf(
            ShopItem("n_neon_01", ItemCategory.THEME, "Neon Cyberpunk", "مظهر نيوني احترافي", 500, rarity = "Epic"),
            ShopItem("n_dark_01", ItemCategory.THEME, "Vantablack", "أسود حالك لعشاق الهدوء", 300, rarity = "Rare"),
            ShopItem("a_hacker_01", ItemCategory.AVATAR, "Ethical Hacker", "شخصية الهكر الأخلاقي", 1000, rarity = "Legendary"),
            ShopItem("a_coder_01", ItemCategory.AVATAR, "Swift Coder", "المبرمج السريع", 200, rarity = "Common"),
            ShopItem("f_gold_01", ItemCategory.FRAME, "Golden Frame", "إطار ذهبي ملكي", 700, rarity = "Epic"),
            ShopItem("e_matrix_01", ItemCategory.EFFECT, "Matrix Rain", "تأثير سقوط أكواد الماتريكس", 1500, rarity = "Legendary")
        )
        
        val batch = firestore.batch()
        defaultItems.forEach { item ->
            val ref = firestore.collection("shop_items").document(item.id)
            batch.set(ref, item)
        }
        batch.commit().await()
    }

    override suspend fun purchaseItem(userId: String, item: ShopItem): Result<Unit> {
        return try {
            val userRef = firestore.collection("users").document(userId)
            
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentCoins = snapshot.getLong("coins") ?: 0
                val purchasedIds = (snapshot.get("purchasedItemIds") as? List<*>)?.map { it.toString() } ?: emptyList()
                
                if (purchasedIds.contains(item.id)) {
                    throw Exception("العنصر مملوك بالفعل")
                }
                
                if (currentCoins >= item.price) {
                    transaction.update(userRef, "coins", currentCoins - item.price)
                    transaction.update(userRef, "purchasedItemIds", FieldValue.arrayUnion(item.id))
                } else {
                        throw Exception("لا تمتلك عملات BIT كافية")
                }
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun equipItem(userId: String, item: ShopItem): Result<Unit> {
        return try {
            val userRef = firestore.collection("users").document(userId)
            val fieldName = when (item.category) {
                ItemCategory.THEME -> "equippedItems.themeId"
                ItemCategory.AVATAR -> "equippedItems.avatarId"
                ItemCategory.FRAME -> "equippedItems.frameId"
                ItemCategory.EFFECT -> "equippedItems.effectId"
            }
            userRef.update(fieldName, item.id).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unequipItem(userId: String, category: ItemCategory): Result<Unit> {
        return try {
            val userRef = firestore.collection("users").document(userId)
            val fieldName = when (category) {
                ItemCategory.THEME -> "equippedItems.themeId"
                ItemCategory.AVATAR -> "equippedItems.avatarId"
                ItemCategory.FRAME -> "equippedItems.frameId"
                ItemCategory.EFFECT -> "equippedItems.effectId"
            }
            userRef.update(fieldName, null).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
