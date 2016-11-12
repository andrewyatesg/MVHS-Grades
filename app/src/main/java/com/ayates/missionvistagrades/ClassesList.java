package com.ayates.missionvistagrades;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

import static com.ayates.missionvistagrades.LoginPanel.PORTAL;

public class ClassesList extends Activity implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener
{
    private SwipeRefreshLayout mSwipeContainer;
    private ListView listView;
    private ClassListArrayAdapter classListArrayAdapter;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes_list);

        mSwipeContainer = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeContainer.setOnRefreshListener(this);

        listView = (ListView) findViewById(R.id.listview_classes);
        listView.setOnItemClickListener(this);

        classListArrayAdapter = new ClassListArrayAdapter(this, R.layout.class_list_element, new ArrayList<Classroom>());
        listView.setAdapter(classListArrayAdapter);

        new ReloadTask().execute(); //onCreate must execute a ReloadTask to update classes and the UI. Later, user can just swipe up to redo this process
    }

    @Override
    public void onRefresh()
    {
        new ReloadTask().execute();
        Log.d(LoginPanel.TAG, "Refresh has been called");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Classroom classroom = (Classroom) parent.getItemAtPosition(position);

        Intent i = new Intent(this, AssignmentListActivity.class);
        i.putExtra("Classroom", classroom.getId());
        startActivity(i);

        Log.d(LoginPanel.TAG, "\"" + classroom.getName() + "\" has been clicked in listview.");
    }

    /**
     * This method is called from ReloadTask after finished execution.
     *
     * @param code result code of reload
     */
    private void onFinishReload(int code)
    {
        if (code == 0) //If success
        {
            classListArrayAdapter.clear();
            classListArrayAdapter.addAll(PORTAL.getClasses());
            classListArrayAdapter.notifyDataSetChanged();
        }
        else if (code == ParentPortalFetcher.SESSION_TIMEOUT) //If the session has timed out, login and obtain a new session ID
        {
            new LoginTask().execute(); //Executes a login in separate thread
        }
        else if (code == ParentPortalFetcher.NO_CONNECT)
        {
            Toast toast = Toast.makeText(this, "Couldn't connect to Parent Portal, please try again later.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }
    }

    /**
     * This method is called from LoginTask after finished execution.
     *
     * @param code result code of login
     */
    private void onLoginAfterReloadFailed(int code)
    {
        if (code == 0) //If success
        {
            Log.d(LoginPanel.TAG, "Login was successful after failed attempt at reload.");
            new ReloadTask().execute(); //Reload the classes now that a new session ID was obtained
        }
        else if (code == ParentPortalFetcher.INCORRECT_LOGIN)
        {
            Log.d(LoginPanel.TAG, "Login was unsuccessful (due to incorrect credentials) after failed attempt at reload.");
            Intent i = new Intent(this, LoginPanel.class);
            i.putExtra("Purpose", "ReloadFailed");
            startActivity(i); //Given the login credentials are incorrect, start the LoginPanel activity again for the user to correct this issue
            finish(); //Destroys this activity so it cannot be accessed from back button or other methods.
        }
        else if (code == ParentPortalFetcher.NO_CONNECT)
        {
            Log.d(LoginPanel.TAG, "Login was unsuccessful (due to connection issues) after failed attempt at reload."); //A connection error, user can retry after client and/or server issue is resolved
        }
        else if (code == ParentPortalFetcher.SESSION_ERROR)
        {
            Log.d(LoginPanel.TAG, "Login was unsuccessful (due to some weird unresolved session error) after failed attempt at reload."); //A connection error, user can retry after client and/or server issue is resolved
        }
    }

    private class LoginTask extends AsyncTask<String, Integer, Integer>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            mSwipeContainer.setRefreshing(true);

            Log.d(LoginPanel.TAG, "Logging in because reload failed.");
        }

        @Override
        protected Integer doInBackground(String... params)
        {
            String usr = LoginPanel.passwordStorage.getUsername();
            String pass = LoginPanel.passwordStorage.getPassword();

            PORTAL = new ParentPortalFetcher();
            return PORTAL.login(usr, pass);
        }

        @Override
        protected void onPostExecute(Integer code)
        {
            super.onPostExecute(code);

            mSwipeContainer.setRefreshing(false);

            Log.d(LoginPanel.TAG, "Finished logging in (due to reload fail) with code=" + code);

            onLoginAfterReloadFailed(code);
        }
    }

    private class ReloadTask extends AsyncTask<String, Integer, Integer>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            mSwipeContainer.setRefreshing(true);

            Log.d(LoginPanel.TAG, "Refreshing class list.");
        }

        @Override
        protected Integer doInBackground(String... params)
        {
            return PORTAL.refresh();
        }

        @Override
        protected void onPostExecute(Integer code)
        {
            super.onPostExecute(code);

            mSwipeContainer.setRefreshing(false);

            Log.d(LoginPanel.TAG, "Refreshing class list finished with code=" + code);

            onFinishReload(code);
        }
    }

    private class ClassListArrayAdapter extends ArrayAdapter<Classroom>
    {

        private final Context context;
        private final List<Classroom> values;

        public ClassListArrayAdapter(Context context, int layout, List<Classroom> values)
        {
            super(context, layout, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.class_list_element, parent, false);

            TextView grade = (TextView) rowView.findViewById(R.id.grade);
            TextView name = (TextView) rowView.findViewById(R.id.name);
            TextView avg = (TextView) rowView.findViewById(R.id.avg);

            grade.setText(values.get(pos).getMark());
            name.setText(values.get(pos).getName());
            avg.setText(values.get(pos).getPercent() + "%");

            return rowView;
        }
    }
}
