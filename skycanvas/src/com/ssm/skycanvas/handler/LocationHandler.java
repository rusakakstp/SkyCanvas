package com.ssm.skycanvas.handler;

import com.ssm.skycanvas.view.CommonUtil;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

public class LocationHandler implements LocationListener {

	private Context context;
	private LocationManager locManager;
	private Location myLocation;
	private String provider=null;
	public static double ALTITUDE=0;
	public static double LONGTITUDE=0;
	public static double LATITUDE=0;
	
	public static double perLatitude = 111111;
	public static double perLongtitude = 133333;
	
	public static double radius = 50;
	
	private DataHandler dataHandle;
	
	

	public DataHandler getDataHandle() {
		return dataHandle;
	}

	public void setDataHandle(DataHandler dataHandle) {
		this.dataHandle = dataHandle;
	}

	public LocationHandler(Context context , String provider) {
		// TODO Auto-generated constructor stub]

		this.context = context;
		this.provider = provider;
		
		setLocManager(provider);

	}

	public double getAltitude() {
		return ALTITUDE;
	}


	public void setAltitude(double altitude) {
		this.ALTITUDE = altitude;
	}


	public double getLongtitude() {
		return LONGTITUDE;
	}


	public void setLongtitude(double longtitude) {
		this.LONGTITUDE = longtitude;
	}


	public double getLatitude() {
		return LATITUDE;
	}


	public void setLatitude(double latitude) {
		this.LATITUDE = latitude;
	}

	

	public void setLocManager(String provider){
		
		locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		locManager.requestLocationUpdates(provider, 0, 0, this);
//		locManager.
	}

	@Override
	public void onLocationChanged(Location loc) {
		// TODO Auto-generated method stub

		myLocation = loc;
		getLocation();
		CommonUtil.tolog("location getLatitude = " + loc.getLatitude());
		CommonUtil.tolog("location getLongitude = " + loc.getLongitude());
		if (dataHandle != null) {
			dataHandle.updateMarker(loc.getLatitude(), loc.getLongitude());
		}
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

		CommonUtil.tolog("location onProviderDisabled");
	}


	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		CommonUtil.tolog("location onProviderEnabled");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

		String sStatus = null;
		switch (status) {
		case LocationProvider.OUT_OF_SERVICE:
			sStatus = "범위 벗어남";
			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			sStatus = "일시적 불능";
			break;
		case LocationProvider.AVAILABLE:
			sStatus = "사용 가능";
			break;
		}
		CommonUtil.tolog("location sStatus = " + sStatus);
	}

	public void getLocation() {

		if (myLocation != null) {
			LATITUDE = myLocation.getLatitude();
			LONGTITUDE = myLocation.getLongitude();
			ALTITUDE = myLocation.getAltitude();
			
			CommonUtil.tolog("location getLatitude = " + LATITUDE);
			CommonUtil.tolog("location getLongitude = " + LONGTITUDE);
			CommonUtil.tolog("location getAltitude = " + ALTITUDE);
		} else {
			Location loc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (loc != null) {
				LATITUDE = loc.getLatitude();
				LONGTITUDE=loc.getLongitude();
				ALTITUDE = loc.getAltitude();
				CommonUtil.tolog("location getLatitude = " + loc.getLatitude());
				CommonUtil.tolog("location getLongitude = " + loc.getLongitude());
				CommonUtil.tolog("location getAltitude = " + loc.getAltitude());
				CommonUtil.tolog("location last upper");
			} else {
				CommonUtil.tolog("location is null");

			}
		}

	}

}
