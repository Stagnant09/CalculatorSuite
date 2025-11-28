package org.calculator.ui.utils

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VSpacer(height : Int) {
    Spacer(modifier = Modifier.height(height.dp).fillMaxWidth())
}

@Composable
fun HSpacer(width : Int) {
    Spacer(modifier = Modifier.width(width.dp).fillMaxHeight())
}