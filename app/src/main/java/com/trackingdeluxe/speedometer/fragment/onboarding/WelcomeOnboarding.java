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


public class WelcomeOnboarding extends Fragment {

    private OnButtonClickListener mOnButtonClickListener;

    public interface OnButtonClickListener{
        void pageSwitch(int pageNumber);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Context context = getContext();
        try {
            mOnButtonClickListener = (OnButtonClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement OnButtonClickListener");
        }

        View rootView = (ViewGroup) inflater.inflate(
                R.layout.onboarding1_welcome, container, false);

        Button next = rootView.findViewById(R.id.btn_ob_1);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnButtonClickListener.pageSwitch(1);
            }
        });

        return rootView;
    }
}