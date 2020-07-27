package com.trackingdeluxe.speedometer.fragment.onboarding;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.trackingdeluxe.speedometer.R;

public class HistoryOnboarding extends Fragment {

    private WelcomeOnboarding.OnButtonClickListener mOnButtonClickListener;

    public interface OnButtonClickListener{
        void pageSwitch(int pageNumber);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Context context = getContext();
        try {
            mOnButtonClickListener = (WelcomeOnboarding.OnButtonClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement OnButtonClickListener");
        }

        View rootView = (ViewGroup) inflater.inflate(
                R.layout.onboarding2_history, container, false);

        Button next = rootView.findViewById(R.id.btn_ob_2);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnButtonClickListener.pageSwitch(2);
            }
        });


        return rootView;
    }
}