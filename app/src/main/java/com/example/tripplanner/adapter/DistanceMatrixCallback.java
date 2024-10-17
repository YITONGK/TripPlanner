package com.example.tripplanner.adapter;

import com.example.tripplanner.entity.DistanceMatrixEntry;

import java.util.List;

public interface DistanceMatrixCallback {
    void onSuccess(List<DistanceMatrixEntry> distanceMatrix);
    void onFailure(Exception e);
}