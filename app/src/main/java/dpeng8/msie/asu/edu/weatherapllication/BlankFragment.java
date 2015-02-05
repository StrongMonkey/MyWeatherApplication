package dpeng8.msie.asu.edu.weatherapllication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Encapsulates fetching the forecast and displaying it as a {@link ListView} layout.
 */
public class BlankFragment extends Fragment {
    public static final String Log_Tag = "BlankFragment";
    private ArrayAdapter<String> mForecastAdapter;
    private View rootView;
    public BlankFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(Log_Tag,"?????????");
        updateWeather();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // The ArrayAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mForecastAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_forecast, // The name of the layout ID.
                        R.id.list_item_weather, // The ID of the textview to populate.
                        new ArrayList<String>());
        Log.d(Log_Tag,"!!!!!!!!!!!!");

        rootView = inflater.inflate(R.layout.fragment_blank, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.list_item_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String forecast = mForecastAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void updateWeather() {
        FetchWeatherTaskFromApi weatherTask = new FetchWeatherTaskFromApi(getActivity());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(getString(R.string.Location_key),
                getString(R.string.default_location));
        TextView textView = (TextView) rootView.findViewById(R.id.location);
        textView.setText(location);
        if(location == "zappos"){
            weatherTask.execute("Las Vegas");
        }
        else {
            weatherTask.execute(location);
        }
    }
    public class FetchWeatherTaskFromApi extends AsyncTask<String,Void,String[]> {
        // NameTag for this class
        public final String LOG_TAG = FetchWeatherTaskFromApi.class.getSimpleName();
        public final Context mContext;
        //Constructor
        public FetchWeatherTaskFromApi(Context context){
            mContext = context;
        }

        @Override
        protected String[] doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String WeatherDataJSON = null;
            String FORMAT = "json";
            String units = "metric";
            int numDays = 7;
            try{
                //These are Strings which are used to query the right form of weather data
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                //request the url for the weather data
                Uri BuiltUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM,params[0])
                        .appendQueryParameter(FORMAT_PARAM,FORMAT)
                        .appendQueryParameter(UNITS_PARAM,units)
                        .appendQueryParameter(DAYS_PARAM,Integer.toString(numDays))
                        .build();
                URL url = new URL(BuiltUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer stringBuffer = new StringBuffer();
                if(inputStream == null){
                    WeatherDataJSON = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while((line = reader.readLine())!=null){
                    //read the data and put it into the stringBuffer
                    stringBuffer.append(line+"\n");
                }
                if(stringBuffer == null){
                    return null;
                }
                WeatherDataJSON = stringBuffer.toString();
            }
            catch(IOException e){
                //if the url is invalid, then check the url
                Log.e(LOG_TAG, "IOException error, check your url", e);
            }
            finally {
                if(urlConnection!=null){
                    urlConnection.disconnect();
                }
                if(reader!=null){
                    try {
                        reader.close();
                    }
                    catch(IOException e){
                        Log.e(LOG_TAG,"Error closing reader",e);
                    }
                }
            }
            try{
                String location_settings = Utility.getPreferredLocation(mContext);
                String[] forecastData = getWeatherDataFromJson(WeatherDataJSON, 7, location_settings);
                return forecastData;
            }
            catch(JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
                return null;
            }
        }


        public String[] getWeatherDataFromJson(String weatherData,int numDays,String location_settings) throws JSONException{
            // Location information
            final String OWM_CITY = "city";
            final String OWM_CITY_NAME = "name";
            final String OWM_COORD = "coord";
            final String OWM_COORD_LAT = "lat";
            final String OWM_COORD_LONG = "lon";

            final String name = "list";

            final String OWM_DATETIME = "dt";
            final String OWM_PRESSURE = "pressure";
            final String OWM_HUMIDITY = "humidity";
            final String OWM_WINDSPEED = "speed";
            final String OWM_WIND_DIRECTION = "deg";

            // All temperatures are children of the "temp" object.
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";

            final String OWM_WEATHER = "weather";
            final String OWM_DESCRIPTION = "main";
            final String OWM_WEATHER_ID = "id";

            JSONObject jsonObject = new JSONObject(weatherData);
            JSONArray weatherArray = jsonObject.getJSONArray(name);
            String[] resultStrs = new String[numDays];

            for(int i=0;i<weatherArray.length();i++){

                long dateTime;
                double pressure;
                int humidity;
                double windSpeed;
                double windDirection;

                double high;
                double low;

                String description;
                int weatherId;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);
                dateTime = dayForecast.getLong(OWM_DATETIME);

                pressure = dayForecast.getDouble(OWM_PRESSURE);
                humidity = dayForecast.getInt(OWM_HUMIDITY);
                windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
                windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

                JSONObject weatherObject =
                        dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                weatherId = weatherObject.getInt(OWM_WEATHER_ID);

                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                high = temperatureObject.getDouble(OWM_MAX);
                low = temperatureObject.getDouble(OWM_MIN);
                String highAndLow = formatHighLows(high, low);
                String day = getReadableDateString(dateTime);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }
            return resultStrs;
        }
        private String getReadableDateString(long time) {
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            Date date = new Date(time * 1000);
            SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
            return format.format(date).toString();
        }
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
            String units = pref.getString(mContext.getString(R.string.temperature_units_key), mContext.getString(R.string.default_unit));
            if (units.equals("1")) {
                high = high * 1.8 + 32;
                low = low * 1.8 + 32;
            }
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }
        /*
        After the background thread is done, add all the strings to the ArrayAdaptor
         */
        @Override
        protected void onPostExecute(String[] strings) {
            if (strings != null) {
                    mForecastAdapter.clear();
                for (String s : strings) {
                    mForecastAdapter.add(s);
                }
            }
        }
    }
    public  void getCalled(){
        FetchWeatherTaskFromApi weatherTask = new FetchWeatherTaskFromApi(getActivity());
        String location = "Las Vegas";
        TextView textView = (TextView) rootView.findViewById(R.id.location);
        textView.setText(location);
        weatherTask.execute("Zappos");
    }
}
