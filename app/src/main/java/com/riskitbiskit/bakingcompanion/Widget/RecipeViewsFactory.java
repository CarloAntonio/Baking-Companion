package com.riskitbiskit.bakingcompanion.Widget;

import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.riskitbiskit.bakingcompanion.Details.Ingredient;
import com.riskitbiskit.bakingcompanion.Main.MainActivity;
import com.riskitbiskit.bakingcompanion.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class RecipeViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    public static final String LOG_TAG = RecipeViewsFactory.class.getSimpleName();

    private Context context;
    private int requestedRecipe;
    private List<Ingredient> mIngredients;
    private int totalRecipes;

    public RecipeViewsFactory(Context applicationContext, int requestedRecipe, int totalRecipes) {
        context = applicationContext;
        this.requestedRecipe = requestedRecipe;
        this.totalRecipes = totalRecipes;
    }

    @Override
    public void onCreate() {
        mIngredients = new ArrayList<>();
    }

    @Override
    public void onDataSetChanged() {
        RequestQueue mRequestQueue = Volley.newRequestQueue(context);

        if (requestedRecipe == totalRecipes - 1) {
            requestedRecipe = 0;
        } else {
            requestedRecipe++;
        }
        mIngredients.clear();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, MainActivity.DATA_URL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject currentRecipe = response.getJSONObject(requestedRecipe);
                    String currentRecipeName = currentRecipe.getString("name");
                    mIngredients.add(new Ingredient(0.0, "fakeMesurement", currentRecipeName));

                    //Make Ingredients List
                    JSONArray currentIngredientsList = currentRecipe.getJSONArray("ingredients");
                    for (int i = 0; i < currentIngredientsList.length(); i++) {
                        JSONObject currentIngredient = currentIngredientsList.getJSONObject(i);
                        double quantity = currentIngredient.getDouble("quantity");
                        String measure = currentIngredient.getString("measure");
                        String ingredient = currentIngredient.getString("ingredient");

                        mIngredients.add(new Ingredient(quantity, measure, ingredient));
                    }

                } catch (JSONException JSONE) {
                    JSONE.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, error + "");
            }
        });

        mRequestQueue.add(jsonArrayRequest);

    }

    @Override
    public void onDestroy() {
        //Any volley cleanup needed
        mIngredients.clear();
    }

    @Override
    public int getCount() {
        //total number of volley items
        if (mIngredients.isEmpty()) {
            return 0;
        }
        return mIngredients.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //implement changes to one row/view
        if (mIngredients.isEmpty() || mIngredients.size() == 0) return null;

        Ingredient currentIngredient = mIngredients.get(position);
        String stepIngredient = currentIngredient.getIngredient();
        String capIngredient = stepIngredient.substring(0, 1).toUpperCase() + stepIngredient.substring(1);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_item);

        if (position == 0) {
            views.setTextViewText(R.id.list_item_widget, capIngredient.toUpperCase() + ":" );
        } else {
            views.setTextViewText(R.id.list_item_widget, capIngredient);
        }

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        //Add delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}