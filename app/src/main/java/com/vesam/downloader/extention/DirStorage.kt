package com.vesam.downloader.extention

import android.os.Environment
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

fun getDirPath(): String {
    val quizFolder="quiz_folder"
    val dic =
        Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        ).absolutePath + File.separator + quizFolder
    val parentDir = File(dic)
    if (!parentDir.exists()) parentDir.mkdir()
    return dic
}

fun initFindFileInStorage(name: String): String {
    val resultFile: ArrayList<File> = ArrayList()
    val dic = getDirPath()
    val parentDir = File(dic)
    if (!parentDir.exists()) parentDir.mkdir()
    val files: Queue<File> =
        LinkedList(listOf(*Objects.requireNonNull(parentDir.listFiles())))
    resultFile.addAll(files)
    return when {
        resultFile.isEmpty() -> ""
        else -> initCheckFile(name, resultFile)
    }

}

private fun initCheckFile(name: String, files: ArrayList<File>): String {
    var pathFile = ""
    for (index in 0 until files.size) {
        val fileName = files[index].name
        val hasFile = (name == fileName)
        if (hasFile) {
            pathFile = files[index].path
            break
        }
    }
    return pathFile
}
