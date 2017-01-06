/*
 * Copyright (C) 2016 The Android Open Source Project
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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.sunshine.ForecastAdapter.ForecastAdapterOnClickHandler;
import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;

import java.net.URL;

// TODO (1) Implement the proper LoaderCallbacks interface and the methods of that interface
public class MainActivity extends AppCompatActivity
        implements ForecastAdapterOnClickHandler, LoaderManager.LoaderCallbacks<String[]> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int FORECAST_LOADER_ID = 73;
    private static final String LOCATION_EXTRA = "location";

    private RecyclerView mRecyclerView;
    private ForecastAdapter mForecastAdapter;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_forecast);

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        /*
         * LinearLayoutManager can support HORIZONTAL or VERTICAL orientations. The reverse layout
         * parameter is useful mostly for HORIZONTAL layouts that should reverse for right to left
         * languages.
         */
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);

        /*
         * The ForecastAdapter is responsible for linking our weather data with the Views that
         * will end up displaying our weather data.
         */
        mForecastAdapter = new ForecastAdapter(this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mForecastAdapter);

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        // TODO (7) Remove the code for the AsyncTask and initialize the AsyncTaskLoader
        /* Once all of our views are setup, we can load the weather data. */
        Log.d(TAG, "onCreate : before initLoader(...)");
        String location = SunshinePreferences.getPreferredWeatherLocation(this);
        Bundle bundle = new Bundle();
        bundle.putString(LOCATION_EXTRA, location);

        getSupportLoaderManager().initLoader(FORECAST_LOADER_ID, bundle, this);
//        loadWeatherData();
        Log.d(TAG, "onCreate : after initLoader(...)");
    }

    /**
     * This method will get the user's preferred location for weather, and then tell some
     * background method to get the weather data in the background.
     */
    private void loadWeatherData() {
        showWeatherDataView();

        String location = SunshinePreferences.getPreferredWeatherLocation(this);
        Bundle bundle = new Bundle();
        bundle.putString(LOCATION_EXTRA, location);

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String[]> loader = loaderManager.getLoader(FORECAST_LOADER_ID);
        if(loader == null){
            //Nota mia: Por lo q veo, aqui nunca entra, ya que, al arrancar la app,
            // onCreate ejecuta loaderManager.initLoader(FORECAST_LOADER_ID, null, ..), asi q al dar al
            // boton de buscar se ejecuta este metodo pero loader ya no es null,
            // asi q en este metodo nunca es null!!
            Log.d(TAG, "loadWeatherData : before initLoader(...)");
            loaderManager.initLoader(FORECAST_LOADER_ID, bundle, this);
            Log.d(TAG, "loadWeatherData : after initLoader(...)");
        } else {
            Log.d(TAG, "loadWeatherData : before restartLoader(...)");
            loaderManager.restartLoader(FORECAST_LOADER_ID, bundle, this);
            Log.d(TAG, "loadWeatherData : after restartLoader(...)");
        }
//        new FetchWeatherTask().execute(location);
    }

    // TODO (2) Within onCreateLoader, return a new AsyncTaskLoader that looks a lot like the existing FetchWeatherTask.
    // TODO (3) Cache the weather data in a member variable and deliver it in onStartLoading.

    // TODO (4) When the load is finished, show either the data or an error message if there is no data

    /**
     * This method is overridden by our MainActivity class in order to handle RecyclerView item
     * clicks.
     *
     * @param weatherForDay The weather for the day that was clicked
     */
    @Override
    public void onClick(String weatherForDay) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, weatherForDay);
        startActivity(intentToStartDetailActivity);
    }

    /**
     * This method will make the View for the weather data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showWeatherDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the weather
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<String[]> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String[]>(this) {
            String[] mData;

            @Override
            protected void onStartLoading() {
                Log.d(TAG, "onCreateLoader - AsyncTaskLoader - onStartLoading : Starting");
                if(args == null){
                    Log.d(TAG, "onCreateLoader - AsyncTaskLoader - onStartLoading : args == null -> returning");
                    return;
                }

                mLoadingIndicator.setVisibility(View.VISIBLE);

                if(mData == null){
                    Log.d(TAG, "onCreateLoader - AsyncTaskLoader - onStartLoading : before forceLoad()");
                    forceLoad();
                    Log.d(TAG, "onCreateLoader - AsyncTaskLoader - onStartLoading : after forceLoad()");
                } else {
                    Log.d(TAG, "onCreateLoader - AsyncTaskLoader - onStartLoading : before deliverResult()");
                    deliverResult(mData);
                    Log.d(TAG, "onCreateLoader - AsyncTaskLoader - onStartLoading : after deliverResult()");
                }
                //TODO: En principio no hay q llamar a super.onStartLoading(), pero no se si daria igual llamarlo... Comprobarlo.
                //super.onStartLoading();
            }

            @Override
            public String[] loadInBackground() {
                Log.d(TAG, "onCreateLoader - AsyncTaskLoader - loadInBackground : Starting");
                String location = args.getString(LOCATION_EXTRA);
                if(location == null || TextUtils.isEmpty(location)){
                    Log.d(TAG, "onCreateLoader - AsyncTaskLoader - loadInBackground :  queryUrlString (in args) == null -> returning");
                    return null;
                }

                URL weatherRequestUrl = NetworkUtils.buildUrl(location);

                try {
                    String jsonWeatherResponse = NetworkUtils
                            .getResponseFromHttpUrl(weatherRequestUrl);

                    String[] simpleJsonWeatherData = OpenWeatherJsonUtils
                            .getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);

                    return simpleJsonWeatherData;

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(String[] data) {
                Log.d(TAG, "onCreateLoader - AsyncTaskLoader - deliverResult : Starting");
                //NOTA MIA: Esto lo hacemos para que cuando hemos buscado algo y cambiamos de app
                // y volvemos a esta app NO vuelva a hacer peticion de los datos (sino que reutilice
                // los datos q ya habia buscado antes),
                // y por lo q veo, basta con que guarde cualquier cosa a modo de flag q indique
                // q ya se ha buscado, por ejemplo basta con q aqui guardara cualquier cosa
                // en mData, simplemente para que en onStartLoading(..) no entre en el else
                // q ejecuta el forceLoad() q es lo q llama a loadInBackGround(..)
                // (Si se vuelve a pinchar el boton de search eso llama a
                // loaderManager.restartLoader(FORECAST_LOADER_ID, ..) q se encarga de resetear
                // el loader y asi se vuelve a hacer el forceLoad() y ejecutar la peticion de datos,
                // asi q no hay problema).
                //
                // De hecho, ni siquiera es necesario hacerlo en este metodo, sino que si
                // en loadInBackGround(..) guardo el flag (o sea guardo cualquier cosa
                // en mData, tambien valdria!!
                // Lo dejo aqui, pq me parece mas limpio el codigo, para separar cosas,
                // y tb por ocultar el progressBar (mLoadingIndicator)
                mData = data;

                //NOTA: Añado esto pq progressBar se quedaba visible
                mLoadingIndicator.setVisibility(View.INVISIBLE);

                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String[]> loader, String[] data) {
        Log.d(TAG, "onLoadFinished :  starting");

        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (data != null) {
            showWeatherDataView();
            mForecastAdapter.setWeatherData(data);
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<String[]> loader) {
        Log.d(TAG, "onLoaderReset :  starting");
        //No hay nada q hacer aqui ahora mismo (pero debe estar aqui implementado este metodo.
    }

/*
    // TODO (6) Remove any and all code from MainActivity that references FetchWeatherTask
    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[] doInBackground(String... params) {

            */
/* If there's no zip code, there's nothing to look up. *//*

            if (params.length == 0) {
                return null;
            }

            String location = params[0];
            URL weatherRequestUrl = NetworkUtils.buildUrl(location);

            try {
                String jsonWeatherResponse = NetworkUtils
                        .getResponseFromHttpUrl(weatherRequestUrl);

                String[] simpleJsonWeatherData = OpenWeatherJsonUtils
                        .getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);

                return simpleJsonWeatherData;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] weatherData) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (weatherData != null) {
                showWeatherDataView();
                mForecastAdapter.setWeatherData(weatherData);
            } else {
                showErrorMessage();
            }
        }
    }
*/

    /**
     * This method uses the URI scheme for showing a location found on a
     * map. This super-handy intent is detailed in the "Common Intents"
     * page of Android's developer site:
     *
     * @see <a"http://developer.android.com/guide/components/intents-common.html#Maps">
     *
     * Hint: Hold Command on Mac or Control on Windows and click that link
     * to automagically open the Common Intents page
     */
    private void openLocationInMap() {
        String addressString = "1600 Ampitheatre Parkway, CA";
        Uri geoLocation = Uri.parse("geo:0,0?q=" + addressString);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(TAG, "Couldn't call " + geoLocation.toString()
                    + ", no receiving apps installed!");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.forecast, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // TODO (5) Refactor the refresh functionality to work with our AsyncTaskLoader
        if (id == R.id.action_refresh) {
            mForecastAdapter.setWeatherData(null);
            loadWeatherData();
            return true;
        }

        if (id == R.id.action_map) {
            openLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}