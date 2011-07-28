package com.ahanlon.android;

import oauth.signpost.OAuth;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidTwitter extends Activity implements LocationListener {

	private SharedPreferences prefs;
	private final Handler mTwitterHandler = new Handler();
	private TextView loginStatus;
	private LocationManager locationManager;
	private String provider;
	String latituteField = "";
	String longitudeField= "";
	
    final Runnable mUpdateTwitterNotification = new Runnable() {
        public void run() {
        	Toast.makeText(getBaseContext(), "Tweet sent !", Toast.LENGTH_LONG).show();
        }
    };

    /**
     * onCreate is what fires up from the beginning
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //Display whether or not the user is currently logged in.
        loginStatus = (TextView)findViewById(R.id.login_status);
        Button tweet = (Button) findViewById(R.id.btn_tweet);
        Button clearCredentials = (Button) findViewById(R.id.btn_clear_credentials);
        
        tweet.setOnClickListener(new View.OnClickListener() {
        	/**
        	 * Send a tweet. If the user hasn't authenticated to Tweeter yet, he'll be redirected via a browser
        	 * to the twitter login page. Once the user authenticated, he'll authorize the Android application to send
        	 * tweets on the users behalf.
        	 */
            public void onClick(View v) {
            	if (TwitterUtils.isAuthenticated(prefs)) {
            		sendTweet();
            	} else {
    				Intent i = new Intent(getApplicationContext(), PrepareRequestTokenActivity.class);
    				i.putExtra("tweet_msg","test");
    				startActivity(i);
            	}
            }
        });

        clearCredentials.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	clearCredentials();
            	updateLoginStatus();
            }
        });
        
        
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(provider);
		
		LocationListener locListener = new CustomLocationListener();
		locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, locListener);
		
		if (location != null) {
			System.out.println("Provider " + provider + " has been selected.");
			int lat = (int) (location.getLatitude());
			int lng = (int) (location.getLongitude());
			latituteField = String.valueOf(lat);
			longitudeField = String.valueOf(lng);
		} else {
			latituteField = Constants.UNKNOWN;
			longitudeField = Constants.UNKNOWN;
		}
        
        
	}
	public class CustomLocationListener implements LocationListener{

		@Override
		public void onLocationChanged(Location location) {
			latituteField = String.valueOf( location.getLatitude() );
			longitudeField = String.valueOf( location.getLongitude() );
		}
		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
		}
	}

	
	public void sendTweet() {
			Thread t = new Thread() {
	        String msg;
			public void run() {
	        	
	        	try {
	        		if ( longitudeField.equals( Constants.UNKNOWN ) || latituteField.equals( Constants.UNKNOWN) )
	        		{
	        			msg = "Unable to determine location, and therefore unable to find the nearest McDonald's";
	        		}
	        		else
	        		{
	        			msg = MapUtils.buildGoogleMapsLink(longitudeField, latituteField);
	        		}
	        		TwitterUtils.sendTweet(prefs, msg);
	        		mTwitterHandler.post(mUpdateTwitterNotification);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
	        }

	    };
	    t.start();
	}

	private void clearCredentials() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final Editor edit = prefs.edit();
		edit.remove(OAuth.OAUTH_TOKEN);
		edit.remove(OAuth.OAUTH_TOKEN_SECRET);
		edit.commit();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateLoginStatus();
	}
	
	public void updateLoginStatus() {
		loginStatus.setText("Logged into Twitter : " + TwitterUtils.isAuthenticated(prefs));
	}
	

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
}