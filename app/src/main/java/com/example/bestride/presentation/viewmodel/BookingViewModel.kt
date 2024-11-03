package com.example.bestride.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class BookingUiState(
    val sourceAddress: String = "",
    val destinationAddress: String = "",
    val sourceLocation: LatLng? = null,
    val destinationLocation: LatLng? = null,
    val routePoints: List<LatLng> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val placesClient: PlacesClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState = _uiState.asStateFlow()

    fun getPlacePredictions(query: String, callback: (List<AutocompletePrediction>) -> Unit) {
        if (query.length < 3) {
            callback(emptyList())
            return
        }

        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setCountries("IN") // Restrict to India
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                callback(response.autocompletePredictions)
            }
            .addOnFailureListener { exception ->
                callback(emptyList())
                _uiState.update { it.copy(error = exception.message) }
            }
    }

    fun getPlaceFromPrediction(
        prediction: AutocompletePrediction,
        callback: (Place?) -> Unit
    ) {
        _uiState.update { it.copy(isLoading = true) }

        val placeFields = listOf(Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val request = FetchPlaceRequest.builder(prediction.placeId, placeFields).build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                _uiState.update { it.copy(isLoading = false) }
                callback(response.place)
            }
            .addOnFailureListener { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
                callback(null)
            }
    }

    fun updateSourceAddress(address: String) {
        _uiState.update { it.copy(sourceAddress = address) }
    }

    fun updateDestinationAddress(address: String) {
        _uiState.update { it.copy(destinationAddress = address) }
    }

    fun updateSourceLocation(place: Place) {
        place.latLng?.let { latLng ->
            _uiState.update {
                it.copy(
                    sourceLocation = latLng,
                    sourceAddress = place.address ?: ""
                )
            }
            updateRoute()
        }
    }

    fun updateDestinationLocation(place: Place) {
        place.latLng?.let { latLng ->
            _uiState.update {
                it.copy(
                    destinationLocation = latLng,
                    destinationAddress = place.address ?: ""
                )
            }
            updateRoute()
        }
    }

    private fun updateRoute() {
        val source = _uiState.value.sourceLocation
        val destination = _uiState.value.destinationLocation

        if (source != null && destination != null) {
            // For now, just creating a direct line between points
            _uiState.update {
                it.copy(
                    routePoints = listOf(source, destination),
                    isLoading = false
                )
            }
        }
    }

    fun setLoading(loading: Boolean) {
        _uiState.update { it.copy(isLoading = loading) }
    }

    fun setError(error: String?) {
        _uiState.update { it.copy(error = error) }
    }
}