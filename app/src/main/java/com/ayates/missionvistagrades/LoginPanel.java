package com.ayates.missionvistagrades;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginPanel extends AppCompatActivity implements View.OnClickListener
{
    public static final String TAG = "Parent Portal";
    public static ParentPortalFetcher PORTAL;

    public static PasswordStorage passwordStorage;

    public EditText emailText;
    public EditText passwordText;

    public LoginPanel loginPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_panel);

        findViewById(R.id.btn_login).setOnClickListener(this);

        emailText = (EditText) findViewById(R.id.input_email);
        passwordText = (EditText) findViewById(R.id.input_password);

        loginPanel = this;

        passwordStorage = new PasswordStorage("", "");
        PORTAL = new ParentPortalFetcher();

        passwordStorage.loadCredentials(getApplicationContext());
        emailText.setText(passwordStorage.getUsername());
        passwordText.setText(passwordStorage.getPassword());

        if (!emailText.getText().toString().isEmpty() && !passwordText.getText().toString().isEmpty()) login();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();

        PORTAL = new ParentPortalFetcher();
        passwordStorage.setUsername("").setPassword("");

        if (getIntent().hasExtra("Purpose"))
        {
            if (getIntent().getExtras().getString("Purpose").equals("ReloadFailed"))
            {
                passwordStorage.saveCredentials(getApplicationContext());
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btn_login:
                login();
                break;
        }
    }

    public void login()
    {
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        passwordStorage.setUsername("").setPassword("");

        new LoginTask().execute(email, password);
    }

    class LoginTask extends AsyncTask<String, Integer, Integer>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            ((Button) findViewById(R.id.btn_login)).setText("Loading...");
        }

        @Override
        protected Integer doInBackground(String... params)
        {
            String email = params[0];
            String password = params[1];
            //Log.d("Parent Portal", email + " : " + password);
            int code = PORTAL.login(email, password, getApplicationContext());

            if (code == 0)
            {
                passwordStorage.setUsername(email).setPassword(password);
            }
            else
            {
                passwordStorage.setUsername("").setPassword("");
            }

            return code;
        }

        @Override
        protected void onPostExecute(Integer code)
        {
            ((Button) findViewById(R.id.btn_login)).setText("Login");
            passwordStorage.saveCredentials(getApplicationContext());

            if (code == 0)
            {
                Intent intent = new Intent(loginPanel, ClassesList.class);
                startActivity(intent);
            }
            else
            {
                Toast to = Toast.makeText(loginPanel, "Couldn't connect to Parent Portal: Error code: " + code, Toast.LENGTH_SHORT);
                to.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                to.show();
            }
        }
    }
}
