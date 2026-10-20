package com.starshines.mindzen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.starshines.mindzen.ui.MindZenApp
import com.starshines.mindzen.ui.theme.MindZenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MindZenTheme {
                MindZenApp()
            }
        }
    }
}
