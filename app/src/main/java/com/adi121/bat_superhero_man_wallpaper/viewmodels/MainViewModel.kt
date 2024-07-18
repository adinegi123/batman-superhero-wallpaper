package com.adi121.bat_superhero_man_wallpaper.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.liveData
import com.adi121.bat_superhero_man_wallpaper.paging.ImageRepository


class MainViewModel(private val repository: ImageRepository): ViewModel() {

    fun getImages()= repository.getSearchResults()
        .liveData.cachedIn(viewModelScope)

}