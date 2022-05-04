package com.example.cameraandgallery

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.cameraandgallery.databinding.ActivityCameraBinding
import com.example.cameraandgallery.db.MyDbHelper
import com.example.cameraandgallery.model.ImageModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var currentImagePath : String
    private val requestCode = 1
    private lateinit var photoURI: Uri
    private lateinit var myDbHelper: MyDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCameraBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        myDbHelper = MyDbHelper(this)
        initViews()
    }

    private fun initViews() {
        binding.btnOld.setOnClickListener {
            pickImageFromOldCamera()
        }

        binding.btnNew.setOnClickListener {
            pickImageFromNewCamera()
        }

        binding.btnDelete.setOnClickListener {
            clearImages()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val format = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        val externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_$format", ".jpg", externalFilesDir).apply {
            currentImagePath = absolutePath
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun pickImageFromOldCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)

        val photoFile = createImageFile()

        photoFile.also {
            val photoURI = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, it)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(intent, requestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(::currentImagePath.isInitialized){
            binding.ivCamera.setImageURI(Uri.fromFile(File(currentImagePath)))
        }
    }

    private fun pickImageFromNewCamera() {
        val imageFile = createImageFile()
        photoURI = FileProvider.getUriForFile(
            this,
            BuildConfig.APPLICATION_ID,
            imageFile
        )
        getTakeImageContent.launch(photoURI)
    }

    private val getTakeImageContent = registerForActivityResult(ActivityResultContracts.TakePicture()){
        if(it){
            binding.ivCamera.setImageURI(photoURI)
            val openInputStream = contentResolver?.openInputStream(photoURI)
            val file = File(filesDir, "image.jpg")
            val fileOutputStream = FileOutputStream(file)
            openInputStream?.copyTo(fileOutputStream)
            openInputStream?.close()
            fileOutputStream.close()
            val fileAbsolutePath = file.absolutePath

            insertImage(fileAbsolutePath, file)

            Toast.makeText(this, "file copied to $fileAbsolutePath", Toast.LENGTH_SHORT).show()
            Log.d("@@@", "file copied to $fileAbsolutePath")

        }
    }

    private fun insertImage(fileAbsolutePath : String, file : File) {
        val fileInputStream = FileInputStream(file)
        val readBytes = fileInputStream.readBytes()
        val imageModel = ImageModel(fileAbsolutePath, readBytes)
        myDbHelper.insertImage(imageModel)
    }

    private fun clearImages() {
        val filesDir = filesDir
        if(filesDir.isDirectory){
            val listFiles = filesDir.listFiles()

            for (listFile in listFiles){
                Log.d("@@@", "cleared Image : ${listFile.absolutePath}")
                listFile.delete()
            }
            Toast.makeText(this, "Successfully deleted", Toast.LENGTH_SHORT).show()
        }
    }

}