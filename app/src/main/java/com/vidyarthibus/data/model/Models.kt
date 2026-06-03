package com.vidyarthibus.data.model

data class BusRoute(
    val id: String = "",
    val routeNumber: String = "",
    val name: String = "",
    val origin: String = "",
    val destination: String = "",
    val stops: List<BusStop> = emptyList()
)

data class BusStop(
    val id: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val sequence: Int = 0,
    val etaMinutes: Int = 0
)

data class CrowdReport(
    val uid: String = "",
    val routeId: String = "",
    val crowdLevel: Int = 0,   // 0=Empty 1=Seated 2=Full
    val timestamp: Long = 0L,
    val stopId: String = "",
    val onSchedule: Boolean = false,
    val lastStop: Boolean = false
)

data class CrowdState(
    val routeId: String = "",
    val level: CrowdLevel = CrowdLevel.EMPTY,
    val percentage: Int = 5,
    val seatsAvailable: Int = 40,
    val reportCount: Int = 0,
    val lastUpdated: Long = 0L
)

data class Alternative(
    val id: String = "",
    val name: String = "",
    val type: AlternativeType = AlternativeType.AUTO,
    val detail: String = "",
    val phone: String = "",
    val fare: String = ""
)

enum class CrowdLevel(val label: String) {
    EMPTY("Seats Free"),
    SEATED("Getting Full"),
    FULL("Bus Full")
}

enum class AlternativeType { AUTO, CYCLE, BUS }
