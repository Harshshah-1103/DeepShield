package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.navigation.DeepShieldApp
import com.example.ui.theme.DeepShieldTheme
import com.example.ui.viewmodel.ForensicViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ForensicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeepShieldTheme {
                DeepShieldApp(viewModel = viewModel)
            }
        }
    }
}
