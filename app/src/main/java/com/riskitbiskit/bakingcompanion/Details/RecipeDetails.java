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

public class RecipeDetails extends AppCompatActivity implements RecipeFragment.OnRecipeClickListener{

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
        recipeNumber = intent.getIntExtra(MainActivity.RECIPE_INDEX_NUMBER, 0);

        //setup menu
        setupMenu();

        //grab recipe saved in SIS if there is one
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(RECIPE_SAVED_STATE)) {
                recipeNumber = savedInstanceState.getInt(RECIPE_SAVED_STATE);
            }
        }

        //determine if user is viewing from a tablet(two panel) or phone(single panel)
        if (findViewById(R.id.two_panel_layout) != null) {
            twoPanel = true;
            setupTwoPanel(savedInstanceState);
        } else {
            twoPanel = false;
            setupSinglePanel(savedInstanceState);
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
                intent.putExtra(MainActivity.RECIPE_INDEX_NUMBER, position);
                intent.putStringArrayListExtra(MainActivity.RECIPE_LIST, (ArrayList<String>) recipeNames);
                startActivity(intent);
                finish();
            }
        });

        //setup hamburger image
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
                getSupportActionBar().setTitle(getTitle().toString());
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
    }

    private void setupTwoPanel(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            //setup recipe steps fragment
            RecipeFragment recipeFragment = new RecipeFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .add(R.id.recipe_steps_container, recipeFragment)
                    .commit();

            //setup step details fragment
            StepDetailsFragment stepDetailsFragment = new StepDetailsFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.step_details_frag_container, stepDetailsFragment)
                    .commit();
        }
    }

    private void setupSinglePanel(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            //setup recipe steps fragment
            RecipeFragment recipeFragment = new RecipeFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .add(R.id.recipe_steps_container, recipeFragment)
                    .commit();
        }
    }


    @Override
    public void onRecipeClicked(int position) {
        if (twoPanel) {
            //create new step details fragment
            StepDetailsFragment newFragment = new StepDetailsFragment();
            newFragment.setRecipeNumber(recipeNumber);
            newFragment.setRequestedStep(position);
            newFragment.setIsTwoPanel(true);

            //replace old fragment with newly created fragment
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
