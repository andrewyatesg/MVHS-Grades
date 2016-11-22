package com.ayates.missionvistagrades;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;


public class ChangeStudentFragment extends DialogFragment
{
    ChangeStudentListener changeStudentListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_change_student, null);

        final Spinner spinner = (Spinner) view.findViewById(R.id.change_student_spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, LoginPanel.PORTAL.getStudents());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Okay", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        changeStudentListener.onDialogPositiveClick(ChangeStudentFragment.this, spinner.getSelectedItemPosition());
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try
        {
            // Instantiate the NoticeDialogListener so we can send events to the host
            changeStudentListener = (ChangeStudentListener) activity;
        }
        catch (ClassCastException e)
        {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement ChangeStudentListener and be of type ClassesList");
        }
    }

    public interface ChangeStudentListener
    {
        public void onDialogPositiveClick(DialogFragment dialog, int pos);
    }
}
