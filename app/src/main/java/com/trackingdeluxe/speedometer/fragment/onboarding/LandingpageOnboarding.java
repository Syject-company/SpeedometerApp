package com.trackingdeluxe.speedometer.fragment.onboarding;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.trackingdeluxe.speedometer.R;
import com.trackingdeluxe.speedometer.utils.AppConstants;

import java.util.ArrayList;
import java.util.List;

public class LandingpageOnboarding extends Fragment implements PurchasesUpdatedListener {

    private WelcomeOnboarding.OnButtonClickListener mOnButtonClickListener;
    private BillingClient billingClient;
    private View rootView;
    private SkuDetails sku_annualy, sku_monthly;
    private SharedPreferences sp;
    Context mContext;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> list) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && list != null) {
            for (Purchase purchase : list) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    // Grant entitlement to the user by leaving screen
                    sp.edit().putBoolean("concierge", true).apply();
                    mOnButtonClickListener.pageSwitch(-1);
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "purchased pro version");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Onboard purchase");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Button");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                    // Acknowledge the purchase if it hasn't already been acknowledged.
                    if (!purchase.isAcknowledged()) {
                        AcknowledgePurchaseParams acknowledgePurchaseParams =
                                AcknowledgePurchaseParams.newBuilder()
                                        .setPurchaseToken(purchase.getPurchaseToken())
                                        .build();
                        billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                            @Override
                            public void onAcknowledgePurchaseResponse(BillingResult billingResult) {

                            }
                        });
                    }
                }
            }
        } else if (getActivity()!=null && billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.user_canceled, Snackbar.LENGTH_LONG).setAction(R.string.start_trial, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buyAnnualy();
                }
            }).show();

        } else if(getActivity()!=null){
            Snackbar.make(getActivity().findViewById(android.R.id.content), "", Snackbar.LENGTH_LONG).setAction(R.string.try_again, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buyAnnualy();
                }
            }).show();
        }
    }

    private void buyAnnualy() {
        if(sku_annualy != null) {
            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(sku_annualy)
                    .build();
            billingClient.launchBillingFlow(getActivity(), flowParams);
        }
    }

    private void buyMonthly() {
        if(sku_monthly != null) {
            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(sku_monthly)
                    .build();
            billingClient.launchBillingFlow(getActivity(), flowParams);
        }
    }


    public interface OnButtonClickListener{
        void pageSwitch(int pageNumber);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Context context = getContext();

        if (context!=null) {
            sp = context.getSharedPreferences("my_prefs", Activity.MODE_PRIVATE);
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        }

        try {
            mOnButtonClickListener = (WelcomeOnboarding.OnButtonClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement OnButtonClickListener");
        }

        rootView = (ViewGroup) inflater.inflate(
                R.layout.onboarding4_landingpage, container, false);



        final Button close = rootView.findViewById(R.id.close_landingpage);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnButtonClickListener.pageSwitch(-1);
            }
        });

        new CountDownTimer(4000,1000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                close.setVisibility(View.VISIBLE);
            }
        }.start();

        final Button startTrial = rootView.findViewById(R.id.btn_start_trial);
        startTrial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyAnnualy();
            }
        });


        final Button startTrial2 = rootView.findViewById(R.id.btn_start_alt_trial);
        if(getActivity()!=null) {
            String trial_text3 = getActivity().getResources().getString(R.string.alternative_subscription) + " -" + getActivity().getResources().getString(R.string.alternative_subscription2);
            startTrial2.setText(trial_text3);
        }
        startTrial2.setPaintFlags(startTrial2.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        startTrial2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyMonthly();

            }
        });

        final TextView trialDesc = rootView.findViewById(R.id.trial_description);
        if(getActivity()!=null) {
            String trial_text = getActivity().getResources().getString(R.string.trial_desc_1) + " - " + getActivity().getResources().getString(R.string.trial_desc_2);
            trialDesc.setText(trial_text);
        }

        if(context==null) {
            return rootView;
        }
        billingClient = BillingClient.newBuilder(context).enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS);
                    List<Purchase> result = purchasesResult.getPurchasesList();
                    boolean isPro = false;
                    if(result!=null) {
                        for (Purchase purchase : result) {
                            String sku = purchase.getSku();
                            if (AppConstants.ABO_YEARLY.equals(sku) || AppConstants.ABO_MONTHLY.equals(sku)) {
                                isPro = true;
                            }
                        }
                    }
                    sp.edit().putBoolean("concierge", isPro).apply();

                    List<String> skuList = new ArrayList<>();
                    skuList.add(AppConstants.ABO_YEARLY);
                    skuList.add(AppConstants.ABO_MONTHLY);
                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
                    billingClient.querySkuDetailsAsync(params.build(),
                            new SkuDetailsResponseListener() {
                                @Override
                                public void onSkuDetailsResponse(BillingResult billingResult,
                                                                 List<SkuDetails> skuDetailsList) {
                                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                                        for (SkuDetails skuDetails : skuDetailsList) {
                                            String sku = skuDetails.getSku();
                                            String price = skuDetails.getPrice();
                                            if (AppConstants.ABO_YEARLY.equals(sku)) {
                                                sku_annualy = skuDetails;
                                                if(getActivity()!=null) {
                                                    String trial_text = getActivity().getResources().getString(R.string.trial_desc_1) + " " + price + getActivity().getResources().getString(R.string.trial_desc_2);
                                                    trialDesc.setText(trial_text);
                                                }
                                            } else if (AppConstants.ABO_MONTHLY.equals(sku)) {
                                                sku_monthly = skuDetails;
                                                if(getActivity()!=null) {
                                                    String trial_text2 = getActivity().getResources().getString(R.string.alternative_subscription) + price + getActivity().getResources().getString(R.string.alternative_subscription2);
                                                    startTrial2.setText(trial_text2);
                                                }
                                                startTrial2.setPaintFlags(startTrial2.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                                            }
                                        }
                                    }
                                }
                            });
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                billingClient.startConnection(this);
            }
        });




        return rootView;
    }


}