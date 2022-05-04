package com.example.cameraandgallery

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore.Images.Media.insertImage
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.cameraandgallery.databinding.ActivityGalleryBinding
import com.example.cameraandgallery.databinding.ActivityMainBinding
import com.example.cameraandgallery.db.MyDbHelper
import com.example.cameraandgallery.model.ImageModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class GalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGalleryBinding
    private var REQUEST_CODE = 1
    private lateinit var myDbHelper: MyDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        myDbHelper = MyDbHelper(this)
        initViews()
    }

    private fun initViews() {
        binding.btnOld.setOnClickListener {
            pickImageFromOldGallery()
        }

        binding.btnNew.setOnClickListener {
            pickImageFromNewGallery()
        }

        binding.btnDelete.setOnClickListener {
            clearImages()
        }

    }

    private fun pickImageFromOldGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val uri = data?.data ?: return
            binding.ivGallery.setImageURI(uri)

            val openInputStream = contentResolver?.openInputStream(uri)
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

    private fun pickImageFromNewGallery() {
        getImageContent.launch("image/*")

    }

    private val getImageContent = registerForActivityResult(ActivityResultContracts.GetContent()){ uri ->
        uri ?: return@registerForActivityResult
        binding.ivGallery.setImageURI(uri)

        val openInputStream = contentResolver?.openInputStream(uri)
        val file = File(filesDir, "image.jpg")
        val fileOutputStream = FileOutputStream(file)
        openInputStream?.copyTo(fileOutputStream)
        openInputStream?.close()
        fileOutputStream.close()
        val fileAbsolutePath = file.absolutePath

        insertImage(fileAbsolutePath, file)

        Toast.makeText(this, "file copied to $fileAbsolutePath", Toast.LENGTH_SHORT).show()
    }

    private fun clearImages() {
        val filesDir = filesDir
        if(filesDir.isDirectory){
            val listFiles = filesDir.listFiles()

            for (listFile in listFiles){
                Log.d("@@@", "cleared Image : ${listFile.absolutePath}")
                listFile.delete()
            }
        }
    }
}