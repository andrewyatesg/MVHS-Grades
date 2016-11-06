package com.ayates.missionvistagrades;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AssignmentListActivity extends Activity implements AdapterView.OnItemClickListener
{
    private ListView listView;

    private Classroom classroom;
    private List<Assignment> mockAssignments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_list);

        listView = (ListView) findViewById(R.id.listview_assignments);
        listView.setOnItemClickListener(this);

        classroom = LoginPanel.PORTAL.getClassroom(getIntent().getExtras().getInt("Classroom"));

        if (classroom != null)
        {
            ((TextView) findViewById(R.id.class_name)).setText(classroom.getName());
            ((TextView) findViewById(R.id.class_mark)).setText(classroom.getMark());
            ((TextView) findViewById(R.id.class_percent)).setText("" + classroom.getPercent());
            List<Assignment> assignments = classroom.getAssignmentList();
            final AssignmentListAdapter adapter = new AssignmentListAdapter(this, R.layout.assignment_list_element, assignments);
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
    public void onBackPressed()
    {
        classroom.getAssignmentList().removeAll(mockAssignments); //Remove all mock assignments
        classroom.recalculateGrades(); //Reset grades to original
        //Log.d(LoginPanel.TAG, "Back button pressed in AssignmentListActivity. Reset assignment list, recalculated grades, and destroying activity...");
        super.onBackPressed();
        finish();
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

            category.setText(assignment.getCategory());
            score.setText(assignment.getScore() + " / " + assignment.getMaxScore());
            percent.setText("" + assignment.getPercentage());

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
