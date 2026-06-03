package com.vidyarthi.bus.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vidyarthi.bus.R;
import com.vidyarthi.bus.models.Alternative;
import java.util.List;

public class AlternativeAdapter
        extends RecyclerView.Adapter<AlternativeAdapter.AltViewHolder> {

    public interface OnCallClickListener {
        void onCallClick(String phoneNumber);
    }

    private final List<Alternative>  items;
    private final OnCallClickListener listener;

    public AlternativeAdapter(List<Alternative> items, OnCallClickListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AltViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_alternative, parent, false);
        return new AltViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AltViewHolder holder, int position) {
        Alternative alt = items.get(position);
        holder.tvName.setText(alt.getName());
        holder.tvDetail.setText(alt.getDetail());

        // Pick icon by type
        switch (alt.getType()) {
            case "auto":  holder.ivIcon.setImageResource(R.drawable.ic_auto);  break;
            case "cycle": holder.ivIcon.setImageResource(R.drawable.ic_cycle); break;
            default:      holder.ivIcon.setImageResource(R.drawable.ic_bus);   break;
        }

        holder.btnCall.setOnClickListener(v -> listener.onCallClick(alt.getPhoneNumber()));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class AltViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView  tvName, tvDetail;
        Button    btnCall;

        AltViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon   = itemView.findViewById(R.id.iv_icon);
            tvName   = itemView.findViewById(R.id.tv_alt_name);
            tvDetail = itemView.findViewById(R.id.tv_alt_detail);
            btnCall  = itemView.findViewById(R.id.btn_call);
        }
    }
}
