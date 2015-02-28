package com.parse.loginsample.withdispatchactivity;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;


public class PushNotificationActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.push_notification);
        
        Intent i = getIntent();
        
        final String lat;
        final String lng;
        final String ph;
        
        if (i != null) {
            TextView message = (TextView) findViewById(R.id.messageContent);
            TextView signature = (TextView) findViewById(R.id.signature);
            TextView street = (TextView) findViewById(R.id.streetAddress);
            TextView city = (TextView) findViewById(R.id.city);
            TextView state = (TextView) findViewById(R.id.state);
            //TextView country = (TextView) findViewById(R.id.country);
            TextView phone = (TextView) findViewById(R.id.phone);
            String c = i.getStringExtra("location");
            if (c != null) {
            	if (street != null) {
                    street.setText( "Need help near : " + i.getStringExtra("location"));
                  }                  	
            } else {
            
            	if (street != null) {
            		street.setText( "Street Address : " + i.getStringExtra("street"));
            		//street.setText( "Near by Address : " + c);
            	}
            
            	if (city != null) {
            		city.setText( "City : " + i.getStringExtra("city"));
            	}
            
            	if ((state != null) && (i.getStringExtra("zip") != null)) {
            		state.setText( "State : " + i.getStringExtra("state") + " - " + i.getStringExtra("zip")
            				+ "     Country: " + i.getStringExtra("country"));
            	}
            	 
            }
            if (phone != null) {

                phone.setText("Phone No.: " + i.getStringExtra("phone"));
            }
            
            
            if (signature != null) {
              signature.setText( "Lat: " + i.getStringExtra("Latitude") + " Lng: " + i.getStringExtra("Longitude"));
            }
            
            
            lat = i.getStringExtra("Latitude");
            lng = i.getStringExtra("Longitude");
            ph = i.getStringExtra("phone");
            
            findViewById(R.id.call).setOnClickListener(new OnClickListener() {
                String uri =  String.format(("tel:" + "%s"), ph);
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(uri));
                    startActivity(intent);           
                }
            });
            
            findViewById(R.id.ShowMap).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    
                    String uri = String.format(("http://maps.google.com/maps?daddr=%s,%s"), lat, lng);
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
                            //Uri.parse("http://maps.google.com/maps?saddr=20.344,34.34&daddr=20.5666,45.345"));
                            Uri.parse(uri));
                        startActivity(intent);
                         }
            });
            
            findViewById(R.id.ShowHospitals).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    
                    String geoURI = String.format("geo:%f,%f?q=hospital", Double.valueOf(lat), Double.valueOf(lng));
                    Uri geo = Uri.parse(geoURI);
                    Intent geoMap = new Intent(Intent.ACTION_VIEW, geo);
                    startActivity(geoMap);

                }
            });

        }
    }
}
