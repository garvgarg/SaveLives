package com.parse.loginsample.withdispatchactivity;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;


public class PushReceiver extends ParsePushBroadcastReceiver {

    @Override
    public void onPushOpen(Context context, Intent intent) {
        Log.e("Push", "Clicked");
        Intent i = new Intent(context, PushNotificationActivity.class);
        try {
            String action = intent.getAction();
            String channel = intent.getExtras().getString("com.parse.Channel");
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));

            System.out.println("Got action " + action + " on channel " + channel + " with:");
            Iterator itr = json.keys();
            while (itr.hasNext()) {
              String key = (String) itr.next();
              System.out.println("..." + key + " => " + json.getString(key));
            }
            
            String message = json.getString("location");
            i.putExtra("address", message);
            
            String lat = json.getString("latitude");
            i.putExtra("Latitude", lat);
            
            String lng = json.getString("longitude");
            i.putExtra("Longitude", lng);
            
            String street = json.getString("street");
            i.putExtra("street", street);
            
            String city = json.getString("city");
            i.putExtra("city", city);
            
            String state = json.getString("state");
            i.putExtra("state", state);
            
            String zip = json.getString("zip");
            i.putExtra("zip", zip);
            
            String country = json.getString("country");
            i.putExtra("country", country);
            
            String p = json.getString("phone");
            i.putExtra("phone", p);
            
            
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            
          } catch (JSONException e) {
            System.out.println("JSONException: " + e.getMessage());
          }
    }
}
