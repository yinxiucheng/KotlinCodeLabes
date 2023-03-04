package com.example.kotlincoroutine.viewmodel

import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    val liveData = mutableListOf<String>()
}