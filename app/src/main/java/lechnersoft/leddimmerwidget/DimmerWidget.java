package lechnersoft.leddimmerwidget;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Date;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Implementation of App Widget functionality.
 */

public class DimmerWidget extends AppWidgetProvider {


    private boolean ON = false;
    private String destinationAdress = "http://192.168.1.12/";

    public  static String WIDGET_UPDATE = "WIDGET_UPDATE";
    private static final String ONOFFBUTTON = "toggleONOFF";
    private static final String INCRBUTTON = "incr";
    private static final String DECRBUTTON = "decr";
    private static String NEXTAlARM = "";


    @TargetApi(Build.VERSION_CODES.N)
    public static void updateNextAlarmTime(long time){
        Date d = new Date(time);
        NEXTAlARM = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format(d);
        Log.i("updateNextAlarmTime", "New Update Time : " + NEXTAlARM);
//        PendingIntend intend = getPendingSelfIntent(context, "ACTION_APPWIDGET_UPDATE")
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.dimmer_widget);
        ComponentName thisWidget = new ComponentName(context, DimmerWidget.class);
        remoteViews.setOnClickPendingIntent(R.id.ONOFF, getPendingSelfIntent(context, ONOFFBUTTON));
        remoteViews.setOnClickPendingIntent(R.id.incr, getPendingSelfIntent(context, INCRBUTTON));
        remoteViews.setOnClickPendingIntent(R.id.decr, getPendingSelfIntent(context, DECRBUTTON));
        Log.i("Update Widget View", "New Update Time : " + NEXTAlARM);
        remoteViews.setTextViewText(R.id.textView, "Next Alarm: " + NEXTAlARM);

        appWidgetManager.updateAppWidget(thisWidget, remoteViews);

        // Register Callback
        scheduleAlarmPolling(context);

    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


    public void onReceive(Context context, Intent intent) {

        WakeupInterface apiClient = LEDDimmerAPIClient.getClient(destinationAdress).create(WakeupInterface.class);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.dimmer_widget);
        remoteViews.setTextViewText(R.id.textView, "Next Alarm: " + NEXTAlARM);

        super.onReceive(context, intent);

        if (ONOFFBUTTON.equals(intent.getAction())){
            if(state.instance().ON) {
                remoteViews.setTextViewText(R.id.ONOFF, "ON");
                send(apiClient.on_light());
            }else{
                remoteViews.setTextViewText(R.id.ONOFF, "OFF");
                send(apiClient.off_light());
            }
            state.instance().ON = !state.instance().ON;
        }

        if (INCRBUTTON.equals(intent.getAction())){
            send(apiClient.incr_light());
        }

        if (DECRBUTTON.equals(intent.getAction())){
            send(apiClient.decr_light());
        }

        if (WIDGET_UPDATE.equals(intent.getAction())) {
            Call<ResponseBody> resp = apiClient.createPostWake(new PostWake(NEXTAlARM));
            send(resp);


//            AppWidgetManager appWidgetManager  = AppWidgetManager.getInstance(context);
//            int[] widgetIds = appWidgetManager.getAppWidgetIds(
//                    new ComponentName(context, DimmerWidget.class)
//            );
//            RemoteViews updatedViews = new RemoteViews(); // Actually get the updated views
//            appWidgetManager.updateAppWidget(widgetIds, updatedViews);
//            //manager.updateAppWidget(thisWidget, remoteViews);
        }

//        pushWidgetUpdate(context);

    }

    public static void pushWidgetUpdate(Context context) {
//        ComponentName myWidget = new ComponentName(context, DimmerWidget.class);
//        AppWidgetManager manager = AppWidgetManager.getInstance(context);
//        manager.updateAppWidget(myWidget, remoteViews);
//        Log.i("UPDATED", "testing");

        Intent intent = new Intent(context.getApplicationContext(), DimmerWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        int[] ids = widgetManager.getAppWidgetIds(new ComponentName(context, DimmerWidget.class));
        widgetManager.notifyAppWidgetViewDataChanged(ids, android.R.id.list);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }

    private void send(Call<ResponseBody> resp){

        resp.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i("Send:","response");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i("Send:","sending failure");
            }
        });
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }


    // Setup a recurring alarm every half hour
    public void scheduleAlarmPolling(Context context) {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(context, AlarmPollingReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, AlarmPollingReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every every half hour from this point onwards
        long firstMillis = System.currentTimeMillis(); // alarm is set right away

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
        AlarmManager.INTERVAL_FIFTEEN_MINUTES, pIntent);
    }


//    private class CallAPI extends AsyncTask<String, Void, Void> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected Void doInBackground(String... params) {
//            String urlString = params[0];
//            String msg = params[1];
//            OutputStream out = null;
//            try {
//                URL url = new URL(urlString);
//                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//                out = new BufferedOutputStream(urlConnection.getOutputStream());
//                BufferedWriter writer = new BufferedWriter (new OutputStreamWriter(out, "UTF-8"));
//
//                writer.write(msg);
//                writer.flush();
//                writer.close();
//                out.close();
//                urlConnection.connect();
//
//            } catch (Exception e) {
//                System.out.println(e.getMessage());
//            }
//            return null;
//        }
//    }
}

