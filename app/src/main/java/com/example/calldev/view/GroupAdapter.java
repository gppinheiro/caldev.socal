package com.example.calldev.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.calldev.item.GroupItem;
import com.example.calldev.R;

import java.util.ArrayList;

/**
 * Date: Dec 5-2020.
 * This a class that extends adapter (more specifically the recyclerview adapter class).
 * This adapter provides a binding from an app-specific data set to views that are displayed within a RecyclerView (in this case the RecyclerView used for the Groups).
 * @author CALDEV.
 */

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder>
{
    /**
     * String constant used to distinguish this class (used in debugging).
     */
    public static final String TAG = "GroupAdapter";

    /**
     * ArrayList object containing groups in the form of GroupItem objects.
     */
    private final ArrayList<GroupItem> GroupList;
    /**
     * Listener object that is handled from the GroupsActivity class.
     */
    private GroupAdapter.onGroupClickListener groupListener;

    /**
     * Date: Dec 5-2020.
     * Interface class used to create the onclick listener method.
     * It passes the current position that the user clicked to the GroupsActivity class.
     * @author CALDEV.
     */

    public interface onGroupClickListener
    {
        void onGroupClick(GroupItem groupItem);
    }

    /**
     * Method that is used in the GroupsActivity class to connect the GroupAdapter with the interface defined earlier and pass the listener variable.
     * @param groupListener the listener variable that is handled from the GroupsActivity class.
     */
    public void setOnGroupClickListener(onGroupClickListener groupListener)
    {
        this.groupListener = groupListener;
    }

    /**
    * Date: Dec 5-2020.
    * This subclass extends RecyclerView.ViewHolder.
    * A ViewHolder describes an item view and metadata regarding its place within the RecyclerView.
    * The RecyclerView.Adapter implements this subclass ViewHolder adding fields for caching potentially expensive View.findViewById(int) results.
    * @author CALDEV.
    */

    public class GroupViewHolder extends RecyclerView.ViewHolder
    {
        /**
         * ImageView XML object used to display the groups user's profile picture and the group's colour based on its type.
         */
        private final ImageView PersonColourImageView;
        /**
         * TextView XML object used to display the group's name and the group user's email address.
         */
        private final TextView GroupNameTextView;
        /**
         * TextView XML object used to display the group's owner email address and the group user's status (owner or member).
         */
        private final TextView GroupOwnerTextView;

        /**
         * Method that links the XML objects from the XML layout file chosen.
         * It also sets up an onclick listener to the entire object.
         * @param itemView the ViewHolder created in the onCreateViewHolder() method.
         */
        public GroupViewHolder(View itemView)
        {
            super(itemView);

            PersonColourImageView = itemView.findViewById(R.id.PersonColourImageView);
            GroupNameTextView = itemView.findViewById(R.id.GroupNameTextView);
            GroupOwnerTextView = itemView.findViewById(R.id.GroupOwnerTextView);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                /**
                 *  Method that allows onclick interactions with the specific group/user item that the user touches.
                 *  It analyses the current position that the user clicked and calls the onGroupClick() method to pass the position to the interface.
                 *  It also checks if the item was deleted, but the user clicked it before the UI removed it.
                 */
                @Override
                public void onClick(View v)
                {
                    int position = getAdapterPosition();
                    if(groupListener != null && position != RecyclerView.NO_POSITION)
                    {
                        groupListener.onGroupClick(GroupList.get(position));
                    }
                }
            });

        }
    }

    /**
     * Constructor used for the GroupAdapter class.
     * Also notify the user that the data set has changed.
     * @param groupList the ArrayList passed from the GroupsActivity class containing all the groups/users retrieved.
     */
    public GroupAdapter(ArrayList<GroupItem> groupList)
    {
        this.GroupList = groupList;
        notifyDataSetChanged();
    }

    /**
     * Method that returns the current item.
     * @return the current item, representing the current group/user that was selected.
     */
    public GroupItem getGroupItem(int position)
    {
        return GroupList.get(position);
    }

    /**
     * Method called when the RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item.
     * This new ViewHolder is constructed with a new View that can represent the items of a given type.
     * In this case we inflate it from the groups_item XML layout file. This file is a CardView object populated with several XML objects.
     * The new ViewHolder is used to display the items of the adapter using onBindViewHolder(ViewHolder, int, List).
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return The new ViewHolder that holds a View of the given view type.
     */
    @Override
    public GroupAdapter.GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View group = LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_item, parent, false);
        GroupAdapter.GroupViewHolder groupViewHolder = new GroupAdapter.GroupViewHolder(group);

        return groupViewHolder;
    }

    /**
     * Method called by RecyclerView to display the data at a specified position.
     * This method updates the contents of the RecyclerView.ViewHolder.itemView to reflect the item at the given position.
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(GroupAdapter.GroupViewHolder holder, int position)
    {
        GroupItem currentItem = GroupList.get(position);
        holder.PersonColourImageView.setImageResource(currentItem.getColour());
        holder.GroupOwnerTextView.setText(currentItem.getOwnerEmail());
        holder.GroupNameTextView.setText(currentItem.getName());
    }

    /**
     * Method that returns the total number of items in the data set held by the adapter.
     * @return the size of the GroupList ArrayList.
     */
    @Override
    public int getItemCount()
    {
        return GroupList.size();
    }
}
