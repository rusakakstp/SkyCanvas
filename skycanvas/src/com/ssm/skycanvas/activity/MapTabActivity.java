/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ssm.skycanvas.activity;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import javax.microedition.khronos.opengles.GL;

import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.Cassandra.Client;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.Compression;
import org.apache.cassandra.thrift.CqlResult;
import org.apache.cassandra.thrift.CqlRow;
import org.apache.cassandra.utils.UUIDGen;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.maps.MyLocationOverlay;
import com.ssm.skycanvas.activity.R.string;
import com.ssm.skycanvas.data.MarkerData;
import com.ssm.skycanvas.handler.DataHandler;
import com.ssm.skycanvas.handler.LocationHandler;
import com.ssm.skycanvas.view.CommonUtil;

public class MapTabActivity extends Activity implements OnCameraChangeListener {

	private GoogleMap mMap;
	private TSocket transport;
	private TFramedTransport framedTransport;
	private TBinaryProtocol protocol;
	private Client client;
	private ColumnParent parent;
	private CqlResult cql;
	private ArrayList<MarkerData> tempMarkers;
	
	private static final String UTF8 = "UTF8";
	private final String ROWKEY = "key";
	private final String USERID = "userid";
	private final String ACTIVATED = "activated";
	private final String LATITUDE = "latitude";
	private final String LONGTITUDE = "longtitude";
	private final String ALTITUDE = "altitude";
	private final String AREA = "area";
	private final String TITLE = "title";
	private final String MEMO = "memo";
	private final String PICDATA = "picdata";
	private int flag_add;
	private ArrayList<MarkerData> markers;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		setContentView(R.layout.activity_map);
		
		openCassandra();
		markers = new ArrayList<MarkerData>();
		tempMarkers = new ArrayList<MarkerData>();

		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.setOnCameraChangeListener(this);

		LinearLayout rightLinear = (LinearLayout) findViewById(R.id.map_right_linear);
		rightLinear.setVisibility(View.GONE);

		MarkerOptions marker = new MarkerOptions();
		LatLng myLocation = new LatLng(LocationHandler.LATITUDE, LocationHandler.LONGTITUDE);
		CommonUtil.tolog("LocationHandler.LATITUDE = " + LocationHandler.LATITUDE);
		CommonUtil.tolog("LocationHandler.LONGTITUDE = " + LocationHandler.LONGTITUDE);
		marker.position(myLocation);
		marker.title("현재위치");
//		marker.
		mMap.addMarker(marker);
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(myLocation, 16);
		mMap.moveCamera(update);

		// LinearLayout rightLinear2 =
		// (LinearLayout)findViewById(R.id.tab_right_linear);
		// rightLinear2.bringToFront();`
	
//		mMap.setInfoWindowAdapter()
	
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// mMap.addMarker(new MarkerOptions().position(new
		// LatLng(LocationHandler.LATITUDE,
		// LocationHandler.LONGTITUDE)).title("My Position"));

	}

	@Override
	public void onCameraChange(CameraPosition caPos) {
		// TODO Auto-generated method stub


		double leftLongitude = mMap.getProjection().getVisibleRegion().farLeft.longitude;
		double rightLongitude = mMap.getProjection().getVisibleRegion().farRight.longitude;
		double topLatitude = mMap.getProjection().getVisibleRegion().farLeft.latitude;
		double bottomLatitude = mMap.getProjection().getVisibleRegion().nearLeft.latitude;

		String query = "SELECT * FROM markers WHERE activated = '1' AND longtitude >= " + leftLongitude + " AND longtitude <= " + rightLongitude + " AND latitude >= " + bottomLatitude + " AND latitude <= " + topLatitude + "";
		
		try {

			cql = client.execute_cql_query(ByteBuffer.wrap(query.getBytes("UTF-8")), Compression.NONE);
			CommonUtil.tolog("1st cql row size  = " + cql.getRowsSize());
			// CommonUtil.tolog("tempMarkers.size()  = " +
			// tempMarkers.size());

			if (cql.getRowsSize() != markers.size()) {
				flag_add = 1;
				tempMarkers = makeTempMarkerArray(cql);
				CommonUtil.tolog("1st make temp marker array");
			}
			else {
				flag_add = 0;
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (flag_add == 1) {
		addNremoveMarkers(0, 0);
		}
	}

	public void openCassandra() {
		// ---------------------------
		// Open Cassandra server
		// ---------------------------

		try {
			transport = new TSocket(DataHandler.HOST, DataHandler.PORT);
			framedTransport = new TFramedTransport(transport);
			protocol = new TBinaryProtocol(framedTransport);
			client = new Cassandra.Client(protocol);
			parent = new ColumnParent("markers");

			framedTransport.open();
			client.set_keyspace(DataHandler.KEY_SPACE_NAME);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private ArrayList<MarkerData> makeTempMarkerArray(CqlResult cql) throws UnsupportedEncodingException {

		ArrayList<MarkerData> temp = new ArrayList<MarkerData>();
		MarkerData tempMarker;
		tempMarkers.clear();

		for (Iterator<CqlRow> iterator = cql.getRowsIterator(); iterator.hasNext();) {
			CqlRow row = (CqlRow) iterator.next();
			tempMarker = new MarkerData();
			for (Iterator<Column> iterator2 = row.getColumnsIterator(); iterator2.hasNext();) {
				Column col = (Column) iterator2.next();

				if (new String(col.getName(), UTF8).equalsIgnoreCase(ROWKEY)) {
					tempMarker.setRowKey(UUIDGen.getUUID(ByteBuffer.wrap(col.getValue())).toString());
					// CommonUtil.tolog("--------------------------row key check col.getValue()  = "
					// + col.getValue());
					// CommonUtil.tolog("--------------------------row key check UUIDGen.getUUID(ByteBuffer.wrap(col.getValue())).toString();  = "
					// +
					// UUIDGen.getUUID(ByteBuffer.wrap(col.getValue())).toString());
				}

			}

			temp.add(tempMarker);

		}

		return temp;

	}
	
	
	
	private void addNremoveMarkers(double x, double y) {

		int remove_flag = 0;
		int add_flag = 0;

		MarkerData marker;
		String query;

		boolean flag_exist = false;
		String markerRowkey = null;
		String TempMarkerRowkey = null;
		int markerSize = markers.size();
		int tempMarkerSize = tempMarkers.size();

		StringBuffer queryRowKeySet = new StringBuffer();

		String colName = null;
		CommonUtil.tolog("markerSize  = " + markerSize);
		CommonUtil.tolog("tempMarkerSize  = " + tempMarkerSize);

		for (int i = 0; i < markers.size(); i++) {
			markerRowkey = markers.get(i).getRowKey();
//			CommonUtil.tolog("markerRowkey  = " + markerRowkey);
//			CommonUtil.tolog("i  = " + i);
			for (int j = 0; j < tempMarkers.size(); j++) {
				TempMarkerRowkey = tempMarkers.get(j).getRowKey();
//				CommonUtil.tolog("TempMarkerRowkey  = " + TempMarkerRowkey);
//				CommonUtil.tolog("j  = " + j);
				if (markerRowkey.equals(TempMarkerRowkey)) {
					flag_exist = true;
					CommonUtil.tolog("remove tempMarkers View  = " + j);
					tempMarkers.remove(j);
					
					break;
				}
			}

			if (flag_exist == false) {
				markers.remove(i);
				CommonUtil.tolog("remove markers View  = " + i);
//				CommonUtil.tolog("j  = " + j);
				i--;
//				break;
			}
			
			flag_exist = false;

		}

		if (tempMarkers.size() != 0) {

			for (int i = 0; i < tempMarkers.size(); i++) {
				if (i == 0) {
					queryRowKeySet.append(tempMarkers.get(i).getRowKey());
				}
				else {
					queryRowKeySet.append(", " + tempMarkers.get(i).getRowKey());
				}
			}
			CommonUtil.tolog("queryRowKeySet.toString()  = " + queryRowKeySet.toString());

			tempMarkers.clear();

			query = "SELECT * FROM markers WHERE KEY IN (" + queryRowKeySet.toString() + ");";
			// query =
			// "SELECT * FROM markers WHERE key = '1259804d-3685-31b1-bd71-84f004aedc21'";
			// query = "SELECT * FROM markers ";

			queryRowKeySet.delete(0, queryRowKeySet.length());

			try {
				client.set_cql_version("3.0.0");
				cql = client.execute_cql_query(ByteBuffer.wrap(query.getBytes("UTF-8")), Compression.NONE);

				CommonUtil.tolog("------------insert complete---------------");
				CommonUtil.tolog("2st cql row size  = " + cql.getRowsSize());
				// CommonUtil.tolog("column size  = " +
				// cql.getRows().get(0).getColumnsSize());

				for (Iterator<CqlRow> iterator = cql.getRowsIterator(); iterator.hasNext();) {
					CqlRow row = (CqlRow) iterator.next();
					// CommonUtil.tolog("CqlRow  = ");
					marker = new MarkerData();
					for (Iterator<Column> iterator2 = row.getColumnsIterator(); iterator2.hasNext();) {
						Column col = (Column) iterator2.next();
						colName = new String(col.getName(), UTF8);
						if (colName.equalsIgnoreCase(USERID)) {
							marker.setUserid(new String(col.getValue(), UTF8));
							CommonUtil.tolog("marker.getUserid()  = " + marker.getUserid());
						}
						else if (colName.equalsIgnoreCase(ROWKEY)) {
							marker.setRowKey(UUIDGen.getUUID(ByteBuffer.wrap(col.getValue())).toString());
							CommonUtil.tolog("marker.getRowKey()  = " + marker.getRowKey());
						}
						else if (colName.equalsIgnoreCase(LATITUDE)) {
							marker.setLatitude(ByteBuffer.wrap(col.getValue()).getDouble());
							CommonUtil.tolog("marker.getLatitude()  = " + marker.getLatitude());
						}
						else if (colName.equalsIgnoreCase(LONGTITUDE)) {
							marker.setLongtitude(ByteBuffer.wrap(col.getValue()).getDouble());
							CommonUtil.tolog("marker.getLongtitude()  = " + marker.getLongtitude());
						}
						else if (colName.equalsIgnoreCase(ALTITUDE)) {
							marker.setAltitude(ByteBuffer.wrap(col.getValue()).getDouble());
							CommonUtil.tolog("marker.getAltitude()  = " + marker.getAltitude());
						}
						else if (colName.equalsIgnoreCase(AREA)) {
							marker.setArea(ByteBuffer.wrap(col.getValue()).getDouble());
							CommonUtil.tolog("marker.getArea()  = " + marker.getArea());
						}
						else if (colName.equalsIgnoreCase(TITLE)) {
							marker.setTitle(new String(col.getValue(), UTF8));
							CommonUtil.tolog("marker.getTitle()  = " + marker.getTitle());
						}
						else if (colName.equalsIgnoreCase(MEMO)) {
							marker.setMemo(new String(col.getValue(), UTF8));
							CommonUtil.tolog("marker.getMemo()  = " + marker.getMemo());
						}
						else if (colName.equalsIgnoreCase(PICDATA)) {

							byte[] picdata = col.getValue();
							Bitmap picBM = BitmapFactory.decodeByteArray(picdata, 0, picdata.length);
							marker.setPic(picBM);
						}
						else {
							// CommonUtil.tolog("marker.name  = " + colName);
							// CommonUtil.tolog("marker.else  = " +
							// UUIDGen.getUUID(ByteBuffer.wrap(col.getValue())).toString());
						}
					}

					tempMarkers.add(marker);

				}

				for (int i = 0; i < tempMarkers.size(); i++) {
					this.markers.add(tempMarkers.get(i));
					
					MarkerData markerData = tempMarkers.get(i);
					
					MarkerOptions newMarker = new MarkerOptions();
					LatLng markerLocation = new LatLng(tempMarkers.get(i).getLatitude(),tempMarkers.get(i).getLongtitude());
					newMarker.position(markerLocation);
					newMarker.title(markerData.getTitle());
					newMarker.snippet(markerData.getMemo());
					newMarker.icon(BitmapDescriptorFactory.fromBitmap(markerData.getPic()));
					
					mMap.addMarker(newMarker);
				}
				tempMarkers.clear();

				// add n remove markers
				// CommonUtil.tolog("tempMarkers size = " + tempMarkers.size());
			}
			catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		else {

		}

	}
}
