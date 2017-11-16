package com.riskitbiskit.bakingcompanion.Main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.riskitbiskit.bakingcompanion.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecipeRVAdapter extends RecyclerView.Adapter<RecipeRVAdapter.RecipeViewHolder> {

    private LayoutInflater mLayoutInflater;
    private List<Recipe> mRecipes;
    private Context mContext;
    private ListItemClicked mListItemClicked;

    public interface ListItemClicked {
        void onListItemClick(int clickedItemIndex);
    }

    public RecipeRVAdapter(Context context, List<Recipe> recipes, ListItemClicked listItemClicked){
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
        mRecipes = recipes;
        mListItemClicked = listItemClicked;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = mLayoutInflater.inflate(R.layout.recipe_item, parent, false);

        RecipeViewHolder recipeViewHolder = new RecipeViewHolder(rootView);

        return recipeViewHolder;
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        Recipe currentRecipe = mRecipes.get(position);

        if (!currentRecipe.getImage().equals("")) {
            if (holder.recipeImage != null) {
                holder.recipeImage.setVisibility(View.VISIBLE);
            }
            if (holder.recipeNameTV != null) {
                holder.recipeNameTV.setVisibility(View.INVISIBLE);
            }
            Picasso.with(mContext).load(currentRecipe.getImage()).into(holder.recipeImage);
        } else {
            if (holder.recipeImage != null) {
                holder.recipeImage.setVisibility(View.INVISIBLE);
            }
            if (holder.recipeNameTV != null) {
                holder.recipeNameTV.setVisibility(View.VISIBLE);
            }
            holder.recipeNameTV.setText(currentRecipe.getName());
        }

    }

    @Override
    public int getItemCount() {
        return mRecipes.size();
    }

    public class RecipeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView recipeImage;
        TextView recipeNameTV;

        public RecipeViewHolder(View itemView) {
            super(itemView);

            recipeImage = itemView.findViewById(R.id.recipe_image);
            recipeNameTV = itemView.findViewById(R.id.recipe_name);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mListItemClicked.onListItemClick(clickedPosition);
        }
    }
}
