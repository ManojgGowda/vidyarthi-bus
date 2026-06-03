package com.vidyarthi.bus.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vidyarthi.bus.R;
import com.vidyarthi.bus.models.BusStop;
import java.util.List;

public class StopAdapter extends RecyclerView.Adapter<StopAdapter.StopViewHolder> {

    // In a real app, currentStopIndex would come from Firebase bus position tracking.
    // Here we default to the 3rd stop (index 2) to demonstrate the timeline.
    private final List<BusStop> stops;
    private final int           currentStopIndex;

    public StopAdapter(List<BusStop> stops) {
        this.stops            = stops;
        this.currentStopIndex = stops.size() >= 3 ? 2 : 0;
    }

    @NonNull
    @Override
    public StopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_stop, parent, false);
        return new StopViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StopViewHolder holder, int position) {
        BusStop stop = stops.get(position);
        holder.tvStopName.setText(stop.getStopName());

        if (position < currentStopIndex) {
            // Past stop — green dot
            holder.ivDot.setImageResource(R.drawable.dot_done);
            holder.tvStopName.setAlpha(0.5f);
            holder.tvEta.setText("Passed");
            holder.tvEta.setAlpha(0.5f);
        } else if (position == currentStopIndex) {
            // Current stop — saffron pulsing dot
            holder.ivDot.setImageResource(R.drawable.dot_current);
            holder.tvStopName.setAlpha(1f);
            holder.tvEta.setText("⬤ Bus here now");
            holder.tvEta.setTextColor(Color.parseColor("#FF6B1A"));
            holder.tvEta.setAlpha(1f);
        } else {
            // Upcoming stop — grey dot
            holder.ivDot.setImageResource(R.drawable.dot_next);
            holder.tvStopName.setAlpha(1f);
            int minsAway = (position - currentStopIndex) * 6;
            holder.tvEta.setText("~" + minsAway + " min");
            holder.tvEta.setTextColor(Color.parseColor("#8C7C6C"));
            holder.tvEta.setAlpha(1f);
        }

        // Show / hide the vertical connector line
        holder.vLine.setVisibility(
                position < stops.size() - 1 ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemCount() { return stops.size(); }

    static class StopViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDot;
        TextView  tvStopName, tvEta;
        View      vLine;

        StopViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDot      = itemView.findViewById(R.id.iv_dot);
            tvStopName = itemView.findViewById(R.id.tv_stop_name);
            tvEta      = itemView.findViewById(R.id.tv_eta);
            vLine      = itemView.findViewById(R.id.v_line);
        }
    }
}
