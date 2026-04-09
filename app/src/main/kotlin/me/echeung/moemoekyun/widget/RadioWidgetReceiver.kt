package me.echeung.moemoekyun.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RadioWidgetReceiver : GlanceAppWidgetReceiver() {

    @Inject
    lateinit var radioWidgetUpdater: RadioWidgetUpdater

    override val glanceAppWidget: GlanceAppWidget = RadioWidget()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // Ensure the observer is running (e.g. if the app process was killed and restarted by the
        // system solely to service a widget update broadcast).
        radioWidgetUpdater.startObserving()
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Also make sure observing is active on any broadcast the receiver handles.
        radioWidgetUpdater.startObserving()
    }
}
