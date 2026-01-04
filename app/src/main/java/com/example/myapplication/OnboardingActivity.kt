package com.example.myapplication

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.myapplication.ui.NavGraph
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.OnboardingViewModel

class OnboardingActivity : BaseActivity() {

    private val factory by lazy { ViewModelFactory(application) }
    private val onboardingViewModel: OnboardingViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                NavGraph(
                    startDestination = "onboarding",
                    factory = factory
                )
            }
        }
    }
}