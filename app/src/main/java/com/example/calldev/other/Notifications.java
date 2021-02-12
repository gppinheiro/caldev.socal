package com.example.calldev.other;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

/**
* Date: Dec 5-2020.
* This is a class that extends application and it is where the notifications channels are created.
* The Application class that this class extends has the characteristic of being the first created when the app is launched.
* @author CALDEV.
*/

public class Notifications extends Application
{
    /**
     * String constant used to create the notification channel associated with events.
     */
    public static final String CHANNEL_1_ID = "EventChannel";
    /**
     * String constant used to create the notification channel associated with gym activities such as drink water reminders.
     */
    public static final String CHANNEL_2_ID = "GymChannel";

    /**
     * Called whenever this class is created.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
    }

    /**
     * Method that creates the notification channels that the app will have and it's characteristics such as it's name and importance level.
     * This operation must be done inside an if statement to make sure that the device has an API level > 26 Oreo.
     */
    private void createNotificationChannels()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationManager manager = getSystemService(NotificationManager.class);
            NotificationChannel channel1 = new NotificationChannel(CHANNEL_1_ID, "Event", NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("This is Channel 1 for the Event component");
            NotificationChannel channel2 = new NotificationChannel(CHANNEL_2_ID, "Training", NotificationManager.IMPORTANCE_HIGH);
            channel2.setDescription("This is Channel 2 for the Training component");

            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);
        }

    }
}
