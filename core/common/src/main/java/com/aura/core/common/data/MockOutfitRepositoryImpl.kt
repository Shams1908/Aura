package com.aura.core.common.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockOutfitRepositoryImpl @Inject constructor() : OutfitRepository {

    private val mockOutfits = listOf(
        OutfitModel(
            id = "1",
            title = "Oversized Knit Hoodie",
            brand = "Uniqlo",
            description = "A warm, premium cotton-blend knit hoodie with a modern oversized fit. Perfect for layering in transitional weather.",
            imageUrl = "https://images.unsplash.com/photo-1556821840-3a63f95609a7?q=80&w=600",
            category = "Oversized",
            color = "Beige",
            tags = listOf("Cozy", "Minimalist", "Oversized"),
            price = 49.90
        ),
        OutfitModel(
            id = "2",
            title = "Vintage Denim Trucker Jacket",
            brand = "Levi's",
            description = "Classic vintage fit denim jacket featuring faded wash patterns and authentic metal button details. Designed to age gracefully.",
            imageUrl = "https://images.unsplash.com/photo-1611312449412-6cefac5dc3e4?q=80&w=600",
            category = "Vintage",
            color = "Blue Denim",
            tags = listOf("Vintage", "Denim", "Classic"),
            price = 89.50
        ),
        OutfitModel(
            id = "3",
            title = "Korean Summer Linen Blazer",
            brand = "Mango",
            description = "Lightweight structure crafted from organic flax linen. Designed in Seoul, featuring an unlined back for maximum summer breathability.",
            imageUrl = "https://images.unsplash.com/photo-1548624149-f9b1859aa730?q=80&w=600",
            category = "Summer Korean",
            color = "Cream",
            tags = listOf("Korean", "Linen", "Blazer"),
            price = 79.99
        ),
        OutfitModel(
            id = "4",
            title = "Pleated Tailored Trousers",
            brand = "Zara",
            description = "High-waist tailored pants featuring front pleats, pockets, and straight-leg flow. An essential foundation for casual tailoring.",
            imageUrl = "https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?q=80&w=600",
            category = "Business Casual",
            color = "Charcoal",
            tags = listOf("Tailored", "Trousers", "Sleek"),
            price = 59.90
        ),
        OutfitModel(
            id = "5",
            title = "Casual Linen Shirt",
            brand = "H&M",
            description = "A relaxed-fit linen shirt with a soft resort collar. Breathable, pre-washed fabric offers a broken-in luxury feel.",
            imageUrl = "https://images.unsplash.com/photo-1603252109303-2751441dd157?q=80&w=600",
            category = "Summer Korean",
            color = "White",
            tags = listOf("Linen", "Casual", "Beach"),
            price = 29.99
        ),
        OutfitModel(
            id = "6",
            title = "Monochrome Tech Parka",
            brand = "Zara",
            description = "Waterproof nylon shell featuring tape-sealed seams, modular hood adjustments, and an internal harness carrier.",
            imageUrl = "https://images.unsplash.com/photo-1544441893-675973e31985?q=80&w=600",
            category = "Oversized",
            color = "Matte Black",
            tags = listOf("Techwear", "Waterproof", "Minimal"),
            price = 129.00
        ),
        OutfitModel(
            id = "7",
            title = "Relaxed Double-Breasted Trench",
            brand = "Mango",
            description = "Classic silhouette reimagined with a fluid drape, dropped shoulders, and a self-tie belt. Fully lined.",
            imageUrl = "https://images.unsplash.com/photo-1591047139829-d91aecb6caea?q=80&w=600",
            category = "Business Casual",
            color = "Khaki",
            tags = listOf("Trenchcoat", "Aesthetic", "Layering"),
            price = 149.99
        ),
        OutfitModel(
            id = "8",
            title = "Classic Leather Varsity Jacket",
            brand = "Vintage",
            description = "Retro-style leather wool blend varsity jacket with ribbed collar, cuffs, and snap buttons. Sourced from vintage collections.",
            imageUrl = "https://images.unsplash.com/photo-1551028719-00167b16eac5?q=80&w=600",
            category = "Vintage",
            color = "Forest Green",
            tags = listOf("Retro", "Varsity", "Leather"),
            price = 180.00
        ),
        OutfitModel(
            id = "9",
            title = "Light Knit Polo Shirt",
            brand = "Uniqlo",
            description = "Finely woven polo crafted from premium Supima cotton. Offers a slightly boxy retro silhouette.",
            imageUrl = "https://images.unsplash.com/photo-1581655353564-df123a1eb820?q=80&w=600",
            category = "Summer Korean",
            color = "Olive",
            tags = listOf("Knit", "Supima", "Classic"),
            price = 39.90
        ),
        OutfitModel(
            id = "10",
            title = "Oversized Boxy Tee",
            brand = "H&M",
            description = "Heavyweight organic cotton jersey with dropped shoulders, wide sleeves, and a clean thick mock neck line.",
            imageUrl = "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?q=80&w=600",
            category = "Oversized",
            color = "White",
            tags = listOf("Essential", "Heavyweight", "Boxy"),
            price = 19.99
        ),
        OutfitModel(
            id = "11",
            title = "Tailored Check Blazer",
            brand = "Mango",
            description = "Double-breasted checkered blazer. Slim lapels and flap pockets create an instant smart office finish.",
            imageUrl = "https://images.unsplash.com/photo-1598808503746-f34c53b9323e?q=80&w=600",
            category = "Business Casual",
            color = "Gray Check",
            tags = listOf("Checked", "Tailored", "Blazer"),
            price = 99.90
        ),
        OutfitModel(
            id = "12",
            title = "Retro Ribbed Cardigan",
            brand = "Vintage",
            description = "Soft wool-blend chunky knit cardigan. Hand-stitched tortoiseshell button closures.",
            imageUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?q=80&w=600",
            category = "Vintage",
            color = "Brown",
            tags = listOf("Cardigan", "Chunky", "Cozy"),
            price = 75.00
        ),
        OutfitModel(
            id = "13",
            title = "Relaxed Drawstring Shorts",
            brand = "Uniqlo",
            description = "Airy linen-cotton blend shorts with a comfortable elastic waistband and adjustable drawcord.",
            imageUrl = "https://images.unsplash.com/photo-1591195853828-11db59a44f6b?q=80&w=600",
            category = "Summer Korean",
            color = "Sand",
            tags = listOf("Linen", "Shorts", "Beachwear"),
            price = 29.90
        ),
        OutfitModel(
            id = "14",
            title = "Heavyweight Oversized Sweatshirt",
            brand = "Nike",
            description = "Loopback fleece cotton crewneck sweatshirt. Vintage wash gives it an instantly cozy, broken-in character.",
            imageUrl = "https://images.unsplash.com/photo-1563178406-4cd2c5d07bb9?q=80&w=600",
            category = "Oversized",
            color = "Navy Blue",
            tags = listOf("Crewneck", "Fleece", "Athletic"),
            price = 65.00
        ),
        OutfitModel(
            id = "15",
            title = "Sleek Chelsea Boots",
            brand = "Zara",
            description = "Smooth Italian calfskin leather upper with premium stretch gore sides and robust stacked heels.",
            imageUrl = "https://images.unsplash.com/photo-1608256246200-53e635b5b65f?q=80&w=600",
            category = "Business Casual",
            color = "Tan",
            tags = listOf("Leather", "Boots", "Footwear"),
            price = 119.00
        ),
        OutfitModel(
            id = "16",
            title = "High-Waist Wide-Leg Denim",
            brand = "H&M",
            description = "Vintage-inspired light wash rigid denim. Sits high at the waist with an ultra-flattering wide-leg profile.",
            imageUrl = "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?q=80&w=600",
            category = "Vintage",
            color = "Light Blue",
            tags = listOf("High-waist", "Wide-leg", "Rigid"),
            price = 39.99
        ),
        OutfitModel(
            id = "17",
            title = "Korean Knitted Vest",
            brand = "Mango",
            description = "V-neck knit sweater vest featuring side slit accents and a relaxed boxy fit. Ideal for layering over collared shirts.",
            imageUrl = "https://images.unsplash.com/photo-1620799140408-edc6dcb6d633?q=80&w=600",
            category = "Summer Korean",
            color = "Cream",
            tags = listOf("Sweater-Vest", "Layering", "Korean"),
            price = 45.00
        ),
        OutfitModel(
            id = "18",
            title = "Utility Cargo Pants",
            brand = "Uniqlo",
            description = "Tough cotton ripstop cargo pants. Multi-pocket design with customizable drawcord ankle cuffs.",
            imageUrl = "https://images.unsplash.com/photo-1517423738875-5ce310acd3da?q=80&w=600",
            category = "Oversized",
            color = "Sage Green",
            tags = listOf("Cargo", "Utility", "Ripstop"),
            price = 49.90
        ),
        OutfitModel(
            id = "19",
            title = "Smart Merino Sweater",
            brand = "Uniqlo",
            description = "Knitted with ultra-fine wool fibers. Offers lightweight warmth and an elegant finish suited for office wear.",
            imageUrl = "https://images.unsplash.com/photo-1614975058789-41316d0e2e9c?q=80&w=600",
            category = "Business Casual",
            color = "Charcoal",
            tags = listOf("Merino", "Knitwear", "Office"),
            price = 59.90
        ),
        OutfitModel(
            id = "20",
            title = "Vintage Suede Bomber",
            brand = "Zara",
            description = "Crafted from genuine goatskin suede. Supple ribbed hem detailing and double zip front enclosures.",
            imageUrl = "https://images.unsplash.com/photo-1495105787522-5334e3ffa0ef?q=80&w=600",
            category = "Vintage",
            color = "Camel",
            tags = listOf("Suede", "Bomber", "Luxury"),
            price = 199.00
        )
    )

    override fun getTrendingOutfits(): Flow<List<OutfitModel>> = flow {
        delay(300) // Simulate mild network delay
        emit(mockOutfits.take(8))
    }

    override fun getRecommendedOutfits(): Flow<List<OutfitModel>> = flow {
        delay(300)
        emit(mockOutfits.shuffled().take(8))
    }

    override fun searchOutfits(query: String): Flow<List<OutfitModel>> = flow {
        delay(400)
        if (query.isBlank()) {
            emit(mockOutfits)
        } else {
            val lowerQuery = query.lowercase()
            val filtered = mockOutfits.filter {
                it.title.lowercase().contains(lowerQuery) ||
                it.brand.lowercase().contains(lowerQuery) ||
                it.category.lowercase().contains(lowerQuery) ||
                it.tags.any { tag -> tag.lowercase().contains(lowerQuery) }
            }
            emit(filtered)
        }
    }

    override fun getOutfitDetails(id: String): Flow<OutfitModel?> = flow {
        delay(200)
        emit(mockOutfits.find { it.id == id })
    }
}
