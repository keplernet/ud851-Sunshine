/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.utilities.SunshineDateUtils;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;

public class DetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
//      TODO (21) Implement LoaderManager.LoaderCallbacks<Cursor>

    private static final String TAG = DetailActivity.class.getSimpleName();

    /*
     * In this Activity, you can share the selected day's forecast. No social sharing is complete
     * without using a hashtag. #BeTogetherNotTheSame
     */
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

//  TODO (18) Create a String array containing the names of the desired data columns from our ContentProvider
    String[] DETAIL_FORECAST_PROJECTION = {
        WeatherContract.WeatherEntry.COLUMN_DATE,
        WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
        WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
        WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
        WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
        WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
        WeatherContract.WeatherEntry.COLUMN_DEGREES,
        WeatherContract.WeatherEntry.COLUMN_PRESSURE
    };
//  TODO (19) Create constant int values representing each column name's position above
    private static final int INDEX_WEATHER_DATE = 0;
    private static final int INDEX_WEATHER_ID = 1;
    private static final int INDEX_WEATHER_MAX_TEMP = 2;
    private static final int INDEX_WEATHER_MIN_TEMP = 3;
    private static final int INDEX_WEATHER_HUMIDITY = 4;
    private static final int INDEX_WEATHER_WIND_SPEED = 5;
    private static final int INDEX_WEATHER_WIND_DIRECTION = 6;
    private static final int INDEX_WEATHER_PRESSURE = 7;

//  TODO (20) Create a constant int to identify our loader used in DetailActivity
    private static final int DETAIL_LOADER_ID = 73737373;

    /* A summary of the forecast that can be shared by clicking the share button in the ActionBar */
    private String mForecastSummary;

//  TODO (15) Declare a private Uri field called mUri
    Uri mUri;

//  TODO (10) Remove the mWeatherDisplay TextView declaration

//  TODO (11) Declare TextViews for the date, description, high, low, humidity, wind, and pressure
    TextView mTextViewDate, mTextViewDescription, mTextViewHigh,
        mTextViewLow, mTextViewHumidity, mTextViewWind, mTextViewPressure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
//      TODO (12) Remove mWeatherDisplay TextView
//      TODO (13) Find each of the TextViews by ID
        mTextViewDate = (TextView) findViewById(R.id.tv_date);
        mTextViewDescription = (TextView) findViewById(R.id.tv_description);
        mTextViewHigh = (TextView) findViewById(R.id.tv_high_temperature);
        mTextViewLow = (TextView) findViewById(R.id.tv_low_temperature);
        mTextViewHumidity = (TextView) findViewById(R.id.tv_humidity);
        mTextViewWind = (TextView) findViewById(R.id.tv_wind);
        mTextViewPressure = (TextView) findViewById(R.id.tv_pressure);

//      TODO (14) Remove the code that checks for extra text

//      TODO (16) Use getData to get a reference to the URI passed with this Activity's Intent

        mUri = getIntent().getData();
//      TODO (17) Throw a NullPointerException if that URI is null
        if(mUri == null) throw new NullPointerException("Uri in Data in the intent is null");

//      TODO (35) Initialize the loader for DetailActivity
        getSupportLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
    }

    /**
     * This is where we inflate and set up the menu for this Activity.
     *
     * @param menu The options menu in which you place your items.
     *
     * @return You must return true for the menu to be displayed;
     *         if you return false it will not be shown.
     *
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.forecast, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    /**
     * Callback invoked when a menu item was selected from this Activity's menu. Android will
     * automatically handle clicks on the "up" button for us so long as we have specified
     * DetailActivity's parent Activity in the AndroidManifest.
     *
     * @param item The menu item that was selected by the user
     *
     * @return true if you handle the menu click here, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* Get the ID of the clicked item */
        int id = item.getItemId();

        /* Settings menu item clicked */
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        /* Share menu item clicked */
        if (id == R.id.action_share) {
            Intent shareIntent = createShareForecastIntent();
            startActivity(shareIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Uses the ShareCompat Intent builder to create our Forecast intent for sharing.  All we need
     * to do is set the type, text and the NEW_DOCUMENT flag so it treats our share as a new task.
     * See: http://developer.android.com/guide/components/tasks-and-back-stack.html for more info.
     *
     * @return the Intent to use to share our weather forecast
     */
    private Intent createShareForecastIntent() {
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mForecastSummary + FORECAST_SHARE_HASHTAG)
                .getIntent();
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        return shareIntent;
    }

//  TODO (22) Override onCreateLoader
//          TODO (23) If the loader requested is our detail loader, return the appropriate CursorLoader
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");
        switch(id){
            case DETAIL_LOADER_ID:

                String selection = null;
                String[] selectionArgs = null;
                String sortOrder = null;

                Log.d(TAG, "mUri: " + mUri.toString());
                return new CursorLoader(
                        this,
                        mUri,
                        DETAIL_FORECAST_PROJECTION,
                        selection,
                        selectionArgs,
                        sortOrder
                );
            default:
                throw new RuntimeException("Loader NOT implemented: " + id);
        }
    }

    //  TODO (24) Override onLoadFinished
//      TODO (25) Check before doing anything that the Cursor has valid data
//      TODO (26) Display a readable data string
//      TODO (27) Display the weather description (using SunshineWeatherUtils)
//      TODO (28) Display the high temperature
//      TODO (29) Display the low temperature
//      TODO (30) Display the humidity
//      TODO (31) Display the wind speed and direction
//      TODO (32) Display the pressure
//      TODO (33) Store a forecast summary in mForecastSummary
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinisihed");
        if(data == null
//                || data.getCount() == 0
                || !data.moveToFirst()
                ){
            return;
        }

//        data.moveToFirst();
        long date = data.getLong(INDEX_WEATHER_DATE);
        Log.d(TAG,"date: " + date);
        Log.d(TAG,"date - SunshineDateUtils.getFriendlyDateString(this, date, false): " + SunshineDateUtils.getFriendlyDateString(this, date, false));
        Log.d(TAG,"date - SunshineDateUtils.getFriendlyDateString(this, date, true): " + SunshineDateUtils.getFriendlyDateString(this, date, true));

        //Get the values from the cursor (=from data)
        int weatherId = data.getInt(INDEX_WEATHER_ID);
        String description = SunshineWeatherUtils.getStringForWeatherCondition(this, weatherId);
        Log.d(TAG,"weatherId: " + weatherId);
        Log.d(TAG,"description: " + description);

        //NOTA: Este meteodo formatTemperature es el que se esta encargando de cambiar entre metric
        // y imperial (las unidades de la temperatura q se cambian en Settings). Asi q en realidad,
        // cuando estamos en DetailActivity y cambiamos las unidades en "settings", al dar al boton
        // de volver y volver a DetailActivity NO ESTA HACIENDO NUEVA CONSULTA NI SE HA
        // RECARGADO EL Cursor, sino q los datos son los mismos pero el metodo formatWeather
        // cambia lo q muestra!!!
        double high = data.getDouble(INDEX_WEATHER_MAX_TEMP);
        String highStr = SunshineWeatherUtils.formatTemperature(this, high);
        Log.d(TAG,"high: " + high);
        Log.d(TAG,"highStr: " + highStr);
        double low = data.getDouble(INDEX_WEATHER_MIN_TEMP);
        String lowStr = SunshineWeatherUtils.formatTemperature(this, low);
        Log.d(TAG,"low: " + low);
        Log.d(TAG,"lowStr: " + lowStr);

        float humidity = data.getFloat(INDEX_WEATHER_HUMIDITY);
        String humidityString = getString(R.string.format_humidity, humidity);
        Log.d(TAG,"humidity: " + humidity);
        Log.d(TAG,"humidityString: " + humidityString);

        float windSpeed = data.getFloat(INDEX_WEATHER_WIND_SPEED);
        float windDirection = data.getFloat(INDEX_WEATHER_WIND_DIRECTION);
        String windString = SunshineWeatherUtils.getFormattedWind(this, windSpeed, windDirection);
        Log.d(TAG,"windSpeed: " + windSpeed);
        Log.d(TAG,"windDirection: " + windDirection);
        Log.d(TAG,"windString: " + windString);

        /*
         * Format the pressure text using string resources. The reason we directly access
         * resources using getString rather than using a method from SunshineWeatherUtils as
         * we have for other data displayed in this Activity is because there is no
         * additional logic that needs to be considered in order to properly display the
         * pressure.
         */
        float pressure = data.getFloat(INDEX_WEATHER_PRESSURE);
        String pressureString = getString(R.string.format_pressure, pressure);
        Log.d(TAG,"pressure: " + pressure);
        Log.d(TAG,"pressureString: " + pressureString);

        //Set the TextViews
        mTextViewDate.setText(SunshineDateUtils.getFriendlyDateString(this, date, true));
        mTextViewDescription.setText(description);
        mTextViewHigh.setText(highStr);
        mTextViewLow.setText(lowStr);
        mTextViewHumidity.setText(humidityString);
        mTextViewWind.setText(windString);
        mTextViewPressure.setText(pressureString);

        mForecastSummary =
                SunshineDateUtils.getFriendlyDateString(this, date, false)
                + " - " + description
                + " - " + highStr
                + " - " + lowStr
                + " - " + humidityString
                + " - " + windString
                + " - " + pressureString
            ;

//        COMPLETED (33) Store a forecast summary in mForecastSummary
//        /* Store the forecast summary String in our forecast summary field to share later */
//        mForecastSummary = String.format("%s - %s - %s/%s",
//                dateText, description, highString, lowString);
    }

//  TODO (34) Override onLoaderReset, but don't do anything in it yet
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset");
    }

}