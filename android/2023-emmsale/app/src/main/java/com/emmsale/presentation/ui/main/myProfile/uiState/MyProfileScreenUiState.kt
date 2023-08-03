package com.emmsale.presentation.ui.main.myProfile.uiState

import com.emmsale.data.activity.ActivityType
import com.emmsale.data.member.Member1

data class MyProfileScreenUiState(
    val isLoading: Boolean,
    val isError: Boolean,
    val errorMessage: String,
    val memberId: Long,
    val memberName: String,
    val description: String,
    val memberImageUrl: String,
    val jobs: List<ActivityUiState>,
    val educations: List<ActivityUiState>,
    val clubs: List<ActivityUiState>,
    val events: List<ActivityUiState>,
    val isNotLogin: Boolean = false,
) {
    companion object {
        private const val BLANK_DESCRIPTION_SUBSTITUTION = "소개말이 없습니다."

        val Loading = MyProfileScreenUiState(
            isLoading = true,
            isError = false,
            errorMessage = "",
            memberId = -1,
            memberName = "",
            description = "",
            memberImageUrl = "",
            jobs = listOf(),
            educations = listOf(),
            clubs = listOf(),
            events = listOf()
        )

        fun from(member: Member1): MyProfileScreenUiState {
            return MyProfileScreenUiState(
                isLoading = false,
                isError = false,
                errorMessage = "",
                memberId = member.id,
                memberName = member.name,
                description = member.description.ifBlank { BLANK_DESCRIPTION_SUBSTITUTION },
                memberImageUrl = member.imageUrl,
                jobs = member.getActivities(ActivityType.JOB).map(ActivityUiState::from),
                educations = member.getActivities(ActivityType.EDUCATION)
                    .map(ActivityUiState::from),
                clubs = member.getActivities(ActivityType.CLUB).map(ActivityUiState::from),
                events = member.getActivities(ActivityType.EVENT).map(ActivityUiState::from)
            )
        }
    }
}