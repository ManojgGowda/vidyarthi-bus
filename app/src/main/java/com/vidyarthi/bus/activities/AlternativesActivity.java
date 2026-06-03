package com.vidyarthi.bus.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.vidyarthi.bus.R;
import com.vidyarthi.bus.adapters.AlternativeAdapter;
import com.vidyarthi.bus.utils.RouteDataProvider;

public class AlternativesActivity extends AppCompatActivity
        implements AlternativeAdapter.OnCallClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alternatives);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Alternative Transport");
        }

        RecyclerView rv = findViewById(R.id.rv_alternatives);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new AlternativeAdapter(RouteDataProvider.getAlternatives(), this));
    }

    @Override
    public void onCallClick(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL,
                Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
