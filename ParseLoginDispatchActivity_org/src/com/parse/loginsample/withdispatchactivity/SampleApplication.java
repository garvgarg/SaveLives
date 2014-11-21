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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SaveCallback;

public class SampleApplication extends Application {
  @SuppressWarnings("deprecation")
@Override
  public void onCreate() {
    super.onCreate();
    // Required - Initialize the Parse SDK
    Parse.initialize(this, getString(R.string.parse_app_id),
        getString(R.string.parse_client_key));
    // Associate the device with a user
  /*  ParseInstallation installation = ParseInstallation.getCurrentInstallation();
    installation.put("user",ParseUser.getCurrentUser());
    installation.saveInBackground();
    */ParsePush.subscribeInBackground("", new SaveCallback() {
        @Override
        public void done(ParseException e) {
            if (e == null) {
            Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
          } else {
            Log.e("com.parse.push", "failed to subscribe for push", e);
          }
        }
     });
    
    Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

    // Optional - If you don't want to allow Facebook login, you can
    // remove this line (and other related ParseFacebookUtils calls)
    ParseFacebookUtils.initialize(getString(R.string.facebook_app_id));

    // Optional - If you don't want to allow Twitter login, you can
    // remove this line (and other related ParseTwitterUtils calls)
    ParseTwitterUtils.initialize(getString(R.string.twitter_consumer_key),
        getString(R.string.twitter_consumer_secret));
    
    try {
        PackageInfo info = getPackageManager().getPackageInfo(
                "com.parse.loginsample.withdispatchactivity", 
                PackageManager.GET_SIGNATURES);
        for (Signature signature : info.signatures) {
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(signature.toByteArray());
            Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
    } catch (NameNotFoundException e) {

    } catch (NoSuchAlgorithmException e) {

    }
  }
}
