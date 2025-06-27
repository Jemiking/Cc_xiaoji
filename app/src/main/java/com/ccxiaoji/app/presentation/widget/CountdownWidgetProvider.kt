package com.ccxiaoji.app.presentation.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.ccxiaoji.app.R

class CountdownWidgetProvider : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // 第一个小组件被添加时调用
        // 可以在这里初始化定时器或注册监听器
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        // 最后一个小组件被移除时调用
        // 可以在这里清理资源或取消监听器
        super.onDisabled(context)
    }
    
    companion object {
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.countdown_widget)
            
            // TODO: Update widget with actual countdown data
            views.setTextViewText(R.id.widget_title, "倒计时")
            views.setTextViewText(R.id.widget_days, "0 天")
            
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}