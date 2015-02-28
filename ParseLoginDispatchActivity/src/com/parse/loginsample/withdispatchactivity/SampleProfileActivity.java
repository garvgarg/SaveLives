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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.flurry.android.FlurryAgent;
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
  private String address;
  private String city;
  private String street;
  private String state ;
  private String zip;
  private String country;
  private RequestQueue queue;
  private Location loc;
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
    queue = Volley.newRequestQueue(this);
    // Collect coordinates in case of emergency and send help request
    findViewById(R.id.button1).setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
            if (locationclient != null && locationclient.isConnected()) {
                    loc = locationclient.getLastLocation();
                    emergencyLocation = String.format("Emergency GPS Location :" + loc.getLatitude() + "," + loc.getLongitude());
                    System.out.println(emergencyLocation);
            }
            
            //https://maps.googleapis.com/maps/api/geocode/json?latlng=28.6609484,77.282058&key=AIzaSyA2yCrevhK9ZRfJBepJ21aD8aRVzqq4a8M
            //&location_type=ROOFTOP
            String url =("https://maps.googleapis.com/maps/api/geocode/json?latlng=" +
            		loc.getLatitude() + "," +loc.getLongitude() +"&result_type=street_address&key="
            		+ getString(R.string.google_apikey));
            
            // Request a string response from the provided URL.
        	@SuppressWarnings("unchecked")
			StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener() {
        
				@Override
				public void onResponse(Object response) {
					// TODO Auto-generated method study	
					
					//System.out.println("Response is: "+ response.toString());
					String json = response.toString();
					try {
						
						JSONObject jsonObject = new JSONObject(json);
						if (jsonObject.getString("status").equalsIgnoreCase("OK")){
							
							JSONArray results = jsonObject.getJSONArray("results");	
						    address = results.getJSONObject(0).getString("formatted_address");
						    System.out.println("Formatted address " + address);
						    /*street = results.getJSONObject(0).getString("street");
						    city = results.getJSONObject(0).getString("");
						    state = results.getJSONObject(0).getString("state");
						    zip = results.getJSONObject(0).getString("zip");
						    country = results.getJSONObject(0).getString("country");*/
						} else {
						    Address a = getAddress(getApplicationContext(), loc);
						    String addressText = null;
						            try {
						            	//address = null;
						            	street = (a.getMaxAddressLineIndex() > 0 ? a.getAddressLine(0) : "").toString();
						            	city = a.getLocality().toString();
						            	state = a.getAdminArea().toString();
						            	zip = a.getPostalCode().toString();
						                country = a.getCountryCode().toString();
						            	
						                addressText = String.format(
						            			"%s, %s, %s - %s, %s",
						                        // If there's a street address, add it
						                        a.getMaxAddressLineIndex() > 0 ? a.getAddressLine(0) : "",
						                        // Locality is usually a city
						                        a.getLocality(),
						                        a.getAdminArea(),
						                        a.getPostalCode(),
						                        // The country of the address
						                        a.getCountryCode());
						            	//parameters for parse cloud code
						            	Log.e("com.savelife.app", "Street address" + addressText );
						            	Log.e("com.savelife.app", "Street address" + a.getAddressLine(0).toString());
						            	Log.e("com.savelife.app", "Locality" + a.getLocality().toString());
						            	Log.e("com.savelife.app", "getAdminArea" + a.getAdminArea().toString());
						            	Log.e("com.savelife.app", "Zip" + a.getPostalCode().toString());
						            	Log.e("com.savelife.app", " Country code" + a.getCountryCode().toString());

						            } catch (NullPointerException e) {
						            	Log.e("com.savelife.app", "Invalid street address");
						            	e.printStackTrace();
						            }	            
							}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					System.out.println("address" + address);
		            HashMap<String, Object> params = new HashMap<String, Object>();
		            params.put("address", address);
		        	params.put("street", street);
		        	params.put("city", city);
		        	params.put("state", state);
		        	params.put("zip", zip);
		        	params.put("country", country);
		        	
		            params.put("lat", loc.getLatitude());
		            params.put("lng", loc.getLongitude());
		            ParseUser user = ParseUser.getCurrentUser();
		            //System.out.println(user.getObjectId());
		            //System.out.println(user.getString("phone"));
		            params.put("userid", user.getObjectId());
		            params.put("phone", user.getString("phone"));
		            
		            String message = user.getString("name") + " Needs your help! at " 
		      			  + address + ". " + emergencyLocation;
		            String emergencyContactPhoneNumber = user.getString("emergencyContact");
		            
		            try {
		                SmsManager smsManager = SmsManager.getDefault();
		                smsManager.sendTextMessage(emergencyContactPhoneNumber, null, message, null, null);
		                Toast.makeText(getApplicationContext(), "SMS sent to emergency contact.",
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
            }, new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                	System.out.println("That didn't work!");
                }
            });
            // Add the request to the RequestQueue.
            queue.add(stringRequest);
            
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
    
    findViewById(R.id.video_button).setOnClickListener(new OnClickListener() {
        //@TargetApi(Build.VERSION_CODES.HONEYCOMB)
        //@Override
        public void onClick(View v) {    
        	Intent myIntent = new Intent(getApplicationContext(), 
        								 TrainingVideoPlayerActivity.class);
        	//myIntent.putExtra("key", value); //Optional parameters
        	startActivity(myIntent);
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
    //FlurryAgent.onStartSession(this);
  }
  
  protected void onStop () {
	  super.onStop();
	  //FlurryAgent.onEndSession(this);
  }

  protected void onResume() {
	  super.onResume();
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
