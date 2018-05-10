//package lechnersoft.leddimmerwidget;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.util.Log;
//
///**
// * Created by Michael on 26 Jan 2018.
// */
//
//public class AlarmPollingReceiver extends BroadcastReceiver {
//        public static final int REQUEST_CODE = 12345;
//        public static final String ACTION = "com.codepath.example.servicesdemo.alarm";
//
//        // Triggered by the Alarm periodically (starts the service to run task)
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.i("AlarmPollingReceiver","onReceive");
//
//
//            Intent i = new Intent(context, AlarmPolling.class);
//            i.putExtra("foo", "bar");
//            context.startService(i);
//        }
//
//}
