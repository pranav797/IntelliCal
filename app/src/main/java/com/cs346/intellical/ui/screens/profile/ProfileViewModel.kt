package com.cs346.intellical.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.cs346.intellical.domain.Achievement
import com.cs346.intellical.domain.AppModel
import com.cs346.intellical.domain.TreeTypeInfo
import com.cs346.intellical.domain.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AchievementInfo(
    val label: String,
    val unlocked: Boolean
)

data class StatInfo(
    val value: String,
    val label: String
)

data class ProfileUiState(
    val userName: String,
    val userInitial: String,
    val memberSince: String,
    val level: String,
    val stats: List<StatInfo>,
    val trees: List<TreeTypeInfo>,
    val achievements: List<AchievementInfo>,
    val achievementsUnlockedText: String,
    val currentWeight: Float,
    val targetWeight: Float,
    val startWeight: Float,
    val newlyUnlocked: Achievement? = null
) {
    companion object {
        fun fromDomain(user: User, achievements: List<Achievement>, forestLevel: Int, newlyUnlocked: Achievement? = null): ProfileUiState {
            val unlockedCount = achievements.count { it.isUnlocked }
            val totalCount = achievements.size

            return ProfileUiState(
                userName = user.username,
                userInitial = user.initial,
                memberSince = user.memberSince,
                level = "Level $forestLevel",
                stats = listOf(
                    StatInfo("${user.streak}", "Day Streak"),
                    StatInfo("${AppModel.getForest().treeCount}", "Trees"),
                    StatInfo("${user.daysTracked}", "Days Tracked"),
                    StatInfo("${user.mealsLogged}", "Meals Logged")
                ),
                trees = AppModel.getTreeDefinitionsWithLockState(),
                achievements = achievements.map { AchievementInfo(it.label, it.isUnlocked) },
                achievementsUnlockedText = "$unlockedCount/$totalCount unlocked",
                currentWeight = user.currentWeight,
                targetWeight = user.targetWeight,
                startWeight = user.startWeight,
                newlyUnlocked = newlyUnlocked
            )
        }
    }
}

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private fun buildInitialState(): ProfileUiState {
        val user = AppModel.getUser()
        val achievements = AppModel.getAchievements()
        val forestLevel = AppModel.getForest().forestLevel
        return ProfileUiState.fromDomain(user, achievements, forestLevel)
    }

    fun refresh() {
        val user = AppModel.getUser()
        val achievements = AppModel.getAchievements()
        val forestLevel = AppModel.getForest().forestLevel

        val newlyUnlocked = AppModel.consumeUnlock()
        
        _uiState.update { 
            ProfileUiState.fromDomain(user, achievements, forestLevel, newlyUnlocked)
        }
    }

    fun dismissAchievementDialog() {
        _uiState.update { it.copy(newlyUnlocked = null) }
    }
}