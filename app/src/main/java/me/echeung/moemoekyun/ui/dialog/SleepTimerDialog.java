package me.echeung.moemoekyun.ui.dialog;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.service.RadioService;
import me.echeung.moemoekyun.util.system.PluralsUtil;

public class SleepTimerDialog {

    private Activity activity;

    public SleepTimerDialog(@NonNull Activity activity) {
        this.activity = activity;

        initDialog();
    }

    private void initDialog() {
        View layout = activity.getLayoutInflater().inflate(R.layout.dialog_sleep_timer, activity.findViewById(R.id.layout_root_sleep));
        TextView sleepTimerText = layout.findViewById(R.id.sleep_timer_text);
        SeekBar sleepTimerSeekBar = layout.findViewById(R.id.sleep_timer_seekbar);

        // Init seekbar + text
        int prevSleepTimer = App.getPreferenceUtil().getSleepTimer();
        if (prevSleepTimer != 0) {
            sleepTimerSeekBar.setProgress(prevSleepTimer);
        }

        sleepTimerText.setText(PluralsUtil.getString(activity, R.plurals.minutes, prevSleepTimer));
        sleepTimerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sleepTimerText.setText(PluralsUtil.getString(activity, R.plurals.minutes, progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Build dialog
        AlertDialog.Builder sleepTimerDialog = new AlertDialog.Builder(activity, R.style.DialogTheme)
                .setTitle(R.string.sleep_timer)
                .setView(layout)
                .setPositiveButton(R.string.set, (dialogInterface, i) -> {
                    int minutes = sleepTimerSeekBar.getProgress();
                    setAlarm(minutes);
                })
                .setNegativeButton(R.string.close, null);

        // Show cancel button if a timer is currently set
        if (prevSleepTimer != 0) {
            sleepTimerDialog.setNeutralButton(R.string.cancel_timer, (dialogInterface, i) -> cancelAlarm());
        }

        sleepTimerDialog.create().show();
    }

    private void setAlarm(int minutes) {
        if (minutes == 0) {
            cancelAlarm();
            return;
        }

        PendingIntent pi = makeTimerPendingIntent(PendingIntent.FLAG_CANCEL_CURRENT);

        App.getPreferenceUtil().setSleepTimer(minutes);

        long timerTime = SystemClock.elapsedRealtime() + (minutes * 60 * 1000);
        AlarmManager am = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, timerTime, pi);

        Toast.makeText(activity, PluralsUtil.getString(activity, R.plurals.sleep_timer_set, minutes), Toast.LENGTH_SHORT).show();
    }

    private void cancelAlarm() {
        PendingIntent previous = makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE);
        if (previous != null) {
            AlarmManager am = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            am.cancel(previous);
            previous.cancel();

            App.getPreferenceUtil().clearSleepTimer();

            Toast.makeText(activity, activity.getString(R.string.sleep_timer_canceled), Toast.LENGTH_SHORT).show();
        }
    }

    private PendingIntent makeTimerPendingIntent(int flag) {
        return PendingIntent.getService(activity, 0, makeTimerIntent(), flag);
    }

    private Intent makeTimerIntent() {
        return new Intent(activity, RadioService.class)
                .setAction(RadioService.TIMER_STOP);
    }
}
