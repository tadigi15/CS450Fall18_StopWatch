package edu.stlawu.stopwatch;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import static android.graphics.Color.*;

public class MainActivity extends AppCompatActivity {

    // Define variable for our views
    private TextView tv_count = null;
    private Button bt_start = null;
    private Button bt_stop = null;
    //added reset and resume buttons
    private Button bt_reset =null;
    private Button bt_resume =null;

    private Timer t = null;
    private Counter ctr = null;  // TimerTask

    public AudioAttributes  aa = null;
    private SoundPool soundPool = null;
    private int bloopSound = 0;
    //keeps track if we've previously started the app
    private boolean didWeStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize views
        this.tv_count = findViewById(R.id.tv_count);
        this.bt_start = findViewById(R.id.bt_start);
        this.bt_start.setBackgroundColor(Color.GREEN);
        this.bt_stop = findViewById(R.id.bt_stop);
        this.bt_stop.setBackgroundColor(Color.RED);
        this.bt_resume = findViewById(R.id.bt_resume);
        this.bt_resume.setBackgroundColor(Color.GRAY);
        this.bt_resume.setEnabled(false);
        this.bt_reset = findViewById(R.id.bt_reset);
        this.bt_reset.setBackgroundColor(Color.BLUE);

        //start listener
        this.bt_start.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 didWeStart = true;
                 bt_start.setEnabled(false);
                 bt_start.setBackgroundColor(Color.GRAY);
                 //enable stop button since time is running
                 bt_stop.setEnabled(true);
                 bt_stop.setBackgroundColor(Color.RED);
                 t.scheduleAtFixedRate(ctr, 0, 1000);
             }
            });

        //stop listener
        this.bt_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bt_stop.setEnabled(false);
                bt_stop.setBackgroundColor(Color.GRAY);
                //enable resume button since stopped
                bt_resume.setEnabled(true);
                bt_resume.setBackgroundColor(Color.YELLOW);
                t.cancel();
                t.purge();
                getPreferences(MODE_PRIVATE)
                        .edit()
                        .putInt("COUNT", ctr.count)
                        .apply();
                onStop();
            }
        });

        //reset listener
        this.bt_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //give the app a fresh start
                ctr.count = 0;
                getPreferences(MODE_PRIVATE)
                        .edit()
                        .putInt("COUNT", ctr.count)
                        .apply();
                t.cancel();
                t.purge();
                ctr.cancel();
                onStart();
            }
        });

        //resume listener
        this.bt_resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bt_resume.setEnabled(false);
                bt_resume.setBackgroundColor(Color.GRAY);
                bt_stop.setEnabled(true);
                bt_stop.setBackgroundColor(Color.RED);

                int count = getPreferences(MODE_PRIVATE).
                        getInt("COUNT", 0);
                onResume();
            }
        });


        this.aa = new AudioAttributes
                .Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build();

        this.soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(aa)
                .build();
        this.bloopSound = this.soundPool.load(
                this, R.raw.bloop, 1);

        this.tv_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPool.play(bloopSound, 1f,
                        1f, 1, 0, 1f);
                Animator anim = AnimatorInflater
                        .loadAnimator(MainActivity.this,
                                       R.animator.counter);
                anim.setTarget(tv_count);
                anim.start();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //configure buttons
        bt_start.setEnabled(true);
        bt_start.setBackgroundColor(Color.GREEN);
        bt_stop.setEnabled(false);
        bt_stop.setBackgroundColor(Color.GRAY);
        bt_resume.setEnabled(false);
        bt_resume.setBackgroundColor(Color.GRAY);

        // releoad the count from a previous
        // run, if first time running, start at 0.
        /// preferences to share state

        int count;
        boolean didWeStart = getPreferences(MODE_PRIVATE)
                .getBoolean("didWeStart",false);
        if(didWeStart){
            count = getPreferences(MODE_PRIVATE).
                    getInt("COUNT", 0);
        }
        else{
            count = 0;
        }
        didWeStart = true;

        this.tv_count.setText(timeConverter(count));
        this.ctr = new Counter();
        this.ctr.count = count;
        this.t = new Timer();

        // factory method - design pattern
        Toast.makeText(this, "Taylor's Stopwatch is ready to go!",
                        Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onPause() {
        super.onPause();
        didWeStart=true;
    }

    @Override
    protected void onStop() {
        didWeStart=true;
        getPreferences(MODE_PRIVATE)
                .edit()
                .putBoolean("didWeStart", didWeStart)
                .apply();
        super.onStop();
    }

    @Override
    protected void onRestart(){
        super.onRestart();
    }

    @Override
    protected void onResume(){
        bt_start.setEnabled(false);
        bt_start.setBackgroundColor(Color.GRAY);
        bt_resume.setEnabled(false);
        bt_resume.setBackgroundColor(Color.GRAY);
        //TODO IMPLEMENT IF STATEMENT
        int count;
        boolean didWeStart = getPreferences(MODE_PRIVATE)
                .getBoolean("didWeStart",false);
        if(didWeStart){
            count = getPreferences(MODE_PRIVATE).
                    getInt("COUNT", 0);
        }
        else{
            count = 0;
        }
        didWeStart = true;

        this.tv_count.setText(timeConverter(count));
        this.ctr = new Counter();
        this.ctr.count = count;
        this.t = new Timer();
        t.scheduleAtFixedRate(ctr, 0, 1000);
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPreferences(MODE_PRIVATE)
                .edit()
                .putInt("COUNT", ctr.count)
                .apply();
        getPreferences(MODE_PRIVATE)
                .edit()
                .putBoolean("didWeStart", didWeStart)
                .apply();
    }

    //method to convert time to include tenths of seconds
    protected String timeConverter(int counter){
        int minutes = counter/60;
        int seconds = counter % 60;
        int milliseconds = counter / 1000;
        if (minutes < 10){
            if (seconds < 10){
                return "0" + minutes + " : 0" + seconds + " : " + milliseconds;
            }
            return "0" + minutes + " : " + seconds + " : " + milliseconds;
        }
        if (seconds < 10){
            return minutes + " : 0" + seconds + " : " + milliseconds;
        }
        return minutes + " : " + seconds + " : " + milliseconds;
    }

    private void setTimeInMillis(long millis){};

    class Counter extends TimerTask {
        private int count = getPreferences(MODE_PRIVATE).
        getInt("COUNT", 0);
        //private int count = 0;
        @Override
        public void run() {
            MainActivity.this.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.tv_count.setText(timeConverter(count));
                            //MainActivity.this.tv_count.setText(
                            //        Integer.toString(count));
                            count++;
                        }
                    }
            );
        }
    }
}
