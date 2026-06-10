package com.cs346.intellical.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ForestTest {

//    private fun treeWith(health: TreeHealthState) = Tree(
//        row = 0, col = 0,
//        type = TreeType.Oak,
//        healthState = health
//    )

    private fun emptyForest() = Forest.empty()

//    private fun forestWithTrees(vararg trees: Tree): Forest {
//        var f = Forest.empty()
//        for (tree in trees) f = f.plantTree(tree.row, tree.col, tree.type!!)
//        return f
//    }

    @Test
    fun forestLevel_atXP0_isLevel0() {
        val forest = emptyForest()
        assertEquals(0, forest.forestLevel)
    }

    @Test
    fun forestLevel_atXP100_isLevel1() {
        val forest = emptyForest().copy(totalXP = 100)
        assertEquals(1, forest.forestLevel)
    }

    @Test
    fun forestLevel_atXP300_isLevel2() {
        val forest = emptyForest().copy(totalXP = 300)
        assertEquals(2, forest.forestLevel)
    }

    @Test
    fun forestLevel_justBelow100_isLevel0() {
        val forest = emptyForest().copy(totalXP = 99)
        assertEquals(0, forest.forestLevel)
    }

    @Test
    fun treeCount_emptyForest_isZero() {
        val forest = emptyForest()
        assertEquals(0, forest.treeCount)
    }

    @Test
    fun treeCount_withTrees_correct() {
        val forest = emptyForest()
            .plantTree(0, 0, TreeType.Oak)
            .plantTree(1, 1, TreeType.Pine)
        assertEquals(2, forest.treeCount)
    }

    @Test
    fun forestHealthScore_emptyForest_isZero() {
        val forest = emptyForest()
        assertEquals(0, forest.forestHealthScore)
    }

    @Test
    fun forestHealthScore_allThriving_is100() {
        var forest = emptyForest().plantTree(0, 0, TreeType.Oak)
        val idx = 0
        val thriving = forest.slots[idx]!!.copy(healthState = TreeHealthState.THRIVING)
        forest = forest.copy(slots = forest.slots.toMutableList().also { it[idx] = thriving })
        assertEquals(100, forest.forestHealthScore)
    }

    @Test
    fun forestHealthScore_mixedHealth_correct() {
        var forest = emptyForest()
            .plantTree(0, 0, TreeType.Oak)
            .plantTree(0, 1, TreeType.Pine)
        val thriving = forest.slots[0]!!.copy(healthState = TreeHealthState.THRIVING)
        val withering = forest.slots[1]!!.copy(healthState = TreeHealthState.WITHERING)
        forest = forest.copy(slots = forest.slots.toMutableList().also {
            it[0] = thriving
            it[1] = withering
        })
        val expected = (100 + 10) / 2
        assertEquals(expected, forest.forestHealthScore)
    }

    @Test
    fun thrivingCount_correct() {
        var forest = emptyForest()
            .plantTree(0, 0, TreeType.Oak)
            .plantTree(0, 1, TreeType.Pine)
            .plantTree(0, 2, TreeType.Maple)
        val thriving0 = forest.slots[0]!!.copy(healthState = TreeHealthState.THRIVING)
        val thriving1 = forest.slots[1]!!.copy(healthState = TreeHealthState.THRIVING)
        val healthy2 = forest.slots[2]!!.copy(healthState = TreeHealthState.HEALTHY)
        forest = forest.copy(slots = forest.slots.toMutableList().also {
            it[0] = thriving0; it[1] = thriving1; it[2] = healthy2
        })
        assertEquals(2, forest.thrivingCount)
    }
}
