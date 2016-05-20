package necisam.kk_map;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    HashMap<String,String> result = new HashMap<String,String>();
    private GoogleMap mMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    class MyResponseListener implements Response.Listener<String> {
        String id;
        public MyResponseListener(String id){
            this.id = id;
        }
        @Override
        public void onResponse(String response) {
            JSONObject j = null;
            try {
                j = new JSONObject(response);
                if(j.getString("status").equals("OK")){
                    JSONArray ja = j.getJSONArray("results");
                    JSONObject first = ja.getJSONObject(0);
                    JSONObject geo = new JSONObject(first.getString("geometry"));
                    JSONObject lo = new JSONObject(geo.getString("location"));
                    double lat = lo.getDouble("lat");
                    double lng = lo.getDouble("lng");
                    LatLng sydney = new LatLng(lat, lng);
                    JSONObject ha = new JSONObject(MapsActivity.this.result.get(id));
                    mMap.addMarker(new MarkerOptions().position(sydney).title(ha.getString("ADDR")).snippet(ha.getString("APP_NAME") + "\nfor: "+ ha.getString("NPURP")));
                    System.out.println("at latlng" + lat +" , " + lng);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        ConnectivityManager cm  = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ninfo = cm.getActiveNetworkInfo();
        if(ninfo!=null && ninfo.isConnected()){
            System.out.println("network connected");

            final RequestQueue queue = Volley.newRequestQueue(this);
            final String opentaipei ="http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=201d8ae8-dffc-4d17-ae1f-e58d8a95b162";
            final String ggeo = "https://maps.googleapis.com/maps/api/geocode/json?address=";
            final String geokey = "GEOKEY";
            final String mykey = "&key="+geokey;
            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, opentaipei,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jo = new JSONObject(response);
                                JSONObject re = new JSONObject(jo.getString("result"));
                                System.out.println("result + " + jo.getString("result"));
                                JSONArray ja = re.getJSONArray("results");
                                for(int i = 0 ; i < ja.length();i++){
                                    JSONObject repair = ja.getJSONObject(i);
                                    MapsActivity.this.result.put(repair.getString("_id"),repair.toString());
                                    System.out.println("ADDR " + repair.getString("ADDR"));

                                    StringRequest stringRequest = new StringRequest(Request.Method.GET, ggeo+ URLEncoder.encode(repair.getString("ADDR").split("、")[0], "UTF-8")+mykey,
                                            new MyResponseListener(repair.getString("_id")), new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            error.printStackTrace();
                                        }
                                    });
                                    queue.add(stringRequest);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
            queue.add(stringRequest);
        }
        else{
            System.out.println("no network connected");
        }
        try {
            //move the camera to Taipei,zoom
            LatLng taippei = new LatLng(25.0191563,121.534134);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(taippei));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(12) );
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
