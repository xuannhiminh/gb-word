package com.google.android.gms.ads.ez.nativead;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.ez.EzApplication;
import com.google.android.gms.ads.ez.IAPUtils;
import com.google.android.gms.ads.ez.LogUtils;
import com.google.android.gms.ads.ez.R;
import com.google.android.gms.ads.ez.Utils;
import com.google.android.gms.ads.ez.adparam.AdUnit;
import com.google.android.gms.ads.ez.listenner.NativeAdListener;
import com.google.android.gms.ads.ez.observer.MyObserver;
import com.google.android.gms.ads.ez.observer.MySubject;
import com.google.android.gms.ads.ez.remote.AppConfigs;
import com.google.android.gms.ads.ez.remote.RemoteKey;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

public class AdmobNativeAdView extends RelativeLayout {
    protected Context mContext;
    protected NativeAdListener nativeAdListener;
    private NativeAd nativeAd;
    private NativeAdView adView;
    private String TAG = "AdmobNativeAdView";
    private long lastTimeLoadAds = 0;
    private OnTouchListener onTouch;

    private ShimmerFrameLayout container;

    private View shimmerView;
    private View rootView;
    private MyObserver mObserver = new MyObserver() {
        @Override
        public void update(String message) {
            if (message.equals(IAPUtils.KEY_PURCHASE_SUCCESS)) {
                LogUtils.logString(AdmobNativeAdView.class, "AdmobNativeAdView user purchase observer -> remove ads");
                setVisibility(GONE);
                if (nativeAdListener != null) {
                    nativeAdListener.onPurchased(AdmobNativeAdView.this);
                }
            }
        }
    };

    public AdmobNativeAdView(Context context, int id) {
        super(context);

        MySubject.getInstance().attach(mObserver);
        if (IAPUtils.getInstance().isPremium()) {
            LogUtils.logString(AdmobNativeAdView.class, "AdmobNativeAdView user purchase init -> remove ads");
            return;
        }


        mContext = context;
        View view = LayoutInflater.from(mContext).inflate(id, this);

        loadAd();
    }

    public AdmobNativeAdView(Context context) {
        super(context);

        MySubject.getInstance().attach(mObserver);
        if (IAPUtils.getInstance().isPremium()) {
            LogUtils.logString(AdmobNativeAdView.class, "AdmobNativeAdView user purchase init -> remove ads");
            return;
        }


        mContext = context;
        View view = LayoutInflater.from(mContext).inflate(R.layout.native_admod_home, this);

        loadAd();

    }

    public AdmobNativeAdView(Context context, AttributeSet attrs) {
        super(context, attrs);

        MySubject.getInstance().attach(mObserver);
        if (IAPUtils.getInstance().isPremium()) {
            LogUtils.logString(AdmobNativeAdView.class, "AdmobNativeAdView user purchase init -> remove ads");
            return;
        }


        mContext = context;
        View view = LayoutInflater.from(mContext).inflate(R.layout.native_admod_home, this);

        loadAd();
    }

    public static AdmobNativeAdView getNativeAd(Context context, int idLayout, NativeAdListener nativeAdListener) {
        AdmobNativeAdView nativeAdView = new AdmobNativeAdView(context, idLayout);
        nativeAdView.setAdListener(nativeAdListener);
        return nativeAdView;
    }


    public void setAdListener(NativeAdListener nativeAdListener) {
        this.nativeAdListener = nativeAdListener;
    }


    public void loadAd() {

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);

        if (AppConfigs.getInt(RemoteKey.APP_UPDATE_VERSION) == Utils.getVersionCode(EzApplication.getInstance().getCurrentActivity())) {
            if (nativeAdListener != null) {
                nativeAdListener.onError();
            }
            setVisibility(GONE);
            return;
        }
        if (System.currentTimeMillis() - lastTimeLoadAds < 300000) {
            return;
        }

        container = findViewById(R.id.shimmer_view_container);
        rootView = findViewById(R.id.root_view);
        shimmerView = findViewById(R.id.shimmerView);

        if (container != null) {
            shimmerView.setVisibility(VISIBLE);
            rootView.setVisibility(GONE);
            container.startShimmer();
        }


        lastTimeLoadAds = System.currentTimeMillis();
        LogUtils.logString(AdmobNativeAdView.class, "Ad Unit " + AdUnit.getAdmobNativeId());
        nativeAd = null;
        AdLoader.Builder builder = new AdLoader.Builder(mContext, AdUnit.getAdmobNativeId())
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(NativeAd nati) {
                        LogUtils.logString(AdmobNativeAdView.class, "Ad Loaded");
                        nativeAd = nati;
                        setAdToLayout();
                        if (nativeAdListener != null) {
                            nativeAdListener.onLoaded(AdmobNativeAdView.this);
                        }
                    }
                });


        builder.withNativeAdOptions(new NativeAdOptions.Builder().setVideoOptions(new VideoOptions.Builder().setStartMuted(true).build()).build());
        builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                LogUtils.logString(AdmobNativeAdView.class, "Ad Load Fail");
                loadAdx();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                if (nativeAdListener != null) {
                    nativeAdListener.onClickAd();
                }
            }
        }).build().loadAd(new AdRequest.Builder().build());
    }

    public void loadAdx() {
        Log.e(TAG, "loadAdx2: ");
        nativeAd = null;
        AdLoader.Builder builder = new AdLoader.Builder(mContext, AdUnit.getAdxNativeId())
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(NativeAd nati) {
                        Log.e(TAG, "onNativeAdLoaded2: ");
                        nativeAd = nati;
                        setAdToLayout();
                        if (nativeAdListener != null) {
                            nativeAdListener.onLoaded(AdmobNativeAdView.this);
                        }
                    }
                });


        builder.withNativeAdOptions(new NativeAdOptions.Builder().setVideoOptions(new VideoOptions.Builder().setStartMuted(true).build()).build());
        builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                Log.e(TAG, "onAdFailedToLoad2: ");
                if (nativeAdListener != null) {
                    nativeAdListener.onError();
                }
            }


            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                if (nativeAdListener != null) {
                    nativeAdListener.onClickAd();
                }
            }
        }).build().loadAd(new AdRequest.Builder().build());
    }

    public void setAdToLayout() {

        adView = (NativeAdView) findViewById(R.id.ad_view);
        if (container != null) {
            shimmerView.setVisibility(GONE);
            rootView.setVisibility(VISIBLE);
            container.stopShimmer();
            container.hideShimmer();
        }
        int[] location = new int[2];
        adView.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        LogUtils.logString("XXX " + x + "   " + y);
        LogUtils.logString("XXX " + getLayoutParams().width + "   " + getLayoutParams().height);

        MediaView mediaView = adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.GONE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (adView.getStoreView() != null) {
            if (nativeAd.getStore() == null) {
                adView.getStoreView().setVisibility(View.GONE);
            } else {
                adView.getStoreView().setVisibility(View.VISIBLE);
                ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
            }
        }


        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.GONE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }


        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.GONE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }
        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.

//

//        if (adView.getMediaView().getVisibility() == VISIBLE) {
//            DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
//            LayoutParams layoutParams = new LayoutParams(
//                    LayoutParams.MATCH_PARENT,
//                    (metrics.widthPixels * 2));
//            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
//            adView.setLayoutParams(layoutParams);
//        }


        adView.setNativeAd(nativeAd);


    }


    public void setOnTouch(OnTouchListener onTouchListener) {
        this.onTouch = onTouchListener;
        if (onTouch != null && adView != null) {
            adView.setOnTouchListener(onTouch);
        }
    }

    public void addViewToLayout(ViewGroup viewGroup) {
        if (getParent() != null) {
            ((ViewGroup) getParent()).removeView(this);
        }
        viewGroup.addView(this);
    }

    public boolean isLoaded() {
        return true;
    }
}
