package com.example.calldev;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Date: Dec 8-2020.
 * This is a class that extends activity and represents the subpage of the app in which the user can create a new group.
 * It is spawned from the GroupsActivity class.
 * The user must have premium status to access this class.
 * @author CALDEV.
 */
public class AddGroupActivity extends AppCompatActivity
{
    /**
     * String constant used to distinguish this activity (used in debugging and to show the user pop up messages).
     */
    private static final String TAG = "AddGroupActivity";
    /**
     * String constant passed to the GroupsActivity class with the inserted group's name.
     */
    public static final String EXTRA_GROUP_NAME = "com.example.login.example.EXTRA_GROUP_NAME";
    /**
     * String constant passed to the GroupsActivity class with the inserted group's type (public or private).
     */
    public static final String EXTRA_GROUP_STATE = "com.example.login.example.EXTRA_GROUP_STATE";

    /**
     * EditText XML object used to insert the group's name.
     */
    private EditText NewGroupEditText;
    /**
     * RadioGroup XML object used to show the group's type options.
     */
    private RadioGroup GroupRadioGroup;
    /**
     * RadioButton XML object used to choose the group's type option.
     */
    private RadioButton OptionRadio;

    /**
     * String value of the selected group's type.
     */
    private String groupOption = null;

    /**
     * Called whenever this activity is created.
     * Sets up the XML variables of the activity and the title of the page.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        setTitle("New Group");

        NewGroupEditText = findViewById(R.id.NewGroupEditText);
        GroupRadioGroup = findViewById(R.id.GroupRadioGroup);
    }

    /**
     * Method called upon choosing an option of the type of group that the user creates (Public or Private).
     * @param view the object of class View in which the UI is drawn upon.
     */
    public void onClickCheckButtonState(View view)
    {
        int optionId = GroupRadioGroup.getCheckedRadioButtonId();

        OptionRadio = findViewById(optionId);

        groupOption = String.valueOf(OptionRadio.getText());
    }

    /**
     * Method that is called when the users presses the save button on the top right corner of the page.
     * It deals with the treatment of the inserted new group data and exits this subpage.
     * If the user did not fill in all required information a message will appear.
     */
    private void saveGroup()
    {
        String newGroup = NewGroupEditText.getText().toString();

        if(newGroup.trim().isEmpty())
        {
            Toast.makeText(this, "Please Enter A Name for the Group", Toast.LENGTH_SHORT).show();
        }
        else if(groupOption == null)
        {
            Toast.makeText(this, "Please Choose a Option for the Group", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Intent groupdata = new Intent();
            groupdata.putExtra(EXTRA_GROUP_NAME, newGroup);
            groupdata.putExtra(EXTRA_GROUP_STATE, groupOption);
            setResult(RESULT_OK, groupdata);
            finish();
        }
    }

    /**
     * Method responsible for setting up a menu from a XML file, upon which the UI can draw upon in the top right corner of the page.
     * @return a boolean that is true for the menu to be displayed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.save_menu, menu);
        return true;
    }

    /**
     * Method responsible for dealing with the onclick interactions of the menu defined earlier.
     * Upon these interactions, the respective methods will be called upon.
     * @return a boolean true to allow menu processing to proceed in this activity.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.SaveMenu)
        {
            saveGroup();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}