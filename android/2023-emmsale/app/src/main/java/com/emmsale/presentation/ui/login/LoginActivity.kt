package com.emmsale.presentation.ui.login

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.emmsale.BuildConfig
import com.emmsale.R
import com.emmsale.databinding.ActivityLoginBinding
import com.emmsale.presentation.common.extension.checkPostNotificationPermission
import com.emmsale.presentation.common.extension.showToast
import com.emmsale.presentation.ui.login.uistate.LoginUiState
import com.emmsale.presentation.ui.onboarding.OnboardingActivity
import com.emmsale.presentation.utils.builder.uri
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {
    private val viewModel: LoginViewModel by viewModels { LoginViewModel.factory }
    private val binding: ActivityLoginBinding by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) showToast(getString(R.string.post_notification_permission_granted_message))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.viewModel = viewModel
        setupClickListener()
        setupLoginState()
        askNotificationPermission()
    }

    private fun setupClickListener() {
        setupGithubLoginClickListener()
    }

    private fun setupGithubLoginClickListener() {
        binding.btnGithubLogin.setOnClickListener {
            navigateToGithubLogin()
        }
    }

    private fun setupLoginState() {
        viewModel.loginState.observe(this) { loginState ->
            when (loginState) {
                is LoginUiState.Login -> navigateToMain()
                is LoginUiState.Register -> navigateToOnboarding()
                is LoginUiState.Loading -> changeLoadingVisibility(true)
                is LoginUiState.Error -> showLoginFailedMessage()
            }
        }
    }

    private fun navigateToMain() {
        // startActivity(MainActivity.getIntent(this))
        finish()
    }

    private fun navigateToOnboarding() {
        OnboardingActivity.startActivity(this)
        finish()
    }

    private fun navigateToGithubLogin() {
        val customTabIntent = CustomTabsIntent.Builder().build()
        customTabIntent.launchUrl(this, getGithubLoginUri())
    }

    private fun getGithubLoginUri(): Uri = uri {
        scheme("https")
        authority("github.com")
        appendPath("login")
        appendPath("oauth")
        appendPath("authorize")
        appendQueryParameter("client_id", BuildConfig.GITHUB_CLIENT_ID)
    }

    private fun showLoginFailedMessage() {
        changeLoadingVisibility(false)
        Snackbar.make(binding.root, "로그인에 실패했어요 \uD83D\uDE25", Snackbar.LENGTH_SHORT).show()
    }

    private fun changeLoadingVisibility(isShow: Boolean) {
        when (isShow) {
            true -> binding.pbLogin.visibility = View.VISIBLE
            false -> binding.pbLogin.visibility = View.GONE
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        githubLogin(intent)
    }

    private fun githubLogin(intent: Intent?) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
            viewModel.login(
                fcmToken = fcmToken,
                code = intent?.parseGithubCode() ?: ""
            )
        }
    }

    private fun Intent.parseGithubCode(): String? =
        data?.getQueryParameter(GITHUB_CODE_PARAMETER)

    companion object {
        private const val GITHUB_CODE_PARAMETER = "code"
    }

    @SuppressLint("InlinedApi")
    private fun askNotificationPermission() {
        if (checkPostNotificationPermission()) return
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}