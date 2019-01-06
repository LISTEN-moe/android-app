package me.echeung.moemoekyun.ui.dialog

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.SystemClock
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.service.RadioService
import me.echeung.moemoekyun.util.system.alarmManager
import me.echeung.moemoekyun.util.system.getPluralString
import me.echeung.moemoekyun.util.system.toast

class SleepTimerDialog(@param:NonNull private val activity: Activity) {

    init {
        initDialog()
    }

    private fun initDialog() {
        val layout = activity.layoutInflater.inflate(R.layout.dialog_sleep_timer, activity.findViewById(R.id.layout_root_sleep))
        val sleepTimerText = layout.findViewById<TextView>(R.id.sleep_timer_text)
        val sleepTimerSeekBar = layout.findViewById<SeekBar>(R.id.sleep_timer_seekbar)

        // Init seekbar + text
        val prevSleepTimer = App.preferenceUtil!!.sleepTimer
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
        val sleepTimerDialog = AlertDialog.Builder(activity, R.style.DialogTheme)
                .setTitle(R.string.sleep_timer)
                .setView(layout)
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

        val pi = makeTimerPendingIntent(PendingIntent.FLAG_CANCEL_CURRENT)

        App.preferenceUtil!!.sleepTimer = minutes

        val timerTime = SystemClock.elapsedRealtime() + minutes * 60 * 1000
        activity.alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, timerTime, pi)

        activity.toast(activity.getPluralString(R.plurals.sleep_timer_set, minutes))
    }

    private fun cancelAlarm() {
        val previous = makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE)
        if (previous != null) {
            activity.alarmManager.cancel(previous)
            previous.cancel()

            App.preferenceUtil!!.clearSleepTimer()

            activity.toast(activity.getString(R.string.sleep_timer_canceled))
        }
    }

    private fun makeTimerPendingIntent(flag: Int): PendingIntent? {
        return PendingIntent.getService(activity, 0, makeTimerIntent(), flag)
    }

    private fun makeTimerIntent(): Intent {
        return Intent(activity, RadioService::class.java)
                .setAction(RadioService.TIMER_STOP)
    }
}
