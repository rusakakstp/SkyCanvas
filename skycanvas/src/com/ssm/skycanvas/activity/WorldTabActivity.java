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

import static android.hardware.SensorManager.SENSOR_DELAY_GAME;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.ssm.skycanvas.handler.AugmentedViewHandler;
import com.ssm.skycanvas.handler.DataHandler;
import com.ssm.skycanvas.handler.LocationHandler;
import com.ssm.skycanvas.view.CameraPreview;
import com.ssm.skycanvas.view.CommonUtil;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

// ----------------------------------------------------------------------

public class WorldTabActivity extends Activity implements SensorEventListener, OnClickListener, OnSeekBarChangeListener, OnTouchListener {

	public static final int SMOOTH_ARRAY_SIZE = 50;

	private CameraPreview mPreview;
	Camera mCamera;
	int numberOfCameras;
	int cameraCurrentlyLocked;
	float RTmp[] = new float[9];
	float Rot[] = new float[9];
	float I[] = new float[9];
	float grav[] = new float[3];
	float mag[] = new float[3];
	float ori[] = new float[3];

	double[] smoothAzi = new double[SMOOTH_ARRAY_SIZE];
	double[] smoothPit = new double[SMOOTH_ARRAY_SIZE];
	double[] smoothRol = new double[SMOOTH_ARRAY_SIZE];
	int smoothIndex = 0;

	public static double azimuth = 0;
	public static double pitch = 0;
	public static double roll = 0;

	public static int width = 0;
	public static int height = 0;

	// The first rear facing camera
	int defaultCameraId;
	private SensorManager sensorMgr;
	private List<Sensor> sensors;
	private Sensor sensorGrav;
	private Sensor sensorMag;
	private AugmentedViewHandler augViewHandler;
	private LocationManager locManager;
	private Location myLocation;
	private double latPoint;
	private double lngPoint;

	public static float verticalAngle = 0;
	public static float horizontalAngle = 0;

	private boolean flag_openCamea = false;

	private DataHandler hand;

	private TextView distance;

	private SeekBar seekBar;

	private PreviewCallback previewCallback;

	private ShutterCallback shutterCallback;

	private PictureCallback pictureCallback;

	protected ImageView captureView;

	private View capRootView;

	private ViewGroup addedViews;

	private String captureMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		final Window wm = getWindow();

		// int topBarHeight =
		// wm.findViewById(Window.ID_ANDROID_CONTENT).getMeasuredHeight();

		// MainActivity.height -= topBarHeight;
		//
		// CommonUtil.tolog("topBarHeight = "+topBarHeight);
		// wm.getCurrentFocus().getHeight()

		Handler handler = new Handler();

		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				int topBarHeight = wm.findViewById(Window.ID_ANDROID_CONTENT).getMeasuredHeight();
				WorldTabActivity.height = topBarHeight;

				CommonUtil.tolog("topBarHeight = " + topBarHeight);
				Rect rec = new Rect();
				getWindow().getDecorView().getWindowVisibleDisplayFrame(rec);
				// this.height = rec.;
				CommonUtil.tolog("rec.left = " + rec.left);
				CommonUtil.tolog("rec.top = " + rec.top);
				CommonUtil.tolog("rec.right = " + rec.right);
				CommonUtil.tolog("rec.bottom = " + rec.bottom);
			}
		}, 1000);

		hand = new DataHandler(this);
		// Create a RelativeLayout container that will hold a SurfaceView,
		// and set it as the content of our activity.
		
		Criteria cri = new Criteria();
		cri.setAccuracy(Criteria.ACCURACY_FINE);
		
		LocationManager locman = ((LocationManager)getSystemService(Context.LOCATION_SERVICE));
		String provider = locman.getBestProvider(cri, true);
		
//		LocationHandler gpsLocHandler = new LocationHandler(this, LocationManager.GPS_PROVIDER);
//		gpsLocHandler.setDataHandle(hand);
		LocationHandler netLocHandler = new LocationHandler(this, LocationManager.NETWORK_PROVIDER);
		netLocHandler.setDataHandle(hand);
//		LocationHandler passiveLocHandler = new LocationHandler(this, LocationManager.PASSIVE_PROVIDER);
//		passiveLocHandler.setDataHandle(hand);
//		LocationHandler passiveLocHandler = new LocationHandler(this, provider);
//		passiveLocHandler.setDataHandle(hand);
		
		CommonUtil.tolog("provider= " + provider);

		// LocationHandler location = new LocationHandler(this);
		// location.getLocation();

		mPreview = new CameraPreview(this);
		setContentView(mPreview);
		mPreview.setDrawingCacheEnabled(true);

		openCamera();

		LayoutInflater infalte = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// View view = infalte.inflate(R.layout.shotshare, null);
		// addContentView(view, new LayoutParams(LayoutParams.FILL_PARENT,
		// LayoutParams.FILL_PARENT));
		
		addedViews = new FrameLayout(this);
		
		
		
		View additional = infalte.inflate(R.layout.activity_world_data, null);
		
		
		addedViews.addView(additional, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		addedViews.addView(hand.getAugViewHandler().getAugView(), new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		addContentView(addedViews, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		distance = (TextView) findViewById(R.id.world_txt_distance);
		distance.setText("test");

		seekBar = (SeekBar) findViewById(R.id.world_bottomSeekBar);
		seekBar.setOnSeekBarChangeListener(this);
		seekBar.setKeyProgressIncrement(100);
		seekBar.setProgress(1000);
		seekBar.setOnTouchListener(this);

		ImageView btn_back = (ImageView) findViewById(R.id.world_btn_back);
		btn_back.setOnClickListener(this);

		ImageView btn_pic = (ImageView) findViewById(R.id.world_btn_pic);
		btn_pic.setOnClickListener(this);

		ImageView btn_share = (ImageView) findViewById(R.id.world_btn_share);
		btn_share.setOnClickListener(this);
		
	
		
//		addContentView(hand.getAugViewHandler().getAugView(), new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		shutterCallback = new Camera.ShutterCallback() {
			
			@Override
			public void onShutter() {
				// TODO Auto-generated method stub
				
			}
		};

		previewCallback = new Camera.PreviewCallback() {

			@Override
			public void onPreviewFrame(byte[] data, Camera camera) {
				// TODO Auto-generated method stub
				
			}
		};
		
		pictureCallback = new Camera.PictureCallback() {
			
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				// TODO Auto-generated method stub
				
				BitmapFactory.Options option = new BitmapFactory.Options();
				Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
				
////				Display dis = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
////				Point size = new Point();
////				dis.getSize(size);
////				CommonUtil.tolog("size.x = "+size.x);
////				CommonUtil.tolog("size.y = "+size.y);
////				CommonUtil.tolog("bm.getWidth() = "+bm.getWidth());
////				CommonUtil.tolog("bm.getHeight() = "+bm.getHeight());
//				
//				Bitmap resized = Bitmap.createScaledBitmap(bm, size.x, size.y, false);
				captureView(bm);
			}
		};
		//
		// Find the total number of cameras available
		numberOfCameras = Camera.getNumberOfCameras();

		// Find the ID of the default camera
		CameraInfo cameraInfo = new CameraInfo();
		for (int i = 0; i < numberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
				defaultCameraId = i;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// mPreview.setVisibility(View.VISIBLE);
		// mPreview.forceLayout();
		// Open the default i.e. the first rear facing camera.
		// mPreview.getmHolder().addCallback(mPreview);
		// mPreview.getmSurfaceView().dispatchWindowVisibilityChanged(View.GONE);

		openCamera();

		// 센서 관리자 설정
		sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

		// 각 센서들 등록
		// 가속도 센서
		sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (sensors.size() > 0) {
			sensorGrav = sensors.get(0);
		}

		// 지자기 센서
		sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
		if (sensors.size() > 0) {
			sensorMag = sensors.get(0);
		}

		// 센서 관리자에 레지스터리스너를 이용해 각 센서들을 등록
		sensorMgr.registerListener(this, sensorGrav, SENSOR_DELAY_GAME);
		sensorMgr.registerListener(this, sensorMag, SENSOR_DELAY_GAME);

		CommonUtil.tolog("==========on resume============");
		
		Toast.makeText(this, "Loading Memo...", Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Because the Camera object is a shared resource, it's very
		// important to release it when the activity is paused.
		flag_openCamea = false;

		if (mCamera != null) {
			mPreview.setCamera(null);
			// mPreview.destroyDrawingCache();
			// mPreview.getmHolder().lockCanvas();
			// mPreview.setVisibility(View.GONE);
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
			// mPreview.getmSurfaceView().refreshDrawableState();
			// mPreview.getmSurfaceView().requestLayout();

			// mSurfaceView.refreshDrawableState();
			// mSurfaceView.requestLayout();
		}
		try {
			sensorMgr.unregisterListener(this, sensorGrav);
		}
		catch (Exception ignore) {
		}
		try {
			sensorMgr.unregisterListener(this, sensorMag);
		}
		catch (Exception ignore) {
		}
		sensorMgr = null;

		CommonUtil.tolog("==========on pause============");
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent evt) {
		// TODO Auto-generated method stub

		int x = SensorManager.AXIS_X;
		int y = SensorManager.AXIS_Y;
		int z = SensorManager.AXIS_Z;

		double tempAzi = 0;
		double tempPit = 0;
		double tempRol = 0;

		if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			grav[0] = evt.values[0];
			grav[1] = evt.values[1];
			grav[2] = evt.values[2];

		}
		else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			mag[0] = evt.values[0];
			mag[1] = evt.values[1];
			mag[2] = evt.values[2];

		}

		Display dis = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		dis.getRotation();
		// CommonUtil.tolog(dis.getRotation());

		// 회전 행렬값들을 저장
		SensorManager.getRotationMatrix(RTmp, I, grav, mag);
		// 축 변경(?)
		SensorManager.remapCoordinateSystem(RTmp, SensorManager.AXIS_X, SensorManager.AXIS_Z, Rot);

		SensorManager.getOrientation(Rot, ori);

		azimuth = (double) Math.toDegrees(ori[0]);
		pitch = (double) Math.toDegrees(ori[1]);
		roll = (double) Math.toDegrees(ori[2]);

		pitch = pitch * (-1);

		roll = (double) Math.toDegrees(ori[2]);

		roll += 90;

		if (roll > 180) {
			roll -= 360;
		}

		smoothAzi[smoothIndex] = (double) Math.toDegrees(ori[0]);
		smoothPit[smoothIndex] = ((double) Math.toDegrees(ori[1]) * (-1));
		smoothRol[smoothIndex] = (double) Math.toDegrees(ori[2]) + 90;

		if (smoothRol[smoothIndex] > 180) {
			smoothRol[smoothIndex] -= 360;
		}

		for (int i = 0; i < SMOOTH_ARRAY_SIZE; i++) {
			tempAzi += smoothAzi[i];
			tempPit += smoothPit[i];
			tempRol += smoothRol[i];
		}

		WorldTabActivity.azimuth = tempAzi / (double) SMOOTH_ARRAY_SIZE;
		WorldTabActivity.pitch = tempPit / (double) SMOOTH_ARRAY_SIZE;
		WorldTabActivity.roll = tempRol / (double) SMOOTH_ARRAY_SIZE;
		smoothIndex++;

		if (smoothIndex == smoothAzi.length) {
			smoothIndex = 0;
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		if (v.getId() == R.id.world_btn_back) {
			super.onBackPressed();
		}
		else if (v.getId() == R.id.world_btn_pic) {
			
			capRootView = v;
			captureMode = "Capture";
			mCamera.takePicture(shutterCallback, null, pictureCallback);
			
		}
		else if (v.getId() == R.id.world_btn_share) {
			
			captureMode = "Share";
			mCamera.takePicture(shutterCallback, null, pictureCallback);
			
			
		}

	}
	
	public static Bitmap overlay(Bitmap bmp1,Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp2.getWidth(), bmp2.getHeight(),      bmp2.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, 0,0, null);
        canvas.drawBitmap(bmp2, 0, 0, null);
        
        return bmOverlay;
    }
	
	public void captureView(Bitmap surfaceBMP){
		
		
		addedViews.setDrawingCacheEnabled(true);
		
		Bitmap bm = addedViews.getDrawingCache();
		
		Bitmap result = overlay( surfaceBMP,bm);
		
		FileOutputStream fos = null;
		String fileName = null;
		String pattern = "yyyy-MM-dd HH:mm:ss";
		
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		java.util.Date now = new java.util.Date();
		fileName = sdf.format(now);
		
		File sd = Environment.getExternalStorageDirectory();
		CommonUtil.tolog("filename = "+fileName);
		CommonUtil.tolog("sd = "+sd.toString());
		
		File file = new File(sd+"/Sky Canvas");
		
		if (! file.isDirectory()) {
			file.mkdirs();
		}
		
		String filepath = file+"/"+fileName+".png";
		
		CommonUtil.tolog("filepath = "+filepath);
		try {
			fos = new FileOutputStream(filepath);
			
			ByteArrayOutputStream str = new ByteArrayOutputStream();
			result.compress(CompressFormat.PNG, 100, str);
			
			fos.write(str.toByteArray());
			fos.flush();
			fos.close();
			Toast.makeText(this, "Save File : "+filepath, Toast.LENGTH_LONG).show();
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		if (captureMode.equals("Share")) {
			Intent picIntent = new Intent(Intent.ACTION_SEND);
			picIntent.setType("image/png");
			
			picIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filepath)));
			startActivity(Intent.createChooser(picIntent, "Share"));
		}
		
		
		
	}

	public void openCamera() {

		if (flag_openCamea == false) {
			// mCamera
			// this.set
			mCamera = Camera.open();
			cameraCurrentlyLocked = defaultCameraId;
			mPreview.setCamera(mCamera);

			verticalAngle = mPreview.getmCamera().getParameters().getVerticalViewAngle();
			horizontalAngle = mPreview.getmCamera().getParameters().getHorizontalViewAngle();

			CommonUtil.tolog("verticalAngle = " + verticalAngle);
			CommonUtil.tolog("horizontalAngle = " + horizontalAngle);
			// mPreview.getmCamera().getParameters().
			flag_openCamea = true;
		}

	}

	@Override
	public void onProgressChanged(SeekBar arg0, int value, boolean arg2) {
		// TODO Auto-generated method stub
		distance.setText(Integer.toString(value));
		hand.setScanRadius(value);
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		
		if (v.getId()==R.id.world_bottomSeekBar) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				hand.updateMarker(LocationHandler.LATITUDE, LocationHandler.LONGTITUDE);
				Toast.makeText(this, "Loading Memo...", Toast.LENGTH_LONG).show();
			}
		}
		
		return false;
	}

}
