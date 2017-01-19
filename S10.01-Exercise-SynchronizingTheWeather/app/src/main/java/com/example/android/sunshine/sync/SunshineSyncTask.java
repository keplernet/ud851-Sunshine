package com.example.android.sunshine.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

//  TODO (1) Create a class called SunshineSyncTask
//  TODO (2) Within SunshineSyncTask, create a synchronized public static void method called syncWeather
//      TODO (3) Within syncWeather, fetch new weather data
//      TODO (4) If we have valid results, delete the old data and insert the new
public class SunshineSyncTask{

    synchronized public static void syncWeather(Context context){
       URL url = NetworkUtils.getUrl(context);

        try {
            String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(url);

            ContentValues[] contentValues = OpenWeatherJsonUtils.getWeatherContentValuesFromJson(context, jsonWeatherResponse);

            if(contentValues != null && contentValues.length > 0){
                ContentResolver cr = context.getContentResolver();
                cr.delete(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        null,
                        null);

                cr.bulkInsert(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        contentValues);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}