package com.example.shrutitask

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shrutitask.Constant.downloadID
import com.example.shrutitask.databinding.ActivityMainBinding
import com.example.shrutitask.model.Video
import com.infowind.tenszo.Home.Search.SearchAdapter
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val videoList = ArrayList<Video>()
    private var searchAdapter: SearchAdapter? = null
    private var layoutManager: LinearLayoutManager? = null
    private var mViewModel: MainViewModel? = null

    private val WRITE_PERMISSION = 1001
    private val btn_download: Button? = null
  //  private var downloadID: Long = 0

    // using broadcast method
    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            //Fetching the download id received with the broadcast
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                Toast.makeText(this@MainActivity, "Download Completed", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )

        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.searchRecycler.setLayoutManager(layoutManager)

        mViewModel = ViewModelProvider(this, defaultViewModelProviderFactory).get(
            MainViewModel::class.java
        )

        mViewModel!!.parseJson(this)

        mViewModel!!.getList()?.observe(this) { list ->

            if(list != null){
                searchAdapter = SearchAdapter(this, list )
                binding.searchRecycler.setAdapter(searchAdapter)
            }
        }

        registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
//        btn_download?.setOnClickListener {
//            beginDownload()
//            }


    }
    override fun onDestroy() {
        super.onDestroy()
        // using broadcast method
        unregisterReceiver(onDownloadComplete)
    }

   /* private fun beginDownload() {
        // String url = "http://speedtest.ftp.otenet.gr/files/test10Mb.db";
        val url = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
        val uri: Uri = Uri.parse(url)
        var fileName = url.substring(url.lastIndexOf('/') + 1)
        fileName = fileName.substring(0, 1).uppercase(Locale.getDefault()) + fileName.substring(1)
        //NOT USING THIS AS OF NOW
        // File file = Util.createDocumentFile(fileName, getApplicationContext());
        var request: DownloadManager.Request? =
            null // Set if download is allowed on roaming network
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            request = DownloadManager.Request(uri)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // Visibility of the download Notification
                //.setDestinationUri(Uri.fromFile(file))// Uri of the destination file
                .setTitle(fileName) // Title of the Download Notification
                .setDescription("Downloading") // Description of the Download Notification
                .setRequiresCharging(false) // Set if charging is required to begin the download
                .setAllowedOverMetered(true) // Set if download is allowed on Mobile network
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setMimeType(getMimeType(uri))
        }
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request) // enqueue puts the download request in the queue.

        // using query method
        var finishDownload = false
        var progress: Int
        while (!finishDownload) {
            val cursor: Cursor =
                downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
            if (cursor.moveToFirst()) {
                @SuppressLint("Range") val status: Int = cursor.getInt(
                    cursor.getColumnIndex(
                        DownloadManager.COLUMN_STATUS
                    )
                )
                when (status) {
                    DownloadManager.STATUS_FAILED -> {
                        finishDownload = true
                    }
                    DownloadManager.STATUS_PAUSED -> {}
                    DownloadManager.STATUS_PENDING -> {}
                    DownloadManager.STATUS_RUNNING -> {
                        @SuppressLint("Range") val total: Long = cursor.getLong(
                            cursor.getColumnIndex(
                                DownloadManager.COLUMN_TOTAL_SIZE_BYTES
                            )
                        )
                        if (total >= 0) {
                            @SuppressLint("Range") val downloaded: Long = cursor.getLong(
                                cursor.getColumnIndex(
                                    DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR
                                )
                            )
                            progress = (downloaded * 100L / total).toInt()
                            // if you use downloadmanger in async task, here you can use like this to display progress.
                            // Don't forget to do the division in long to get more digits rather than double.
                            //  publishProgress((int) ((downloaded * 100L) / total));
                        }
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        progress = 100
                        // if you use aysnc task
                        // publishProgress(100);
                        finishDownload = true
                        Toast.makeText(this@MainActivity, "Download Completed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun getMimeType(uri: Uri): String? {
        val resolver = contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(resolver.getType(uri))
    }*/

}