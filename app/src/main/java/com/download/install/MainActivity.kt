package com.download.install

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.com.download.install.R
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*


class MainActivity : AppCompatActivity() {
    private var versionText: TextView? = null
    private var version: String? = null
    var apIinterface: APIinterface? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        apIinterface = ApiBuilder.create()
        try {
            val pInfo = this.packageManager.getPackageInfo(packageName, 0)
            version = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        versionText = findViewById(R.id.txt_version)
        versionText!!.text = "Current Version " + version!!
    }

    /** TODO: Must need to check the External Storage Permission Because we are storing the
     * ApK in the External Or Internal Storage.
     */
    private fun checkWriteExternalStoragePermission() {

        if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            /** If we have permission than we can Start the Download the task  */
            downloadTask()
        } else {
            /** If we don't have permission than requesting  the permission  */
            requestWriteExternalStoragePermission()
        }
    }

    private fun requestWriteExternalStoragePermission() {
        if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE)
        } else {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE && grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            downloadTask()
        } else {
            Toast.makeText(this@MainActivity, "Permission Not Granted.", Toast.LENGTH_SHORT).show()
        }
    }


    fun download(view: View) {
        /** First check the external storage permission. */
        checkWriteExternalStoragePermission()
    }

    private fun downloadTask() {
        val reqCall = apIinterface?.getREspone("https://a2.files.diawi.com/app-file/pAwTTXSYDfyNCG0KiLQd.apk")
        reqCall?.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {

                    object : AsyncTask<Void, Void, Void>() {
                        override fun doInBackground(vararg voids: Void): Void? {
                            val writtenToDisk = writeResponseBodyToDisk(this@MainActivity, response.body(), null)

                            Log.d(localClassName, "file download was a success? $writtenToDisk")

                            return null
                        }
                    }.execute()
                } else {
                    Log.d(localClassName, "server contact failed")
                }
            }

        })
    }

    companion object {
        private const val MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1001
    }

    private fun writeResponseBodyToDisk(activity: MainActivity, body: ResponseBody?, nothing: Nothing?): Boolean {
        try {

            // todo change the file location/name according to your needs
            val PATH = Environment.getExternalStorageDirectory().toString() + "/Download/"
            val file = File(PATH)
            file.mkdirs()

            val outputFile = File(file, "app-debug.apk")

            if (outputFile.exists()) {
                outputFile.delete()
            }

            val futureStudioIconFile = File(getExternalFilesDir(null).toString() + File.separator + "app.apk")
            val d = futureStudioIconFile.absolutePath
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                val fileReader = ByteArray(4096)

                val fileSize = body?.contentLength()
                var fileSizeDownloaded: Long = 0

                inputStream = body?.byteStream()
                outputStream = FileOutputStream(outputFile)

                while (true) {
                    val read = inputStream!!.read(fileReader)

                    if (read == -1) {
                        break
                    }

                    outputStream.write(fileReader, 0, read)

                    fileSizeDownloaded += read.toLong()

                    Log.d(localClassName, "file download: $fileSizeDownloaded of $fileSize")
                }

                outputStream.flush()
                OpenNewVersion(PATH)
                return true
            } catch (e: IOException) {
                return false
            } finally {
                inputStream?.close()

                outputStream?.close()
            }
        } catch (e: IOException) {
            return false
        }

    }
///storage/emulated/0/Android/data/com.dcastalia.localapkupdatelibrary/files/app.apk


    private fun OpenNewVersion(location: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(getUriFromFile(location),
                "application/vnd.android.package-archive")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
        finish()

    }

    private fun getUriFromFile(location: String): Uri {

        return if (Build.VERSION.SDK_INT < 24) {
            Uri.fromFile(File(location + "app-debug.apk"))
        } else {
            FileProvider.getUriForFile(this,
                    this.applicationContext.packageName + ".provider",
                    File(location + "app-debug.apk"))
        }
    }

}
