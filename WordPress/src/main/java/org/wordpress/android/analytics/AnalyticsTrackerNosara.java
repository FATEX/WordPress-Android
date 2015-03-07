package org.wordpress.android.analytics;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.wordpress.android.WordPress;
import org.wordpress.android.util.AppLog;

import java.util.Map;
import java.util.UUID;

public class AnalyticsTrackerNosara implements AnalyticsTracker.Tracker {
    public static final String LOGTAG = "AnalyticsTrackerNosara";

    private static final String DOTCOM_USER = "dotcom_user";
    private static final String JETPACK_USER = "jetpack_user";
    private static final String MIXPANEL_NUMBER_OF_BLOGS = "number_of_blogs";

    private static final String EVENTS_PREFIX = "wpandroid_";

    private String mAnonID = null;
    private String mWpcomUserName = null;

    private NosaraClient mNosaraClient;

    public AnalyticsTrackerNosara(Context ctx) {
        if (null == ctx || !checkBasicConfiguration(ctx)) {
            mNosaraClient = null;
            return;
        }
        mNosaraClient = new NosaraClient(ctx);
    }


    private static boolean checkBasicConfiguration(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        final String packageName = context.getPackageName();

        if (PackageManager.PERMISSION_GRANTED != packageManager.checkPermission("android.permission.INTERNET", packageName)) {
            Log.w(LOGTAG, "Package does not have permission android.permission.INTERNET - Nosara Client will not work at all!");
            Log.i(LOGTAG, "You can fix this by adding the following to your AndroidManifest.xml file:\n" +
                    "<uses-permission android:name=\"android.permission.INTERNET\" />");
            return false;
        }

        if (PackageManager.PERMISSION_GRANTED != packageManager.checkPermission("android.permission.ACCESS_NETWORK_STATE", packageName)) {
            Log.w(LOGTAG, "Package does not have permission android.permission.ACCESS_NETWORK_STATE - Nosara Client will not work at all!");
            Log.i(LOGTAG, "You can fix this by adding the following to your AndroidManifest.xml file:\n" +
                    "<uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\" />");
            return false;
        }

        return true;
    }


    @Override
    public void track(AnalyticsTracker.Stat stat) {
        track(stat, null);
    }

    @Override
    public void track(AnalyticsTracker.Stat stat, Map<String, ?> properties) {
        if (mNosaraClient == null) {
            return;
        }

        String eventName;

        switch (stat) {
            case APPLICATION_STARTED:
                eventName = "application_started";
                break;
            case APPLICATION_OPENED:
                eventName = "application_opened";
                break;
            case APPLICATION_CLOSED:
                eventName = "application_closed";
                break;
            case READER_ACCESSED:
                eventName = "reader_accessed";
                //instructions.setSuperPropertyAndPeoplePropertyToIncrement("number_of_times_accessed_reader");
                //instructions.setCurrentDateForPeopleProperty("last_time_accessed_reader");
                break;
            case READER_OPENED_ARTICLE:
                eventName = "reader_opened_article";
                //instructions.setSuperPropertyAndPeoplePropertyToIncrement("number_of_times_opened_article");
                //instructions.setCurrentDateForPeopleProperty("last_time_opened_reader_article");
                break;
            case READER_LIKED_ARTICLE:
                eventName = "reader_liked_article";
                //instructions.setSuperPropertyAndPeoplePropertyToIncrement("number_of_times_liked_article");
                //instructions.setCurrentDateForPeopleProperty("last_time_liked_reader_article");
                break;
            case READER_INFINITE_SCROLL:
                eventName = "reader_infinite_scroll_performed";
                //instructions.setSuperPropertyAndPeoplePropertyToIncrement(
                //      "number_of_times_reader_performed_infinite_scroll");
                //instructions.setCurrentDateForPeopleProperty("last_time_performed_reader_infinite_scroll");
                break;
            case NOTIFICATIONS_ACCESSED:
                eventName = "notifications_accessed";
                //instructions.setSuperPropertyAndPeoplePropertyToIncrement("number_of_times_accessed_notifications");
                //instructions.setCurrentDateForPeopleProperty("last_time_accessed_notifications");
                break;
            case STATS_ACCESSED:
                eventName = "stats_accessed";
                //instructions.setSuperPropertyAndPeoplePropertyToIncrement("number_of_times_accessed_stats");
                //instructions.setCurrentDateForPeopleProperty("last_time_accessed_stats");
                break;
            case STATS_VIEW_ALL_ACCESSED:
                eventName = "stats_view_all_accessed";
                //instructions.setSuperPropertyAndPeoplePropertyToIncrement("number_of_times_accessed_view_all_screen_stats");
                //instructions.setCurrentDateForPeopleProperty("last_time_accessed_view_all_screen_stats");
                break;
            case STATS_SINGLE_POST_ACCESSED:
                eventName = "stats_single_post_accessed";
                //instructions.setSuperPropertyAndPeoplePropertyToIncrement("number_of_times_accessed_single_post_screen_stats");
                //instructions.setCurrentDateForPeopleProperty("last_time_accessed_single_post_screen_stats");
                break;
            case STATS_OPENED_WEB_VERSION:
                eventName = "stats_web_version_accessed";
                //instructions.setSuperPropertyAndPeoplePropertyToIncrement("number_of_times_accessed_web_version_of_stats");
                //instructions.setCurrentDateForPeopleProperty("last_time_accessed_web_version_of_stats");
                break;
            case STATS_TAPPED_BAR_CHART:
                eventName = "stats_tapped_bar_chart";
                //instructions.setSuperPropertyAndPeoplePropertyToIncrement("number_of_times_tapped_stats_bar_chart");
                //instructions.setCurrentDateForPeopleProperty("last_time_tapped_stats_bar_chart");
                break;
            case STATS_SCROLLED_TO_BOTTOM:
                eventName = "stats_scrolled_to_bottom";
                //instructions.setSuperPropertyAndPeoplePropertyToIncrement("number_of_times_scrolled_to_bottom_of_stats");
                //instructions.setCurrentDateForPeopleProperty("last_time_scrolled_to_bottom_of_stats");
                break;
            default:
                eventName = null;
                break;
        }

        /*
        if (eventName == null) {
            return;
        }
*/
        String user = mAnonID != null ? mAnonID : mWpcomUserName;
        NosaraClient.NosaraUserType userType = mAnonID != null ?
                NosaraClient.NosaraUserType.ANON :
                NosaraClient.NosaraUserType.WPCOM;

        mNosaraClient.track(EVENTS_PREFIX + "danilotest", user, userType);
    }

    @Override
    public void beginSession() {
        if (mNosaraClient == null) {
            return;
        }

        refreshMetadata();
    }

    private String getNewAnonID() {
        String uuid = UUID.randomUUID().toString();
        String[] uuidSplitted = uuid.split("-");
        StringBuilder builder = new StringBuilder();
        for(String currentPart : uuidSplitted) {
            builder.append(currentPart);
        }
        uuid = builder.toString();
        Log.d(LOGTAG, "anon UUID generato " + uuid);
        return uuid;
    }

    @Override
    public void endSession() {
        if (mNosaraClient == null) {
            return;
        }
        mNosaraClient.flush();
    }

    @Override
    public void refreshMetadata() {
        if (mNosaraClient == null) {
            return;
        }

        boolean connected = WordPress.hasDotComToken(WordPress.getContext());
        if (connected) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WordPress.getContext());
            String username = preferences.getString(WordPress.WPCOM_USERNAME_PREFERENCE, null);
            // properties.put("username", username);
            mWpcomUserName = username;

            // should we re-unify the user?
            if (mAnonID != null) {
                // TODO call the _aliasUser event here!!!

                mAnonID = null;
            }
        } else {
            // Not wpcom connected. Check if mAnon is already present
            mWpcomUserName = null;
            if (mAnonID == null) {
                mAnonID = getNewAnonID();
            }
        }

        boolean jetpackUser = WordPress.wpDB.hasAnyJetpackBlogs();
        int numBlogs = WordPress.wpDB.getVisibleAccounts().size();
        try {
            JSONObject properties = new JSONObject();
            properties.put(JETPACK_USER, jetpackUser);
            properties.put(MIXPANEL_NUMBER_OF_BLOGS, numBlogs);
            mNosaraClient.registerUserProperties(properties);
        } catch (JSONException e) {
            AppLog.e(AppLog.T.UTILS, e);
        }
    }

    @Override
    public void clearAllData() {
        if (mNosaraClient == null) {
            return;
        }
        mNosaraClient.clearUserProperties();

        // Reset the anon token here
        mAnonID = null;
        mWpcomUserName = null;
    }

    @Override
    public void registerPushNotificationToken(String regId) {
        return;
    }
}