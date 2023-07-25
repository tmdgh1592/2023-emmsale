package com.emmsale.presentation

import android.app.Application
import com.emmsale.data.common.ServiceFactory
import com.emmsale.presentation.di.RepositoryContainer
import com.emmsale.presentation.di.ServiceContainer

class KerdyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        repositoryContainer = RepositoryContainer(
            context = applicationContext,
            serviceContainer = ServiceContainer(ServiceFactory())
        )
    }

    companion object {
        lateinit var repositoryContainer: RepositoryContainer
            private set
    }
}
