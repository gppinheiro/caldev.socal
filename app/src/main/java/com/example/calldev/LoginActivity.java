package com.example.calldev;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Date: Nov 28-2020.
 * This is a class that extends activity and represents the login page of the app.
 * @author CALDEV.
 */

public class LoginActivity extends AppCompatActivity
{
    /**
     * String constant used to distinguish this activity (used in debugging and to show the user pop up messages).
     */
    private static final String TAG = "LoginActivity";
    /**
     * String constant used to pass the email address to the SigninActivity class.
     */
    public static final String EXTRA_EMAIL_LOGIN = "com.example.login.example.EXTRA_EMAIL";
    /**
     * String constant passed to the SigninActivity class to signal that the user logout.
     */
    public static final String EXTRA_LOGIN_LOGOUT = "com.example.login.example.EXTRA_LOGIN_LOGOUT";

    /**
     * EditText XML object used by the user to insert it's email address.
     */
    private EditText EmailEditText;

    /**
     * Called whenever this activity is created.
     * Sets up the XML variables of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EmailEditText = findViewById(R.id.EmailEditText);
    }
    
    /**
     * Method called upon the user clicking the login button.
     * Checks if the user inserts any text and then proceeds to the SigninActivity class.
     * This activity is also called when the user presses the logout button in any of the activity pages of the app.
     * @param view the object of class View in which the UI is drawn upon.
     */
    public void onClickLoginButton(View view)
    {
        String email = EmailEditText.getText().toString();

        if(email.trim().length() > 0)
        {
            Intent login = new Intent(this, SignInActivity.class);
            login.putExtra(EXTRA_EMAIL_LOGIN, email);
            if(checkIncomingIntent())
            {
                login.putExtra(EXTRA_LOGIN_LOGOUT, "LOGOUT");
            }
            login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(login);
            finish();
        }
        else
        {
            Toast.makeText(this, "Please Input Data", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Method responsible for checking the reception of the logoff button interaction from other activities.
     * These constants are then used in the SigInActivity class for the selection of the google account.
     * If it receives the constant signifying this interaction it will clear data stored in the shared preferences.
     * @return a boolean either true or false.
     */
    private boolean checkIncomingIntent()
    {
        Intent intent = getIntent();
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        settings.edit().clear().commit();
        return intent.hasExtra(CalendarActivity.EXTRA_CALENDAR_LOGOUT) || intent.hasExtra(ProfileActivity.EXTRA_PROFILE_LOGOUT) || intent.hasExtra(EventsActivity.EXTRA_EVENTS_LOGOUT) || intent.hasExtra(GroupsActivity.EXTRA_GROUPS_LOGOUT);
    }
}