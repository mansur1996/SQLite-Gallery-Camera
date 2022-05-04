package com.example.cameraandgallery.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.cameraandgallery.model.ImageModel

class MyDbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object{
        const val DB_NAME = "img.db"
        const val DB_VERSION = 1
        const val TABLE_NAME = "image_table"
    }

    override fun onCreate(db : SQLiteDatabase?) {
        val query = "create table $TABLE_NAME (id integer primary key autoincrement not null, img_path text not null, image blob not null)"
        db?.execSQL(query)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {

    }

    fun insertImage(imageMode: ImageModel){
        val database = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("img_path", imageMode.imagePath)
        contentValues.put("image", imageMode.image)
        database.insert(TABLE_NAME, null, contentValues)
        database.close()
    }

    fun getAllImages() : List<ImageModel>{
        val list = ArrayList<ImageModel>()
        val query = "select * from $TABLE_NAME"
        val database = this.readableDatabase
        val cursor = database.rawQuery(query, null)
        if(cursor.moveToFirst()){
            do {
                val imageModel = ImageModel()
                imageModel.id = cursor.getInt(0)
                imageModel.imagePath = cursor.getString(1)
                imageModel.image = cursor.getBlob(2)
                list.add(imageModel)
            }while (cursor.moveToNext())
        }
        return list
    }

}