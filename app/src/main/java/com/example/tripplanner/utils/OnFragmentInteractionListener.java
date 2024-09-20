// OnFragmentInteractionListener.java
package com.example.tripplanner.utils;

import org.json.JSONException;

public interface OnFragmentInteractionListener {
    void DaysInteraction(String data) throws JSONException;

    void DatesInteraction(String startDate, String endDate) throws JSONException;
}