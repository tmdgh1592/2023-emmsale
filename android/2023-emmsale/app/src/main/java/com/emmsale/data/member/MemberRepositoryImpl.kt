package com.emmsale.data.member

import com.emmsale.data.common.ApiError
import com.emmsale.data.common.ApiResult
import com.emmsale.data.common.ApiSuccess
import com.emmsale.data.common.handleApi
import com.emmsale.data.member.dto.MemberApiModel
import com.emmsale.data.member.mapper.toData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class MemberRepositoryImpl(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val memberService: MemberService,
) : MemberRepository {

    override suspend fun getMember(memberId: Long): ApiResult<Member1> = withContext(dispatcher) {
        val memberResponseDeferred = async { memberService.getMember(memberId) }
        val activitiesResponseDeferred = async { memberService.getActivities(memberId) }
        val memberResponse = memberResponseDeferred.await()
        val activitiesResponse = activitiesResponseDeferred.await()

        val memberApiModel = memberResponse.body() ?: return@withContext ApiError(
            memberResponse.code(),
            memberResponse.errorBody().toString(),
        )
        val activitiesApiModels = activitiesResponse.body() ?: return@withContext ApiError(
            activitiesResponse.code(),
            activitiesResponse.errorBody().toString(),
        )

        ApiSuccess(memberApiModel.toData(activitiesApiModels))
    }

    override suspend fun updateMember(member: Member): ApiResult<Unit> = withContext(dispatcher) {
        handleApi(memberService.updateMember(MemberApiModel.from(member))) { }
    }
}
