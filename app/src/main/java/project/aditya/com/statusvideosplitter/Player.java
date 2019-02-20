package project.aditya.com.statusvideosplitter;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.universalvideoview.UniversalMediaController;
import com.universalvideoview.UniversalVideoView;

import java.io.File;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Player extends AppCompatActivity {


    RemoteViews contentView;

    File savingwhere;
    Uri uri;

    int Gallerycode = 1000, FILE_REQUEST_CODE = 3;

    String TAG = "Aditya working";

    int lengthofsegment;
    Context context;
    FFmpeg ffmeg;
    static final File Externalstoragedirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/YOUR_FOLDER_NAME");
    static final String DIR_Name = "DASHRECORDER";
    int i = 0;
    public static final String CHANNEL_ID = "exampleServiceChannel";
    int millSecond;

    String remainingTime;
    private int mNotificationId = 1;
    private boolean firstTimeyou = true;


    NotificationManager notificationmanagerupdate;
    NotificationCompat.Builder mBuilderupdate;


    UniversalVideoView mVideoView;
    UniversalMediaController mMediaController;
    FrameLayout video_layout;
    View mVideoLayout;
    String path;
    Button split;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Intent i = getIntent();

        path = i.getStringExtra("videoPath");


        split = findViewById(R.id.splitButton);

        notificationmanagerupdate = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        mBuilderupdate =  new NotificationCompat.Builder(this);




        mVideoLayout = findViewById(R.id.video_layout);
        video_layout = findViewById(R.id.video_layout);

        mVideoView = (UniversalVideoView) findViewById(R.id.videoView);
        mMediaController = (UniversalMediaController) findViewById(R.id.media_controller);
        mVideoView.setMediaController(mMediaController);
//        mVideoView.setVideoPath(path);
//        mVideoView.start();



        mVideoView.setVideoViewCallback(new UniversalVideoView.VideoViewCallback() {

            @Override
            public void onScaleChange(boolean isFullscreen) {

            }

            @Override
            public void onPause(MediaPlayer mediaPlayer) { // Video pause

            }

            @Override
            public void onStart(MediaPlayer mediaPlayer) { // Video start/resume to play

            }

            @Override
            public void onBufferingStart(MediaPlayer mediaPlayer) {// steam start loading

            }

            @Override
            public void onBufferingEnd(MediaPlayer mediaPlayer) {// steam end loading

            }
        });




        split.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Split Clicked --- " + path , Toast.LENGTH_SHORT).show();

                lengthofsegment=30;
                splitVideoCommand( path ,lengthofsegment);
            }
        });


        loadffmeg();

    }

    public void videoPlayer() {

    }


    public void loadffmeg() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                if (ffmeg == null) {
                    ffmeg = FFmpeg.getInstance(getApplicationContext());
                }

                try {

                    ffmeg.loadBinary(new LoadBinaryResponseHandler() {
                        @Override
                        public void onFailure() {
                            super.onFailure();
                            Player.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(Player.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onSuccess() {
                            super.onSuccess();
                            Log.i(TAG, "load sucess");

                        }

                        @Override
                        public void onStart() {
                            super.onStart();

                        }

                        @Override
                        public void onFinish() {
                            super.onFinish();
                            Log.i(TAG, "load start");

                        }
                    });
                } catch (FFmpegNotSupportedException e) {
                    e.printStackTrace();

                    Player.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(Player.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }


    private void splitVideoCommand(String path, int lengthtocut) {

        if (isExternalStorageWritable()) {

            Toast.makeText(getApplicationContext(), " Creating Directory " + isExternalStorageWritable(),  Toast.LENGTH_SHORT).show();


            File Exdir = Environment.getExternalStorageDirectory();
            File moviesDir = new File(Exdir, "Video");

            if (!moviesDir.exists()) {
                moviesDir.mkdirs();
            }

            File innerDirectry = new File(moviesDir, "video Splitted");

            if (innerDirectry.exists()) {
                innerDirectry = new File(moviesDir, "video Splitted" + UUID.randomUUID());

            }

            innerDirectry.mkdirs();

            String filePrefix = "split_video";
            String fileExtn = ".mp4";
            String yourRealPath = path;


            savingwhere = innerDirectry;

            File dest = new File(innerDirectry, filePrefix + "%03d" + fileExtn);

            String length = String.valueOf(lengthtocut);

            String[] complexCommand = { "-i", yourRealPath, "-c:v", "libx264", "-preset", "ultrafast", "-crf", "22", "-map", "0", "-segment_time", length, "-g", "9", "-sc_threshold", "0", "-force_key_frames", "expr:gte(t,n_forced*6)", "-f", "segment", dest.getAbsolutePath()};
            execuateFFmpegBnary(complexCommand);

        } else {
            Toast.makeText(this, "No media found, Please check your disk", Toast.LENGTH_SHORT).show();
        }

    }

    private void execuateFFmpegBnary(final String[] command) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                createNotificationChannel();

                try {
                    ffmeg.execute(command , new ExecuteBinaryResponseHandler(){
                        @Override
                        public void onSuccess(String message) {
                            super.onSuccess(message);

                                stopserviceofnotifaction(getCurrentFocus());

                                stopTiming();

                        sendNotification("Completed Splitting Video");

                        }

                        @Override
                        public void onProgress(String message) {
                            super.onProgress(message);
                            //this takes some time
                            i++;

                            //show progress activity by going new inennt
                            remainingTime = getRemainingTime(message, millSecond);
                            //                            int time=Integer.parseInt(remainingTime);
                            sendtimingNotification(remainingTime);

                        }

                        @Override
                        public void onFailure(final String message) {
                            super.onFailure(message);

                                Player.this.runOnUiThread(new Runnable() {
                                    public void run() {

                                        Toast.makeText(Player.this, "Failed...." + message , Toast.LENGTH_LONG).show();
                                        Log.i(TAG, "errrrrr ---  " + message );


                                        startActivity(new Intent(getApplicationContext(), Player.class));
                                        finish();

                                        stopTiming();

                                        stopserviceofnotifaction(getCurrentFocus());
                                        sendNotification("Failed Splitting Video");

                                    }
                                });
                        }

                        @Override
                        public void onStart() {
                            super.onStart();

                            startActivity(new Intent(getApplicationContext(),DoingAct.class));
                            finish();
                            //startsserviceofnotifaction(getCurrentFocus());

                        }

                        @Override
                        public void onFinish() {
                            super.onFinish();

                                stopserviceofnotifaction(getCurrentFocus());
                                sendNotification("Completed Splitting Video");


                                stopTiming();


                        }
                    });
                } catch (FFmpegCommandAlreadyRunningException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Doing Video Splitting....\nthe best takes time",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public void startsserviceofnotifaction(View v) {


        Intent serviceIntent = new Intent(this, ExampleService.class);
        serviceIntent.putExtra("inputExtra", "Doing Video Splitting.. Please Wait!");

        ContextCompat.startForegroundService(this, serviceIntent);

    }

    public void stopserviceofnotifaction(View v) {
        Intent serviceIntent = new Intent(this, ExampleService.class);
        stopService(serviceIntent);

    }


    private void sendNotification(String msg) {
        NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, FinishedDoing.class);
        intent.putExtra("path",savingwhere.getAbsolutePath());

        intent.putExtra("yourpackage.notifyId", 0000);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder =  new NotificationCompat.Builder(this)
                .setContentTitle("VIDEO STATUS SPLITTER")
                .setSmallIcon(R.drawable.cutting)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(msg))
                .addAction(R.drawable.cutting, "View Videos", pIntent)
                .setContentIntent(pIntent)
                .setContentText(msg)
                .setOngoing(false)
                .setAutoCancel(true);
        notificationmanager.notify(0000, mBuilder.build());
    }

    private  void sendtimingNotification( String time){


        if (firstTimeyou) {
            mBuilderupdate.setSmallIcon(R.drawable.cutting)
                    .setContentTitle("Video Status Splitter")
                    .setOngoing(false)
                    .setOnlyAlertOnce(true)
                    .setContentText("Time Remaining is:    "+time)
                    .setTimeoutAfter(millSecond+10000000)
                    .setAutoCancel(false);
            firstTimeyou=false;
            Log.i("enterit","yeah"+firstTimeyou);
        }
        mBuilderupdate.setContentText("Time Remaining is:    "+time);

        notificationmanagerupdate.notify(123,mBuilderupdate.build());

    }

    private void stopTiming(){

        NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationmanager.cancel(123);

    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public String getRemainingTime(String message, long videoLengthInMillis) {
        Pattern pattern = Pattern.compile("time=([\\d\\w:]{8}[\\w.][\\d]+)");
        if (message.contains("speed")) {
            Matcher matcher = pattern.matcher(message);
            @SuppressWarnings("UnusedAssignment")
            String tempTime = "";
            if (matcher.find()) {
                tempTime = String.valueOf(matcher.group(1));
                String[] arrayTime = tempTime.split("[:|.]");
                long time =
                        TimeUnit.HOURS.toMillis(Long.parseLong(arrayTime[0])) +
                                TimeUnit.MINUTES.toMillis(Long.parseLong(arrayTime[1])) +
                                TimeUnit.SECONDS.toMillis(Long.parseLong(arrayTime[2])) +
                                Math.round(Long.parseLong(arrayTime[3]));
                String speed = message.substring(message.indexOf("speed=") + 1, message.indexOf("x")).split("=")[1];
                long eta = Math.round((Math.round(videoLengthInMillis) - time) / Float.valueOf(speed));


                long percentage=(videoLengthInMillis-eta)/100;
                String estimatedTime = toTimer(eta);
                Log.d("getRemainingTime", "EstimateTime -> " + estimatedTime);
                Log.d("getRemainingTime", "time -> " + time);
                Log.d("getRemainingTime", "speed -> " + speed);
                Log.d("getRemainingTime", "percentage -> " + percentage);
                return estimatedTime;
            }
        }
        return "";
    }

    private String toTimer(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
        if (hours!= 0 && hours>0)
            return String.format(Locale.getDefault(), "%02d:%02d:%02d",hours,minutes,seconds);
        else
            return String.format(Locale.getDefault(), "%02d:%02d",minutes,seconds);
    }
}
