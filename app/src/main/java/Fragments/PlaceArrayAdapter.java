package Fragments;

/**
 * Created by jon92 on 1/4/2017.
 */

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class PlaceArrayAdapter
        extends ArrayAdapter<PlaceArrayAdapter.PlaceAutocomplete> implements Filterable {
    private static final String TAG = "PlaceArrayAdapter";
    private GoogleApiClient mGoogleApiClient;
    private AutocompleteFilter mPlaceFilter;
    private LatLngBounds mBounds;
    private ArrayList<PlaceAutocomplete> mResultList = new ArrayList<PlaceAutocomplete>();

    /**
     * Constructor
     *
     * @param context
     * @param resource
     * @param bounds
     * @param filter
     */
    public PlaceArrayAdapter(Context context, int resource, LatLngBounds bounds,
                             AutocompleteFilter filter) {
        super(context, resource);
        mBounds = bounds;
        mPlaceFilter = filter;
    }

    /**
     * This methods set the google API client
     * @param googleApiClient
     */
    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        if (googleApiClient == null || !googleApiClient.isConnected()) {
            mGoogleApiClient = null;
        } else {
            mGoogleApiClient = googleApiClient;
        }
    }

    /**
     * This method returns the size of mResultList
     * @return
     */
    @Override
    public int getCount() {
        return mResultList.size();
    }

    /**
     * Returns the item at the specified position in the mResultList
     * @param position
     * @return
     */
    @Override
    public PlaceAutocomplete getItem(int position) {
        return mResultList.get(position);
    }

    /**
     * This method gets prediction based on the text input
     * using the input the method will get prediction from the google place API
     * It will then wait up to 60s for the prediction to be done
     * If prediction is unsuccessful, it will return a NULL value and print a log message saying prediction has failed
     * Else it will return the arraylist which contains up to 5 elements of the prediction
     * @param constraint refers to the input text
     * @return
     */
    private ArrayList<PlaceAutocomplete> getPredictions(CharSequence constraint) {
        if (mGoogleApiClient != null) {
            Log.i(TAG, "Executing autocomplete query for: " + constraint);
            PendingResult<AutocompletePredictionBuffer> results =
                    Places.GeoDataApi
                            .getAutocompletePredictions(mGoogleApiClient, constraint.toString(),
                                    mBounds, mPlaceFilter);
            // Wait for predictions, set the timeout.
            AutocompletePredictionBuffer autocompletePredictions = results
                    .await(60, TimeUnit.SECONDS);
            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
                //Toast.makeText(getContext(), "Error: " + status.toString(),
                        //Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error getting place predictions: " + status
                        .toString());
                autocompletePredictions.release();
                return null;
            }

            Log.i(TAG, "Query completed. Received " + autocompletePredictions.getCount()
                    + " predictions.");
            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
            ArrayList resultList = new ArrayList<>(autocompletePredictions.getCount());
            while (iterator.hasNext()) {
                AutocompletePrediction prediction = iterator.next();
                resultList.add(new PlaceAutocomplete(prediction.getPlaceId(),
                        prediction.getFullText(null)));
            }
            // Buffer release
            autocompletePredictions.release();
            return resultList;
        }
        Log.e(TAG, "Google API client is not connected.");
        return null;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            /**
             * This methods filters out the resultlist based on the constraint
             * if constraint is Null, it will return a null value
             * @param constraint refers to the input text
             * @return
             */
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint != null) {
                    // Query the autocomplete API for the entered constraint
                    mResultList = getPredictions(constraint);
                    if (mResultList != null) {
                        // Results
                        results.values = mResultList;
                        results.count = mResultList.size();
                    }
                }
                return results;
            }

            /**
             * This method updates the dataset based on result from place API
             * IF API returns at least 1 result, update dataset
             * Else , invalidate the dataset
             * @param constraint
             * @param results
             */
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    // The API returned at least one result, update the data.
                    notifyDataSetChanged();
                } else {
                    // The API did not return any results, invalidate the data set.
                    if(mResultList!=null){
                        Log.i(TAG, "mResultList WORKSSSSS");
                        notifyDataSetInvalidated();
                    }else{
                        Log.i(TAG,"mResultList is null");
                    }

                }
            }
        };
        return filter;
    }

    public class PlaceAutocomplete {

        public CharSequence placeId;
        public CharSequence description;

        /**
         * Class constructor
         * @param placeId
         * @param description
         */
        public PlaceAutocomplete(CharSequence placeId, CharSequence description) {
            this.placeId = placeId;
            this.description = description;
        }

        /**
         * Return the string description
         * @return
         */
        @Override
        public String toString() {
            return description.toString();
        }
    }
}