package ru.bratusev.hostesnavigation.ui.map

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import net.lingala.zip4j.ZipFile
import java.io.File

class FileHelper(private val context: Context) {

    @SuppressLint("SdCardPath")
    private val SDPath =
        Environment.getExternalStorageDirectory().absolutePath + "/Android/data/ru.bratusev.hostesnavigation"
    private val dataPath = "$SDPath/files/tiles/"
    private val unzipPath = "$SDPath/files/tiles/"

    internal fun fileDownload(uRl: String?) {
        val request = DownloadManager.Request(Uri.parse(uRl))
            .setTitle("tiles.zip")
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, "tiles", "tiles.zip")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

    internal fun unzip(fileName: String): Boolean? {
        return try {
            val zipFile = ZipFile(dataPath + fileName)
            zipFile.extractAll(unzipPath)
            true
        } catch (e: Exception) {
            Log.d("MyLog", e.message.toString())
            false
        }
    }

    internal fun getLevelCount(fileName: String): Int{
        val directory = File("$unzipPath/$fileName")
        val files = directory.listFiles()
        return files?.size ?: 0
    }
}