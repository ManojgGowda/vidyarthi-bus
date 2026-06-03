package com.vidyarthibus.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.vidyarthibus.data.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object FirebaseRepository {

    private val db   = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    const val REPORT_TTL_MS = 15 * 60 * 1000L   // 15 minutes

    // ── Static data ──────────────────────────────────────────────────────────

    val routes = listOf(
        BusRoute("7b","7-B","Nagpur Depot → GEC Campus","Nagpur Depot","GEC Campus",
            listOf(
                BusStop("7b_1","Nagpur Depot",   21.1458,79.0882,1, 0),
                BusStop("7b_2","Sitabuldi",      21.1473,79.0906,2,12),
                BusStop("7b_3","Kamptee Road",   21.1528,79.0974,3,22),
                BusStop("7b_4","Nari Road",      21.1594,79.1031,4,30),
                BusStop("7b_5","GEC Campus",     21.1641,79.1098,5,42)
            )),
        BusRoute("12a","12-A","Wardha Road → VNIT College","Wardha Road","VNIT College",
            listOf(
                BusStop("12a_1","Wardha Road",   21.1119,79.0452,1, 0),
                BusStop("12a_2","Ramdaspeth",    21.1197,79.0561,2,10),
                BusStop("12a_3","Shankar Nagar", 21.1287,79.0672,3,20),
                BusStop("12a_4","S. Ambazari",   21.1356,79.0731,4,28),
                BusStop("12a_5","VNIT College",  21.1448,79.0589,5,38)
            )),
        BusRoute("3c","3-C","Hingna → Laxminarayan College","Hingna","Laxminarayan College",
            listOf(
                BusStop("3c_1","Hingna Depot",   21.0952,78.9891,1, 0),
                BusStop("3c_2","Nandanvan",      21.1043,78.9982,2,14),
                BusStop("3c_3","Gokulpeth",      21.1134,79.0134,3,25),
                BusStop("3c_4","Dharampeth",     21.1223,79.0267,4,34),
                BusStop("3c_5","Laxminarayan",   21.1312,79.0389,5,45)
            ))
    )

    val alternatives = listOf(
        Alternative("a1","Raju Shared Auto",  AlternativeType.AUTO, "Kamptee Rd → GEC · ₹20/seat","+91 94220 11234","₹20"),
        Alternative("a2","Sunita Auto Stand", AlternativeType.AUTO, "Nari Road → VNIT · ₹15/seat","+91 98760 55678","₹15"),
        Alternative("a3","College Cycle Shuttle",AlternativeType.CYCLE,"Free · 7:30-8:30 AM · Sitabuldi","+91 71230 99001","Free"),
        Alternative("a4","MSRTC Bus #54",     AlternativeType.BUS,  "Nagpur Main → College Rd · ₹12","+91 0712 226600","₹12")
    )

    // ── Auth ──────────────────────────────────────────────────────────────────

    suspend fun ensureAnonymousAuth(): String {
        val user = auth.currentUser ?: auth.signInAnonymously().await().user
        return user!!.uid
    }

    // ── Live crowd state ──────────────────────────────────────────────────────

    fun observeCrowdState(routeId: String): Flow<CrowdState> = callbackFlow {
        val ref = db.child("reports").child(routeId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cutoff = System.currentTimeMillis() - REPORT_TTL_MS
                val fresh = snapshot.children
                    .mapNotNull { it.getValue(CrowdReport::class.java) }
                    .filter { it.timestamp >= cutoff }
                trySend(compute(routeId, fresh))
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    private fun compute(routeId: String, reports: List<CrowdReport>): CrowdState {
        if (reports.isEmpty()) return CrowdState(routeId)
        val avg = reports.map { it.crowdLevel }.average()
        val pct = ((avg / 2.0) * 100).toInt().coerceIn(5, 99)
        val level = when {
            pct <= 40 -> CrowdLevel.EMPTY
            pct <= 72 -> CrowdLevel.SEATED
            else      -> CrowdLevel.FULL
        }
        val seats = ((1.0 - pct / 100.0) * 45).toInt().coerceAtLeast(0)
        return CrowdState(routeId, level, pct, seats, reports.size, System.currentTimeMillis())
    }

    // ── Submit report ─────────────────────────────────────────────────────────

    suspend fun submitReport(routeId: String, crowdLevel: Int, stopId: String,
                             onSchedule: Boolean, lastStop: Boolean) {
        val uid = ensureAnonymousAuth()
        val report = CrowdReport(uid, routeId, crowdLevel,
            System.currentTimeMillis(), stopId, onSchedule, lastStop)
        db.child("reports").child(routeId).child(uid).setValue(report).await()
    }

    // ── Purge stale reports ───────────────────────────────────────────────────

    fun purgeExpired(routeId: String) {
        val cutoff = System.currentTimeMillis() - REPORT_TTL_MS
        db.child("reports").child(routeId)
            .orderByChild("timestamp").endAt(cutoff.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(s: DataSnapshot) { s.children.forEach { it.ref.removeValue() } }
                override fun onCancelled(e: DatabaseError) {}
            })
    }
}
