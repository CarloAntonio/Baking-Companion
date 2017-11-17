package com.riskitbiskit.bakingcompanion.Details;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.riskitbiskit.bakingcompanion.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StepsAdapter extends RecyclerView.Adapter<StepsAdapter.StepsViewHolder> {

    private LayoutInflater inflater;
    private List<Instructions> instructions;
    private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    public StepsAdapter(Context context, List<Instructions> instructions, ListItemClickListener listener) {
        inflater = LayoutInflater.from(context);
        this.instructions = instructions;
        mOnClickListener = listener;
    }

    @Override
    public StepsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = inflater.inflate(R.layout.recipe_step_item, parent, false);
        StepsViewHolder stepsViewHolder = new StepsViewHolder(rootView);
        return stepsViewHolder;
    }

    public void onBindViewHolder(StepsViewHolder holder, int position) {
        Instructions currentStep = instructions.get(position);
        String currentStepInstruction = currentStep.getShortDescription();
        holder.mInstructionsTV.setText(currentStepInstruction);
    }

    @Override
    public int getItemCount() {
        return instructions.size();
    }

    class StepsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.instruction_step)
        TextView mInstructionsTV;

        public StepsViewHolder(View itemView) {
            super(itemView);
            //bind views
            ButterKnife.bind(this, itemView);
            //set click listener
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
        }
    }
}