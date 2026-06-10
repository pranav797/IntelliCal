package com.cs346.intellical.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class TreeTest {

    private fun newTree(): Tree = Tree(row = 0, col = 0, type = TreeType.Oak)

    @Test
    fun newTree_startsAsSapling() {
        val tree = newTree()
        assertEquals(TreeGrowthStage.SAPLING, tree.growthStage)
    }

    @Test
    fun addPoints_staysInSaplingBelow30() {
        val tree = newTree().addGrowthPoints(29)
        assertEquals(TreeGrowthStage.SAPLING, tree.growthStage)
    }

    @Test
    fun addPoints_advancesToJuvenileAt30() {
        val tree = newTree().addGrowthPoints(30)
        assertEquals(TreeGrowthStage.JUVENILE, tree.growthStage)
    }

    @Test
    fun addPoints_staysJuvenileBetween30And100() {
        val tree = newTree().addGrowthPoints(99)
        assertEquals(TreeGrowthStage.JUVENILE, tree.growthStage)
    }

    @Test
    fun addPoints_advancesToMatureAt100() {
        val tree = newTree().addGrowthPoints(100)
        assertEquals(TreeGrowthStage.MATURE, tree.growthStage)
    }

    @Test
    fun addPoints_staysMatureAbove100() {
        val tree = newTree().addGrowthPoints(999)
        assertEquals(TreeGrowthStage.MATURE, tree.growthStage)
    }

    @Test
    fun stageProgress_saplingAt0pts() {
        val tree = newTree()
        assertEquals(0.0f, tree.stageProgress, 0.001f)
    }

    @Test
    fun stageProgress_saplingHalfway() {
        val tree = newTree().addGrowthPoints(15)
        assertEquals(0.5f, tree.stageProgress, 0.001f)
    }

    @Test
    fun stageProgress_juvenileHalfway() {
        val tree = newTree().addGrowthPoints(65)
        assertEquals(TreeGrowthStage.JUVENILE, tree.growthStage)
        val expected = (65f - 30f) / (100f - 30f)
        assertEquals(expected, tree.stageProgress, 0.001f)
    }

    @Test
    fun stageProgress_matureAlwaysOne() {
        val tree = newTree().addGrowthPoints(100)
        assertEquals(1.0f, tree.stageProgress, 0.001f)
    }
}
