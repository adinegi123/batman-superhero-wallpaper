package com.adi121.bat_superhero_man_wallpaper.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.google.firebase.firestore.FirebaseFirestore

class ImageRepository(private val db:FirebaseFirestore) {

    fun getSearchResults()=Pager(
        config = PagingConfig(
            pageSize = 5,
            maxSize = 100,
            initialLoadSize = 5,
            enablePlaceholders = false
        ),
            pagingSourceFactory ={ ImagePagingSource(db) }
    )
}