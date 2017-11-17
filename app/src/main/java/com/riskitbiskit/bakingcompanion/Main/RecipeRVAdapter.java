package com.riskitbiskit.bakingcompanion.Main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.riskitbiskit.bakingcompanion.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecipeRVAdapter extends RecyclerView.Adapter<RecipeRVAdapter.RecipeViewHolder> {

    //Fields
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

    //TODO: feat - add a picture of a picture isnt present
    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        Recipe currentRecipe = mRecipes.get(position);

        if (!currentRecipe.getImage().equals("")) {
            if (holder.mRecipeIV != null) {
                holder.mRecipeIV.setVisibility(View.VISIBLE);
            }
            if (holder.mRecipeNameTV != null) {
                holder.mRecipeNameTV.setVisibility(View.INVISIBLE);
            }
            Glide.with(mContext).load(currentRecipe.getImage()).into(holder.mRecipeIV);
        } else {
            //use image in data drawable instead
            if (holder.mRecipeIV != null) {
                holder.mRecipeIV.setVisibility(View.VISIBLE);
            }
            if (holder.mRecipeNameTV != null) {
                holder.mRecipeNameTV.setVisibility(View.INVISIBLE);
            }

            //get recipe name
            String recipeName = currentRecipe.getName();

            //find correct drawable image
            if (recipeName.contentEquals("Nutella Pie")) {
                Glide.with(mContext).load(R.drawable.pie).into(holder.mRecipeIV);
            } else if (recipeName.contentEquals("Brownies")) {
                Glide.with(mContext).load(R.drawable.brownies).into(holder.mRecipeIV);
            } else if (recipeName.contentEquals("Yellow Cake")) {
                Glide.with(mContext).load(R.drawable.cake).into(holder.mRecipeIV);
            } else if (recipeName.contentEquals("Cheesecake")) {
                Glide.with(mContext).load(R.drawable.cheesecake).into(holder.mRecipeIV);
            } else {
                //Original code, specified by guideline, but UI looks unappealing
                if (holder.mRecipeIV != null) {
                    holder.mRecipeIV.setVisibility(View.INVISIBLE);
                }
                if (holder.mRecipeNameTV != null) {
                    holder.mRecipeNameTV.setVisibility(View.VISIBLE);
                }
                holder.mRecipeNameTV.setText(currentRecipe.getName());
            }
        }

    }

    @Override
    public int getItemCount() {
        return mRecipes.size();
    }

    public class RecipeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //Views
        @BindView(R.id.recipe_image_IV)
        ImageView mRecipeIV;
        @BindView(R.id.recipe_name_TV)
        TextView mRecipeNameTV;

        public RecipeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mListItemClicked.onListItemClick(clickedPosition);
        }
    }
}
