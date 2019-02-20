package project.aditya.com.statusvideosplitter;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import static project.aditya.com.statusvideosplitter.Player.CHANNEL_ID;

public class ExampleService extends Service {

    String input;
    PendingIntent pendingIntent;
    Intent notificationIntent;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        input = intent.getStringExtra("inputExtra");

        notificationIntent = new Intent(this, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setContentTitle("Video Staus Splitter")
                        .setContentText(input)
                        .setSmallIcon(R.drawable.cutting)
                        .setContentIntent(pendingIntent)
                        .build();

                startForeground(1, notification);
            }
        });

        //do heavy work on a background thread
        //stopSelf();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}