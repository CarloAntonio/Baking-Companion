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

public class RecipeStepsFragment extends Fragment implements StepsAdapter.ListItemClickListener {

    public static final String VOLLEY_TAG = "volleyTag";
    public static final String SAVED_RV = "savedRV";

    RequestQueue mRequestQueue;
    ArrayList<Ingredient> ingredients;
    ArrayList<Instructions> instructions;
    RecyclerView mRecyclerView;
    StepsAdapter mStepsAdapter;
    int recipeNumber;
    LinearLayout mLinearLayout;
    TextView ingredientsListTV;
    LinearLayoutManager mLinearLayoutManager;

    OnRecipeClickListener mCallback;
    private Parcelable mListState;

    public interface  OnRecipeClickListener {
        void onRecipeClicked(int position);
    }

    public RecipeStepsFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recipe_steps_frag, container, false);

        mLinearLayout = rootView.findViewById(R.id.view_steps_frag);

        Intent intent = getActivity().getIntent();

        if (intent.hasExtra(MainActivity.RECIPE_INDEX_NUMBER)) {
            recipeNumber = intent.getIntExtra(MainActivity.RECIPE_INDEX_NUMBER, 0);
        }

        ingredients = new ArrayList<>();
        instructions = new ArrayList<>();

        mRecyclerView = rootView.findViewById(R.id.ingredient_steps_rv);
        ingredientsListTV = rootView.findViewById(R.id.ingredients_list);

        mRequestQueue = Volley.newRequestQueue(getContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, MainActivity.DATA_URL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject currentRecipe = response.getJSONObject(recipeNumber);

                    //Make Ingredients List
                    JSONArray currentIngredientsList = currentRecipe.getJSONArray("ingredients");
                    for (int i = 0; i < currentIngredientsList.length(); i++) {
                        JSONObject currentIngredient = currentIngredientsList.getJSONObject(i);
                        double quantity = currentIngredient.getDouble("quantity");
                        String measure = currentIngredient.getString("measure");
                        String ingredient = currentIngredient.getString("ingredient");

                        ingredients.add(new Ingredient(quantity, measure, ingredient));
                    }

                    String ingredientsList = "Ingredients: \n";
                    for (int i = 0; i < ingredients.size(); i++) {

                        //https://stackoverflow.com/questions/3904579/how-to-capitalize-the-first-letter-of-a-string-in-java
                        String ingredient = ingredients.get(i).getIngredient();
                        String capIngredient = ingredient.substring(0, 1).toUpperCase() + ingredient.substring(1);

                        ingredientsList = ingredientsList
                                //https://stackoverflow.com/questions/3429546/how-do-i-add-a-bullet-symbol-in-textview
                                + "\u2022 "
                                + capIngredient
                                + " ("
                                + Double.toString(ingredients.get(i).getQuantity())
                                + " "
                                + ingredients.get(i).getMeasurement()
                                + ")\n";
                    }



                    ingredientsListTV.setText(ingredientsList);

                    //Make Instructions List
                    JSONArray currentInstructionsList = currentRecipe.getJSONArray("steps");
                    for (int i = 0; i < currentInstructionsList.length(); i++) {
                        JSONObject currentStep = currentInstructionsList.getJSONObject(i);
                        int id = currentStep.getInt("id");
                        String shortDescription = currentStep.getString("shortDescription");
                        String description = currentStep.getString("description");
                        String videoUrl = currentStep.getString("videoURL");
                        String thumbnail = currentStep.getString("thumbnailURL");

                        instructions.add(new Instructions(id, shortDescription, description, videoUrl, thumbnail));
                    }

                    mStepsAdapter.notifyDataSetChanged();

                    if (mListState != null) {
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

        mStepsAdapter = new StepsAdapter(getContext(), instructions, this);

        mRecyclerView.setAdapter(mStepsAdapter);

        if (mLinearLayoutManager == null) {
            mLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        }

        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        return rootView;
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
        outState.putParcelable(SAVED_RV, mLinearLayoutManager.onSaveInstanceState());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

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