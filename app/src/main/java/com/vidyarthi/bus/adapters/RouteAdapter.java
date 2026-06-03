package com.vidyarthi.bus.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vidyarthi.bus.R;
import com.vidyarthi.bus.models.BusRoute;
import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {

    public interface OnRouteClickListener {
        void onRouteClick(BusRoute route);
    }

    private final List<BusRoute>        routes;
    private final OnRouteClickListener  listener;

    // Simulated crowd levels for the list view (in production, listen to Firebase)
    private static final int[]    CROWD_PERCENTS = { 28, 65, 96 };
    private static final String[] CROWD_LABELS   = { "Seats Free", "Getting Full", "Bus Full" };
    private static final int[]    CROWD_COLORS   = {
        Color.parseColor("#1AAD4E"),
        Color.parseColor("#F5A623"),
        Color.parseColor("#E8322A")
    };

    public RouteAdapter(List<BusRoute> routes, OnRouteClickListener listener) {
        this.routes   = routes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_route, parent, false);
        return new RouteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        BusRoute route = routes.get(position);
        holder.tvRouteName.setText(route.getRouteName());
        holder.tvRouteDesc.setText(route.getRouteDescription());

        // Determine crowd level colour for the status pill
        int level = position < 3 ? getDefaultLevel(position) : 0;
        holder.tvCrowdPill.setText(CROWD_LABELS[level]);
        holder.tvCrowdPill.setTextColor(CROWD_COLORS[level]);
        holder.tvCrowdPill.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(
                alphaColor(CROWD_COLORS[level], 30)));

        holder.itemView.setOnClickListener(v -> listener.onRouteClick(route));
    }

    @Override
    public int getItemCount() { return routes.size(); }

    private int getDefaultLevel(int position) {
        if (position == 0) return 0; // green
        if (position == 1) return 1; // amber
        return 2;                    // red
    }

    private int alphaColor(int color, int alpha) {
        return Color.argb(alpha,
                Color.red(color), Color.green(color), Color.blue(color));
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        TextView tvRouteName, tvRouteDesc, tvCrowdPill;

        RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRouteName  = itemView.findViewById(R.id.tv_route_name);
            tvRouteDesc  = itemView.findViewById(R.id.tv_route_desc);
            tvCrowdPill  = itemView.findViewById(R.id.tv_crowd_pill);
        }
    }
}
