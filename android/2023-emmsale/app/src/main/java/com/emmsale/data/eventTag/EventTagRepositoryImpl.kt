package com.emmsale.data.eventTag

import com.emmsale.data.common.ApiResult
import com.emmsale.data.common.handleApi
import com.emmsale.data.conference.EventCategory
import com.emmsale.data.eventTag.dto.ConferenceTagApiModel
import com.emmsale.data.eventTag.mapper.toData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventTagRepositoryImpl(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val eventTagService: EventTagService,
) : EventTagRepository {
    override suspend fun getEventTags(category: EventCategory): ApiResult<List<EventTag>> =
        withContext(dispatcher) {
            handleApi(
                response = eventTagService.getConferenceTags(category.text),
                mapToDomain = List<ConferenceTagApiModel>::toData
            )
        }
}