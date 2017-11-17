package com.riskitbiskit.bakingcompanion.Details;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.riskitbiskit.bakingcompanion.Main.MainActivity;
import com.riskitbiskit.bakingcompanion.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecipeFragment extends Fragment implements StepsAdapter.ListItemClickListener {

    //Constants
    public static final String VOLLEY_TAG = "volleyTag";
    public static final String SAVED_RV = "savedRV";

    //Fields
    RequestQueue mRequestQueue;
    ArrayList<Ingredient> ingredients;
    ArrayList<Instructions> instructions;
    StepsAdapter mStepsAdapter;
    int recipeNumber;
    LinearLayoutManager mLinearLayoutManager;
    OnRecipeClickListener mCallback;
    private Parcelable mListState;

    //Views
    @BindView(R.id.view_steps_frag)
    LinearLayout mLinearLayout;
    @BindView(R.id.ingredient_steps_rv)
    RecyclerView mRecyclerView;
    @BindView(R.id.ingredients_list)
    TextView mIngredientsListTV;

    public interface  OnRecipeClickListener {
        void onRecipeClicked(int position);
    }

    public RecipeFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //inflate layout
        View rootView = inflater.inflate(R.layout.recipe_steps_frag, container, false);

        //bind view
        ButterKnife.bind(this, rootView);

        //initialize variables
        ingredients = new ArrayList<>();
        instructions = new ArrayList<>();

        //extract data from intent
        Intent intent = getActivity().getIntent();
        if (intent.hasExtra(MainActivity.RECIPE_INDEX_NUMBER)) {
            recipeNumber = intent.getIntExtra(MainActivity.RECIPE_INDEX_NUMBER, 0);
        }

        makeNetworkRequest();

        setupRecyclerView();

        return rootView;
    }

    private void makeNetworkRequest() {
        mRequestQueue = Volley.newRequestQueue(getContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, MainActivity.DATA_URL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    //grab root object
                    JSONObject currentRecipe = response.getJSONObject(recipeNumber);

                    makeIngredientsList(currentRecipe);

                    //set ingredients list message
                    mIngredientsListTV.setText(createIngredientsListMessage());

                    makeInstructionsList(currentRecipe);

                    //need to notify because response may be after adapter is created
                    mStepsAdapter.notifyDataSetChanged();

                    //check to see if parcelable LLM is present
                    //needs to be here to keep list position
                    //TODO: understand why placing this code here keeps list in correct place
                    if (mListState != null) {
                        //if so, restore it back to a LLM
                        mLinearLayoutManager.onRestoreInstanceState(mListState);
                    }

                } catch (JSONException JSONE) {
                    JSONE.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error + "");
            }
        });

        jsonArrayRequest.setTag(VOLLEY_TAG);

        mRequestQueue.add(jsonArrayRequest);
    }

    private void makeIngredientsList(JSONObject currentRecipe) throws JSONException {
        //move to ingredients list node
        JSONArray currentIngredientsList = currentRecipe.getJSONArray("ingredients");

        //cycle through the ingredients list
        for (int i = 0; i < currentIngredientsList.length(); i++) {
            //grab relevant data from each ingredient
            JSONObject currentIngredient = currentIngredientsList.getJSONObject(i);
            double quantity = currentIngredient.getDouble("quantity");
            String measure = currentIngredient.getString("measure");
            String ingredient = currentIngredient.getString("ingredient");

            //add to ingredients list
            ingredients.add(new Ingredient(quantity, measure, ingredient));
        }
    }

    private String createIngredientsListMessage() {
        //create start of message
        String ingredientsList = "Ingredients: \n";

        //cycle through each ingredient
        for (int i = 0; i < ingredients.size(); i++) {

            //get current ingredient name
            String ingredient = ingredients.get(i).getIngredient();
            //capitalize ingredient name
            String capIngredient = ingredient.substring(0, 1).toUpperCase() + ingredient.substring(1);

            //add message
            ingredientsList = ingredientsList
                    //add a bullet point
                    + "\u2022 "
                    //add ingredient name
                    + capIngredient
                    + " ("
                    //add quantity
                    + Double.toString(ingredients.get(i).getQuantity())
                    + " "
                    //add measurement type
                    + ingredients.get(i).getMeasurement()
                    + ")\n";
        }

        //return ingredients list message
        return ingredientsList;
    }

    //make instructions list
    private void makeInstructionsList(JSONObject currentRecipe) throws JSONException {
        //move to list of instructions node
        JSONArray currentInstructionsList = currentRecipe.getJSONArray("steps");
        //cycle through each step
        for (int i = 0; i < currentInstructionsList.length(); i++) {
            //grab relevant data from each step
            JSONObject currentStep = currentInstructionsList.getJSONObject(i);
            int id = currentStep.getInt("id");
            String shortDescription = currentStep.getString("shortDescription");
            String description = currentStep.getString("description");
            String videoUrl = currentStep.getString("videoURL");
            String thumbnail = currentStep.getString("thumbnailURL");

            //add to list of instructions
            instructions.add(new Instructions(id, shortDescription, description, videoUrl, thumbnail));
        }
    }

    private void setupRecyclerView() {
        //create a new steps adapter
        mStepsAdapter = new StepsAdapter(getContext(), instructions, this);

        mRecyclerView.setAdapter(mStepsAdapter);

        //initialize linear layout manager if not already initialized
        if (mLinearLayoutManager == null) {
            mLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        }

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnRecipeClickListener) context;
        } catch (ClassCastException CCE) {
            throw new ClassCastException(context.toString() + " must implement OnRecipeClickListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //save LLM as parcelable so it's not recreated during orientation change
        outState.putParcelable(SAVED_RV, mLinearLayoutManager.onSaveInstanceState());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        //pull LLM as parcelable from saved state
        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(SAVED_RV);
        }
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        mCallback.onRecipeClicked(clickedItemIndex);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(VOLLEY_TAG);
        }
    }
}