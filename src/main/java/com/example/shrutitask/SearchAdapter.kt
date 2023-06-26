package com.infowind.tenszo.Home.Search


import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shrutitask.Constant
import com.example.shrutitask.Constant.URL
import com.example.shrutitask.Constant.downloadID
import com.example.shrutitask.R

import com.example.shrutitask.model.Video
import java.util.*
import kotlin.collections.ArrayList


class SearchAdapter(
        val context: Context,
        private var videoList: ArrayList<Video>
) :
    RecyclerView.Adapter<SearchAdapter.MyViewHolder>() {


    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.txtEmail)
        var videoView: VideoView = view.findViewById(R.id.videoView)
        var videoIcon: ImageView = view.findViewById(R.id.videoIcon)
        var progress: ProgressBar = view.findViewById(R.id.progress)
        var imageView: ImageView = view.findViewById(R.id.imageView)
        var downloadIcon: ImageView = view.findViewById(R.id.downloadIcon)
        var pauseIcon: ImageView = view.findViewById(R.id.pauseIcon)
    }

    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_row, parent, false)
        return MyViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: MyViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val items = videoList[position]


        holder.title.setText(items.title)
        var uri = Uri.parse(Constant.URL+items.thumb)

        Glide.with(context)
            .load(URL+items.thumb)
            .into(holder.imageView)

        holder.videoIcon.setOnClickListener{
            holder.imageView.visibility = View.GONE
            holder.videoIcon.visibility = View.GONE
            holder.videoView.visibility = View.VISIBLE
            holder.downloadIcon.visibility = View.VISIBLE
            holder.pauseIcon.visibility = View.VISIBLE

            initializePlayer(items.sources, holder.videoView, holder.progress)
        }


        holder.pauseIcon.setOnClickListener{
           holder.videoView.stopPlayback()
            holder.pauseIcon.visibility = View.GONE
            holder.videoIcon.visibility = View.VISIBLE
        }
        holder.downloadIcon.setOnClickListener{
            //download(items.sources!!, items.title!!)
            beginDownload(items.sources!!)
        }

    }

    override fun getItemCount(): Int {
        println(" videoList Size from SearchAdapter "+videoList.size)
        return videoList.size

    }

    private fun initializePlayer(videoUrl: String?, videoView: VideoView, progress: ProgressBar) {
        progress?.setVisibility(VideoView.VISIBLE)
        /*  binding.videoView?.setVisibility(VideoView.VISIBLE)*/
        val videoUri = Uri.parse(videoUrl)
        videoView.setVideoURI(videoUri)
        println("VIDEO VIEW....setURL from EditPost");


        videoView.setOnPreparedListener(
            MediaPlayer.OnPreparedListener {
                progress?.setVisibility(VideoView.GONE)
                println("VIDEO VIEW....onPrepared");
                videoView.start()
            })

        videoView.setOnCompletionListener(
            MediaPlayer.OnCompletionListener {

                println("VIDEO VIEW....  oncomplete");
                videoView.stopPlayback()
                // binding.videoView.setVisibility(View.GONE)
                progress?.setVisibility(View.GONE)


            })
    }

 /*   @RequiresApi(Build.VERSION_CODES.M)
    fun download(url : String, title :String){
        val downloader = AndroidDownloader(context, title)
        downloader.downloadFile(url)
    }*/

    private fun beginDownload(url : String) {
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
        val downloadManager = context.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
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
                        Toast.makeText(context, "Download Completed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun getMimeType(uri: Uri): String? {
        val resolver = context.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(resolver.getType(uri))
    }
}