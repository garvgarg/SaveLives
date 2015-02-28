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

package com.parse.ui;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
//import com.google.android.maps.GeoPoint;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.parse.ParseGeoPoint;
/**
 * Fragment for the user signup screen.
 */
public class ParseSignupFragment extends ParseLoginFragmentBase implements
                OnClickListener,
                GooglePlayServicesClient.ConnectionCallbacks,
                GooglePlayServicesClient.OnConnectionFailedListener {
  public static final String USERNAME = "com.parse.ui.ParseSignupFragment.USERNAME";
  public static final String PASSWORD = "com.parse.ui.ParseSignupFragment.PASSWORD";

  private EditText usernameField;
  private EditText passwordField;
  private EditText confirmPasswordField;
  private EditText emailField;
  private EditText nameField;
  private EditText addressField;
  private EditText phoneField;
  private EditText emergencyContactField;
  private Button createAccountButton;
  private ParseOnLoginSuccessListener onLoginSuccessListener;
  private LocationClient locationclient;
  private RadioGroup volunteerUserGroup;
  //private CheckBox volunteerUser;
  //private CheckBox user;
  private RadioButton volunteerUser;
  private RadioButton user;
  
  private ParseLoginConfig config;
  private int minPasswordLength;

  private static final String LOG_TAG = "ParseSignupFragment";
  private static final int DEFAULT_MIN_PASSWORD_LENGTH = 6;
  private static final String USER_OBJECT_NAME_FIELD = "name";
  private static final String USER_OBJECT_ADDRESS_FIELD = "address";
  private static final String USER_OBJECT_GPS_COORDINATES_LAT_FIELD = "lat";
  private static final String USER_OBJECT_GPS_COORDINATES_L0NG_FIELD = "long";
  private static final String USER_OBJECT_VOLUNTEER_USER_FIELD = "volunteerNuser";
  private static final String USER_OBJECT_PHONE_FIELD = "phone";
  private static final String USER_OBJECT_GEOPOINT_FIELD = "location";
  private static final String USER_OBJECT_EMERGENCY_CONTACT_FIELD = "emergencyContact";
  
  public static ParseSignupFragment newInstance(Bundle configOptions, String username, String password) {
    ParseSignupFragment signupFragment = new ParseSignupFragment();
    Bundle args = new Bundle(configOptions);
    args.putString(ParseSignupFragment.USERNAME, username);
    args.putString(ParseSignupFragment.PASSWORD, password);
    signupFragment.setArguments(args);
    return signupFragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                           Bundle savedInstanceState) {

    Bundle args = getArguments();
    config = ParseLoginConfig.fromBundle(args, getActivity());

    minPasswordLength = DEFAULT_MIN_PASSWORD_LENGTH;
    if (config.getParseSignupMinPasswordLength() != null) {
      minPasswordLength = config.getParseSignupMinPasswordLength();
    }

    String username = (String) args.getString(USERNAME);
    String password = (String) args.getString(PASSWORD);

    View v = inflater.inflate(R.layout.com_parse_ui_parse_signup_fragment,
        parent, false);
    ImageView appLogo = (ImageView) v.findViewById(R.id.app_logo);
    usernameField = (EditText) v.findViewById(R.id.signup_username_input);
    passwordField = (EditText) v.findViewById(R.id.signup_password_input);
    confirmPasswordField = (EditText) v
        .findViewById(R.id.signup_confirm_password_input);
    emailField = (EditText) v.findViewById(R.id.signup_email_input);
    nameField = (EditText) v.findViewById(R.id.signup_name_input);
    addressField = (EditText) v.findViewById(R.id.signup_address_input);
    phoneField = (EditText) v.findViewById(R.id.signup_phone_input);
    emergencyContactField = (EditText) v.findViewById(R.id.signup_emergency_contact_input);
    createAccountButton = (Button) v.findViewById(R.id.create_account);
    volunteerUserGroup = (RadioGroup) v.findViewById(R.id.radioGroup1);
    volunteerUser = (RadioButton) v.findViewById(R.id.volunteerNuser);
    user = (RadioButton) v.findViewById(R.id.user);
    
    usernameField.setText(username);
    passwordField.setText(password);
    
    int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
    if (resp == ConnectionResult.SUCCESS) {
        locationclient = new LocationClient(getActivity(),this,this);
        locationclient.connect();
    } else {
        showToast("Google Play Service Error " + resp);
    }

    if (appLogo != null && config.getAppLogo() != null) {
      appLogo.setImageResource(config.getAppLogo());
    }

    if (config.isParseLoginEmailAsUsername()) {
      usernameField.setHint(R.string.com_parse_ui_email_input_hint);
      usernameField.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
      if (emailField != null) {
        emailField.setVisibility(View.GONE);
      }
    }

    if (config.getParseSignupSubmitButtonText() != null) {
      createAccountButton.setText(config.getParseSignupSubmitButtonText());
    }
    createAccountButton.setOnClickListener(this);

    return v;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    if (activity instanceof ParseOnLoginSuccessListener) {
      onLoginSuccessListener = (ParseOnLoginSuccessListener) activity;
    } else {
      throw new IllegalArgumentException(
          "Activity must implemement ParseOnLoginSuccessListener");
    }

    if (activity instanceof ParseOnLoadingListener) {
      onLoadingListener = (ParseOnLoadingListener) activity;
    } else {
      throw new IllegalArgumentException(
          "Activity must implemement ParseOnLoadingListener");
    }
  }

  @Override
  public void onClick(View v) {
    String username = usernameField.getText().toString();
    String password = passwordField.getText().toString();
    String passwordAgain = confirmPasswordField.getText().toString();

    String email = null;
    if (config.isParseLoginEmailAsUsername()) {
      email = usernameField.getText().toString();
    } else if (emailField != null) {
      email = emailField.getText().toString();
    }

    String name = null;
    if (nameField != null) {
      name = nameField.getText().toString();
    }

    String address = null;
    if (addressField != null) {
        address = addressField.getText().toString();
      }
    
    Location loc = null;
    if (locationclient != null && locationclient.isConnected()) {
            loc = locationclient.getLastLocation();
            System.out.println("Last Known Location :" + loc.getLatitude() + "," + loc.getLongitude());
    }
    
    String phone = null;
    if (phoneField != null) {
        phone = phoneField.getText().toString();
      }
    
    //Pattern pattern1 = Pattern.compile("[0-9+-]");
    //Pattern pattern1 = Pattern.compile("\\+\\d \\d{3} \\d{4} \\d{4}");
    Pattern pattern1 = Pattern.compile("\\d{11}");
    Matcher matcher1 = pattern1.matcher(phone);
    
    String emergencyCnt = null;
    if (emergencyContactField != null) {
        emergencyCnt = emergencyContactField.getText().toString();
    }
    
    //Pattern pattern2 = Pattern.compile("[0-9]");
    //Pattern pattern2 = Pattern.compile("\\+?\\d?\\d?\\d?\\d?\\d-\\d{3}-\\d{4}-\\d{4}");
    Pattern pattern2 = Pattern.compile("\\d{11}");
    Matcher matcher2 = pattern2.matcher(emergencyCnt);
    
    int selectId = volunteerUserGroup.getCheckedRadioButtonId();
    System.out.println("Button id:" + selectId);
    System.out.println("volunteer n user id" + volunteerUser.getId());    
    
    if (username.length() == 0) {
      showToast(R.string.com_parse_ui_no_username_toast);
    } else if (password.length() == 0) {
      showToast(R.string.com_parse_ui_no_password_toast);
    } else if (password.length() < minPasswordLength) {
      showToast(getResources().getQuantityString(
          R.plurals.com_parse_ui_password_too_short_toast,
          minPasswordLength, minPasswordLength));
    } else if (passwordAgain.length() == 0) {
      showToast(R.string.com_parse_ui_reenter_password_toast);
    } else if (!password.equals(passwordAgain)) {
      showToast(R.string.com_parse_ui_mismatch_confirm_password_toast);
      confirmPasswordField.selectAll();
      confirmPasswordField.requestFocus();
    } else if (email != null && email.length() == 0) {
      showToast(R.string.com_parse_ui_no_email_toast);
    } else if (name != null && name.length() == 0) {
      showToast(R.string.com_parse_ui_no_name_toast);
    } else if (address.length() == 0) {  
      showToast(R.string.com_parse_ui_no_address_toast);
    } else if (address != null && address.length() == 0) {
      showToast(R.string.com_parse_ui_no_address_toast);
    } else if (phone.length() == 0) {  
        showToast(R.string.com_parse_ui_no_phone_toast);
    } /*else if (phone != null && ((phone.length() < 11 || 
               phone.length() > 18) || !matcher1.matches())) {
        showToast(R.string.com_parse_ui_invalid_phone_toast);
    } else if (emergencyCnt.length() == 0) {  
        showToast(R.string.com_parse_ui_no_emergency_contact_toast);
    } else if (emergencyCnt != null && (emergencyCnt.length() < 11 ||
               emergencyCnt.length() > 18 || !matcher2.matches())) {
        showToast(R.string.com_parse_ui_invalid_emergency_contact_toast);
    } */else {
      ParseUser user = new ParseUser();

      // Set standard fields
      user.setUsername(username);
      user.setPassword(password);
      user.setEmail(email);
      
      // Set additional custom fields only if the user filled it out
      if (name.length() != 0) {
        user.put(USER_OBJECT_NAME_FIELD, name);
      }

      if (address.length() != 0) {
          user.put(USER_OBJECT_ADDRESS_FIELD, address);
      }
      if (selectId == volunteerUser.getId()) {
          user.put(USER_OBJECT_VOLUNTEER_USER_FIELD, "volunteerNuser");
      } else {
          user.put(USER_OBJECT_VOLUNTEER_USER_FIELD, "user");
      }
      
      if (loc != null) {
          user.put(USER_OBJECT_GPS_COORDINATES_LAT_FIELD, String.valueOf(loc.getLatitude()));
          user.put(USER_OBJECT_GPS_COORDINATES_L0NG_FIELD, String.valueOf(loc.getLongitude()));
      }
      
      ParseGeoPoint gp = searchLocationByName(getActivity(), address);
      
      if (gp != null) {
          try {
          user.put(USER_OBJECT_GEOPOINT_FIELD, gp);
          } catch (Exception e){
              e.printStackTrace();
          }
      }
      
      if (phone.length() != 0) {
          user.put(USER_OBJECT_PHONE_FIELD, phone);
      }
      
      if (emergencyCnt.length() != 0) {
          user.put(USER_OBJECT_EMERGENCY_CONTACT_FIELD, emergencyCnt);
      }
      
      loadingStart();
      user.signUpInBackground(new SignUpCallback() {

        @Override
        public void done(ParseException e) {
          if (isActivityDestroyed()) {
            return;
          }

          if (e == null) {
            loadingFinish();
            signupSuccess();
         // Associate the device with a user
            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
            installation.put("user",ParseUser.getCurrentUser());
            installation.saveInBackground();
            
          } else {
            loadingFinish();
            if (e != null) {
              debugLog(getString(R.string.com_parse_ui_login_warning_parse_signup_failed) +
                  e.toString());
              switch (e.getCode()) {
                case ParseException.INVALID_EMAIL_ADDRESS:
                  showToast(R.string.com_parse_ui_invalid_email_toast);
                  break;
                case ParseException.USERNAME_TAKEN:
                  showToast(R.string.com_parse_ui_username_taken_toast);
                  break;
                case ParseException.EMAIL_TAKEN:
                  showToast(R.string.com_parse_ui_email_taken_toast);
                  break;
                default:
                  showToast(R.string.com_parse_ui_signup_failed_unknown_toast);
              }
            }
          }
        }
      });
    }
  }

  public static ParseGeoPoint searchLocationByName(Context context, String locationName){
      Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
      ParseGeoPoint gp = null;
      Address ad = null;
      try {
          List<Address> addresses = geoCoder.getFromLocationName(locationName, 1);
          for(Address address : addresses){
              gp = new ParseGeoPoint(address.getLatitude(), address.getLongitude());
              address.getAddressLine(1);
              ad = address;
          }
      } catch (IOException e) {
          e.printStackTrace();
      }
      return gp;
  }
  
  @Override
  protected String getLogTag() {
    return LOG_TAG;
  }

  private void signupSuccess() {
    onLoginSuccessListener.onLoginSuccess();
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
