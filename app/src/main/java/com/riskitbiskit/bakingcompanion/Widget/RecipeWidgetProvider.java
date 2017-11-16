package com.riskitbiskit.bakingcompanion.Widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.riskitbiskit.bakingcompanion.Details.RecipeDetails;
import com.riskitbiskit.bakingcompanion.R;

public class RecipeWidgetProvider extends AppWidgetProvider {
    public static final String LOG_TAG = RecipeWidgetProvider.class.getSimpleName();

    public static final String RECIPE_TAG = "recipeTag";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int requestedRecipe, int totalRecipes, int appWidgetId) {


        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.recipe_widget_provider);

        //Setup button intent
        Intent buttonIntent = new Intent(context, UpdateWidgetService.class);
        buttonIntent.putExtra(RECIPE_TAG, requestedRecipe);
        buttonIntent.setAction(UpdateWidgetService.ACTION_UPDATE_WIDGET);
        PendingIntent buttonPendingIntent = PendingIntent.getService(context, 0, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.next_button_widget, buttonPendingIntent);

        //Setup the WidgetService intent to act as the adapter for the ListView
        Intent intent = new Intent(context, WidgetService.class);
        //Add the id of the recipe we want to display as a ListView
        intent.putExtra(WidgetService.REQUESTED_RECIPE, requestedRecipe);
        //Send off to have the Widget Service to create the adapter
        intent.putExtra(WidgetService.TOTAL_RECIPES, totalRecipes);
        views.setRemoteAdapter(R.id.widget_lv_steps, intent);

        //Set the RecipeDetails activity to launch when clicked
        Intent appLaunchIntent = new Intent(context, RecipeDetails.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 , appLaunchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_lv_steps, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        UpdateWidgetService.startActionWidgetUpdate(context);
    }

    public static void updateWidgets(Context context, AppWidgetManager appWidgetManager,
                                     int requestedRecipe, int totalRecipes, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, requestedRecipe, totalRecipes, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}