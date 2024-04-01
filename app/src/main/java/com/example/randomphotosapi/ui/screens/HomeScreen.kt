package com.example.randomphotosapi.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import java.io.File


@Composable
fun HomeScreen(
    photoUiState: PhotoUiState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),


    ) {
    when (photoUiState) {
        is PhotoUiState.Loading -> LoadingScreen()
        is PhotoUiState.Success -> ResultScreen(modifier = modifier.fillMaxSize())
        is PhotoUiState.Error -> ErrorScreen( modifier = modifier.fillMaxSize())
    }
}

@Composable
fun LoadingScreen() {
    CircularProgressIndicator()

}


@Composable
fun ErrorScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Failed to load", modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun ResultScreen(modifier: Modifier = Modifier) {
    val imageMap = "/MyImages"
    val context = LocalContext.current
    val path = context.filesDir.path+imageMap
    val files = File(path).listFiles()?.toList() ?: emptyList()
    val columns = 2
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier
    ) {

        items(files) { file ->
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            bitmap?.let {
                ImageComposable(bild = bitmap)
            }
        }
    }
}




@Composable
fun ImageComposable(bild: Bitmap) {

    Box(
        modifier = Modifier
            .size(150.dp)
            .padding(4.dp)
            .clip(RoundedCornerShape(15.dp))
    ) {
        SubcomposeAsyncImage(
            model = bild,
            loading = {
                CircularProgressIndicator()
            },
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            contentDescription = "randomPhotos"
        )
    }
}
