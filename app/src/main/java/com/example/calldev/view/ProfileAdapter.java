package com.example.calldev.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.calldev.ProfileActivity;
import com.example.calldev.other.GlobalStorage;
import com.example.calldev.item.ProfileItem;
import com.example.calldev.R;
import com.example.calldev.server.ServerProfileAsyncTask;

import java.util.ArrayList;

/**
 * Date: Nov 20-2020.
 * This a class that extends adapter (more specifically the recyclerview adapter class).
 * This adapter provides a binding from an app-specific data set to views that are displayed within a RecyclerView (in this case the RecyclerView used for the Profile).
 * @author CALDEV.
 */

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>
{
    /**
     * String constant used to distinguish this class (used in debugging).
     */
    public static final String TAG = "ProfileAdapter";

    /**
     * ArrayList object containing the profile data in the form of ProfileItem objects.
     */
    private final ArrayList<ProfileItem> ProfileList;

    /**
     * Date: Nov 20-2020.
     * This subclass extends RecyclerView.ViewHolder.
     * A ViewHolder describes an item view and metadata regarding its place within the RecyclerView.
     * The RecyclerView.Adapter implements this subclass ViewHolder adding fields for caching potentially expensive View.findViewById(int) results.
     * @author CALDEV.
     */

    public static class ProfileViewHolder extends RecyclerView.ViewHolder
    {
        /**
         * ImageView XML object used to display the user's profile picture.
         */
        private final ImageView ProfileIconImageView;
        /**
         * TextView XML object used to display the profile item's name or the user's username.
         */
        private final TextView ItemTextView;
        /**
         * TextView XML object used to display the user's email address bellow the ItemTextView object.
         */
        private final TextView SubItemTextView;
        /**
         * TextView XML object used to display the profile item's data.
         */
        private final TextView DataTextView;
        /**
         * Switch XML object used to display the notifications' options.
         */
        private final Switch ProfileSwitch;

        /**
         * Method that links the XML objects from the XML layout file chosen.
         * @param itemView the ViewHolder created in the onCreateViewHolder() method.
         */
        public ProfileViewHolder(View itemView)
        {
            super(itemView);

            ProfileIconImageView = itemView.findViewById(R.id.ProfileIconImageView);
            ItemTextView = itemView.findViewById(R.id.ItemTextView);
            SubItemTextView = itemView.findViewById(R.id.SubItemTextView);
            DataTextView = itemView.findViewById(R.id.DataTextView);
            ProfileSwitch = itemView.findViewById(R.id.ProfileSwitch);
        }
    }

    /**
     * Constructor used for the ProfileAdapter class.
     * Also notify the user that the data set has changed.
     * @param profileList the ArrayList passed from the ProfileActivity class containing all the profile data retrieved.
     */
    public ProfileAdapter(ArrayList<ProfileItem> profileList)
    {
        this.ProfileList = profileList;
        notifyDataSetChanged();
    }

    /**
     * Method called when the RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item.
     * This new ViewHolder is constructed with a new View that can represent the items of a given type.
     * In this case we inflate it from the profile_item XML layout file. This file is a CardView object populated with several XML objects.
     * The new ViewHolder is used to display the items of the adapter using onBindViewHolder(ViewHolder, int, List).
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return The new ViewHolder that holds a View of the given view type.
     */
    @Override
    public ProfileViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View profile = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_item, parent, false);
        ProfileViewHolder profileViewHolder = new ProfileViewHolder(profile);

        return profileViewHolder;
    }

    /**
     * Method called by RecyclerView to display the data at a specified position.
     * This method updates the contents of the RecyclerView.ViewHolder.itemView to reflect the item at the given position.
     * If the user changes any of the settings switch's, this action will signal to the server to update their stored values.
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ProfileViewHolder holder, int position)
    {
        ProfileItem currentItem = ProfileList.get(position);
        holder.ProfileIconImageView.setImageResource(currentItem.getPhoto());
        holder.ItemTextView.setText(currentItem.getText());
        holder.SubItemTextView.setText(currentItem.getSubText());
        if(currentItem.getFlag() == 1)
        {
            holder.DataTextView.setText(currentItem.getData());
            holder.DataTextView.setVisibility(View.VISIBLE);
        }
        else if(currentItem.getFlag() == 2)
        {
            holder.ProfileSwitch.setVisibility(View.VISIBLE);
            holder.ProfileSwitch.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    boolean state = currentItem.getState();
                    currentItem.setState(!state);
                    if(position == 5)
                    {
                        GlobalStorage.getInstance().setEventNots(currentItem.getState());
                    }
                    else if(position == 6)
                    {
                        GlobalStorage.getInstance().setGymNots(currentItem.getState());
                    }
                    new ServerProfileAsyncTask(ProfileAdapter.this, "notifications", GlobalStorage.getInstance().getEmail()).execute();
                }
            });
            holder.ProfileSwitch.setChecked(currentItem.getState());
        }
        else
        {
            holder.DataTextView.setVisibility(View.INVISIBLE);
            holder.ProfileSwitch.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Method that returns the total number of items in the data set held by the adapter.
     * @return the size of the ProfileList ArrayList.
     */
    @Override
    public int getItemCount()
    {
        return ProfileList.size();
    }
}
