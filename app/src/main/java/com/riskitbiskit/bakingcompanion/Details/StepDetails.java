package com.riskitbiskit.bakingcompanion.Details;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.riskitbiskit.bakingcompanion.R;

public class StepDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_details);

        if (savedInstanceState == null) {
            StepDetailsFragment stepDetailsFragment = new StepDetailsFragment();

            FragmentManager fragmentManager = getSupportFragmentManager();

            fragmentManager.beginTransaction()
                    .add(R.id.step_details_frag_container, stepDetailsFragment)
                    .commit();
        }
    }
}
