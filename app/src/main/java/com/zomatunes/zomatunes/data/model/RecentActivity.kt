package com.zomatunes.zomatunes.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class RecentActivity(
    var _id : String,
    var id : String, // map recent  song id , album id , artist id to this
    var name : String,
    var image : String,
    var type: String,
    var date: Date
): BaseModel(baseId = id , baseTittle = name , baseSubTittle = type , baseImagePath = image){
    fun toBaseModel() = BaseModel(baseId = id , baseTittle = name , baseSubTittle = type , baseImagePath = image)
}