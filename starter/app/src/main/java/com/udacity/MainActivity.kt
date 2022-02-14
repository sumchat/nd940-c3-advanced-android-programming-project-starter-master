package com.udacity

import android.app.DownloadManager
import android.app.DownloadManager.COLUMN_LOCAL_FILENAME
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Environment

import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

import com.udacity.utils.sendNotification
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.File
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var fileToDownload: String? = null

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


       registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

       custom_button.setOnClickListener {
            download()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
            if(intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE && id == downloadID)
              //  if(action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE) && id == downloadID)
            {
                val myDownloadQuery = DownloadManager.Query()
                myDownloadQuery.setFilterById(downloadID)
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val cursor: Cursor = downloadManager.query(myDownloadQuery)
                if(cursor.moveToFirst()){
                   var downloadStatus = checkStatus(cursor);


                    fileToDownload?.let {
                        if (context != null) {
                            notificationManager.sendNotification(it,downloadStatus,context)
                        }
                    }
                    custom_button.setNewButtonState(ButtonState.Completed)
                    custom_button.isEnabled = true
                   // }
                }
            }
        }
    }

    fun checkStatus(cursor:Cursor): String {
        var statusText = "Status_"
        val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        val status = cursor.getInt(columnIndex)
        val columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
        val reason = cursor.getInt(columnReason)

         when(status){
            DownloadManager.STATUS_SUCCESSFUL -> statusText = "Status_Successful"
            DownloadManager.STATUS_RUNNING -> statusText = "Status_Running"
            DownloadManager.STATUS_FAILED -> statusText = "Status_Failed"
            DownloadManager.STATUS_PAUSED -> statusText = "Status_Paused"
            DownloadManager.STATUS_PENDING -> statusText = "Status_Pending"

        }
        return statusText
    }
    private fun displayToast() {
        Toast.makeText(this, getString(R.string.select_file_for_download), Toast.LENGTH_SHORT).show()
    }
    @RequiresApi(Build.VERSION_CODES.N)
    private fun download() {

        if (downloadUrl.isEmpty()) {
            displayToast()
            return

        }
        custom_button.setNewButtonState(ButtonState.Loading)
        notificationManager = ContextCompat.getSystemService(applicationContext, NotificationManager::class.java) as NotificationManager
        createChannel(getString(R.string.file_notification_channel_id), getString(R.string.file_notification_channel_name))
        var file = File(getExternalFilesDir(null), "/repos")

        if (!file.exists()) {
            file.mkdirs()
        }
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        try {
            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                // DownloadManager.Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setDestinationInExternalFilesDir(this,
                    Environment.DIRECTORY_DOCUMENTS,
                    "/repos/repository.zip"
                )
            custom_button.setNewButtonState(ButtonState.Loading)
            custom_button.isEnabled = false

            // val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadID =
                downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        }
        catch(ex:Exception)
        {
            Toast.makeText(this, getString(R.string.select_file_for_download), Toast.LENGTH_SHORT).show()
            custom_button.isEnabled = true
        }

    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Download is done!"

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    fun isDownloadCompleted(){

    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.radio_pirates ->
                    if (checked) {
                        // Pirates are the best
                        downloadUrl = "https://github.com/bumptech/glide/archive/master.zip"
                        fileToDownload = getString(R.string.glide)
                    }
                R.id.radio_loadApp ->
                    if(checked){
                        downloadUrl =  "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
                        fileToDownload = getString(R.string.loadapp)
                    }
                R.id.radio_ninjas ->
                    if (checked) {
                        // Ninjas rule
                       downloadUrl = "https://github.com/square/retrofit/archive/master.zip"
                        fileToDownload = getString(R.string.retrofit)
                    }
            }
        }
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
        private var downloadUrl=""

    }

}
