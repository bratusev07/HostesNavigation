/**
 * Класс для работы с файловой системой
 * @Author Братусев Денис
 * @Since 01.06.2023
 * @Version 1.0
 * */
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

/**
 * Класс для работы с файловой системой
 * @Param [context] для работы с сервисами
 * @Param [locationName] название локации для подгрузки
 * @Constructor Создаёт FileHelper для работы с системой
 */
class FileHelper(private val context: Context, val locationName: String) {

    /**
     * @Param [SDPath] путь к файлам проекта
     * @Param [dataPath] путь для скачивания архива
     * @Param [unzipPath] путь для разархивации
     * */
    @SuppressLint("SdCardPath")
    private val SDPath =
        Environment.getExternalStorageDirectory().absolutePath + "/Android/data/ru.bratusev.hostesnavigation"
    private var dataPath = "$SDPath/files/locations/"
    private var unzipPath = "$SDPath/files/locations/"

    /**
     * Скачивает архив с тайлами и графом навигации
     * @Param [uRl] идентификатор документа для скачивания
     * @See [FileHelper.convertUrl] метод для дополнения ссылки
     * @See [FileHelper.onComplete] ресивер для обработки конца загрузки
     */
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

    /**
     * @Param [onComplete] ресивер для обработки конца загрузки
     * @See [FileHelper.fileDownload]
     * */
    private val onComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            unzip(locationName)
        }
    }

    /**
     * @Param [id] идентификатор документа на google Drive
     * @See [FileHelper.fileDownload]
     * */
    private fun convertUrl(id: String): String {
        return "https://drive.google.com/uc?export=download&confirm=no_antivirus&id=$id"
    }

    /**
     * Получает строковое представление карты из файла
     * @Param name название локации для отображения
     * @Return карта в строковом представлении
     */
    internal fun getJsonMap(name: String): String{
        return try {
            return File("$dataPath$name/map.json").readText()
        }catch (e: Exception){
            e.message.toString()
        }
    }

    /**
     * Метод для разархивации карты и тайлов
     * @Param [fileName] название локации для разархивации
     * @Return успех / провал
     * */
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

    /**
     * Получение количество уровней приближения карты по количеству папок в директории
     * @Param fileName название локации для карты
     * @Return количество уровней приближения
     */
    internal fun getLevelCount(fileName: String): Int {
        val directory = File("$unzipPath/$locationName/$fileName")
        val files = directory.listFiles()
        return files?.size ?: 0
    }
}