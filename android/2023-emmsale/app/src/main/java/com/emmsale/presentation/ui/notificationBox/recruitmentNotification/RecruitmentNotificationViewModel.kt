package com.emmsale.presentation.ui.notificationBox.recruitmentNotification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emmsale.data.common.ApiError
import com.emmsale.data.common.ApiException
import com.emmsale.data.common.ApiSuccess
import com.emmsale.data.eventdetail.EventDetailRepository
import com.emmsale.data.member.MemberRepository
import com.emmsale.data.notification.Notification
import com.emmsale.data.notification.NotificationRepository
import com.emmsale.presentation.KerdyApplication
import com.emmsale.presentation.common.ViewModelFactory
import com.emmsale.presentation.common.livedata.NotNullLiveData
import com.emmsale.presentation.common.livedata.NotNullMutableLiveData
import com.emmsale.presentation.ui.notificationBox.recruitmentNotification.uistate.RecruitmentNotificationBodyUiState
import com.emmsale.presentation.ui.notificationBox.recruitmentNotification.uistate.RecruitmentNotificationHeaderUiState
import com.emmsale.presentation.ui.notificationBox.recruitmentNotification.uistate.RecruitmentNotificationMemberUiState
import com.emmsale.presentation.ui.notificationBox.recruitmentNotification.uistate.RecruitmentNotificationsUiState
import com.emmsale.presentation.ui.notificationBox.recruitmentNotification.uistate.RecruitmentUiState
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class RecruitmentNotificationViewModel(
    private val memberRepository: MemberRepository,
    private val eventDetailRepository: EventDetailRepository,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {
    private val _notifications = NotNullMutableLiveData(RecruitmentNotificationsUiState())
    val notifications: NotNullLiveData<RecruitmentNotificationsUiState> = _notifications

    private val _recruitmentUiState = NotNullMutableLiveData(RecruitmentUiState())
    val recruitmentUiState: NotNullLiveData<RecruitmentUiState> = _recruitmentUiState

    init {
        fetchNotifications()
    }

    private fun fetchNotifications() {
        viewModelScope.launch {
            _notifications.postValue(_notifications.value.copy(isLoading = true))

            when (val notificationsResult = notificationRepository.getNotifications()) {
                is ApiSuccess -> {
                    updateNotifications(notificationsResult)
                    updateNotificationsToReadStatus()
                }

                is ApiException, is ApiError -> _notifications.postValue(
                    RecruitmentNotificationsUiState(
                        isError = true,
                    ),
                )
            }
        }
    }

    private suspend fun updateNotifications(
        notificationsResult: ApiSuccess<List<Notification>>,
    ) {
        val notifications = getNotificationBody(notificationsResult).groupBy { it.conferenceId }
        val notificationHeaders = notifications.map { (conferenceId, notifications) ->
            RecruitmentNotificationHeaderUiState(
                eventId = conferenceId,
                conferenceName = notifications[0].conferenceName,
                notifications = notifications,
            )
        }

        _notifications.value = _notifications.value.copy(notifications = notificationHeaders)
    }

    private suspend fun getNotificationBody(
        result: ApiSuccess<List<Notification>>,
    ): List<RecruitmentNotificationBodyUiState> = result.data.map { notification ->
        val notiMember = getNotificationMemberAsync(notification.otherUid)
        val conferenceName = getConferenceNameAsync(notification.eventId)
        awaitAll(notiMember, conferenceName).run {
            RecruitmentNotificationBodyUiState.from(
                notification = notification,
                notificationMember = getOrNull(MEMBER_INDEX) as? RecruitmentNotificationMemberUiState,
                conferenceName = getOrNull(CONFERENCE_NAME_INDEX).toString(),
            )
        }
    }

    private suspend fun getNotificationMemberAsync(userId: Long): Deferred<RecruitmentNotificationMemberUiState?> =
        viewModelScope.async {
            when (val member = memberRepository.getMember(userId)) {
                is ApiSuccess -> RecruitmentNotificationMemberUiState(member.data.name, member.data.imageUrl)
                is ApiException, is ApiError -> null
            }
        }

    private suspend fun getConferenceNameAsync(conferenceId: Long): Deferred<String?> =
        viewModelScope.async {
            when (val conference = eventDetailRepository.getEventDetail(conferenceId)) {
                is ApiSuccess -> conference.data.name
                is ApiException, is ApiError -> null
            }
        }

    fun toggleExpand(eventId: Long) {
        _notifications.postValue(_notifications.value.toggleNotificationExpanded(eventId))
    }

    fun acceptRecruit(notificationId: Long) {
        viewModelScope.launch {
            _recruitmentUiState.value = recruitmentUiState.value.changeToLoadingState()

            when (notificationRepository.updateRecruitmentAcceptedStatus(notificationId, true)) {
                is ApiSuccess ->
                    _recruitmentUiState.value = recruitmentUiState.value.changeToAcceptedState()

                is ApiException, is ApiError ->
                    _recruitmentUiState.value = recruitmentUiState.value.changeToErrorState()
            }
        }
    }

    fun rejectRecruit(notificationId: Long) {
        viewModelScope.launch {
            _recruitmentUiState.value = recruitmentUiState.value.changeToLoadingState()

            when (notificationRepository.updateRecruitmentAcceptedStatus(notificationId, false)) {
                is ApiSuccess -> {
                    _recruitmentUiState.value = recruitmentUiState.value.changeToRejectedState()
                    _notifications.value = _notifications.value.deleteNotification(notificationId)
                }

                is ApiException, is ApiError ->
                    _recruitmentUiState.value = recruitmentUiState.value.changeToErrorState()
            }
        }
    }

    private fun updateNotificationsToReadStatus() {
        viewModelScope.launch {
            notifications.value.notifications
                .flatMap { header -> header.notifications }
                .filter { notification -> !notification.isRead }
                .forEach { notification -> updateNotificationReadStatus(notification.id) }
        }
    }

    private suspend fun updateNotificationReadStatus(notificationId: Long) {
        notificationRepository.updateNotificationReadStatus(
            notificationId = notificationId,
            isRead = true,
        )
    }

    companion object {
        private const val MEMBER_INDEX = 0
        private const val CONFERENCE_NAME_INDEX = 1

        val factory = ViewModelFactory {
            RecruitmentNotificationViewModel(
                memberRepository = KerdyApplication.repositoryContainer.memberRepository,
                eventDetailRepository = KerdyApplication.repositoryContainer.eventDetailRepository,
                notificationRepository = KerdyApplication.repositoryContainer.notificationRepository,
            )
        }
    }
}