package com.vidyarthibus.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vidyarthibus.R
import com.vidyarthibus.utils.CrowdUtils

class RouteAdapter(private val onClick: (RouteUiItem) -> Unit) :
    ListAdapter<RouteUiItem, RouteAdapter.VH>(DIFF) {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName  : TextView = v.findViewById(R.id.tvRouteName)
        val tvStops : TextView = v.findViewById(R.id.tvRouteStops)
        val tvPill  : TextView = v.findViewById(R.id.tvCrowdPill)
        val tvIcon  : TextView = v.findViewById(R.id.tvRouteIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_route, parent, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = getItem(pos)
        val ctx  = h.itemView.context
        h.tvName.text  = "Route ${item.route.routeNumber}"
        h.tvStops.text = item.route.name
        h.tvIcon.text  = when (item.crowdState.level.ordinal) { 0 -> "🟢"; 1 -> "🟡"; else -> "🔴" }
        CrowdUtils.applyToPill(ctx, h.tvPill, item.crowdState)
        h.itemView.setOnClickListener { onClick(item) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<RouteUiItem>() {
            override fun areItemsTheSame(a: RouteUiItem, b: RouteUiItem) = a.route.id == b.route.id
            override fun areContentsTheSame(a: RouteUiItem, b: RouteUiItem) = a == b
        }
    }
}
