package com.example.getinthegame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.getinthegame.ui.MainScreen
import com.example.getinthegame.ui.theme.GetInTheGameTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            GetInTheGameTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(title = { stringResource(R.string.app_name) })
                    }
                ) { paddingValues -> 
                    MainScreen(
                        paddingValues = paddingValues
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LandingScreenPreview() {
    GetInTheGameTheme {
        MainScreen()
    }
}