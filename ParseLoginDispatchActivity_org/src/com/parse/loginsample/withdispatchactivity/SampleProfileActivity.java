/*
 *  Copyright (c) 2014, Facebook, Inc. All rights reserved.
 *
 *  You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 *  copy, modify, and distribute this software in source code or binary form for use
 *  in connection with the web services and APIs provided by Facebook.
 *
 *  As with any software that integrates with the Facebook platform, your use of
 *  this software is subject to the Facebook Developer Principles and Policies
 *  [http://developers.facebook.com/policy/]. This copyright notice shall be
 *  included in all copies or substantial portions of the software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.parse.loginsample.withdispatchactivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import android.content.Context;

import com.parse.FunctionCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SaveCallback;

/**
 * Shows the user profile. This simple activity can only function when there is a valid
 * user, so we must protect it with SampleDispatchActivity in AndroidManifest.xml.
 */
public class SampleProfileActivity extends Activity implements
            GooglePlayServicesClient.ConnectionCallbacks,
            GooglePlayServicesClient.OnConnectionFailedListener {
  private TextView titleTextView;
  private TextView emailTextView;
  private TextView nameTextView;
  private LocationClient locationclient;
  private String emergencyLocation;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);
    titleTextView = (TextView) findViewById(R.id.profile_title);
    emailTextView = (TextView) findViewById(R.id.profile_email);
    nameTextView = (TextView) findViewById(R.id.profile_name);
    titleTextView.setText(R.string.profile_title_logged_in);
      
    int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    if (resp == ConnectionResult.SUCCESS) {
        locationclient = new LocationClient(this,this,this);
        locationclient.connect();
    } else {
        Toast.makeText(this,"Google Play Service Error " + resp,Toast.LENGTH_LONG).show();
    }
    
    // Collect coordinates in case of emergency and send help request
    findViewById(R.id.button1).setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
            Location loc = null;
            if (locationclient != null && locationclient.isConnected()) {
                    loc = locationclient.getLastLocation();
                    emergencyLocation = String.format("Emergency GPS Location :" + loc.getLatitude() + "," + loc.getLongitude());
                    System.out.println(emergencyLocation);
            }
            
            HashMap<String, Object> params = new HashMap<String, Object>();
            Address a = getAddress(getApplicationContext(), loc);
            String addressText = String.format(
                    "%s, %s, %s - %s, %s",
                        // If there's a street address, add it
                        a.getMaxAddressLineIndex() > 0 ? a.getAddressLine(0) : "",
                        // Locality is usually a city
                        a.getLocality(),
                        a.getAdminArea(),
                        a.getPostalCode(),
                        // The country of the address
                        a.getCountryCode());
            System.out.println("String address: " + addressText);
            params.put("address", addressText);
            // parameters for parse cloud code
            params.put("lat", loc.getLatitude());
            params.put("lng", loc.getLongitude());
            params.put("street", (a.getMaxAddressLineIndex() > 0 ? a.getAddressLine(0) : "").toString());
            params.put("city", a.getLocality().toString());
            params.put("state", a.getAdminArea().toString());
            params.put("zip", a.getPostalCode().toString());
            params.put("country", a.getCountryCode().toString());
            ParseUser user = ParseUser.getCurrentUser();
            //System.out.println(user.getObjectId());
            //System.out.println(user.getString("phone"));
            params.put("userid", user.getObjectId());
            params.put("phone", user.getString("phone"));
           
            String message = addressText + " " + emergencyLocation;
            String emergencyContactPhoneNumber = user.getString("emergencyContact");
            
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(emergencyContactPhoneNumber, null, message, null, null);
                Toast.makeText(getApplicationContext(), "SMS sent to Emergency Contact",
                Toast.LENGTH_LONG).show();
             } catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                "Emergency SMS faild, please try again.",
                Toast.LENGTH_LONG).show();
                e.printStackTrace();
             }
            
            ParseCloud.callFunctionInBackground("needhelp", params, new FunctionCallback<String>() {
               public void done(String result, ParseException e) {
                   if (e == null) {
                       System.out.println(result);
                       Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                   } else {
                       System.out.println("No result found");
                   }
               }
            });
        }
    });

    findViewById(R.id.logout_button).setOnClickListener(new OnClickListener() {
      @TargetApi(Build.VERSION_CODES.HONEYCOMB)
      @Override
      public void onClick(View v) {
        ParseUser.logOut();

        // FLAG_ACTIVITY_CLEAR_TASK only works on API 11, so if the user
        // logs out on older devices, we'll just exit.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
          Intent intent = new Intent(SampleProfileActivity.this,
              SampleDispatchActivity.class);
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
              | Intent.FLAG_ACTIVITY_NEW_TASK);
          startActivity(intent);
        } else {
          finish();
        }      
      }      
    });    
  }
  
  public Address getAddress(Context context, Location loc){
      Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
      ParseGeoPoint gp = null;
      Address ad = null;
      try {
          List<Address> addresses = geoCoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
          for(Address address : addresses){
              gp = new ParseGeoPoint(address.getLatitude(), address.getLongitude());
              address.getAddressLine(1);
              ad = address;
          }
      } catch (IOException e) {
          e.printStackTrace();
      }
      System.out.println("value of addr: " + ad);
      return ad;
  }
  
  
  @Override
  protected void onStart() {
    super.onStart();
    // Set up the profile page based on the current user.
    ParseUser user = ParseUser.getCurrentUser();
    showProfile(user);
  }
  
  /**
   * Shows the profile of the given user.
   *
   * @param user
   */
  private void showProfile(ParseUser user) {
    if (user != null) {
      emailTextView.setText(user.getEmail());
      String fullName = user.getString("name");
      if (fullName != null) {
        nameTextView.setText(fullName);
      }
    }
  }

@Override
public void onConnectionFailed(ConnectionResult arg0) {
    // TODO Auto-generated method stub
    
}

@Override
public void onConnected(Bundle arg0) {
    // TODO Auto-generated method stub
    
}

@Override
public void onDisconnected() {
    // TODO Auto-generated method stub
    
}
}
