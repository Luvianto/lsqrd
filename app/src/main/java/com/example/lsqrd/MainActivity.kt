package com.example.lsqrd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.lsqrd.ui.AppNavGraph
import com.example.lsqrd.ui.theme.LsqrdTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LsqrdTheme {
                AppNavGraph()
            }
        }
    }
}