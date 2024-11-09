package com.example.bestride.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CabComparisonViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val sourceLat: Float = savedStateHandle.get<Float>("sourceLat") ?: 0f
    val sourceLng: Float = savedStateHandle.get<Float>("sourceLng") ?: 0f
    val destLat: Float = savedStateHandle.get<Float>("destLat") ?: 0f
    val destLng: Float = savedStateHandle.get<Float>("destLng") ?: 0f
    val sourceAddress: String = savedStateHandle.get<String>("sourceAddress") ?: ""
    val destAddress: String = savedStateHandle.get<String>("destAddress") ?: ""
}