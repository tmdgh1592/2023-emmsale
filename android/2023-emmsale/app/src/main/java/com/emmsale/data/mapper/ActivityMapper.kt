package com.emmsale.data.mapper

import com.emmsale.data.apiModel.response.ActivitiesResponse
import com.emmsale.data.apiModel.response.ActivityResponse
import com.emmsale.data.apiModel.response.MemberActivitiesResponse
import com.emmsale.data.model.Activity
import com.emmsale.data.model.ActivityType

fun List<ActivitiesResponse>.toData(): List<Activity> = flatMap { it.toData() }

fun ActivitiesResponse.toData(): List<Activity> = activities.map { it.toData(category) }

@JvmName("mapMemberActivitiesApiModelToData")
fun List<MemberActivitiesResponse>.toData(): List<Activity> = flatMap { it.toData() }

fun MemberActivitiesResponse.toData(): List<Activity> =
    memberActivityResponses.map { it.toData(activityType) }

fun ActivityResponse.toData(category: String): Activity = Activity(
    id = id,
    activityType = category.toData(),
    name = name,
)

private fun String.toData(): ActivityType = when (this) {
    "동아리" -> ActivityType.CLUB
    "교육" -> ActivityType.EDUCATION
    "직무" -> ActivityType.FIELD
    else -> throw IllegalStateException("회원의 활동 Json 데이터를 도메인 모델로 매핑하는 데 실패했습니다. 서버와 Api 스펙을 다시 상의해보세요.")
}