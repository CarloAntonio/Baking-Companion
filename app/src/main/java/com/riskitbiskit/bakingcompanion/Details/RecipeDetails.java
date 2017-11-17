package com.riskitbiskit.bakingcompanion.Details;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.riskitbiskit.bakingcompanion.Main.MainActivity;
import com.riskitbiskit.bakingcompanion.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecipeDetails extends AppCompatActivity implements RecipeStepsFragment.OnRecipeClickListener{

    //Testing
    public static final String LOG_TAG = RecipeDetails.class.getSimpleName();

    //Constants
    public static final String INSTRUCTION_STEP = "instructionsStep";
    public static final String RECIPE_SAVED_STATE = "recipeSavedState";

    //Fields
    private int recipeNumber;
    private boolean twoPanel;
    private List<String> recipeNames;
    private ArrayAdapter<String> arrayAdapter;
    private String mActivityTitle;
    private ActionBarDrawerToggle mDrawerToggle;

    //Views
    @BindView(R.id.drawer_layout_steps)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.left_drawer_steps)
    ListView mDrawerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);
        ButterKnife.bind(this);

        //setup action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //get data passed in from intent
        Intent intent = getIntent();
        recipeNames = intent.getStringArrayListExtra(MainActivity.RECIPE_LIST);
        mActivityTitle = getTitle().toString();

        //setup menu
        setupMenu();

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(RECIPE_SAVED_STATE)) {
                recipeNumber = savedInstanceState.getInt(RECIPE_SAVED_STATE);
            }
        }

        if (findViewById(R.id.two_panel_layout) != null) {
            twoPanel = true;

            recipeNumber = intent.getIntExtra(MainActivity.RECIPE_INDEX_NUMBER, 0);

            if (savedInstanceState == null) {
                //Set-up recipe steps fragment
                RecipeStepsFragment recipeStepsFragment = new RecipeStepsFragment();

                FragmentManager fragmentManager = getSupportFragmentManager();

                fragmentManager.beginTransaction()
                        .add(R.id.recipe_steps_container, recipeStepsFragment)
                        .commit();

                //Set-up Step Details Fragment
                StepDetailsFragment stepDetailsFragment = new StepDetailsFragment();

                fragmentManager.beginTransaction()
                        .add(R.id.step_details_frag_container, stepDetailsFragment)
                        .commit();
            }
        } else {
            twoPanel = false;

            if (savedInstanceState == null) {

                recipeNumber = intent.getIntExtra(MainActivity.RECIPE_INDEX_NUMBER, 0);

                RecipeStepsFragment recipeStepsFragment = new RecipeStepsFragment();

                FragmentManager fragmentManager = getSupportFragmentManager();

                fragmentManager.beginTransaction()
                        .add(R.id.recipe_steps_container, recipeStepsFragment)
                        .commit();
            }
        }
    }

    @Override
    public void onRecipeClicked(int position) {
        if (twoPanel) {
            StepDetailsFragment newFragment = new StepDetailsFragment();
            newFragment.setRecipeNumber(recipeNumber);
            newFragment.setRequestedStep(position);
            newFragment.setIsTwoPanel(true);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.step_details_frag_container, newFragment)
                    .commit();

        } else {
            Intent intent = new Intent(this, StepDetails.class);
            intent.putExtra(INSTRUCTION_STEP, position);
            intent.putExtra(MainActivity.RECIPE_INDEX_NUMBER, recipeNumber);
            startActivity(intent);
        }
    }

    //Setup Hamburger Menu
    private void setupMenu() {
        //Refactored code from: https://developer.android.com/training/implementing-navigation/nav-drawer.html
        //and http://blog.teamtreehouse.com/add-navigation-drawer-android

        arrayAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, recipeNames);
        mDrawerList.setAdapter(arrayAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getBaseContext(), RecipeDetails.class);
                intent.putExtra(MainActivity.RECIPE_INDEX_NUMBER, position);
                intent.putStringArrayListExtra(MainActivity.RECIPE_LIST, (ArrayList<String>) recipeNames);
                startActivity(intent);
                finish();
            }
        });

        //Setup Hamburger Image
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Navigation");
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getSupportActionBar().setTitle(mActivityTitle);
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
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
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(RECIPE_SAVED_STATE, recipeNumber);
    }
}
