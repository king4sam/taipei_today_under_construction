package necisam.kk_map;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    String[] aArr = null;
    ArrayList<String> allinfo = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityManager cm  = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ninfo = cm.getActiveNetworkInfo();
        if(ninfo!=null && ninfo.isConnected()){

            final RequestQueue queue = Volley.newRequestQueue(this);
            final String opentaipei ="http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=201d8ae8-dffc-4d17-ae1f-e58d8a95b162";

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, opentaipei,
                    new Response.Listener<String>() {
                        ArrayList<String> tmp = new ArrayList<String>();
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jo = new JSONObject(response);
                                JSONObject re = new JSONObject(jo.getString("result"));
                                JSONArray ja = re.getJSONArray("results");
                                for(int i = 0 ; i < ja.length();i++){
                                    allinfo.add(ja.getJSONObject(i).toString());
                                    JSONObject repair = ja.getJSONObject(i);
                                    tmp.add(repair.getString("ADDR"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            int layoutId = android.R.layout.simple_list_item_1;
                            aArr = new String[tmp.size()];
                            aArr = tmp.toArray(aArr);
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, layoutId, aArr);
                            ListView item_list = (ListView) findViewById(R.id.listView);
                            item_list.setAdapter(adapter);
                            item_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                @Override
                                public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                                    AlertDialog.Builder d = new AlertDialog.Builder(MainActivity.this);
                                    try {
                                        StringBuffer sb = new StringBuffer();
                                        JSONObject i = new JSONObject(MainActivity.this.allinfo.get(position));
                                        sb.append("_id : " + i.getString("_id") + "\n");
                                        sb.append("核准施工起日 : " + i.getString("CB_DA") + "\n");
                                        sb.append("核准施工迄日 : " + i.getString("CE_DA") + "\n");
                                        sb.append("施工時段 : " + i.getString("CO_TI") + "\n");
                                        sb.append("挖掘目的 : " + i.getString("NPURP") + "\n");
                                        sb.append("施工位置 : " + i.getString("ADDR") + "\n");
                                        d.setTitle("more information")
                                                .setMessage(sb.toString())
                                                .show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

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
    }

    public void changeToMap(View view){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
}
