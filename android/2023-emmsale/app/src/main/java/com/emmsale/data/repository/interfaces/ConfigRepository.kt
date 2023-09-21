package com.emmsale.data.repository.interfaces

import com.emmsale.data.model.Config

interface ConfigRepository {

    fun getConfig(): Config

    fun saveAllNotificationReceiveConfig(isReceive: Boolean)

    fun saveAutoLoginConfig(isAutoLogin: Boolean)

    fun saveFollowNotificationReceiveConfig(isReceive: Boolean)

    fun saveCommentNotificationReceiveConfig(isReceive: Boolean)

    fun saveInterestEventNotificationReceiveConfig(isReceive: Boolean)

    fun saveMessageNotificationReceiveConfig(isReceive: Boolean)
}