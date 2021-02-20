package com.zomatunes.zomatunes.data.model

import android.os.Parcelable
import androidx.room.Ignore
import kotlinx.android.parcel.Parcelize

@Parcelize
open class BaseModel(
    @Ignore
    var baseId: String? = null,

    // to store album id or artist id  for adding song or album to library respectively
    @Ignore
    var baseTittle: String? = null,

    //
    @Ignore
    var baseSubTittle: String? = null,
    @Ignore
    var baseDescription: String? = null,
    @Ignore
    var baseType: Int? = null,   // for library type identification
    @Ignore
    var baseImagePath: String? = null,

    @Ignore// to store song artist id or album song id  for adding song or album to library respectively
    var baseListOfInfo: List<String>? = null,

    // to store song artist id or album song id  for adding song or album to library respectively  (when there is more than 1 list of information to pass
    @Ignore
    var baseListofInfo2: List<String>? = null

) : Parcelable