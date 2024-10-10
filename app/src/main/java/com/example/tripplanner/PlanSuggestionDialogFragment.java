package com.example.tripplanner;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.example.tripplanner.R;

public class PlanSuggestionDialogFragment extends DialogFragment {

    private static final String ARG_MESSAGE = "message";
    private static final String ARG_PLAN = "plan";

    public static PlanSuggestionDialogFragment newInstance(String message, String plan) {
        PlanSuggestionDialogFragment fragment = new PlanSuggestionDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_PLAN, plan);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String message = getArguments().getString(ARG_MESSAGE);
        String plan = getArguments().getString(ARG_PLAN);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_plan_suggestion, null);
        TextView messageTextView = view.findViewById(R.id.messageTextView);
        TextView planTextView = view.findViewById(R.id.planTextView);

        messageTextView.setText(message);
        planTextView.setText(plan);

        return new AlertDialog.Builder(requireActivity())
                .setView(view)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle confirmation
                    }
                })
                .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle decline
                    }
                })
                .create();
    }
}