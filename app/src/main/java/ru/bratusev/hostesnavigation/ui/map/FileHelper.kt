package ru.bratusev.hostesnavigation.ui.map

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.registerReceiver
import net.lingala.zip4j.ZipFile
import java.io.File

class FileHelper(private val context: Context, val locationName: String) {

    @SuppressLint("SdCardPath")
    private val SDPath =
        Environment.getExternalStorageDirectory().absolutePath + "/Android/data/ru.bratusev.hostesnavigation"
    private var dataPath = "$SDPath/files/locations/"
    private var unzipPath = "$SDPath/files/locations/"

    internal fun fileDownload(uRl: String) {
        dataPath+= "$locationName/"
        unzipPath+= "$locationName/"
        val request = DownloadManager.Request(Uri.parse(convertUrl(uRl)))
            .setTitle("$locationName.zip")
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, "locations/$locationName", "$locationName.zip")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        context.registerReceiver(onComplete, intentFilter)
    }

    private val onComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            unzip(locationName)
        }
    }

    private fun convertUrl(id: String): String {
        return "https://drive.google.com/uc?export=download&confirm=no_antivirus&id=$id"
    }

    internal fun getJsonMap(name: String): String{
        return try {
            return File("$dataPath$name/map.json").readText()
        }catch (e: Exception){
            e.message.toString()
        }
    }

    private fun unzip(fileName: String): Boolean? {
        return try {
            val zipFile = ZipFile("$dataPath$fileName.zip")
            zipFile.extractAll(unzipPath)
            File("$dataPath$fileName.zip").delete()
            Toast.makeText(context, "Установка успешна", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Log.d("MyLog", e.message.toString())
            false
        }
    }

    internal fun getLevelCount(fileName: String): Int {
        val directory = File("$unzipPath/$locationName/$fileName")
        val files = directory.listFiles()
        return files?.size ?: 0
    }
}