package com.inducesmile.workoutassistant;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class ChooseObjective extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Spinner spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_objective);
        spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.choices, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if(parent.getItemAtPosition(position).equals("Select Activity"))
        {
            // do nothing
        }
        else
        {
            String text = parent.getItemAtPosition(position).toString();
            Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();

            if(parent.getItemAtPosition(position).equals("Lap Count"))
            {
                Intent i = new Intent(ChooseObjective.this, LapCount.class);
                startActivity(i);
            }
            else if(parent.getItemAtPosition(position).equals("Sprint Count"))
            {
                Intent i = new Intent(ChooseObjective.this, SprintActivity.class);
                startActivity(i);
            }

        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}