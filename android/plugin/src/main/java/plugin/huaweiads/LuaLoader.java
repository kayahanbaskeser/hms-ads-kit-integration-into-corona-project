//
//  LuaLoader.java
//  TemplateApp
//
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.

//  Kayahan BASKESER
//  https://github.com/kayahanbaskeser

// This corresponds to the name of the Lua library,
// e.g. [Lua] require "plugin.huaweiads"
package plugin.huaweiads;

import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.ansca.corona.CoronaActivity;
import com.ansca.corona.CoronaEnvironment;
import com.ansca.corona.CoronaLua;
import com.ansca.corona.CoronaRuntime;
import com.ansca.corona.CoronaRuntimeListener;
import com.ansca.corona.CoronaRuntimeTask;
import com.ansca.corona.CoronaRuntimeTaskDispatcher;
import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.BannerAdSize;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.InterstitialAd;
import com.huawei.hms.ads.banner.BannerView;
import com.huawei.hms.ads.reward.RewardAd;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.NamedJavaFunction;

import java.util.HashMap;


/**
 * Implements the Lua interface for a Corona plugin.
 * <p>
 * Only one instance of this class will be created by Corona for the lifetime of the application.
 * This instance will be re-used for every new Corona activity that gets created.
 */
@SuppressWarnings("WeakerAccess")
public class LuaLoader implements JavaFunction, CoronaRuntimeListener {
    /**
     * Lua registry ID to the Lua function to be called when the ad request finishes.
     */
    public AdParam adParam;
    private BannerView bannerView;
    private FrameLayout layout;
    public String bannarAdId = "";
    public String interstitialAdId = "";

    private InterstitialAd interstitialAd;
    private int fListener;

    /**
     * This corresponds to the event name, e.g. [Lua] event.name
     */
    private static final String EVENT_NAME = "HMS Ads Kit Integration";

    private AdListener bannerAdListener = new AdListener() {
        @Override
        public void onAdLoaded() {
            dispatchEvent("Ad Loaded", false, "Loaded", "", "Banner");
        }

        @Override
        public void onAdFailed(int errorCode) {
            dispatchEvent(errorCode + "", true, "Loaded", "", "Banner");
        }

        @Override
        public void onAdOpened() {
            dispatchEvent("", false, "Openeed", "", "Banner");
        }

        @Override
        public void onAdClicked() {
            dispatchEvent("", false, "Clicked", "", "Banner");
        }

        @Override
        public void onAdLeave() {
            dispatchEvent("", false, "Leaved", "", "Banner");
        }

        @Override
        public void onAdClosed() {
            dispatchEvent("", false, "Closed", "", "Banner");
        }
    };

    private AdListener interstitialAdListener = new AdListener() {
        @Override
        public void onAdLoaded() {
            super.onAdLoaded();
            dispatchEvent("", false, "onAdLoaded", "", "interstitialAd");
            showInterstitial();
        }

        @Override
        public void onAdFailed(int errorCode) {
            dispatchEvent("" + errorCode, true, "onAdFailed", "", "interstitialAd");
        }

        @Override
        public void onAdClosed() {
            dispatchEvent("", false, "onAdClosed", "", "interstitialAd");
        }

        @Override
        public void onAdClicked() {
            dispatchEvent("", false, "onAdClicked", "", "interstitialAd");
        }

        @Override
        public void onAdLeave() {
            dispatchEvent("", false, "onAdLeave", "", "interstitialAd");
        }

        @Override
        public void onAdOpened() {
            dispatchEvent("", false, "onAdOpened", "", "interstitialAd");
        }

        @Override
        public void onAdImpression() {
            dispatchEvent("", false, "onAdImpression", "", "interstitialAd");
        }
    };


    /**
     * Creates a new Lua interface to this plugin.
     * <p>
     * Note that a new LuaLoader instance will not be created for every CoronaActivity instance.
     * That is, only one instance of this class will be created for the lifetime of the application process.
     * This gives a plugin the option to do operations in the background while the CoronaActivity is destroyed.
     */
    @SuppressWarnings("unused")
    public LuaLoader() {
        // Initialize member variables.
        fListener = CoronaLua.REFNIL;

        // Set up this plugin to listen for Corona runtime events to be received by methods
        // onLoaded(), onStarted(), onSuspended(), onResumed(), and onExiting().
        CoronaEnvironment.addRuntimeListener(this);
    }

    /**
     * Called when this plugin is being loaded via the Lua require() function.
     * <p>
     * Note that this method will be called every time a new CoronaActivity has been launched.
     * This means that you'll need to re-initialize this plugin here.
     * <p>
     * Warning! This method is not called on the main UI thread.
     *
     * @param L Reference to the Lua state that the require() function was called from.
     * @return Returns the number of values that the require() function will return.
     * <p>
     * Expected to return 1, the library that the require() function is loading.
     */
    @Override
    public int invoke(LuaState L) {
        // Register this plugin into Lua with the following functions.
        NamedJavaFunction[] luaFunctions = new NamedJavaFunction[]{
                new InitWrapper(),
                new ShowWrapper(),
                new BannerWrapper(),
                new InterstitialAdWrapper(),
        };
        String libName = L.toString(1);
        L.register(libName, luaFunctions);

        // Returning 1 indicates that the Lua require() function will return the above Lua library.
        return 1;
    }

    /**
     * Called after the Corona runtime has been created and just before executing the "main.lua" file.
     * <p>
     * Warning! This method is not called on the main thread.
     *
     * @param runtime Reference to the CoronaRuntime object that has just been loaded/initialized.
     *                Provides a LuaState object that allows the application to extend the Lua API.
     */
    @Override
    public void onLoaded(CoronaRuntime runtime) {
        // Note that this method will not be called the first time a Corona activity has been launched.
        // This is because this listener cannot be added to the CoronaEnvironment until after
        // this plugin has been required-in by Lua, which occurs after the onLoaded() event.
        // However, this method will be called when a 2nd Corona activity has been created.

    }

    /**
     * Called just after the Corona runtime has executed the "main.lua" file.
     * <p>
     * Warning! This method is not called on the main thread.
     *
     * @param runtime Reference to the CoronaRuntime object that has just been started.
     */
    @Override
    public void onStarted(CoronaRuntime runtime) {
    }

    /**
     * Called just after the Corona runtime has been suspended which pauses all rendering, audio, timers,
     * and other Corona related operations. This can happen when another Android activity (ie: window) has
     * been displayed, when the screen has been powered off, or when the screen lock is shown.
     * <p>
     * Warning! This method is not called on the main thread.
     *
     * @param runtime Reference to the CoronaRuntime object that has just been suspended.
     */
    @Override
    public void onSuspended(CoronaRuntime runtime) {
    }

    /**
     * Called just after the Corona runtime has been resumed after a suspend.
     * <p>
     * Warning! This method is not called on the main thread.
     *
     * @param runtime Reference to the CoronaRuntime object that has just been resumed.
     */
    @Override
    public void onResumed(CoronaRuntime runtime) {
    }

    /**
     * Called just before the Corona runtime terminates.
     * <p>
     * This happens when the Corona activity is being destroyed which happens when the user presses the Back button
     * on the activity, when the native.requestExit() method is called in Lua, or when the activity's finish()
     * method is called. This does not mean that the application is exiting.
     * <p>
     * Warning! This method is not called on the main thread.
     *
     * @param runtime Reference to the CoronaRuntime object that is being terminated.
     */
    @Override
    public void onExiting(CoronaRuntime runtime) {
        // Remove the Lua listener reference.
        CoronaLua.deleteRef(runtime.getLuaState(), fListener);
        fListener = CoronaLua.REFNIL;
    }

    /**
     * Simple example on how to dispatch events to Lua. Note that events are dispatched with
     * Runtime dispatcher. It ensures that Lua is accessed on it's thread to avoid race conditions
     *
     * @param // to sent to Lua in 'message' field.
     */
    @SuppressWarnings("unused")
    public void dispatchEvent(final String data, final Boolean isError, final String phase, final String response, final String type) {
        CoronaEnvironment.getCoronaActivity().getRuntimeTaskDispatcher().send(new CoronaRuntimeTask() {
            @Override
            public void executeUsing(CoronaRuntime runtime) {
                LuaState L = runtime.getLuaState();

                CoronaLua.newEvent(L, EVENT_NAME);

                L.pushBoolean(isError);
                L.setField(-2, "isError");

                L.pushString(phase);
                L.setField(-2, "phase");

                L.pushString(response);
                L.setField(-2, "response");

                L.pushString(type);
                L.setField(-2, "type");

                L.pushString(data);
                L.setField(-2, "message");

                L.pushString("HMS Ads Kit");
                L.setField(-2, "provider");

                try {
                    CoronaLua.dispatchEvent(L, fListener, 0);
                } catch (Exception ignored) {
                }
            }
        });
    }

    /**
     * The following Lua function has been called:  library.init( listener )
     * <p>
     * Warning! This method is not called on the main thread.
     *
     * @param L Reference to the Lua state that the Lua function was called from.
     * @return Returns the number of values to be returned by the library.init() function.
     */
    @SuppressWarnings({"WeakerAccess", "SameReturnValue"})
    public int init(LuaState L) {
        int listenerIndex = 1;

        if (CoronaLua.isListener(L, listenerIndex, EVENT_NAME)) {
            fListener = CoronaLua.newRef(L, listenerIndex);
        }

        HwAds.init(CoronaEnvironment.getApplicationContext());
        adParam = new AdParam.Builder().build();

        return 0;
    }


    @SuppressWarnings({"WeakerAccess"})
    public int showBanner(LuaState L) {
        CoronaActivity activity = CoronaEnvironment.getCoronaActivity();
        if (activity == null) {
            return 0;
        }

        if (L.isTable(1)) {
            L.getField(1, "adId");

            if (L.isString(2)) {
                bannarAdId = L.toString(2);
            }
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CoronaActivity activity = CoronaEnvironment.getCoronaActivity();
                if (activity == null) {
                    return;
                }

                if (layout == null) {
                    layout = new FrameLayout(activity);
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                    activity.getOverlayView().addView(layout, layoutParams);
                }

                if (bannerView == null) {
                    bannerView = new BannerView(CoronaEnvironment.getCoronaActivity());
                    bannerView.setAdId(bannarAdId);
                    bannerView.setBannerAdSize(BannerAdSize.BANNER_SIZE_320_50);
                    bannerView.setAdListener(bannerAdListener);
                    bannerView.loadAd(adParam);
                    layout.addView(bannerView);
                }
            }
        });
        return 0;
    }

    private void showInterstitial() {
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            dispatchEvent("", true, "Ad did not load", "", "InterstitialAd");
        }
    }

    @SuppressWarnings({"WeakerAccess"})
    public int InterstitialAd(LuaState L) {
        if (L.isTable(1)) {
            L.getField(1, "adId");

            if (L.isString(2)) {
                interstitialAdId = L.toString(2);
            }
        }

        interstitialAd = new InterstitialAd(CoronaEnvironment.getApplicationContext());
        interstitialAd.setAdId(interstitialAdId);
        interstitialAd.setAdListener(interstitialAdListener);
//        adParam = new AdParam.Builder().build();
        interstitialAd.loadAd(adParam);
        return 0;
    }

    /**
     * The following Lua function has been called:  library.show( word )
     * <p>
     * Warning! This method is not called on the main thread.
     *
     * @param L Reference to the Lua state that the Lua function was called from.
     * @return Returns the number of values to be returned by the library.show() function.
     */
    @SuppressWarnings("WeakerAccess")
    public int show(LuaState L) {
        return 0;
    }

    /**
     * Implements the library.init() Lua function.
     */
    @SuppressWarnings("unused")
    private class InitWrapper implements NamedJavaFunction {
        /**
         * Gets the name of the Lua function as it would appear in the Lua script.
         *
         * @return Returns the name of the custom Lua function.
         */
        @Override
        public String getName() {
            return "init";
        }

        /**
         * This method is called when the Lua function is called.
         * <p>
         * Warning! This method is not called on the main UI thread.
         *
         * @param L Reference to the Lua state.
         *          Needed to retrieve the Lua function's parameters and to return values back to Lua.
         * @return Returns the number of values to be returned by the Lua function.
         */
        @Override
        public int invoke(LuaState L) {
            return init(L);
        }
    }

    /**
     * Implements the library.show() Lua function.
     */
    @SuppressWarnings("unused")
    private class ShowWrapper implements NamedJavaFunction {
        /**
         * Gets the name of the Lua function as it would appear in the Lua script.
         *
         * @return Returns the name of the custom Lua function.
         */
        @Override
        public String getName() {
            return "show";
        }

        /**
         * This method is called when the Lua function is called.
         * <p>
         * Warning! This method is not called on the main UI thread.
         *
         * @param L Reference to the Lua state.
         *          Needed to retrieve the Lua function's parameters and to return values back to Lua.
         * @return Returns the number of values to be returned by the Lua function.
         */
        @Override
        public int invoke(LuaState L) {
            return show(L);
        }
    }

    @SuppressWarnings("unused")
    private class BannerWrapper implements NamedJavaFunction {

        @Override
        public String getName() {
            return "showBanner";
        }

        @Override
        public int invoke(LuaState L) {
            return showBanner(L);
        }
    }

    @SuppressWarnings("unused")
    private class InterstitialAdWrapper implements NamedJavaFunction {
        /**
         * Gets the name of the Lua function as it would appear in the Lua script.
         *
         * @return Returns the name of the custom Lua function.
         */
        @Override
        public String getName() {
            return "InterstitialAd";
        }

        /**
         * This method is called when the Lua function is called.
         * <p>
         * Warning! This method is not called on the main UI thread.
         *
         * @param L Reference to the Lua state.
         *          Needed to retrieve the Lua function's parameters and to return values back to Lua.
         * @return Returns the number of values to be returned by the Lua function.
         */
        @Override
        public int invoke(LuaState L) {
            return InterstitialAd(L);
        }
    }

}