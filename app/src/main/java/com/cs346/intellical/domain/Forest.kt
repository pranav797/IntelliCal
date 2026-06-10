package com.cs346.intellical.domain

data class Forest(
    val slots: List<Tree?>,
    val totalXP: Int = 0,
    val streakDays: Int = 0,
    val gridSize: Int = 5
) {
    val treeCount: Int get() = slots.count { it != null }
    val emptySlotCount: Int get() = slots.count { it == null }

    val forestHealthScore: Int get() {
        if (treeCount == 0) return 0
        val sum: Int = slots.filterNotNull().sumOf { tree ->
            when (tree.healthState) {
                TreeHealthState.THRIVING -> 100
                TreeHealthState.HEALTHY -> 75
                TreeHealthState.STRUGGLING -> 40
                TreeHealthState.WITHERING -> 10
            } as Int
        }
        return sum / treeCount
    }

    val thrivingCount: Int get() = slots.count { it?.healthState == TreeHealthState.THRIVING }
    val matureCount: Int get() = slots.count { it?.growthStage == TreeGrowthStage.MATURE }
    val maxGrowthPercent: Int get() = if (treeCount == 0) 0 else matureCount * 100 / treeCount

    val forestLevel: Int get() {
        val thresholds = listOf(0, 100, 300, 600, 1000, 1500, 2100, 2800, 3600, 4500, 5500)
        return thresholds.indexOfLast { totalXP >= it }.coerceAtLeast(0)
    }

    val xpForCurrentLevel: Int get() {
        val thresholds = listOf(0, 100, 300, 600, 1000, 1500, 2100, 2800, 3600, 4500, 5500)
        return thresholds.getOrElse(forestLevel) { 0 }
    }

    val xpForNextLevel: Int get() {
        val thresholds = listOf(0, 100, 300, 600, 1000, 1500, 2100, 2800, 3600, 4500, 5500)
        return thresholds.getOrElse(forestLevel + 1) { thresholds.last() + 1000 }
    }

    val levelProgress: Float get() {
        val xpInLevel = totalXP - xpForCurrentLevel
        val xpNeeded = xpForNextLevel - xpForCurrentLevel
        return (xpInLevel.toFloat() / xpNeeded).coerceIn(0f, 1f)
    }

    fun plantTree(row: Int, col: Int, type: TreeType): Forest {
        val index = row * gridSize + col
        if (index < 0 || index >= slots.size || slots[index] != null) return this
        val newTree = Tree(
            row = row,
            col = col,
            type = type,
            growthStage = TreeGrowthStage.SAPLING,
            healthState = TreeHealthState.HEALTHY,
            plantedDate = System.currentTimeMillis(),
            growthPoints = 0
        )
        return copy(slots = slots.toMutableList().also { it[index] = newTree })
    }

    fun addXP(pts: Int): Forest = copy(totalXP = totalXP + pts)

    fun updateTree(row: Int, col: Int, updatedTree: Tree): Forest {
        val index = row * gridSize + col
        if (index < 0 || index >= slots.size) return this
        return copy(slots = slots.toMutableList().also { it[index] = updatedTree })
    }

    fun getTree(row: Int, col: Int): Tree? {
        val index = row * gridSize + col
        return slots.getOrNull(index)
    }

    companion object {
        val XP_THRESHOLDS = listOf(0, 100, 300, 600, 1000, 1500, 2100, 2800, 3600, 4500, 5500)

        fun empty(gridSize: Int = 5): Forest =
            Forest(slots = List(gridSize * gridSize) { null }, gridSize = gridSize)
    }
}
