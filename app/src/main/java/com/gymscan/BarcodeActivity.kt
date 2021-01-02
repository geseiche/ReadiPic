package com.gymscan

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.io.IOException

class BarcodeActivity : AppCompatActivity() {

    var isFullScreen : Boolean = true
    lateinit var barcodeImage : ImageView
    val GALLERY_INTENT = 1
    val PHOTO_PATH_KEY = "photo_path_key"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode)

        barcodeImage = findViewById(R.id.barcode_image)

        val encodedPhoto = getPreferences(Context.MODE_PRIVATE).getString(PHOTO_PATH_KEY, null)
        if (encodedPhoto != null) {
            barcodeImage.setImageBitmap(stringToBitmap(encodedPhoto))
        }


        enterFullScreen();

        barcodeImage.setOnClickListener {
            if(isFullScreen)
                exitFullScreen()
            else
                enterFullScreen()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.upload_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.upload_button -> {
                uploadImage()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun uploadImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        println("hello")
        println(intent.resolveActivity(packageManager) != null)

        //if(intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, GALLERY_INTENT)
        //}

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GALLERY_INTENT) {
            saveImage(data?.data)
        }
    }


    fun loadFromUri(photoUri: Uri): Bitmap? {
        var image: Bitmap? = null
        try {
            // check version of Android on device
            image = if (Build.VERSION.SDK_INT > 27) {
                // on newer versions of Android, use the new decodeBitmap method
                val source: ImageDecoder.Source = ImageDecoder.createSource(this.contentResolver, photoUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                // support older versions of Android by using getBitmap
                MediaStore.Images.Media.getBitmap(this.contentResolver, photoUri)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return image
    }

    private fun saveImage(uri : Uri?) {
        var image: Bitmap? = null
        if (uri != null) {
            image = loadFromUri(uri)
        }
        if(image == null) {
            println("An error occurred retrieving image")
            return
        }

        barcodeImage.setImageBitmap(image)

        val outputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val b = outputStream.toByteArray()
        val str = Base64.encodeToString(b, Base64.DEFAULT)

        getPreferences(Context.MODE_PRIVATE).edit().putString(PHOTO_PATH_KEY, str).apply()
    }

    private fun stringToBitmap(str: String): Bitmap? {
        val b = Base64.decode(str, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(b, 0, b.size)
    }

    private fun enterFullScreen() {
        window.decorView.systemUiVisibility = (
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)

        val lp = this.window.attributes
        lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        this.window.attributes = lp


        isFullScreen = true
    }

    private fun exitFullScreen() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

        val lp = this.window.attributes
        lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        this.window.attributes = lp

        isFullScreen = false
    }
}