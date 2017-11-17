package com.riskitbiskit.bakingcompanion.Main;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.riskitbiskit.bakingcompanion.Details.RecipeDetails;
import com.riskitbiskit.bakingcompanion.R;
import com.riskitbiskit.bakingcompanion.TestingFiles.SimpleIdlingResource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements RecipeRVAdapter.ListItemClicked {

    //Testing
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    //Constants
    public static final String RECIPE_INDEX_NUMBER = "recipeNumber";
    public static final String RECIPE_LIST = "recipeList";
    public static final String DATA_URL = "https://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/baking.json";

    //Fields
    private List<String> recipeNames;
    private RecipeRVAdapter rvAdpter;
    private ArrayAdapter<String> arrayAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    GridLayoutManager gridLayoutManager;
    RequestQueue mRequestQueue;
    List<Recipe> recipes;
    SimpleIdlingResource mIdlingResource;

    //Views
    @BindView(R.id.recipe_list_rv)
    RecyclerView recipeListRV;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.left_drawer)
    ListView mDrawerList;

    //TODO: feat - create on save instance state to prevent unnecessary calls to the network
    //TODO: feat - add a content provider so that we only need to make a network call during main activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //setup action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //initialize variables
        recipes = new ArrayList<>();
        recipeNames = new ArrayList<>();

        makeNetworkRequest();

        //use of recyclerview is a design requirement from Udacity, would have used a gridview
        setupRecyclerView();
    }

    private void makeNetworkRequest() {
        //TODO: refactor - replace with retrofit2
        //Used developer.android.com/training/volley to learn and implement this code
        mRequestQueue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, DATA_URL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                //parse through json response
                parseJsonResponse(response);

                rvAdpter.notifyDataSetChanged();

                //setup hamburger menu
                //Refactored code from: https://developer.android.com/training/implementing-navigation/nav-drawer.html
                //and http://blog.teamtreehouse.com/add-navigation-drawer-android
                setupMenu();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, "Error Fetching Data: " + error);
            }
        });

        mRequestQueue.add(jsonArrayRequest);
    }

    private void parseJsonResponse(JSONArray response) {
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject currentRecipe = response.getJSONObject(i);
                int recipeId = currentRecipe.getInt("id");
                String recipeName = currentRecipe.getString("name");
                String recipeImage = currentRecipe.getString("image");
                recipes.add(new Recipe(recipeId, recipeName, recipeImage));
                recipeNames.add(recipeName);
            }
        } catch (JSONException JSONE) {
            JSONE.printStackTrace();
        }
    }

    //setup hamburger menu
    private void setupMenu() {
        arrayAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, recipeNames);

        mDrawerList.setAdapter(arrayAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getBaseContext(), RecipeDetails.class);
                intent.putExtra(RECIPE_INDEX_NUMBER, position);
                intent.putStringArrayListExtra(RECIPE_LIST, (ArrayList<String>) recipeNames);
                startActivity(intent);
            }
        });

        //TODO: check this code with lumpia app, seems off
        //setup hamburger image
        mDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Navigation");
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getSupportActionBar().setTitle(getTitle().toString());
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);

        mDrawerToggle.syncState();
    }

    private void setupRecyclerView() {
        //Source: https://stackoverflow.com/questions/6465680/how-to-determine-the-screen-width-in-terms-of-dp-or-dip-at-runtime-in-android
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        if (dpWidth < 600) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                gridLayoutManager = new GridLayoutManager(this, 1);
            } else {
                gridLayoutManager = new GridLayoutManager(this, 2);
            }
        } else {
            gridLayoutManager = new GridLayoutManager(this, 3);
        }

        recipeListRV.setLayoutManager(gridLayoutManager);
        rvAdpter = new RecipeRVAdapter(this, recipes, this);
        recipeListRV.setAdapter(rvAdpter);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        Intent intent = new Intent(this, RecipeDetails.class);
        intent.putExtra(RECIPE_INDEX_NUMBER, clickedItemIndex);
        intent.putStringArrayListExtra(RECIPE_LIST, (ArrayList<String>) recipeNames);
        startActivity(intent);
    }

    //idling resource for UI testing
    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource() {
        if (mIdlingResource == null) {
            mIdlingResource = new SimpleIdlingResource();
        }
        return mIdlingResource;
    }
}
