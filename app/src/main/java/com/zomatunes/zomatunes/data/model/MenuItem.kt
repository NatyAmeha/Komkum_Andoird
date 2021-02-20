package com.zomatunes.zomatunes.data.model

import com.zomatunes.zomatunes.R

data class MenuItem(var icon : Int , var title : String){
    companion object{
        var onlineMenuItem   = listOf(
            MenuItem(R.drawable.ic_file_download_black_24dp , "Download"),
            MenuItem(R.drawable.ic_baseline_graphic_eq_24 , "Song Radio"),
            MenuItem(R.drawable.ic_baseline_person_pin_24 , "Artist Radio"),
            MenuItem(R.drawable.ic_baseline_album_24 , "Go to album"),
            MenuItem(R.drawable.ic_person_black_24dp , "Go to artists"),
            MenuItem(R.drawable.ic_baseline_info_24 , "Song Info"),
            MenuItem(R.drawable.ic_baseline_add_24 , "Add to")
        )

        var offlineSongMenuItem   = listOf(
            MenuItem(R.drawable.ic_round_remove_circle_outline_24 , "Remove")
        )
    }
}