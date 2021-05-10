package com.vesam.downloader

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.Progress
import com.google.gson.Gson
import com.vesam.downloader.databinding.ActivityMainBinding
import com.vesam.downloader.model.file_download.FileDownload
import com.vesam.downloader.extention.getDirPath

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val REQUEST_ID_MULTIPLE_PERMISSIONS = 4
    private var downloadId: Int = 0
    private val requiredPermissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initAction()
    }

    private fun initAction() {
        initOnClick()
    }

    private fun initOnClick() {
        binding.btnDownload.setOnClickListener { initPermissions() }
    }

    private fun initPermissions() {
        when {
            !checkAndRequestPermissions() -> initToast(resources.getString(R.string.permission_denied))
            else -> initLoadJson()
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val neededPermissions: List<String> = getNeededPermissions()
        if (neededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    neededPermissions.toTypedArray(),
                    REQUEST_ID_MULTIPLE_PERMISSIONS
            )
            return false
        }
        return true
    }

    private fun getNeededPermissions(): List<String> {
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in requiredPermissions) {
                if (ContextCompat.checkSelfPermission(
                                this,
                                permission
                        ) != PackageManager.PERMISSION_GRANTED
                ) listPermissionsNeeded.add(permission)
            }
        }
        return listPermissionsNeeded
    }

    private fun initLoadJson() {
        val fileDownload=getFileDownload()
        initDownload(fileDownload)
    }

    private fun getFileDownload(): FileDownload {
        val objectString: String = readJSONFromAsset()
        val gson = Gson()
        return gson.fromJson(objectString, FileDownload::class.java)
    }

    private fun readJSONFromAsset(): String {
        return assets.open("json/file_download.json").bufferedReader()
                .use { it.readText() }
    }

    private fun initDownload(fileDownload: FileDownload) {
        //val fileName=fileDownload.title+".mp4" //error api 30
        val fileName="A does.mp4"  //success api 30
        downloadId = PRDownloader.download(fileDownload.url, getDirPath(), fileName)
                .build()
                .setOnProgressListener { initOnProgress(it) }
                .start(object : OnDownloadListener {
                    override fun onDownloadComplete() = initDownloadComplete()

                    override fun onError(error: com.downloader.Error?) {
                        error!!.connectionException.let { if (it != null) initToast(it.message!!) }
                    }
                })
    }

    private fun initDownloadComplete() {
        initToast(resources.getString(R.string.download_successfully))
    }

    private fun initToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("SetTextI18n")
    private fun initOnProgress(it: Progress?) {
        val progress = (it!!.currentBytes * 100 / it.totalBytes).toInt()
        binding.txtProgress.text = "${progress}%"
        binding.progressBar.progress = progress
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ID_MULTIPLE_PERMISSIONS -> initRequestCode(grantResults)
        }
    }

    private fun initRequestCode(grantResults: IntArray) {
        when {
            requiredPermissions.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED -> initLoadJson()
            else -> initToast(resources.getString(R.string.permission_denied))
        }
        return
    }

    override fun onDestroy() {
        super.onDestroy()
        PRDownloader.pause(downloadId)
        PRDownloader.cancel(downloadId)
    }
}