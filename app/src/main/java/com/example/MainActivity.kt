package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge drawing for beautiful modern status and bottom bars
        enableEdgeToEdge()
        setContent {
            val musicViewModel: MusicViewModel = viewModel()
            MyApplicationTheme(viewModel = musicViewModel) {
                MainContainer(viewModel = musicViewModel)
            }
        }
    }
}
