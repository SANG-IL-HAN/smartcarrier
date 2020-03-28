package com.example.hsi33.smartcarrier0to100;

import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class GpsLocation extends AppCompatActivity implements OnMapReadyCallback {

    double latitude, longitude;
    String carrierName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        carrierName = "test";

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment)fragmentManager
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        HttpPostAsyncTask send = new HttpPostAsyncTask(map);
        String [] params = {"http://10.0.2.2:8000/gps/my/", carrierName};
        send.execute(params);


        /*
        MarkerOptions markerOptions = new MarkerOptions();
        LatLng SEOUL = new LatLng(37.56, 126.97);
        markerOptions.position(SEOUL);
        markerOptions.title("서울");
        markerOptions.snippet("한국의 수도");

        map.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
        map.animateCamera(CameraUpdateFactory.zoomTo(10));
        */
    }

    private class HttpPostAsyncTask extends AsyncTask<String, Void, String> {
        GoogleMap map;

        public HttpPostAsyncTask(GoogleMap map) {
            this.map = map;
        }

        @Override
        protected String doInBackground(String... params) {
            String url_str = params[0];
            String name = params[1];

            String result = "";
            HttpURLConnection conn = null;

            URL url = null;
            try {
                url = new URL(url_str);
                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content_Type", "application/json");

                JSONObject data = new JSONObject();
                data.accumulate("name", name);

                OutputStream os = conn.getOutputStream();
                os.write(data.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
                    return null;

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                String line;

                while((line = reader.readLine()) != null){
                    result += line;
                }

                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally{
                if(conn != null)
                    conn.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            Log.d("result", result);

            try {
                JSONObject resultJson = new JSONObject(result);
                latitude = resultJson.getDouble("latitude");
                longitude = resultJson.getDouble("longitude");
                LatLng myCarrier = new LatLng(latitude, longitude);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(myCarrier);
                markerOptions.title("Carrier Position");
                markerOptions.snippet(carrierName);
                map.addMarker(markerOptions);

                map.moveCamera(CameraUpdateFactory.newLatLng(myCarrier));
                map.animateCamera(CameraUpdateFactory.zoomTo(15));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}