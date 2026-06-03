package com.vidyarthi.bus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.vidyarthi.bus.R;
import com.vidyarthi.bus.adapters.RouteAdapter;
import com.vidyarthi.bus.models.BusRoute;
import com.vidyarthi.bus.utils.RouteDataProvider;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements RouteAdapter.OnRouteClickListener {

    public static final String EXTRA_ROUTE_ID   = "route_id";
    public static final String EXTRA_ROUTE_NAME = "route_name";

    private List<BusRoute> routes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        routes = RouteDataProvider.getAllRoutes();

        RecyclerView rv = findViewById(R.id.rv_routes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new RouteAdapter(routes, this));
    }

    @Override
    public void onRouteClick(BusRoute route) {
        Intent intent = new Intent(this, CrowdDetailActivity.class);
        intent.putExtra(EXTRA_ROUTE_ID,   route.getRouteId());
        intent.putExtra(EXTRA_ROUTE_NAME, route.getRouteName());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_alternatives) {
            startActivity(new Intent(this, AlternativesActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
