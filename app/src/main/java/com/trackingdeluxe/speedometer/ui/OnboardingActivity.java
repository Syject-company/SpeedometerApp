package com.trackingdeluxe.speedometer.ui;


import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.trackingdeluxe.speedometer.R;
import com.trackingdeluxe.speedometer.fragment.onboarding.HintOnboarding;
import com.trackingdeluxe.speedometer.fragment.onboarding.HistoryOnboarding;
import com.trackingdeluxe.speedometer.fragment.onboarding.LandingpageOnboarding;
import com.trackingdeluxe.speedometer.fragment.onboarding.WelcomeOnboarding;

import org.jetbrains.annotations.NotNull;


public class OnboardingActivity extends AppCompatActivity implements WelcomeOnboarding.OnButtonClickListener,
        HintOnboarding.OnButtonClickListener, HistoryOnboarding.OnButtonClickListener, LandingpageOnboarding.OnButtonClickListener {
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 4;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else if (mPager.getCurrentItem() == 3) {
            startUpbike();
        }
        else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    private void startUpbike() {
        startActivity(new Intent(OnboardingActivity.this, ActivitySplash.class));
        this.finish();
    }

    @Override
    public void pageSwitch(int pageNumber) {
        if(pageNumber == -1) {
            startUpbike();
        } else {
            mPager.setCurrentItem(pageNumber);
        }

    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return new WelcomeOnboarding();

                case 1:
                    return new HistoryOnboarding();

                case 2:
                    return new HintOnboarding();

                case 3:
                    return new LandingpageOnboarding();

            }

            return null;

        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }










}

