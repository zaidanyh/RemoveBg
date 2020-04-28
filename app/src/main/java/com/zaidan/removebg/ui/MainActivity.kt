package com.zaidan.removebg

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK
import com.zaidan.removebg.helper.ServiceGenerator
import com.zaidan.removebg.service.ApiInterface
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import retrofit2.Call
import retrofit2.Response
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var interstitialAd:InterstitialAd
    private lateinit var progress:ProgressDialog
    private var bitmap:Bitmap?=null
    private var uri:Uri?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StartAppSDK.init(this, "203572536", true)
        setContentView(R.layout.activity_main)
        btnSave.isEnabled = false

        InstanceAdmob()
        requestCamera()

        progress = ProgressDialog(this)
        progress.setMessage("Please Wait..")
        swipe.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener{
            override fun onRefresh() {
                refreshItem()
            }
            fun refreshItem() {
                val drawable:Drawable = resources.getDrawable(R.drawable.ic_android_black_24dp)
                image.setImageDrawable(drawable)
                bitmap = null
                btnSave.isEnabled = false
                onItemLoad()
            }
            fun onItemLoad() {
                swipe.isRefreshing = false
            }
        })
    }
    fun InstanceAdmob() {
        MobileAds.initialize(this, object : OnInitializationCompleteListener{
            override fun onInitializationComplete(p0: InitializationStatus?) {
            }
        })
        interstitialAd = InterstitialAd(this)
        interstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        interstitialAd.loadAd(AdRequest.Builder().build())
        interstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                interstitialAd.loadAd(AdRequest.Builder().build())
            }
        }
    }
    private fun requestCamera() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object :MultiplePermissionsListener{
                override fun onPermissionsChecked(p0: MultiplePermissionsReport) {
                    if (p0.areAllPermissionsGranted()) {

                    }
                    if (p0.isAnyPermissionPermanentlyDenied()) {
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(p0: MutableList<PermissionRequest>, p1: PermissionToken) {
                    p1.continuePermissionRequest()
                }
            })
            .onSameThread()
            .check()
    }
    private fun showSettingsDialog() {
        val builder:AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Need Permissions")
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings")
        builder.setPositiveButton("Go to Settings", object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface, which: Int) {
                dialog.cancel()
                openSettings()
            }
        })
        builder.setNegativeButton("Cancel", object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface, which: Int) {
                dialog.cancel()
            }
        })
        builder.show()
    }
    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri:Uri = Uri.fromParts("package", packageName, null)
        intent.setData(uri)
        startActivityForResult(intent, 101)
    }

    fun handleUploadImage(view: View) {
        openFile()
        StartAppAd.showAd(this)
    }
    fun openFile() {
        val builder:AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Upload Image")
        builder.setMessage("Select Image Source")
        builder.setPositiveButton("Camera", object :DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface, which: Int) {
                startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 1)
                dialog.cancel()
            }
        })
        builder.setNegativeButton("Gallery", object :DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface, which: Int) {
                startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 2)
                dialog.cancel()
            }
        })
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            return
        } else if (requestCode == 1) {
            if (data!=null) {
                uri = data.data
                bitmap = data.getExtras()!!.get("data") as Bitmap
                image.setImageBitmap(bitmap)
            }
        } else if (requestCode == 2) {
            if (data!=null) {
                uri = data.data
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                } catch (e:IOException) {
                    e.printStackTrace()
                }
                image.setImageBitmap(bitmap)
            }
        }
    }

    private fun createTempFile(bitmap: Bitmap):File {
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            System.currentTimeMillis().toString()+"_image.png"
        )
        val byteArrayOutpurStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutpurStream)
        val bitmapdata = byteArrayOutpurStream.toByteArray()
        try {
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(bitmapdata)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e:IOException) {
            e.printStackTrace()
        }
        return file
    }
    fun doUpload() {
        val file:File = createTempFile(bitmap!!)
        val reqfile: RequestBody = RequestBody.create(MediaType.parse("image/*"), file)
        val body:MultipartBody.Part = MultipartBody.Part.createFormData("image_file", file.name, reqfile)

        val service:ApiInterface = ServiceGenerator.createService(ApiInterface::class.java, "yyAJs5dY7bD2VxRfBkCqbeY8 ")
        val call = service.Upload(body)
        call.enqueue(object:retrofit2.Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@MainActivity, "No Internet", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val data = response.body()!!.bytes()
                        bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                    }catch (e:IOException){
                        e.printStackTrace()
                    }
                    progress.dismiss()
                    image.setImageBitmap(bitmap)
                    btnSave.isEnabled = true
                } else {
                    progress.dismiss()
                    Toast.makeText(this@MainActivity, "Can't Upload This Image", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    fun handleSave(view: View) {
        bitmap = (image.drawable as BitmapDrawable).bitmap
        val path = Environment.getExternalStorageDirectory()
        val dir = File(path.toString()+"/VictoriaX/")
        dir.mkdir()
        val simpleFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val currentDateandTime = simpleFormat.format(Date())
        val imagename = "RemoveBackground_"+currentDateandTime+".PNG"
        val file = File(dir, imagename)
        val out:OutputStream
        try {
            out = FileOutputStream(file)
            bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            Toast.makeText(this, "Saved Successfully", Toast.LENGTH_SHORT).show()
        }catch (e:FileNotFoundException) {
            e.printStackTrace()
        }catch (e:IOException){
            e.printStackTrace()
        }
    }
    fun handleRemove(view: View) {
        if (bitmap != null) {
            progress.show()
            doUpload()
        } else{
            Toast.makeText(this, "Choose Image First!", Toast.LENGTH_SHORT).show()
        }
    }
}