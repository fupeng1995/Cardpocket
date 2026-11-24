package com.example.cardpocket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.cardpocket.ui.CardPocketApp
import com.example.cardpocket.ui.theme.CardPocketTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CardPocketTheme {
                CardPocketApp()
            }
        }
    }
}
