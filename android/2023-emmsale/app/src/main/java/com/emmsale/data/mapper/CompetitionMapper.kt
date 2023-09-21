package com.emmsale.data.mapper

import com.emmsale.data.apiModel.response.CompetitionResponse
import com.emmsale.data.model.Competition
import com.emmsale.data.model.CompetitionStatus
import com.emmsale.data.model.EventApplyStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val DATE_TIME_FORMAT = "yyyy:MM:dd:HH:mm:ss"

fun List<CompetitionResponse>.toData(): List<Competition> = map { it.toData() }

fun CompetitionResponse.toData(): Competition = Competition(
    id = id,
    name = name,
    startDate = parseDate(startDate),
    endDate = parseDate(endDate),
    status = status.toData(),
    tags = tags,
    posterUrl = posterUrl,
    dDay = dDay,
    eventApplyStatus = applyStatus.mapToApplyStatus(),
    applyRemainingDays = applyRemainingDays,
    isOnline = isOnline,
    isFree = isFree,
)

private fun parseDate(date: String): LocalDateTime {
    val dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)
    return LocalDateTime.parse(date, dateTimeFormatter)
}

private fun String.toData(): CompetitionStatus = when (this) {
    "IN_PROGRESS" -> CompetitionStatus.IN_PROGRESS
    "UPCOMING" -> CompetitionStatus.SCHEDULED
    "ENDED" -> CompetitionStatus.ENDED
    else -> throw IllegalArgumentException("Unknown conference status: $this")
}

private fun String.mapToApplyStatus(): EventApplyStatus = when (this) {
    "IN_PROGRESS" -> EventApplyStatus.IN_PROGRESS
    "UPCOMING" -> EventApplyStatus.UPCOMING
    "ENDED" -> EventApplyStatus.ENDED
    else -> throw IllegalArgumentException("알 수 없는 신청 상태입니다. api 스펙을 다시 확인해주세요.")
}