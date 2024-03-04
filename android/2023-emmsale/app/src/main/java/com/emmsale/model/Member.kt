package com.emmsale.model

data class Member(
    val id: Long = DEFAULT_ID,
    val githubUrl: String = "",
    val name: String = "",
    val description: String = "",
    val profileImageUrl: String = "",
    val activities: Set<Activity> = emptySet(),
) {
    val fields: List<Activity>
        get() = activities.filter { it.activityType == ActivityType.INTEREST_FIELD }

    val educations: List<Activity>
        get() = activities.filter { it.activityType == ActivityType.EDUCATION }

    val clubs: List<Activity>
        get() = activities.filter { it.activityType == ActivityType.CLUB }

    companion object {
        private const val DEFAULT_ID = -1L
    }
}