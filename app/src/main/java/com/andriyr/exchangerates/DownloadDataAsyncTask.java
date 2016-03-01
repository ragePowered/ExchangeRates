package com.andriyr.exchangerates;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class DownloadDataAsyncTask extends AsyncTask<String, Void, String> {
    private static final String URL = "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=3";

    private Context context;
    private AppWidgetManager appWidgetManager;
    private int appWidgetId;
    private RemoteViews views;

    public DownloadDataAsyncTask(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        this.context = context;
        this.appWidgetManager = appWidgetManager;
        this.appWidgetId = appWidgetId;
        this.views = new RemoteViews(context.getPackageName(), R.layout.exchange_rates_widget);
    }

    @Override
    protected String doInBackground(String... params) {
        StringBuilder tempBuffer = new StringBuilder();

        try {
            InputStreamReader isr = new InputStreamReader(new URL(URL).openConnection().getInputStream());

            int charRead;
            char[] inputBuffer = new char[500];
            while ((charRead = isr.read(inputBuffer)) > 0) {
                tempBuffer.append(String.copyValueOf(inputBuffer, 0, charRead));
            }
        } catch (IOException e) {
            Log.d("getJSONResponse", "IO Exception reading data " + e.getMessage());
        }
        return tempBuffer.toString();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        views.setViewVisibility(R.id.progressBar, View.VISIBLE);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    protected void onPostExecute(String jsonResponse) {
        super.onPostExecute(jsonResponse);
        int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, ExchangeRatesWidget.class));
        Intent clickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, clickIntent, 0);

        views.setTextViewText(R.id.appwidget_text, jsonResponse);
        views.setOnClickPendingIntent(R.id.relativeWidgetlayout, pendingIntent);
        views.setViewVisibility(R.id.progressBar, View.INVISIBLE);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
