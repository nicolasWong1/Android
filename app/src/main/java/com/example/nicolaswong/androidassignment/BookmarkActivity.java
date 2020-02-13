package com.example.nicolaswong.androidassignment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.*;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.*;

import java.util.*;
import cz.msebera.android.httpclient.Header;

public class BookmarkActivity extends AppCompatActivity {
    private DB database;
    ListView list;
    ArrayList<String> bookmark = new ArrayList<String>();
    String apiKey = "25906a2aa1477e44a11abe1481f3698e";
    String apiUrl = "http://api.openweathermap.org/data/2.5/";
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_bookmark);
        try {
            writeDataToList();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        list  = (ListView) findViewById(R.id.bookmark);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String string = bookmark.get(position);
                String replace = string.replace("Place: ","");
                String[] sli = replace.split("\n");
                dialog(sli[0]);
            }
        });
    }

    public void writeDataToList() throws JSONException {
        list = findViewById(R.id.bookmark);
        database = new DB(this);
        String query = "SELECT  * FROM weather ORDER BY id DESC";

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
//                bookmark.add(cursor.getString(1));
                getWeather(cursor.getString(1));

            } while (cursor.moveToNext());
        }
        database.close();
    }

    public void getWeather(String name)throws JSONException{
        AsyncHttpClient client = new AsyncHttpClient();
        String Url = apiUrl + "weather?q="+ name + "&appid=" + apiKey + "&units=metric";
        client.get(Url, null ,new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String name = response.getString("name");
                    String weather = response.getJSONArray("weather").getString(0);
                    JSONObject Jweather = new JSONObject(weather);
                    String desc = Jweather.getString("description");
                    String mintemp = response.getJSONObject("main").getString("temp_min");
                    String maxtemp = response.getJSONObject("main").getString("temp_max");
                    String country = response.getJSONObject("sys").getString("country");
                    String hum = response.getJSONObject("main").getString("humidity");
                    String pres = response.getJSONObject("main").getString("pressure");

                    String[] data = new String[]{"Place: "+name+ "\nWeather: "+desc+
                            "\nTemperature: "+mintemp+"-"+maxtemp+"°C"+
                            "\nCountry: "+country+ "\nHumidity: "+hum+ "Pressure: "+pres};

                    Collections.addAll(bookmark,data);

                    addToListView();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void addToListView(){
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, bookmark);
        list.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void dialog(final String index){
        final AlertDialog.Builder builder = new AlertDialog.Builder(BookmarkActivity.this);
        builder.setTitle("Delete record?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("PO",index);
                        database.deleteData(index);
                        finish();
                        startActivity(getIntent());
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
