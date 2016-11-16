package com.ayates.missionvistagrades;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

public class AssignmentListActivity extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener, AddAssignmentFragment.AddAssignmentListener
{
    private ListView listView;
    private Button addMockButton;

    public Classroom classroom;
    private List<Assignment> mockAssignments = new ArrayList<>();

    private AssignmentListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_list);

        listView = (ListView) findViewById(R.id.listview_assignments);
        listView.setOnItemClickListener(this);

        ((Switch) findViewById(R.id.mock)).setOnCheckedChangeListener(this);

        addMockButton = new Button(this);
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.addRule(RelativeLayout.BELOW, R.id.mock);
        p.addRule(RelativeLayout.CENTER_HORIZONTAL);
        addMockButton.setLayoutParams(p);
        addMockButton.setText("Add mock assignment");
        addMockButton.setTextSize(16);
        addMockButton.setOnClickListener(this);
        addMockButton.setId(R.id.addmockbutton);

        if (getIntent().hasExtra("Classroom"))
        {
            classroom = LoginPanel.PORTAL.getClassroom(getIntent().getExtras().getInt("Classroom"));
            updateClassroomInfoOnUI();
            adapter = new AssignmentListAdapter(this, R.layout.assignment_list_element, classroom.getAssignmentList());
            listView.setAdapter(adapter);
        }
        else
        {
            Log.e(LoginPanel.TAG, "AssignmentListActivity created, but clicked classroom is null.");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {

    }

    @Override
    public void onClick(View v)
    {
        if (v == addMockButton)
        {
            DialogFragment dialogFragment = new AddAssignmentFragment();
            dialogFragment.show(getFragmentManager(), "AddAssignmentFragment");
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if (buttonView.getId() == R.id.mock)
        {
            if (isChecked)
            {
                addMockAssignmentButton();
            }
            else
            {
                classroom.getAssignmentList().removeAll(mockAssignments);
                classroom.recalculateGrades();
                updateClassroomInfoOnUI();
                removeMockAssignmentButton();
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        classroom.getAssignmentList().removeAll(mockAssignments); //Remove all mock assignments
        classroom.recalculateGrades(); //Reset grades to original
        //Log.d(LoginPanel.TAG, "Back button pressed in AssignmentListActivity. Reset assignment list, recalculated grades, and destroying activity...");
        super.onBackPressed();
        finish();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String name, String score, String maxScore, String category)
    {
        float scoreF = 100;
        float maxScoreF = 100;

        if (name.isEmpty()) name = "Mock Assignment";
        try
        {
            scoreF = Float.parseFloat(score);
            maxScoreF = Float.parseFloat(maxScore);
        }
        catch (NumberFormatException e) {}

        Assignment mock = new Assignment("MOCK: " + name, category, scoreF, maxScoreF, scoreF / maxScoreF * 100, true);
        addMockAssignment(mock);
    }

    private void addMockAssignment(Assignment a)
    {
        mockAssignments.add(a);
        classroom.addAssignment(a);
        classroom.recalculateGrades();

        adapter.insert(a, 0);
        adapter.notifyDataSetChanged();

        updateClassroomInfoOnUI();

        Log.d(LoginPanel.TAG, "Added mock assignment '" + a.getName() + "' with category '" + a.getCategory() + "' and percentage " + a.getPercentage());
    }

    private void updateClassroomInfoOnUI()
    {
        ((TextView) findViewById(R.id.class_name)).setText(classroom.getName());
        ((TextView) findViewById(R.id.class_mark)).setText(classroom.getMark());
        ((TextView) findViewById(R.id.class_percent)).setText("" + classroom.getPercent());
    }

    private void addMockAssignmentButton()
    {
        ViewGroup view = (ViewGroup) findViewById(R.id.assign_list_layout);
        if (view != null)
            view.addView(addMockButton);

        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) findViewById(R.id.listview_assignments).getLayoutParams();
        p.removeRule(RelativeLayout.BELOW);
        p.addRule(RelativeLayout.BELOW, R.id.addmockbutton);
    }

    private void removeMockAssignmentButton()
    {
        ViewGroup view = (ViewGroup) findViewById(R.id.assign_list_layout);
        if (view != null)
            view.removeView(addMockButton);

        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) findViewById(R.id.listview_assignments).getLayoutParams();
        p.removeRule(RelativeLayout.BELOW);
        p.addRule(RelativeLayout.BELOW, R.id.class_percent);
    }

    private class AssignmentListAdapter extends ArrayAdapter<Assignment>
    {

        private final Context context;
        private final List<Assignment> values;

        public AssignmentListAdapter(Context context, int layout, List<Assignment> values)
        {
            super(context, layout, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.assignment_list_element, parent, false);

            Assignment assignment = values.get(pos);

            TextView title = (TextView) rowView.findViewById(R.id.title);
            TextView category = (TextView) rowView.findViewById(R.id.category);
            TextView score = (TextView) rowView.findViewById(R.id.score);
            TextView percent = (TextView) rowView.findViewById(R.id.percentage);

            if (!assignment.isSubmitted())
            {
                int c = Color.parseColor("#c9c9c9");
                title.setTextColor(c);
                category.setTextColor(c);
                score.setTextColor(c);
                percent.setTextColor(c);
            }
            else if (assignment.getScore() == 0)
            {
                int c = Color.parseColor("#FF0000");
                title.setTextColor(c);
                category.setTextColor(c);
                score.setTextColor(c);
                percent.setTextColor(c);
            }

            category.setText(assignment.getCategory());
            score.setText(assignment.getScore() + " / " + assignment.getMaxScore());
            percent.setText("" + assignment.getPercentage() + "%");

            if (assignment.getName().length() > 26)
            {
                int index = indexOf(assignment.getName(), " ", 26);
                title.setText(assignment.getName().substring(0, index) + "\n" + assignment.getName().substring(index + 1, assignment.getName().length()));
            }
            else
            {
                title.setText(assignment.getName());
            }

            return rowView;
        }

        /**
         * Returns greatest index of @find below @at in @text
         *
         * @param text
         * @param at
         * @return
         */
        private int indexOf(String text, String find, int at)
        {
            int last = text.indexOf(find);

            for (int i = last; i <= at; i++)
            {
                int latest = text.indexOf(find, i);

                if (latest > last && latest <= at) last = latest;
            }

            return last;
        }
    }
}
