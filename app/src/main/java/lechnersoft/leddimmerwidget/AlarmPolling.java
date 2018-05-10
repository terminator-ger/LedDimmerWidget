//package lechnersoft.leddimmerwidget;
//
//import android.app.AlarmManager;
//import android.app.IntentService;
//import android.appwidget.AppWidgetManager;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.util.Log;
//
///**
// * Created by Michael on 26 Jan 2018.
// */
//
//public class AlarmPolling  extends IntentService {
//
//        public AlarmPolling() {
//            super("AlarmPollingService");
//        }
//
//        @Override
//        protected void onHandleIntent(Intent intent) {
//
//            AlarmManager alarm = (AlarmManager) this.getBaseContext().getSystemService(Context.ALARM_SERVICE);
//            AlarmManager.AlarmClockInfo alinfo = alarm.getNextAlarmClock();
//            long time = alinfo.getTriggerTime();
//            DimmerWidget.updateNextAlarmTime(time);
//
//            Log.i("AlarmPolling", "Service running");
//
//            updateWidgets(intent);
//        }
//
//        public void updateWidgets(Intent intent) {
//
//            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//            // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
//            // since it seems the onUpdate() is only fired on that:
//            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());
//
//            int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(this.getApplicationContext(), DimmerWidget.class));
//
//            appWidgetManager.notifyAppWidgetViewDataChanged(ids, android.R.id.list);
//
//            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
//            this.getBaseContext().sendBroadcast(intent);
//        }
//}
