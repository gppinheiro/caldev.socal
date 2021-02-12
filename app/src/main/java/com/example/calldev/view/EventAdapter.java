package com.example.calldev.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.calldev.item.EventItem;
import com.example.calldev.R;

import java.util.ArrayList;

/**
 * Date: Nov 20-2020.
 * This a class that extends adapter (more specifically the recyclerview adapter class).
 * This adapter provides a binding from an app-specific data set to views that are displayed within a RecyclerView (in this case the RecyclerView used for the Events).
 * @author CALDEV.
 */

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder>
{
    /**
     * String constant used to distinguish this class (used in debugging).
     */
    public static final String TAG = "EventAdapter";

    /**
     * ArrayList object containing events in the form of EventItem objects.
     */
    private final ArrayList<EventItem> EventList;
    /**
     * Listener object that is handled from the EventsActivity class.
     */
    private onEventClickListener eventListener;

    /**
     * Date: Nov 20-2020.
     * Interface class used to create the onclick listener method.
     * It passes the current position that the user clicked to the EventsActivity class.
     * @author CALDEV.
     */

    public interface onEventClickListener
    {
        void onEventClick(EventItem eventItem);
    }

    /**
     * Method that is used in the EventsActivity class to connect the EventAdapter with the interface defined earlier and pass the listener variable.
     * @param eventListener the listener variable that is handled from the EventsActivity class.
     */
    public void setOnEventClickListener(onEventClickListener eventListener)
    {
        this.eventListener = eventListener;
    }

    /**
     * Date: Nov 20-2020.
     * This subclass extends RecyclerView.ViewHolder.
     * A ViewHolder describes an item view and metadata regarding its place within the RecyclerView.
     * The RecyclerView.Adapter implements this subclass ViewHolder adding fields for caching potentially expensive View.findViewById(int) results.
     * @author CALDEV.
     */

    public class EventViewHolder extends RecyclerView.ViewHolder
    {
        /**
         * ImageView XML object used to display the event's colour based on its type.
         */
        private final ImageView EventColourImageView;
        /**
         * TextView XML object used to display the event's type.
         */
        private final TextView EventTypeTextView;
        /**
         * TextView XML object used to display the event's name.
         */
        private final TextView EventNameTextView;
        /**
         * TextView XML object used to display the event's start time.
         */
        private final TextView EventStartTimeTextView;
        /**
         * TextView XML object used to display the event's start date.
         */
        private final TextView EventStartDateTextView;
        /**
         * TextView XML object used to display the event's end time.
         */
        private final TextView EventEndTimeTextView;
        /**
         * TextView XML object used to display the event's end date.
         */
        private final TextView EventEndDateTextView;

        /**
         * Method that links the XML objects from the XML layout file chosen.
         * It also sets up an onclick listener to the entire object.
         * @param itemView the ViewHolder created in the onCreateViewHolder() method.
         */
        public EventViewHolder(View itemView)
        {
            super(itemView);

            EventColourImageView = itemView.findViewById(R.id.EventColourImageView);
            EventTypeTextView = itemView.findViewById(R.id.EventTypeTextView);
            EventNameTextView = itemView.findViewById(R.id.EventNameTextView);
            EventStartTimeTextView = itemView.findViewById(R.id.EventStartTimeTextView);
            EventEndTimeTextView = itemView.findViewById(R.id.EventEndTimeTextView);
            EventStartDateTextView = itemView.findViewById(R.id.EventStartDateTextView);
            EventEndDateTextView = itemView.findViewById(R.id.EventEndDateTextView);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                /**
                 *  Method that allows onclick interactions with the specific event item that the user touches.
                 *  It analyses the current position that the user clicked and calls the onEventClick() method to pass the position to the interface.
                 *  It also checks if the item was deleted, but the user clicked it before the UI removed it.
                 */
                @Override
                public void onClick(View v)
                {
                    int position = getAdapterPosition();
                    if(eventListener != null && position != RecyclerView.NO_POSITION)
                    {
                        eventListener.onEventClick(EventList.get(position));
                    }
                }
            });
        }
    }

    /**
     * Constructor used for the EventAdapter class.
     * Also notify the user that the data set has changed.
     * @param eventList the ArrayList passed from the EventActivity class containing all the events retrieved.
     */
    public EventAdapter(ArrayList<EventItem> eventList)
    {
        this.EventList = eventList;
        notifyDataSetChanged();
    }

    /**
     * Method that returns the current item.
     * @return the current item, representing the current event that was selected.
     */
    public EventItem getEventItem(int position)
    {
        return EventList.get(position);
    }

    /**
     * Method called when the RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item.
     * This new ViewHolder is constructed with a new View that can represent the items of a given type.
     * In this case we inflate it from the event_item XML layout file. This file is a CardView object populated with several XML objects.
     * The new ViewHolder is used to display the items of the adapter using onBindViewHolder(ViewHolder, int, List).
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return The new ViewHolder that holds a View of the given view type.
     */
    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View event = LayoutInflater.from(parent.getContext()).inflate(R.layout.events_item, parent, false);
        EventViewHolder eventViewHolder = new EventViewHolder(event);

        return eventViewHolder;
    }

    /**
     * Method called by RecyclerView to display the data at a specified position.
     * This method updates the contents of the RecyclerView.ViewHolder.itemView to reflect the item at the given position.
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(EventViewHolder holder, int position)
    {
        EventItem currentItem = EventList.get(position);
        holder.EventColourImageView.setImageResource(currentItem.getColour());
        holder.EventTypeTextView.setText(currentItem.getType());
        holder.EventNameTextView.setText(currentItem.getName());
        holder.EventStartTimeTextView.setText(currentItem.getStartTime());
        holder.EventEndTimeTextView.setText(currentItem.getEndTime());
        holder.EventStartDateTextView.setText(currentItem.getStartDate());
        holder.EventEndDateTextView.setText(currentItem.getEndDate());
    }

    /**
     * Method that returns the total number of items in the data set held by the adapter.
     * @return the size of the EventList ArrayList.
    */
     @Override
    public int getItemCount()
    {
        return EventList.size();
    }
}
