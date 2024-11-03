package com.example.bestride.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CabComparisonViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val sourceLat = savedStateHandle.get<String>("sourceLat") ?: ""
    val sourceLng = savedStateHandle.get<String>("sourceLng") ?: ""
    val destLat = savedStateHandle.get<String>("destLat") ?: ""
    val destLng = savedStateHandle.get<String>("destLng") ?: ""
    val sourceAddress = savedStateHandle.get<String>("sourceAddress") ?: ""
    val destAddress = savedStateHandle.get<String>("destAddress") ?: ""

    // Add methods to load cab prices here
}