package com.pigs.borrowit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.pigs.borrowit.presentation.navigation.AppNavGraph
import com.pigs.borrowit.presentation.navigation.GraphRoute
import com.pigs.borrowit.ui.theme.BorrowItTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BorrowItTheme {
                /*
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                */
                 BorrowitApp()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BorrowItTheme {
        Greeting("Android")
    }
}

@Composable
fun BorrowitApp(){
    val navController = rememberNavController()
    AppNavGraph(
        navController = navController,
        startDestination = GraphRoute.MAIN
    )
}