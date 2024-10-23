package com.example.tripplanner.entity;

public class PlanItem {
    public static final int TYPE_ACTIVITY = 0;
    public static final int TYPE_ROUTE_INFO = 1;

    private int type;
    private ActivityItem activityItem;
    private RouteInfo routeInfo;

    public PlanItem(ActivityItem activityItem) {
        this.type = TYPE_ACTIVITY;
        this.activityItem = activityItem;
    }

    public PlanItem(RouteInfo routeInfo) {
        this.type = TYPE_ROUTE_INFO;
        this.routeInfo = routeInfo;
    }

    public int getType() {
        return type;
    }

    public ActivityItem getActivityItem() {
        return activityItem;
    }

    public RouteInfo getRouteInfo() {
        return routeInfo;
    }
}
