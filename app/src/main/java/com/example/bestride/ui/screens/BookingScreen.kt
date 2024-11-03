package com.example.bestride.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bestride.navigation.navigateToCabComparison
import com.example.bestride.presentation.viewmodel.BookingViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@Composable
fun BookingScreen(
    navController: NavHostController,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // Permission state
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.values.reduce { acc, isGranted -> acc && isGranted }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    var cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(12.9716, 77.5946), // Default to Bangalore
            12f
        )
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Map
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
            ) {
                // Source Marker
                uiState.sourceLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Pickup"
                    )
                }

                // Destination Marker
                uiState.destinationLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Drop"
                    )
                }

                // Route Polyline
                if (uiState.routePoints.isNotEmpty()) {
                    Polyline(
                        points = uiState.routePoints,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Where do you want to go?",
                    style = MaterialTheme.typography.headlineSmall
                )

                // Source Location
                PlacesAutoCompleteTextField(
                    value = uiState.sourceAddress,
                    onValueChange = { viewModel.updateSourceAddress(it) },
                    onPlaceSelected = { place ->
                        place.latLng?.let { latLng ->
                            scope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLng(latLng),
                                    durationMs = 1000
                                )
                            }
                            viewModel.updateSourceLocation(place)
                        }
                    },
                    label = "Pickup Location",
                    viewModel = viewModel
                )

                // Destination Location
                PlacesAutoCompleteTextField(
                    value = uiState.destinationAddress,
                    onValueChange = { viewModel.updateDestinationAddress(it) },
                    onPlaceSelected = { place ->
                        place.latLng?.let { latLng ->
                            scope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLng(latLng),
                                    durationMs = 1000
                                )
                            }
                            viewModel.updateDestinationLocation(place)
                        }
                    },
                    label = "Drop Location",
                    viewModel = viewModel
                )

                Button(
                    onClick = {
                        navController.navigateToCabComparison(
                            sourceLat = uiState.sourceLocation?.latitude,
                            sourceLng = uiState.sourceLocation?.longitude,
                            destLat = uiState.destinationLocation?.latitude,
                            destLng = uiState.destinationLocation?.longitude,
                            sourceAddress = uiState.sourceAddress,
                            destAddress = uiState.destinationAddress
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.sourceLocation != null && uiState.destinationLocation != null
                ) {
                    Text("Compare Prices")
                }
            }
        }
    }
}

@Composable
private fun PlacesAutoCompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onPlaceSelected: (Place) -> Unit,
    label: String,
    viewModel: BookingViewModel,
    modifier: Modifier = Modifier
) {
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                viewModel.getPlacePredictions(newValue) { newPredictions ->
                    predictions = newPredictions
                    isExpanded = newPredictions.isNotEmpty()
                }
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (isExpanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LazyColumn {
                    items(predictions) { prediction ->
                        Text(
                            text = prediction.getFullText(null).toString(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onValueChange(prediction.getFullText(null).toString())
                                    viewModel.getPlaceFromPrediction(prediction) { place ->
                                        place?.let { onPlaceSelected(it) }
                                    }
                                    isExpanded = false
                                }
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}