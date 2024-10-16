package com.example.tripplanner.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.tripplanner.entity.ActivityItem;
import com.example.tripplanner.R;

import java.util.ArrayList;
import java.util.List;

public class ActivityItemAdapter extends RecyclerView.Adapter<ActivityItemAdapter.ViewHolder> {
    private Context context;
    private ArrayList<ActivityItem> activityItems;
    private OnItemClickListener onItemClickListener;
    private OnStartDragListener onStartDragListener;

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public void setOnStartDragListener(OnStartDragListener listener) {
        this.onStartDragListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public ActivityItemAdapter(Context context, ArrayList<ActivityItem> activityItems) {
        this.context = context;
        this.activityItems = activityItems;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ActivityItem item = activityItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return activityItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // 定义视图元素，例如 TextView
        public TextView activityName;
        public TextView activityDetails;
        public ImageView dragHandle;

        public ViewHolder(View itemView) {
            super(itemView);
            activityName = itemView.findViewById(R.id.activityName);
            activityDetails = itemView.findViewById(R.id.activityDetails);
            dragHandle = itemView.findViewById(R.id.dragHandle);

            dragHandle.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN && onStartDragListener != null) {
                    onStartDragListener.onStartDrag(this);
                }
                return false;
            });

            // 设置点击事件
            itemView.setOnClickListener(view -> {
                if (onItemClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemClick(position);
                    }
                }
            });
        }

        public void bind(ActivityItem item) {
            activityName.setText(item.getName());
            // 设置其他视图元素
            StringBuilder details = new StringBuilder();
            if (item.getStartTime() != null) {
                details.append("Start Time: ").append(item.getStartTimeString()).append("\n");
            }
            if (item.getEndTime() != null) {
                details.append("End Time: ").append(item.getEndTimeString()).append("\n");
            }
            if (item.getLocation() != null) {
                details.append("Location: ").append(item.getLocation().getName()).append("\n");
            }
            if (item.getNotes() != null && !item.getNotes().isEmpty()) {
                details.append("Note: ").append(item.getNotes());
            }

            activityDetails.setText(details.toString());
        }
    }
}

