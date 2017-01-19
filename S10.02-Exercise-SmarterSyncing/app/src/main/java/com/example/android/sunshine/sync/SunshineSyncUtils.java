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
package com.example.android.sunshine.sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.sunshine.data.WeatherContract;


public class SunshineSyncUtils {

    private static final String TAG = SunshineSyncUtils.class.getSimpleName();

//  TODO (1) Declare a private static boolean field called sInitialized
    private static boolean sInitialized = false;
//    private static Context sContext = null;

    //  TODO (2) Create a synchronized public static void method called initialize
    synchronized public static void initialize(Context context) {
//        sContext = context;
        Log.d(TAG, "initialize -> sInitialized = " + sInitialized);
        if(!sInitialized) {
            Log.d(TAG, "initialize -> NOT yet initialized (so !sInitialized)");
            //  TODO (3) Only execute this method body if sInitialized is false
            //  TODO (4) If the method body is executed, set sInitialized to true
            //  TODO (5) Check to see if our weather ContentProvider is empty

            new CheckContentProviderAsyncTask().execute(context);

            sInitialized = true;
        }
    }

    /**
     * Helper method to perform a sync immediately using an IntentService for asynchronous
     * execution.
     *
     * @param context The Context used to start the IntentService for the sync.
     */
    public static void startImmediateSync(@NonNull final Context context) {
        Log.d(TAG, "startImmediateSync");
        Intent intentToSyncImmediately = new Intent(context, SunshineSyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }

//    public static class aCheckContentProviderAsyncTask extends AsyncTask<Void, Void, Void>{
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            return null;
//        }
//    }

    public static class CheckContentProviderAsyncTask extends AsyncTask<Context, Void, Cursor>{

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute");
            super.onPreExecute();
        }

        @Override
        protected Cursor doInBackground(Context... params) {
            Log.d(TAG, "doInBackground");
            if (params.length == 0) {
                return null;
            }

            Context context = params[0];

            String[] projectionColumns = {WeatherContract.WeatherEntry._ID};
            String selectionStatement = WeatherContract.WeatherEntry
                    .getSqlSelectForTodayOnwards();

            Cursor cursor = context.getContentResolver().query(
                    WeatherContract.WeatherEntry.CONTENT_URI,
                    projectionColumns,
                    selectionStatement,
                    null,
                    null
            );
            Log.d(TAG, "doInBackground -> cursor = " + cursor);

            //Aqui pongo lo q habia hecho primero en onPostExecute (lo meto aqui mejor directamente)
            if(cursor == null || cursor.getCount() == 0){
                Log.d(TAG, "doInBackground -> cursor == null or is empty, so we start sync now");
                //  TODO (6) If it is empty or we have a null Cursor, sync the weather now!
                startImmediateSync(context);
            }

            /* Make sure to close the Cursor to avoid memory leaks! */
            if(cursor != null) cursor.close();

            return cursor;
        }

//        @Override
//        protected void onPostExecute(Cursor cursor) {
//            Log.d(TAG, "onPostExecute");
//            Log.d(TAG, "onPostExecute -> cursor = " + cursor);
//
//            if(cursor == null || cursor.getCount() == 0){
//                Log.d(TAG, "onPostExecute -> cursor == null or is empty, so we start sync now");
//                //  TODO (6) If it is empty or we have a null Cursor, sync the weather now!
//                startImmediateSync(sContext);
//            }
//
//            /* Make sure to close the Cursor to avoid memory leaks! */
//            //NOTA MIA: No se si este cursor apunta al mismo cursor exacto que esta en
//            // el background thread.... asi que igual es mejor poner esto en doInBackground
//            // directamente y ahi cerrar cursor para q no se quede ningun cursor sin cerrar...
//            //NOTA: PArece/Creo que si que es el mismo objeto cursor (lo he sacado por el LogCat)...
//            // pero por si acaso igual es mejor hacerlo en doInBackground (ademas de mas rapido por
//            // no tener q implementar tb onPostExecute y ademas no se necesita tener static sContext)
//            if(cursor != null) cursor.close();
//        }
    }
}