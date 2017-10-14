package me.echeung.moemoekyun.ui.dialogs;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.service.RadioService;
import me.echeung.moemoekyun.utils.PreferenceUtil;

public class SleepTimerDialog {

    private WeakReference<Activity> activityRef;

    public SleepTimerDialog(Activity activity) {
        activityRef = new WeakReference<>(activity);
    }

    public void show() {
        final Activity activity = activityRef.get();
        if (activity == null) {
            return;
        }

        final View layout = activity.getLayoutInflater().inflate(R.layout.dialog_sleep_timer, activity.findViewById(R.id.layout_root_sleep));
        final TextView sleepTimerText = layout.findViewById(R.id.sleep_timer_text);
        final SeekBar sleepTimerSeekBar = layout.findViewById(R.id.sleep_timer_seekbar);

        final int prevSleepTimer = PreferenceUtil.getInstance(activity).getSleepTimer();
        if (prevSleepTimer != 0) {
            sleepTimerSeekBar.setProgress(prevSleepTimer);
        }

        sleepTimerText.setText(getPluralText(R.plurals.minutes, sleepTimerSeekBar.getProgress()));
        sleepTimerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                sleepTimerText.setText(getPluralText(R.plurals.minutes, sleepTimerSeekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        final AlertDialog.Builder sleepTimerDialog = new AlertDialog.Builder(activity, R.style.DialogTheme)
                .setTitle(R.string.sleep_timer)
                .setView(layout)
                .setPositiveButton(R.string.set, (dialogInterface, i) -> {
                    final PendingIntent pi = makeTimerPendingIntent(PendingIntent.FLAG_CANCEL_CURRENT);

                    final int minutes = sleepTimerSeekBar.getProgress();
                    PreferenceUtil.getInstance(activity).setSleepTimer(minutes);

                    final long timerTime = SystemClock.elapsedRealtime() + (minutes * 60 * 1000);
                    AlarmManager am = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
                    am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, timerTime, pi);

                    Toast.makeText(activity, getPluralText(R.plurals.sleep_timer_set, minutes), Toast.LENGTH_SHORT)
                            .show();
                })
                .setNegativeButton(R.string.close, null);

        if (prevSleepTimer != 0) {
            sleepTimerDialog.setNeutralButton(R.string.cancel_timer, (dialogInterface, i) -> {
                final PendingIntent previous = makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE);
                if (previous != null) {
                    AlarmManager am = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
                    am.cancel(previous);
                    previous.cancel();

                    PreferenceUtil.getInstance(activity).clearSleepTimer();

                    Toast.makeText(activity, activity.getString(R.string.sleep_timer_canceled), Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }

        sleepTimerDialog.create().show();
    }

    private PendingIntent makeTimerPendingIntent(int flag) {
        return PendingIntent.getService(activityRef.get(), 0, makeTimerIntent(), flag);
    }

    private Intent makeTimerIntent() {
        return new Intent(activityRef.get(), RadioService.class)
                .setAction(RadioService.STOP);
    }

    private String getPluralText(int pluralId, int value) {
        final String text = activityRef.get().getResources().getQuantityString(pluralId, value);
        return String.format(text, value);
    }
}
