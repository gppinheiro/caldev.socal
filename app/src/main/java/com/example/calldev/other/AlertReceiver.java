package com.example.calldev.other;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.calldev.R;

import static com.example.calldev.other.Notifications.CHANNEL_2_ID;

/**
 * Date: Dec 22-2020.
 * This class extends the BroadCastReceiver class and is able to receive and handle broadcast intents.
 * In this case it receives an intent from the EventsActivity class regarding an alarm that was set up to trigger after 1 minute.
 * It is spawned from the EventsActivity class.
 * @author CALDEV.
 */

public class AlertReceiver extends BroadcastReceiver
{
    /**
     * This method is called when the BroadcastReceiver class is receives an Intent broadcast, in this case where the alarm is fired.
     * It allows a notification, regarding gym interactions such has a drink water reminder.
     * One of the channels created in the Notifications class is used and the contents of the notification are also set up.
     */
    @Override
    public void onReceive(Context context, Intent intent)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_2_ID)
                .setSmallIcon(R.drawable.ic_people)
                .setContentTitle("Training")
                .setContentText("Don't Forget to Drink Water Regularly")
                .setAutoCancel(true)
                .build();

        notificationManager.notify(2, notification);
    }
}
