package com.tuapp.pollenwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.widget.RemoteViews
import kotlinx.coroutines.*

class PollenWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, PollenWidget::class.java))
            onUpdate(context, mgr, ids)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.tuapp.pollenwidget.REFRESH"

        fun updateWidget(context: Context, mgr: AppWidgetManager, id: Int) {
            val loading = RemoteViews(context.packageName, R.layout.widget_pollen)
            loading.setTextViewText(R.id.tv_status, "Actualizando...")
            mgr.updateAppWidget(id, loading)

            CoroutineScope(Dispatchers.IO).launch {
                val data = PollenRepository.fetchPollenData()
                withContext(Dispatchers.Main) {
                    val views = buildViews(context, data)
                    mgr.updateAppWidget(id, views)
                }
            }
        }

        private fun buildViews(context: Context, data: PollenData?): RemoteViews {
            val v = RemoteViews(context.packageName, R.layout.widget_pollen)
            if (data == null) {
                v.setTextViewText(R.id.tv_status, "❌ Sin datos")
                v.setTextViewText(R.id.tv_grass, "—")
                v.setTextViewText(R.id.tv_birch, "—")
                v.setTextViewText(R.id.tv_alder, "—")
                v.setTextViewText(R.id.tv_level_label, "Error de conexión")
            } else {
                val level = data.maxLevel()
                v.setTextViewText(R.id.tv_status, level.label)
                v.setInt(R.id.widget_root, "setBackgroundColor", level.bgColor)
                v.setTextViewText(R.id.tv_grass, "🌾 Gramíneas: ${data.grass.toInt()} — ${label(data.grass)}")
                v.setTextViewText(R.id.tv_birch, "🌳 Abedul:    ${data.birch.toInt()} — ${label(data.birch)}")
                v.setTextViewText(R.id.tv_alder, "🍃 Aliso:     ${data.alder.toInt()} — ${label(data.alder)}")
                v.setTextViewText(R.id.tv_level_label, "Madrid · ${data.timestamp}")
            }
            val pi = PendingIntent.getBroadcast(
                context, 0,
                Intent(context, PollenWidget::class.java).apply { action = ACTION_REFRESH },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            v.setOnClickPendingIntent(R.id.btn_refresh, pi)
            return v
        }

        private fun label(v: Double) = when {
            v < 10  -> "Bajo"
            v < 50  -> "Moderado"
            v < 200 -> "Alto"
            else    -> "Muy alto"
        }
    }
}
