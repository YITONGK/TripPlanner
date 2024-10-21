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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripplanner.entity.ActivityItem;
import com.example.tripplanner.R;
import com.example.tripplanner.entity.PlanItem;
import com.example.tripplanner.entity.RouteInfo;

import java.util.ArrayList;
import java.util.List;

public class ActivityItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<PlanItem> planItems;
    private OnItemClickListener onItemClickListener;
    private OnStartDragListener onStartDragListener;

    private static final int VIEW_TYPE_ACTIVITY = 0;
    private static final int VIEW_TYPE_ROUTE_INFO = 1;

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public void setOnStartDragListener(OnStartDragListener listener) {
        this.onStartDragListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, PlanItem planItem);
    }

    public ActivityItemAdapter(Context context, List<PlanItem> planItems) {
        this.context = context;
        this.planItems = planItems;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ACTIVITY) {
            View view = LayoutInflater.from(context).inflate(R.layout.activity_item, parent, false);
            return new ActivityViewHolder(view);
        } else if (viewType == VIEW_TYPE_ROUTE_INFO) {
            View view = LayoutInflater.from(context).inflate(R.layout.route_info_item, parent, false);
            return new RouteInfoViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PlanItem item = planItems.get(position);
        if (holder instanceof ActivityViewHolder) {
            ((ActivityViewHolder) holder).bind(item.getActivityItem());
        } else if (holder instanceof RouteInfoViewHolder) {
            ((RouteInfoViewHolder) holder).bind(item.getRouteInfo());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return planItems.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return planItems.size();
    }

    public class ActivityViewHolder extends RecyclerView.ViewHolder {
        public TextView activityName;
        public TextView activityDetails;
        public ImageView dragHandle;

        public ActivityViewHolder(View itemView) {
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

            itemView.setOnClickListener(view -> {
                if (onItemClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemClick(position, planItems.get(position));
                    }
                }
            });
        }

        public void bind(ActivityItem item) {
            activityName.setText(item.getName());
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

    public class RouteInfoViewHolder extends RecyclerView.ViewHolder {
        public TextView routeInfoText;

        public RouteInfoViewHolder(View itemView) {
            super(itemView);
            routeInfoText = itemView.findViewById(R.id.routeInfoText);
        }

        public void bind(RouteInfo routeInfo) {
            if (routeInfo != null) {
                routeInfoText.setText("Duration: " + routeInfo.getDuration() + ", Distance: " + routeInfo.getDistance());
            } else {
                routeInfoText.setText("Calculating route...");
            }
        }
    }
}

