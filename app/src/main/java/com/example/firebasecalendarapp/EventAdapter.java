package com.example.firebasecalendarapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_EVENT = 1;
    private static final int VIEW_TYPE_NO_EVENT = 0;

    private List<EventModel> eventList;
    private EventClickListener eventClickListener;

    public EventAdapter(List<EventModel> eventList, EventClickListener eventClickListener) {
        this.eventList = eventList;
        this.eventClickListener = eventClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_NO_EVENT) {
            View view = inflater.inflate(R.layout.item_no_event, parent, false);
            return new NullEventViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_event, parent, false);
            return new EventViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_EVENT) {
            EventModel event = eventList.get(position);
            ((EventViewHolder) holder).textEventName.setText(event.getEventName());

            ((EventViewHolder) holder).btnEdit.setOnClickListener(v -> eventClickListener.onEditClick(position));
            ((EventViewHolder) holder).btnDelete.setOnClickListener(v -> eventClickListener.onDeleteClick(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return eventList.get(position) == null ? VIEW_TYPE_NO_EVENT : VIEW_TYPE_EVENT;
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView textEventName;
        ImageButton btnEdit;
        ImageButton btnDelete;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            textEventName = itemView.findViewById(R.id.textEventName);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    public static class NullEventViewHolder extends RecyclerView.ViewHolder {
        public NullEventViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public interface EventClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }
}