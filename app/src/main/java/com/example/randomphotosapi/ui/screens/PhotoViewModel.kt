package com.example.randomphotosapi.ui.screens

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.http.HttpException
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.randomphotosapi.network.PhotoApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

sealed interface PhotoUiState {

    object Success: PhotoUiState

    object Error : PhotoUiState
    object Loading : PhotoUiState
}


const val folderName = "MyImages"

class PhotoViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext

    var photoUiState: PhotoUiState by mutableStateOf(PhotoUiState.Loading)
        private set
    init {
        getMarsPhotos()
    }

    fun getMarsPhotos() {
        viewModelScope.launch {
            photoUiState = PhotoUiState.Loading
            photoUiState = try {
                val listResult = async { PhotoApi.retrofitService.getPhotos() }.await()
                val imgUrls = listResult.map { it.downloadURL }
                async{ fetchImages(imgUrls) }.await()
                PhotoUiState.Success

            } catch (e: IOException) {
                PhotoUiState.Error
            } catch (e: HttpException) {
                PhotoUiState.Error
            }
        }
    }



    suspend fun fetchImages(urls: List<String>): MutableList<Bitmap> {
        var i = 1;
        val results = mutableListOf<Bitmap>()
        try {
            coroutineScope {
                val tasks = List(urls.size) { index -> // Drar igång hela url listans
                    async(Dispatchers.IO) {
                        performTask(urls[index])
                    }
                }
                tasks.forEach {
                    saveImageToInternalStorage(context,it.await(), i)
                    i++;
                }
            }
        } catch (e: Exception) {
            PhotoUiState.Error
        }
        return results
    }



    fun saveImageToInternalStorage(context: Context, bild: Bitmap, i : Int) {
        val fileName = "IMG_$i.jpg"
        val folder = File(context.filesDir, folderName)
        if (!folder.exists()) {
            folder.mkdirs()
        }

        val picFile = File(folder, fileName)

        if (picFile.exists()) {
            return
        }

        try {
            val outputStream = FileOutputStream(picFile)
            outputStream.use {
                bild.compress(Bitmap.CompressFormat.JPEG, 70, it)
            }

        } catch (e: IOException) {
            e.printStackTrace()
            PhotoUiState.Error

        }
    }

    suspend fun performTask(myUrls: String): Bitmap {
        val response = PhotoApi.retrofitService.downloadImages(myUrls)

        val byteArray = response.bytes()
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)

        options.inSampleSize = calculateInSampleSize(options)

        options.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)

        return bitmap
    }

    //https://developer.android.com/topic/performance/graphics/load-bitmap
    fun calculateInSampleSize(options: BitmapFactory.Options): Int {
        val reqWidth = 180;
        val reqHeight = 255;
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            while (height / inSampleSize >= reqHeight && width / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
