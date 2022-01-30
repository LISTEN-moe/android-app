package me.echeung.moemoekyun.ui.dialog

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.SystemClock
import android.widget.SeekBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.databinding.DialogSleepTimerBinding
import me.echeung.moemoekyun.service.RadioService
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.ext.alarmManager
import me.echeung.moemoekyun.util.ext.getPluralString
import me.echeung.moemoekyun.util.ext.toast
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SleepTimerDialog(private val activity: Activity) : KoinComponent {

    private val preferenceUtil: PreferenceUtil by inject()

    private var binding: DialogSleepTimerBinding =
        DialogSleepTimerBinding.inflate(activity.layoutInflater, null, false)

    init {
        val sleepTimerText = binding.sleepTimerText
        val sleepTimerSeekBar = binding.sleepTimerSeekbar

        // Init seekbar + text
        val prevSleepTimer = preferenceUtil.sleepTimer().get()
        if (prevSleepTimer != 0) {
            sleepTimerSeekBar.progress = prevSleepTimer
        }

        sleepTimerText.text = activity.getPluralString(R.plurals.minutes, prevSleepTimer)
        sleepTimerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                sleepTimerText.text = activity.getPluralString(R.plurals.minutes, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Build dialog
        val sleepTimerDialog = MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.sleep_timer)
            .setView(binding.root)
            .setPositiveButton(R.string.set) { _, _ ->
                val minutes = sleepTimerSeekBar.progress
                setAlarm(minutes)
            }
            .setNegativeButton(R.string.close, null)

        // Show cancel button if a timer is currently set
        if (prevSleepTimer != 0) {
            sleepTimerDialog.setNeutralButton(R.string.cancel_timer) { _, _ -> cancelAlarm() }
        }

        sleepTimerDialog.create().show()
    }

    private fun setAlarm(minutes: Int) {
        if (minutes == 0) {
            cancelAlarm()
            return
        }

        preferenceUtil.sleepTimer().set(minutes)

        val timerTime = SystemClock.elapsedRealtime() + minutes * 60 * 1000
        val pendingIntent = makeTimerPendingIntent(PendingIntent.FLAG_CANCEL_CURRENT)
        activity.alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, timerTime, pendingIntent)

        activity.toast(activity.getPluralString(R.plurals.sleep_timer_set, minutes))
    }

    private fun cancelAlarm() {
        val previous = makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE)
        if (previous != null) {
            activity.alarmManager.cancel(previous)
            previous.cancel()

            preferenceUtil.sleepTimer().delete()

            activity.toast(activity.getString(R.string.sleep_timer_canceled))
        }
    }

    private fun makeTimerPendingIntent(flag: Int): PendingIntent? {
        return PendingIntent.getService(activity, 0, makeTimerIntent(), flag or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun makeTimerIntent(): Intent {
        return Intent(activity, RadioService::class.java)
            .setAction(RadioService.TIMER_STOP)
    }
}
