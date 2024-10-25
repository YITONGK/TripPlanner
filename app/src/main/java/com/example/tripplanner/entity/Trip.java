package com.example.tripplanner.entity;

import com.example.tripplanner.R;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Trip {
    private String id;
    private String name;
    private Timestamp startDate;
    private Timestamp endDate;
    private int numDays;
    private List<Location> locations;
    private String note;
    private Map<String, List<ActivityItem>> plans;
    private List<String> userIds;
    private String trafficMode;

    // No-argument constructor required for Firestore deserialization
    public Trip() {
        // Initialize fields with default values if necessary
        this.locations = new ArrayList<>();
        this.plans = new HashMap<>();
        this.userIds = new ArrayList<>();
    }

    public Trip(String name, Timestamp startDate, int numDays, List<Location> locations, String userId) {
        this.name = name;
        this.startDate = startDate;
        this.numDays = numDays;
        this.endDate = new Timestamp(startDate.getSeconds() + TimeUnit.DAYS.toSeconds(numDays), 0);
        this.locations = locations;
        this.note = "";
        this.plans = new HashMap<>();
        for (int i = 0; i < numDays; i++) {
            plans.put(String.valueOf(i), new ArrayList<>());
        }
        this.userIds = new ArrayList<>();
        this.userIds.add(userId);
        this.trafficMode = "Driving";
    }

    public Trip(String name, Timestamp startDate, Timestamp endDate, List<Location> locations, String userId) {
        this.name = name;
        this.endDate = endDate;
        this.startDate = startDate;
        this.locations = locations;
        this.note = "";
        this.numDays = (int) TimeUnit.SECONDS.toDays(endDate.getSeconds() - startDate.getSeconds());
        this.plans = new HashMap<>();
        for (int i = 0; i < numDays; i++) {
            plans.put(String.valueOf(i), new ArrayList<>());
        }
        this.userIds = new ArrayList<>();
        this.userIds.add(userId);
    }

    // Convert Trip object to Map for Firestore
    public Map<String, Object> convertTripToMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("numDays", numDays);
        map.put("locations", locations.stream()
                                          .map(Location::convertLocationToMap)
                                          .collect(Collectors.toList()));
        map.put("note", note);
        map.put("userIds", userIds);
        Map<String, List<Map<String, Object>>> plansMap = new HashMap<>();
        for (Map.Entry<String, List<ActivityItem>> entry : plans.entrySet()) {
            List<Map<String, Object>> activityMaps = new ArrayList<>();
            for (ActivityItem activity : entry.getValue()) {
                activityMaps.add(activity.toMap());
            }
            plansMap.put(entry.getKey(), activityMaps);
        }
        map.put("plans", plansMap);
        map.put("trafficMode", trafficMode);
        return map;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public Map<String, List<ActivityItem>> getPlans() {
        if (plans == null) {
            plans = new HashMap<>();
        }
        return plans;
    }

    public String getNote() {
        return note;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setNote(String note) {
        this.note = note;
    }

    // Method to add a location to the trip
    public void addLocation(Location location) {
        this.locations.add(location);
    }

     public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public void setNumDays(int numDays) {
        this.numDays = numDays;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTrafficMode() {
        return trafficMode;
    }

    public void setTrafficMode(String trafficMode) {
        this.trafficMode = trafficMode;
    }

    public int getLastingDays() {
        long daysBetween = TimeUnit.SECONDS.toDays(endDate.getSeconds() - startDate.getSeconds());
        return (int) daysBetween;
    }

    public void setPlans(Map<String, List<ActivityItem>> plans) {
        this.plans = plans;
    }

    public int getNumDays() {
        return numDays;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", numDays=" + numDays +
                ", locations=" + locations +
                ", note='" + note + '\'' +
                ", plans=" + plans +
                ", traffic mode=" + trafficMode +
                ", userIds=" + userIds +
                '}';
    }

    // Method to get the drawable resource ID based on the first location's city name
    public int getCityDrawable() {
        if (locations != null && !locations.isEmpty()) {
            String cityName = locations.get(0).getName(); // Assuming Location class has a getName() method
            switch (cityName.toLowerCase()) {
                case "melbourne":
                    return R.drawable.mel; // Replace with your actual drawable resource
                case "sydney":
                    return R.drawable.sydney; // Replace with your actual drawable resource
                case "singapore":
                    return R.drawable.singapore; // Replace with your actual drawable resource
                case "tokyo":
                    return R.drawable.tokyo;
                case "newyork":
                    return R.drawable.newyork;
                case "shanghai":
                    return R.drawable.shanghai;
                case "paris":
                    return R.drawable.paris;
                case "florence":
                    return R.drawable.florence;
                default:
                    return R.drawable.default_image; // Fallback image if city not found
            }
        }
        return R.drawable.default_image; // Fallback image if no locations
    }

    public String getLocationsString() {
        List<Location> locationList = getLocations();
        StringBuilder sb = new StringBuilder();
        for (Location location : locationList) {
            sb.append(location.getName()).append(", ");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }

    public String getDurationString() {
        int days = getNumDays();
        if (days == 1) {
            return "1 day";
        } else if (days == 2) {
            return "2 days and 1 night";
        } else {
            return days + " days and " + (days - 1) + " nights";
        }
    }

    public String getActivityCountString() {
        int count = 0;
        for (List<ActivityItem> plan : getPlans().values()) {
            count += plan.size();
        }
        return count + (count > 1 ? " activities" : " activity");
    }

    public void addUser(String userId){
        if (this.userIds == null){
            this.userIds = new ArrayList<>();
        }
        this.userIds.add(userId);
    }
}

