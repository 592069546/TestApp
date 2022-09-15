package com.example.pagingtest.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class ComposeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MessageCard("Hello world")
            }
        }
    }
}

@Composable
fun MessageCard(content: String) {
    Text(content)
}

@Preview
@Composable
fun PreviewMessageCard() {
    MessageCard("Hello world")
}