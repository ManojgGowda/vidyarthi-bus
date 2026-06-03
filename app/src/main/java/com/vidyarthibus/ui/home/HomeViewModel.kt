package com.vidyarthibus.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vidyarthibus.data.model.BusRoute
import com.vidyarthibus.data.model.CrowdState
import com.vidyarthibus.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RouteUiItem(val route: BusRoute, val crowdState: CrowdState)

class HomeViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<RouteUiItem>>(emptyList())
    val items: StateFlow<List<RouteUiItem>> = _items.asStateFlow()

    init {
        val routes = FirebaseRepository.routes
        // Seed with default states
        _items.value = routes.map { RouteUiItem(it, CrowdState(it.id)) }

        // Subscribe to live updates for each route
        routes.forEachIndexed { idx, route ->
            viewModelScope.launch {
                FirebaseRepository.observeCrowdState(route.id).collect { state ->
                    val current = _items.value.toMutableList()
                    current[idx] = current[idx].copy(crowdState = state)
                    _items.value = current
                }
            }
        }

        // Purge expired reports on open
        routes.forEach { FirebaseRepository.purgeExpired(it.id) }
    }
}
