package lechnersoft.leddimmerwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED;

/**
 * Implementation of App Widget functionality.
 */

public class DimmerWidget extends AppWidgetProvider {

    private static String destinationAdress = "http://192.168.1.20/";
    //public static final String ALARM_SNOOZE_ACTION = "com.android.deskclock.ALARM_SNOOZE";
    //public static final String ALARM_ALERT_ACTION = "com.android.deskclock.ALARM_ALERT";
    private static final String ONOFFBUTTON = "toggleONOFF";
    private static final String INCRBUTTON = "incr";
    private static final String DECRBUTTON = "decr";
    private static final String SUNRISEBUTTON = "sunrise";
    private static long NEXTAlARM = 0;
    private static long SETALARM = 0;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.dimmer_widget);
        ComponentName thisWidget = new ComponentName(context, DimmerWidget.class);
        remoteViews.setOnClickPendingIntent(R.id.ONOFF, getPendingSelfIntent(context, ONOFFBUTTON));
        remoteViews.setOnClickPendingIntent(R.id.incr, getPendingSelfIntent(context, INCRBUTTON));
        remoteViews.setOnClickPendingIntent(R.id.decr, getPendingSelfIntent(context, DECRBUTTON));
        remoteViews.setOnClickPendingIntent(R.id.sunrise, getPendingSelfIntent(context, SUNRISEBUTTON));

        Log.i("Update Widget View", "New Update Time : " + NEXTAlARM);
        long dv = Long.valueOf(NEXTAlARM);// its need to be in milisecond
        Date df = new java.util.Date(dv);
        String vv = new SimpleDateFormat("EEEE hh:mm").format(df);
        Log.i("updateNextAlarmTime", "New Update Time : " + vv);
        remoteViews.setTextViewText(R.id.textView, "Next Alarm: " + vv);

        appWidgetManager.updateAppWidget(thisWidget, remoteViews);

        // Register Callback
//        scheduleAlarmPolling(context);

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
    try {
        RESTWakeupInterface apiClient = LEDDimmerAPIClient.getClient(destinationAdress).create(RESTWakeupInterface.class);


        super.onReceive(context, intent);

        if (ONOFFBUTTON.equals(intent.getAction())) {
            send(apiClient.RestLightToggle());
        }

        if (INCRBUTTON.equals(intent.getAction())) {
            send(apiClient.RestLightIncr());
        }

        if (DECRBUTTON.equals(intent.getAction())) {
            send(apiClient.RestLightDecr());
        }

        if (SUNRISEBUTTON.equals(intent.getAction())) {
            send(apiClient.RestSunrise());
            Handler handler = new Handler();
            final Intent myIntent = intent;
            final Context myConext = context;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pushUIUpdate(myIntent, myConext);
                }
            }, 5000);
        }

        if (intent.getAction().equals(ACTION_NEXT_ALARM_CLOCK_CHANGED)) {
            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            AlarmManager.AlarmClockInfo alinfo = alarm.getNextAlarmClock();
            long time = alinfo.getTriggerTime();

            // Date d = new Date(time);
            // String alarmtime = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format(d);


            // alarm need to be either empty.. new widged or something different than we have allready set
            if (NEXTAlARM == 0 && SETALARM == 0) {
                Log.i("updateNextAlarmTime", "New Update Time : " + time);
                NEXTAlARM = time;
                Call<ResponseBody> resp = apiClient.RestWakeUp(new PostWake(String.valueOf(time)));
                send(resp);

                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.dimmer_widget);
                long dv = Long.valueOf(NEXTAlARM);// its need to be in milisecond
                Date df = new java.util.Date(dv);
                String vv = new SimpleDateFormat("EEEE hh:mm").format(df);
                Log.i("updateNextAlarmTime", "New Update Time : " + vv);
                remoteViews.setTextViewText(R.id.textView, "Next Alarm: " + vv);

            } else if (time != SETALARM) {
                if (Math.abs(time - SETALARM) >= 900) {
                    // ofc disable for already set times-> just update when different
                    if (time != NEXTAlARM) {
                        NEXTAlARM = time;
                        Log.i("updateNextAlarmTime", "New Update Time : " + NEXTAlARM);
                        Call<ResponseBody> resp = apiClient.RestWakeUp(new PostWake(String.valueOf(NEXTAlARM)));
                        send(resp);

                        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.dimmer_widget);
                        long dv = Long.valueOf(NEXTAlARM);// its need to be in milisecond
                        Date df = new java.util.Date(dv);
                        String vv = new SimpleDateFormat("EEEE hh:mm").format(df);
                        Log.i("updateNextAlarmTime", "New Update Time : " + vv);
                        remoteViews.setTextViewText(R.id.textView, "Next Alarm: " + vv);

                    }
                }
            }
            pushUIUpdate(intent, context);
        }
    }
    catch(Exception e){

    }

    }

    private void pushUIUpdate(Intent intent, Context context){
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
            // since it seems the onUpdate() is only fired on that:
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, DimmerWidget.class));

            appWidgetManager.notifyAppWidgetViewDataChanged(ids, android.R.id.list);

            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            context.sendBroadcast(intent);
    }


    private void send(Call<ResponseBody> resp){

        resp.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String msg = "";
                try {
                    msg = response.body().string();
                    String header = response.message().toString();
                    if(header.contains("WAKEUP_OK")){
                        try {
                            SETALARM = Long.parseLong(msg);
                        }catch (Exception e){

                        }
                    }
                    if(header.contains("SUNRISE_OK")){
                        try {
                            SETALARM = Long.parseLong(msg);
                        }catch (Exception e){

                        }
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
                Log.i("Response: ", msg);
                Log.i("Send:","response");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                Log.i("Send:","sending failure");
            }
            private long convertTimestamp(String t){
                Date d = null;
                try {
                    d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").parse(t);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return d.getTime();
            }

        });
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }



}

