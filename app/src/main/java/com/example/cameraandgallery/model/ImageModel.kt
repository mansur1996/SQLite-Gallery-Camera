package com.example.cameraandgallery.model

class ImageModel{
    var id : Int? = null
    var imagePath : String? = null
    var image : ByteArray? = null

    constructor(imagePath: String?, image: ByteArray?) {
        this.imagePath = imagePath
        this.image = image
    }

    constructor()
}
