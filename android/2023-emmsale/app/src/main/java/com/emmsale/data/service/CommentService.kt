package com.emmsale.data.service

import com.emmsale.data.apiModel.request.ChildCommentCreateRequest
import com.emmsale.data.apiModel.request.CommentReportCreateRequest
import com.emmsale.data.apiModel.request.CommentUpdateRequest
import com.emmsale.data.apiModel.response.CommentFamilyApiModel
import com.emmsale.data.apiModel.response.CommentReportResponse
import com.emmsale.data.apiModel.response.CommentResponse
import com.emmsale.data.common.callAdapter.ApiResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CommentService {

    @GET("/comments")
    suspend fun getComments(
        @Query("eventId") eventId: Long,
    ): ApiResponse<List<CommentFamilyApiModel>>

    @GET("/comments")
    suspend fun getCommentsByMemberId(
        @Query("memberId") memberId: Long,
    ): ApiResponse<List<CommentFamilyApiModel>>

    @GET("/comments/{commentId}")
    suspend fun getComment(
        @Path("commentId") commentId: Long,
    ): ApiResponse<CommentFamilyApiModel>

    @POST("/comments")
    suspend fun saveComment(
        @Body childCommentCreateRequest: ChildCommentCreateRequest,
    ): ApiResponse<CommentResponse>

    @PATCH("/comments/{commentId}")
    suspend fun updateComment(
        @Path("commentId") commentId: Long,
        @Body commentUpdateRequest: CommentUpdateRequest,
    ): ApiResponse<CommentResponse>

    @DELETE("/comments/{commentId}")
    suspend fun deleteComment(
        @Path("commentId") commentId: Long,
    ): ApiResponse<Unit>

    @POST("/reports")
    suspend fun reportComment(
        @Body commentReportCreateRequest: CommentReportCreateRequest,
    ): ApiResponse<CommentReportResponse>
}