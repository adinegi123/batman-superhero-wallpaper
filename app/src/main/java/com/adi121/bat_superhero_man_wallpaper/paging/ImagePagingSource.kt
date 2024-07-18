package com.adi121.bat_superhero_man_wallpaper.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.adi121.bat_superhero_man_wallpaper.models.SingleImage

import kotlinx.coroutines.tasks.await
import java.net.UnknownHostException

class ImagePagingSource(
    private val db: FirebaseFirestore
): PagingSource<QuerySnapshot, SingleImage>() {
    override fun getRefreshKey(state: PagingState<QuerySnapshot, SingleImage>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, SingleImage> {
        return try {

            //Step 1
            val currentPage= params.key?:db.collection("wallpapers")
                .limit(5)
                .get()
                .await()

            // Step 2
            val lastDocumentSnapshot = currentPage.documents[currentPage.size() - 1]

            // Step 3
            val nextPage = db.collection("wallpapers").limit(5).startAfter(lastDocumentSnapshot)
                .get()
                .await()

            //Step 4

            LoadResult.Page(
                data = currentPage.toObjects(SingleImage::class.java),
                prevKey = null,
                nextKey = nextPage
            )
        }catch (exception:ArrayIndexOutOfBoundsException){
            LoadResult.Error(exception)
        }catch (exception:UnknownHostException){
            LoadResult.Error(exception)
        }
    }
}