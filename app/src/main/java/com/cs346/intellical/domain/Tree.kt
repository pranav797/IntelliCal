package com.cs346.intellical.domain

enum class TreeGrowthStage(val displayName: String, val sizeMultiplier: Float) {
    SAPLING("Sapling", 0.3f),
    JUVENILE("Juvenile", 0.6f),
    MATURE("Mature", 1.0f)
}

enum class TreeHealthState(val displayName: String) {
    THRIVING("Thriving"),
    HEALTHY("Healthy"),
    STRUGGLING("Struggling"),
    WITHERING("Withering")
}

data class TreeType(
    val id: Int,
    val name: String,
    val emoji: String,
    val description: String,
    val baseColorArgb: Long,
    val thrivingColorArgb: Long = baseColorArgb,
    val witherColorArgb: Long = 0xFF8B7355L,
    val growthRate: Float = 1.0f
) {
    companion object {
        val Oak = TreeType(
            id = 1,
            name = "Oak",
            emoji = "\uD83C\uDF33",
            description = "A strong and sturdy tree that grows slowly but stands the test of time.",
            baseColorArgb = 0xFF52BF90L,
            thrivingColorArgb = 0xFF7CFC00L,
            witherColorArgb = 0xFF8B7355L
        )

        val Pine = TreeType(
            id = 2,
            name = "Pine",
            emoji = "\uD83C\uDF32",
            description = "An evergreen conifer that stays vibrant through all seasons.",
            baseColorArgb = 0xFF2E7D32L,
            thrivingColorArgb = 0xFF4CAF50L,
            witherColorArgb = 0xFF5D4037L
        )

        val Cherry = TreeType(
            id = 3,
            name = "Cherry Blossom",
            emoji = "\uD83C\uDF38",
            description = "A beautiful flowering tree known for its delicate pink petals.",
            baseColorArgb = 0xFFE91E63L,
            thrivingColorArgb = 0xFFFF69B4L,
            witherColorArgb = 0xFF8B4513L
        )

        val Maple = TreeType(
            id = 4,
            name = "Maple",
            emoji = "\uD83C\uDF41",
            description = "A vibrant tree with fiery autumn colors and sweet sap.",
            baseColorArgb = 0xFFFF5722L,
            thrivingColorArgb = 0xFFFF7043L,
            witherColorArgb = 0xFF795548L
        )

        val Willow = TreeType(
            id = 5, name = "Willow", emoji = "\uD83C\uDF3F",
            description = "A graceful tree with flowing branches that sway in the breeze.",
            baseColorArgb = 0xFF81C784L, thrivingColorArgb = 0xFFA5D6A7L, witherColorArgb = 0xFF6D4C41L
        )

        val Bamboo = TreeType(
            id = 6, name = "Bamboo", emoji = "\uD83C\uDF8B",
            description = "A fast-growing giant grass symbolising resilience and flexibility.",
            baseColorArgb = 0xFF8BC34AL, thrivingColorArgb = 0xFFAED581L, witherColorArgb = 0xFF795548L
        )

        val Palm = TreeType(
            id = 7, name = "Palm", emoji = "\uD83C\uDF34",
            description = "A tropical tree that thrives under sunshine and warm weather.",
            baseColorArgb = 0xFFFFB300L, thrivingColorArgb = 0xFFFFD54FL, witherColorArgb = 0xFF8D6E63L
        )

        val Cactus = TreeType(
            id = 8, name = "Cactus", emoji = "\uD83C\uDF35",
            description = "A desert survivor that stores water and stands tall in harsh conditions.",
            baseColorArgb = 0xFF69B276L, thrivingColorArgb = 0xFF81C784L, witherColorArgb = 0xFF78909CL
        )

        val Birch = TreeType(
            id = 9, name = "Birch", emoji = "\uD83C\uDF32",
            description = "A slender tree with white bark, known for its elegance and grace.",
            baseColorArgb = 0xFFE0E0E0L, thrivingColorArgb = 0xFFF5F5F5L, witherColorArgb = 0xFF9E9E9EL
        )

        val Sequoia = TreeType(
            id = 10, name = "Sequoia", emoji = "\uD83C\uDF33",
            description = "One of the tallest trees on Earth, a symbol of ancient strength.",
            baseColorArgb = 0xFF6D4C41L, thrivingColorArgb = 0xFF8D6E63L, witherColorArgb = 0xFF4E342EL
        )

        val Bonsai = TreeType(
            id = 11, name = "Bonsai", emoji = "\uD83C\uDF3F",
            description = "A miniature tree shaped with patience and artistry over many years.",
            baseColorArgb = 0xFF26A69AL, thrivingColorArgb = 0xFF4DB6ACL, witherColorArgb = 0xFF795548L
        )

        val Sakura = TreeType(
            id = 12, name = "Sakura", emoji = "\uD83C\uDF38",
            description = "The iconic Japanese cherry blossom, a symbol of fleeting beauty.",
            baseColorArgb = 0xFFF48FB1L, thrivingColorArgb = 0xFFF8BBD0L, witherColorArgb = 0xFF8D6E63L
        )

        val Eucalyptus = TreeType(
            id = 13, name = "Eucalyptus", emoji = "\uD83C\uDF3F",
            description = "A fast-growing aromatic tree native to Australia.",
            baseColorArgb = 0xFF80CBC4L, thrivingColorArgb = 0xFFB2DFDBL, witherColorArgb = 0xFF607D8BL
        )

        val Baobab = TreeType(
            id = 14, name = "Baobab", emoji = "\uD83C\uDF34",
            description = "The 'Tree of Life' with a massive trunk that stores thousands of litres of water.",
            baseColorArgb = 0xFFBCAAA4L, thrivingColorArgb = 0xFFD7CCC8L, witherColorArgb = 0xFF795548L
        )

        val Redwood = TreeType(
            id = 15, name = "Redwood", emoji = "\uD83C\uDF32",
            description = "A coastal giant celebrated for its towering height and longevity.",
            baseColorArgb = 0xFFB71C1CL, thrivingColorArgb = 0xFFE53935L, witherColorArgb = 0xFF4E342EL
        )

        val Olive = TreeType(
            id = 16, name = "Olive", emoji = "\uD83C\uDF3E",
            description = "An ancient symbol of peace and wisdom, valued for its fruit and oil.",
            baseColorArgb = 0xFF9E9D24L, thrivingColorArgb = 0xFFC6D422L, witherColorArgb = 0xFF795548L
        )

        val Mangrove = TreeType(
            id = 17, name = "Mangrove", emoji = "🌿",
            description = "A coastal tree with tangled roots that protects shorelines.",
            baseColorArgb = 0xFF388E3CL, thrivingColorArgb = 0xFF66BB6AL, witherColorArgb = 0xFF5D4037L
        )

        val Cypress = TreeType(
            id = 18, name = "Cypress", emoji = "🌲",
            description = "A tall, slender tree often associated with Mediterranean landscapes.",
            baseColorArgb = 0xFF1B5E20L, thrivingColorArgb = 0xFF2E7D32L, witherColorArgb = 0xFF4E342EL
        )

        val Dogwood = TreeType(
            id = 19, name = "Dogwood", emoji = "🌸",
            description = "A flowering tree famous for its striking blossoms in spring.",
            baseColorArgb = 0xFFCE93D8L, thrivingColorArgb = 0xFFE1BEE7L, witherColorArgb = 0xFF795548L
        )

        val Fir = TreeType(
            id = 20, name = "Fir", emoji = "🌲",
            description = "A classic evergreen conifer, a symbol of winter and celebration.",
            baseColorArgb = 0xFF1A237EL, thrivingColorArgb = 0xFF3949ABL, witherColorArgb = 0xFF4E342EL
        )

        val allTypes = listOf(
            Oak, Pine, Cherry, Maple, Willow,
            Bamboo, Palm, Cactus, Birch, Sequoia,
            Bonsai, Sakura, Eucalyptus, Baobab, Redwood, Olive,
            Mangrove, Cypress, Dogwood, Fir
        )
    }
}

data class Tree(
    val row: Int,
    val col: Int,
    val type: TreeType? = null,
    val growthStage: TreeGrowthStage = TreeGrowthStage.SAPLING,
    val healthState: TreeHealthState = TreeHealthState.HEALTHY,
    val plantedDate: Long? = null,
    val growthPoints: Int = 0,
    val consecutiveHealthyDays: Int = 0
) {
    val isEmpty: Boolean get() = type == null

    val currentColorArgb: Long get() = when {
        type == null -> 0x00000000L
        healthState == TreeHealthState.THRIVING -> type.thrivingColorArgb
        healthState == TreeHealthState.WITHERING -> type.witherColorArgb
        else -> type.baseColorArgb
    }

    val nextStageThreshold: Int get() = when (growthStage) {
        TreeGrowthStage.SAPLING -> 30
        TreeGrowthStage.JUVENILE -> 100
        TreeGrowthStage.MATURE -> growthPoints
    }

    val currentStageThreshold: Int get() = when (growthStage) {
        TreeGrowthStage.SAPLING -> 0
        TreeGrowthStage.JUVENILE -> 30
        TreeGrowthStage.MATURE -> 100
    }

    val stageProgress: Float get() {
        if (growthStage == TreeGrowthStage.MATURE) return 1f
        val pointsInStage = growthPoints - currentStageThreshold
        val stageRange = nextStageThreshold - currentStageThreshold
        return (pointsInStage.toFloat() / stageRange).coerceIn(0f, 1f)
    }

    val ageDays: Int get() {
        if (plantedDate == null) return 0
        return ((System.currentTimeMillis() - plantedDate) / (1000L * 60 * 60 * 24))
            .toInt().coerceAtLeast(1)
    }

    fun addGrowthPoints(points: Int): Tree {
        val newPoints = growthPoints + points
        val newStage = when {
            newPoints >= 100 -> TreeGrowthStage.MATURE
            newPoints >= 30 -> TreeGrowthStage.JUVENILE
            else -> TreeGrowthStage.SAPLING
        }
        return copy(growthPoints = newPoints, growthStage = newStage)
    }
}
