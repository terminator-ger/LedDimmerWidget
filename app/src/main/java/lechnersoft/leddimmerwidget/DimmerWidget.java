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

    private static final String ONOFFBUTTON = "toggleONOFF";
    private static final String INCRBUTTON = "incr";
    private static final String DECRBUTTON = "decr";
    private static final String SUNRISEBUTTON = "sunrise";
    private static String NEXTAlARM = "";
    private static String SETALARM = "";


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
        remoteViews.setTextViewText(R.id.textView, "Next Alarm: " + NEXTAlARM);

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

        RESTWakeupInterface apiClient = LEDDimmerAPIClient.getClient(destinationAdress).create(RESTWakeupInterface.class);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.dimmer_widget);
        remoteViews.setTextViewText(R.id.textView, "Next Alarm: " + NEXTAlARM);

        super.onReceive(context, intent);

        if (ONOFFBUTTON.equals(intent.getAction())){
            if(state.instance().ON) {
                remoteViews.setTextViewText(R.id.ONOFF, "ON");
                send(apiClient.RestLightOn());
            }else{
                remoteViews.setTextViewText(R.id.ONOFF, "OFF");
                send(apiClient.RestLightOff());
            }
            state.instance().ON = !state.instance().ON;
        }

        if (INCRBUTTON.equals(intent.getAction())){
            send(apiClient.RestLightIncr());
        }

        if (DECRBUTTON.equals(intent.getAction())){
            send(apiClient.RestLightDecr());
        }

        if (SUNRISEBUTTON.equals(intent.getAction())){
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

        if (intent.getAction().equals(ACTION_NEXT_ALARM_CLOCK_CHANGED)){
            if(NEXTAlARM == "" || NEXTAlARM != SETALARM) {
                AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                AlarmManager.AlarmClockInfo alinfo = alarm.getNextAlarmClock();
                long time = alinfo.getTriggerTime();

                Date d = new Date(time);
                String alarmtime = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format(d);
                if(!alarmtime.equals(NEXTAlARM)) {
                    Log.i("updateNextAlarmTime", "New Update Time : " + NEXTAlARM);
                    NEXTAlARM = alarmtime;
                    Call<ResponseBody> resp = apiClient.RestWakeUp(new PostWake(NEXTAlARM));
                    send(resp);
                }
            }
            pushUIUpdate(intent, context);
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
                        SETALARM = msg;
                    }
                    if(header.contains("SUNRISE_OK")){
                        SETALARM = msg;
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

        });
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }



}

