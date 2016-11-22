package com.ayates.missionvistagrades;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Created by ayates on 11/11/16.
 */
public class AddAssignmentFragment extends DialogFragment
{
    AddAssignmentListener addAssignmentListener;
    Classroom classroom;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_addassignment, null);

        final Spinner spinner = (Spinner) view.findViewById(R.id.categories_spinner);

        final boolean weighted = !classroom.getCategoriesMap().isEmpty();

        if (!weighted)
        {
            spinner.setVisibility(View.INVISIBLE);
        }
        else
        {
            ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, classroom.getDisplayCategories());
            categoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(categoriesAdapter);
        }

        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Add", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        addAssignmentListener.onDialogPositiveClick(AddAssignmentFragment.this, ((EditText) view.findViewById(R.id.addassignment)).getText().toString(),
                                (((EditText) view.findViewById(R.id.addassignment_score)).getText().toString()), (((EditText) view.findViewById(R.id.addassignment_maxScore)).getText().toString()),
                                weighted ? spinner.getSelectedItem().toString().split(" \\(")[0] : "");
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        AddAssignmentFragment.this.getDialog().cancel();
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); //Switches on keyboard immediately
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try
        {
            // Instantiate the NoticeDialogListener so we can send events to the host
            classroom = ((AssignmentListActivity) activity).classroom;
            addAssignmentListener = (AddAssignmentListener) activity;
        }
        catch (ClassCastException e)
        {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement AddAssignmentListener and be of type AssignmentListActivity");
        }
    }

    public interface AddAssignmentListener
    {
        public void onDialogPositiveClick(DialogFragment dialog, String name, String score, String maxScore, String category);
    }
}
