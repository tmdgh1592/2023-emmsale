package com.emmsale.data.messageRoom

import com.emmsale.data.common.callAdapter.ApiResponse
import com.emmsale.data.messageRoom.dto.MessageRequest
import com.emmsale.data.messageRoom.dto.MessageResponse
import com.emmsale.data.messageRoom.dto.MessageRoomResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MessageRoomService {
    @GET("/rooms/overview")
    suspend fun getMessageRooms(
        @Query("member-id") memberId: Long,
    ): ApiResponse<List<MessageRoomResponse>>

    @GET("/rooms/{roomId}")
    suspend fun getMessagesByRoomId(
        @Path("roomId") roomId: String,
        @Query("member-id") memberId: Long,
    ): ApiResponse<List<MessageResponse>>

    @GET("/rooms")
    suspend fun getMessagesByMemberIds(
        @Query("member-id") myUid: Long,
        @Query("receiver-id") otherUid: Long,
    ): ApiResponse<List<MessageResponse>>

    @POST("/messages")
    suspend fun sendMessage(
        @Body message: MessageRequest,
    ): ApiResponse<Unit>
}