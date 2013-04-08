package com.ssm.skycanvas.activity;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.Cassandra.Client;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.TimedOutException;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.EventLogTags.Description;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.maps.MapActivity;
import com.samsung.samm.common.SObjectImage;
import com.samsung.samm.common.SObjectStroke;
import com.samsung.samm.common.SOptionSCanvas;
import com.samsung.spen.settings.SettingFillingInfo;
import com.samsung.spen.settings.SettingStrokeInfo;
import com.samsung.spen.settings.SettingTextInfo;
import com.samsung.spensdk.SCanvasConstants;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.ColorPickerColorChangeListener;
import com.samsung.spensdk.applistener.HistoryUpdateListener;
import com.samsung.spensdk.applistener.SCanvasInitializeListener;
import com.samsung.spensdk.applistener.SCanvasModeChangedListener;
import com.samsung.spensdk.applistener.SPenHoverListener;
import com.ssm.skycanvas.handler.DataHandler;
import com.ssm.skycanvas.handler.LocationHandler;
import com.ssm.skycanvas.view.CommonUtil;
import com.ssm.skycanvas.view.CustomDialog;
import com.ssm.skycanvas.view.PreferencesOfSAMMOption;
import com.ssm.skycanvas.view.SPenSDKUtils;

public class DrawTabActivity extends MapActivity {

	private final String TAG = "SPenSDK Sample";

	// ==============================
	// Intent Parameters
	// ==============================
	public final static String KEY_IMAGE_SAVE_PATH = "SavePath";
	public final static String KEY_IMAGE_SRC_PATH = "FilePathOrigin";
 
	// ==============================
	// Application Identifier Setting
	// "SDK Sample Application 1.0"
	// ==============================
	private final String APPLICATION_ID_NAME = "SDK Sample Application";
	private final int APPLICATION_ID_VERSION_MAJOR = 2;
	private final int APPLICATION_ID_VERSION_MINOR = 2;
	private final String APPLICATION_ID_VERSION_PATCHNAME = "Debug";
 
	// ==============================
	// Menu
	// ==============================
	private final int MENU_FILE_GROUP = 2000;
	private final int MENU_FILE_1 = 2001;
	private final int MENU_FILE_2 = 2002;

	private final int MENU_DATA_GROUP = 3000;
	private final int MENU_DATA_1 = 3001;
	private final int MENU_DATA_2 = 3002;

	// ==============================
	// Activity Request code
	// ==============================
	private final int REQUEST_CODE_INSERT_IMAGE_OBJECT = 100;

	// ==============================
	// Variables
	// ==============================
	Context mContext = null;

	private String mSrcImageFilePath = null;
	private Rect mSrcImageRect = null;

	private FrameLayout mLayoutContainer;
	private RelativeLayout mCanvasContainer;
	private SCanvasView mSCanvas;
	private ImageView mPenBtn;
	private ImageView mEraserBtn;
	private ImageView mTextBtn;
	private ImageView mFillingBtn;
	private ImageView mInsertBtn;
	private ImageView mColorPickerBtn;
	private ImageView mUndoBtn;
	private ImageView mRedoBtn;
	private Spinner mSpinner;
	private EditText mTxtField;
	private Button refresh;
	private Button upload;
	private Button complete;
	private Button backToCanvas;
	private ViewFlipper mViewFlipper;

	private ArrayList<String> mSpinnerList;

	private Client client;
	private TSocket transport;
	private TFramedTransport framedTransport;
	private TBinaryProtocol protocol;

	private GoogleMap mMap;

	private ImageView centerX;

	private String title;
	private String description;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.editor_basic_editor);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		mContext = this;

		openCassandra();

		// ------------------------------------
		// UI Setting
		// ------------------------------------
		mPenBtn = (ImageView) findViewById(R.id.penBtn);
		mPenBtn.setOnClickListener(mBtnClickListener);
		mPenBtn.setOnLongClickListener(mBtnLongClickListener);
		mEraserBtn = (ImageView) findViewById(R.id.eraseBtn);
		mEraserBtn.setOnClickListener(mBtnClickListener);
		mEraserBtn.setOnLongClickListener(mBtnLongClickListener);
		mTextBtn = (ImageView) findViewById(R.id.textBtn);
		mTextBtn.setOnClickListener(mBtnClickListener);
		mTextBtn.setOnLongClickListener(mBtnLongClickListener);
		mFillingBtn = (ImageView) findViewById(R.id.fillingBtn);
		mFillingBtn.setOnClickListener(mBtnClickListener);
		mFillingBtn.setOnLongClickListener(mBtnLongClickListener);
		mInsertBtn = (ImageView) findViewById(R.id.insertBtn);
		mInsertBtn.setOnClickListener(mInsertBtnClickListener);
		mColorPickerBtn = (ImageView) findViewById(R.id.colorPickerBtn);
		mColorPickerBtn.setOnClickListener(mColorPickerListener);

		mUndoBtn = (ImageView) findViewById(R.id.undoBtn);
		mUndoBtn.setOnClickListener(undoNredoBtnClickListener);
		mRedoBtn = (ImageView) findViewById(R.id.redoBtn);
		mRedoBtn.setOnClickListener(undoNredoBtnClickListener);
		//
		//
		mViewFlipper = (ViewFlipper) findViewById(R.id.draw_viewflipper);
		mSpinner = (Spinner) findViewById(R.id.draw_size_spinner);
		mTxtField = (EditText) findViewById(R.id.draw_altitude_txtfield);
		refresh = (Button) findViewById(R.id.draw_btn_refresh);
		refresh.setOnClickListener(mRefreshListener);
		upload = (Button) findViewById(R.id.draw_btn_upload);
		upload.setOnClickListener(mUploadListener);
		backToCanvas = (Button) findViewById(R.id.draw_backtocavnas);
		backToCanvas.setOnClickListener(mBackToCanvas);
		complete = (Button) findViewById(R.id.draw_complete);
		complete.setOnClickListener(mCompleteListener);

		centerX = (ImageView) findViewById(R.id.draw_center_x);

		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.draw_map)).getMap();
		mMap.setMyLocationEnabled(true);
		mMap.getUiSettings().setMyLocationButtonEnabled(true);

		mMap.setLocationSource(new LocationSource() {

			@Override
			public void deactivate() {
				// TODO Auto-generated method stub

			}

			@Override
			public void activate(OnLocationChangedListener l) {
				// TODO Auto-generated method stub

			}
		});

		// mMap.addMarker(new MarkerOptions().)
		// mMap.add

		// mSpinner.set
		mSpinnerList = new ArrayList<String>();
		mSpinnerList.add("10M");
		mSpinnerList.add("20M");
		mSpinnerList.add("30M");
		mSpinnerList.add("40M");
		mSpinnerList.add("50M");
		mSpinnerList.add("60M");
		mSpinnerList.add("70M");
		mSpinnerList.add("80M");
		mSpinnerList.add("90M");
		mSpinnerList.add("100M");

		SpinnerAdapter mSpinAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mSpinnerList);

		mSpinner.setAdapter(mSpinAdapter);

		// ------------------------------------
		// Create SCanvasView
		// ------------------------------------
		mLayoutContainer = (FrameLayout) findViewById(R.id.layout_container);
		mCanvasContainer = (RelativeLayout) findViewById(R.id.canvas_container);

		mSCanvas = new SCanvasView(mContext);
		mCanvasContainer.addView(mSCanvas);

		Intent intent = getIntent();
		mSrcImageFilePath = intent.getStringExtra(KEY_IMAGE_SRC_PATH);

		// If initial image exist, resize the canvas size
		if (mSrcImageFilePath != null) {
			mSrcImageRect = getMiniumCanvasRect(mSrcImageFilePath, 20);

			// Place SCanvasView In the Center
			FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mCanvasContainer.getLayoutParams();
			layoutParams.width = mSrcImageRect.right - mSrcImageRect.left;
			layoutParams.height = mSrcImageRect.bottom - mSrcImageRect.top;
			layoutParams.gravity = Gravity.CENTER;
			mCanvasContainer.setLayoutParams(layoutParams);

			// Set Background of layout container
			mLayoutContainer.setBackgroundResource(R.drawable.bg_edit);
		}

		// ------------------------------------
		// SettingView Setting
		// ------------------------------------
		// Resource Map for Layout & Locale
		HashMap<String, Integer> settingResourceMapInt = SPenSDKUtils.getSettingLayoutLocaleResourceMap(true, true, true, true);
		// Resource Map for Custom font path
		HashMap<String, String> settingResourceMapString = SPenSDKUtils.getSettingLayoutStringResourceMap(true, true, true, true);
		// Create Setting View
		mSCanvas.createSettingView(mLayoutContainer, settingResourceMapInt, settingResourceMapString);

		// ====================================================================================
		//
		// Set Callback Listener(Interface)
		//
		// ====================================================================================
		// ------------------------------------------------
		// SCanvas Listener
		// ------------------------------------------------
		mSCanvas.setSCanvasInitializeListener(new SCanvasInitializeListener() {
			@Override
			public void onInitialized() {
				// --------------------------------------------
				// Start SCanvasView/CanvasView Task Here
				// --------------------------------------------
				// Application Identifier Setting
				if (!mSCanvas.setAppID(APPLICATION_ID_NAME, APPLICATION_ID_VERSION_MAJOR, APPLICATION_ID_VERSION_MINOR, APPLICATION_ID_VERSION_PATCHNAME))
					Toast.makeText(mContext, "Fail to set App ID.", Toast.LENGTH_LONG).show();

				// Set Title
				if (!mSCanvas.setTitle("SPen-SDK Test"))
					Toast.makeText(mContext, "Fail to set Title.", Toast.LENGTH_LONG).show();

				// Update button state
				updateModeState();

				// Load the file & set Background Image
				if (mSrcImageFilePath != null) {

					if (SCanvasView.isSAMMFile(mSrcImageFilePath)) {
						loadSAMMFile(mSrcImageFilePath);
						// Set the editing rect after loading
					}
					else {
						// set BG Image
						if (!mSCanvas.setBGImagePath(mSrcImageFilePath)) {
							Toast.makeText(mContext, "Fail to set Background Image Path.", Toast.LENGTH_LONG).show();
						}
					}
				}

				// Restore last setting information
				// mSCanvas.restoreSettingViewStatus();
			}
		});

		// ------------------------------------------------
		// History Change Listener
		// ------------------------------------------------
		mSCanvas.setHistoryUpdateListener(new HistoryUpdateListener() {
			@Override
			public void onHistoryChanged(boolean undoable, boolean redoable) {
				mUndoBtn.setEnabled(undoable);
				mRedoBtn.setEnabled(redoable);
			}
		});

		// ------------------------------------------------
		// SCanvas Mode Changed Listener
		// ------------------------------------------------
		mSCanvas.setSCanvasModeChangedListener(new SCanvasModeChangedListener() {

			@Override
			public void onModeChanged(int mode) {
				updateModeState();
			}
		});

		// ------------------------------------------------
		// Color Picker Listener
		// ------------------------------------------------
		mSCanvas.setColorPickerColorChangeListener(new ColorPickerColorChangeListener() {
			@Override
			public void onColorPickerColorChanged(int nColor) {

				int nCurMode = mSCanvas.getCanvasMode();
				if (nCurMode == SCanvasConstants.SCANVAS_MODE_INPUT_PEN) {
					SettingStrokeInfo strokeInfo = mSCanvas.getSettingViewStrokeInfo();
					if (strokeInfo != null) {
						strokeInfo.setStrokeColor(nColor);
						mSCanvas.setSettingViewStrokeInfo(strokeInfo);
					}
				}
				else if (nCurMode == SCanvasConstants.SCANVAS_MODE_INPUT_ERASER) {
					// do nothing
				}
				else if (nCurMode == SCanvasConstants.SCANVAS_MODE_INPUT_TEXT) {
					SettingTextInfo textInfo = mSCanvas.getSettingViewTextInfo();
					if (textInfo != null) {
						textInfo.setTextColor(nColor);
						mSCanvas.setSettingViewTextInfo(textInfo);
					}
				}
				else if (nCurMode == SCanvasConstants.SCANVAS_MODE_INPUT_FILLING) {
					SettingFillingInfo fillingInfo = mSCanvas.getSettingViewFillingInfo();
					if (fillingInfo != null) {
						fillingInfo.setFillingColor(nColor);
						mSCanvas.setSettingViewFillingInfo(fillingInfo);
					}
				}
			}
		});

		mUndoBtn.setEnabled(false);
		mRedoBtn.setEnabled(false);
		mPenBtn.setSelected(true);
		mSCanvas.setSCanvasHoverPointerStyle(SCanvasConstants.SCANVAS_HOVERPOINTER_STYLE_SPENSDK);

		mSCanvas.setSPenHoverListener(new SPenHoverListener() {

			@Override
			public void onHoverButtonUp(View view, MotionEvent event) {
				// TODO Auto-generated method stub
				int nPreviousMode = mSCanvas.getCanvasMode();

				boolean bIncludeDefinedSetting = true;
				boolean bIncludeCustomSetting = true;
				boolean bIncludeEraserSetting = true;
				SettingStrokeInfo settingInfo = mSCanvas.getNextSettingViewStrokeInfo(bIncludeDefinedSetting, bIncludeCustomSetting, bIncludeEraserSetting);
				if (settingInfo != null) {
					if (mSCanvas.setSettingViewStrokeInfo(settingInfo)) {
						// Mode Change : Pen => Eraser
						if (nPreviousMode == SCanvasConstants.SCANVAS_MODE_INPUT_PEN && settingInfo.getStrokeStyle() == SObjectStroke.SAMM_STROKE_STYLE_ERASER) {
							// Change Mode
							mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_ERASER);
							// Show Setting View
							if (mSCanvas.isSettingViewVisible(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN)) {
								mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN, false);
								mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_ERASER, true);
							}
							updateModeState();
						}
						// Mode Change : Eraser => Pen
						if (nPreviousMode == SCanvasConstants.SCANVAS_MODE_INPUT_ERASER && settingInfo.getStrokeStyle() != SObjectStroke.SAMM_STROKE_STYLE_ERASER) {
							// Change Mode
							mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_PEN);
							// Show Setting View
							if (mSCanvas.isSettingViewVisible(SCanvasConstants.SCANVAS_SETTINGVIEW_ERASER)) {
								mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_ERASER, false);
								mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN, true);
							}
							updateModeState();
						}
					}
				}
			}

			@Override
			public void onHoverButtonDown(View view, MotionEvent event) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean onHover(View view, MotionEvent event) {
				// TODO Auto-generated method stub
				return false;
			}
		});

		// Caution:
		// Do NOT load file or start animation here because we don't know canvas
		// size here.
		// Start such SCanvasView Task at onInitialized() of
		// SCanvasInitializeListener
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Save current final setting information
		// mSCanvas.saveSettingViewStatus();

		// Release SCanvasView resources
		if (!mSCanvas.closeSCanvasView())
			Log.e(TAG, "Fail to close SCanvasView");
	}

	@Override
	public void onBackPressed() {
		SPenSDKUtils.alertActivityFinish(this, "Exit");
	}

	private OnClickListener undoNredoBtnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.equals(mUndoBtn)) {
				mSCanvas.undo();
			}
			else if (v.equals(mRedoBtn)) {
				mSCanvas.redo();
			}
			mUndoBtn.setEnabled(mSCanvas.isUndoable());
			mRedoBtn.setEnabled(mSCanvas.isRedoable());
		}
	};

	private OnClickListener mBtnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int nBtnID = v.getId();
			// If the mode is not changed, open the setting view. If the mode is
			// same, close the setting view.
			if (nBtnID == mPenBtn.getId()) {
				if (mSCanvas.getCanvasMode() == SCanvasConstants.SCANVAS_MODE_INPUT_PEN) {
					mSCanvas.setSettingViewSizeOption(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN, SCanvasConstants.SCANVAS_SETTINGVIEW_SIZE_EXT);
					mSCanvas.toggleShowSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN);
				}
				else {
					mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_PEN);
					mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN, false);
					updateModeState();
				}
			}
			else if (nBtnID == mEraserBtn.getId()) {
				if (mSCanvas.getCanvasMode() == SCanvasConstants.SCANVAS_MODE_INPUT_ERASER) {
					mSCanvas.setSettingViewSizeOption(SCanvasConstants.SCANVAS_SETTINGVIEW_ERASER, SCanvasConstants.SCANVAS_SETTINGVIEW_SIZE_NORMAL);
					mSCanvas.toggleShowSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_ERASER);
				}
				else {
					mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_ERASER);
					mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_ERASER, false);
					updateModeState();
				}
			}
			else if (nBtnID == mTextBtn.getId()) {
				if (mSCanvas.getCanvasMode() == SCanvasConstants.SCANVAS_MODE_INPUT_TEXT) {
					mSCanvas.setSettingViewSizeOption(SCanvasConstants.SCANVAS_SETTINGVIEW_TEXT, SCanvasConstants.SCANVAS_SETTINGVIEW_SIZE_NORMAL);
					mSCanvas.toggleShowSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_TEXT);
				}
				else {
					mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_TEXT);
					mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_TEXT, false);
					updateModeState();
					Toast.makeText(mContext, "Tap Canvas to insert Text", Toast.LENGTH_SHORT).show();
				}
			}
			else if (nBtnID == mFillingBtn.getId()) {
				if (mSCanvas.getCanvasMode() == SCanvasConstants.SCANVAS_MODE_INPUT_FILLING) {
					mSCanvas.setSettingViewSizeOption(SCanvasConstants.SCANVAS_SETTINGVIEW_FILLING, SCanvasConstants.SCANVAS_SETTINGVIEW_SIZE_NORMAL);
					mSCanvas.toggleShowSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_FILLING);
				}
				else {
					mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_FILLING);
					mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_FILLING, false);
					updateModeState();
					Toast.makeText(mContext, "Tap Canvas to fill color", Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	private OnLongClickListener mBtnLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {

			int nBtnID = v.getId();
			// If the mode is not changed, open the setting view. If the mode is
			// same, close the setting view.
			if (nBtnID == mPenBtn.getId()) {
				mSCanvas.setSettingViewSizeOption(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN, SCanvasConstants.SCANVAS_SETTINGVIEW_SIZE_MINI);
				if (mSCanvas.getCanvasMode() == SCanvasConstants.SCANVAS_MODE_INPUT_PEN) {
					mSCanvas.toggleShowSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN);
				}
				else {
					mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_PEN);
					mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN, true);
					updateModeState();
				}
				return true;
			}
			else if (nBtnID == mEraserBtn.getId()) {
				mSCanvas.setSettingViewSizeOption(SCanvasConstants.SCANVAS_SETTINGVIEW_ERASER, SCanvasConstants.SCANVAS_SETTINGVIEW_SIZE_MINI);
				if (mSCanvas.getCanvasMode() == SCanvasConstants.SCANVAS_MODE_INPUT_ERASER) {
					mSCanvas.toggleShowSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_ERASER);
				}
				else {
					mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_ERASER);
					mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_ERASER, true);
					updateModeState();
				}
				return true;
			}
			else if (nBtnID == mTextBtn.getId()) {
				mSCanvas.setSettingViewSizeOption(SCanvasConstants.SCANVAS_SETTINGVIEW_TEXT, SCanvasConstants.SCANVAS_SETTINGVIEW_SIZE_MINI);
				if (mSCanvas.getCanvasMode() == SCanvasConstants.SCANVAS_MODE_INPUT_TEXT) {
					mSCanvas.toggleShowSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_TEXT);
				}
				else {
					mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_TEXT);
					mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_TEXT, true);
					updateModeState();
					Toast.makeText(mContext, "Tap Canvas to insert Text", Toast.LENGTH_SHORT).show();
				}
				return true;
			}
			else if (nBtnID == mFillingBtn.getId()) {
				mSCanvas.setSettingViewSizeOption(SCanvasConstants.SCANVAS_SETTINGVIEW_FILLING, SCanvasConstants.SCANVAS_SETTINGVIEW_SIZE_MINI);
				if (mSCanvas.getCanvasMode() == SCanvasConstants.SCANVAS_MODE_INPUT_FILLING) {
					mSCanvas.toggleShowSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_FILLING);
				}
				else {
					mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_FILLING);
					mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_FILLING, true);
					updateModeState();
					Toast.makeText(mContext, "Tap Canvas to fill color", Toast.LENGTH_SHORT).show();
				}
				return true;
			}

			return false;
		}
	};

	// insert image
	private OnClickListener mInsertBtnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.equals(mInsertBtn)) {
				callGalleryForInputImage(REQUEST_CODE_INSERT_IMAGE_OBJECT);
			}
		}
	};

	// color picker mode
	private OnClickListener mColorPickerListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.equals(mColorPickerBtn)) {
				// Toggle
				boolean bIsColorPickerMode = !mSCanvas.isColorPickerMode();
				mSCanvas.setColorPickerMode(bIsColorPickerMode);
				mColorPickerBtn.setSelected(bIsColorPickerMode);
			}
		}
	};

	private OnClickListener mRefreshListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			mSCanvas.clearScreen();
		}
	};

	private OnClickListener mUploadListener = new OnClickListener() {

		private ConsistencyLevel consistencyLevel = ConsistencyLevel.ONE;

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			if (mTxtField.getText().toString().equals("")) {
				
				Toast.makeText(DrawTabActivity.this, "Please input altitude.", Toast.LENGTH_LONG).show();
			}
			else {

				final CustomDialog dialog = new CustomDialog(DrawTabActivity.this);
				dialog.show();
				dialog.setOnDismissListener(new OnDismissListener() {

					@Override
					public void onDismiss(DialogInterface v) {
						// TODO Auto-generated method stub
						if (dialog.isFlag_ok()) {
							mViewFlipper.showNext();

							title = dialog.getStrTitle();
							description = dialog.getStrDescription();

							mMap.clear();

							MarkerOptions marker = new MarkerOptions();
							LatLng myLocation = new LatLng(LocationHandler.LATITUDE, LocationHandler.LONGTITUDE);
							CommonUtil.tolog("LocationHandler.LATITUDE = " + LocationHandler.LATITUDE);
							CommonUtil.tolog("LocationHandler.LONGTITUDE = " + LocationHandler.LONGTITUDE);
							marker.position(myLocation);
							mMap.addMarker(marker);
							CameraUpdate update = CameraUpdateFactory.newLatLngZoom(myLocation, 16);
							mMap.moveCamera(update);

						}
						else {

						}
					}
				});

			}
		}
	};

	private OnClickListener mCompleteListener = new OnClickListener() {

		ConsistencyLevel consistencyLevel = ConsistencyLevel.ONE;

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			UUID rowkey = UUID.randomUUID();

			long time = System.currentTimeMillis();

			Point center = new Point((int) (centerX.getX() + (centerX.getWidth() / 2)), (int) (centerX.getY() + (centerX.getHeight() / 2)));

			CommonUtil.tolog("center.x = " + center.x);
			CommonUtil.tolog("center.y = " + center.y);

			LatLng currnetLatLng = mMap.getProjection().fromScreenLocation(center);

			Bitmap bm = mSCanvas.getBitmap(true);
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			bm.compress(CompressFormat.PNG, 10, byteArray);
			byte[] bytes = byteArray.toByteArray();

			BitmapFactory.Options option = new Options();
			option.inSampleSize = 4;

			Bitmap resized = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, option);
			ByteArrayOutputStream byteArray2 = new ByteArrayOutputStream();
			resized.compress(CompressFormat.PNG, 10, byteArray2);
			byte[] bytes2 = byteArray2.toByteArray();
			// Bitmap.
			// bm.

			Column userid = new Column();
			userid.setName("userid".getBytes());
			userid.setValue("admin".getBytes());
			userid.setTimestamp(time);

			Column activated = new Column();
			activated.setName("activated".getBytes());
			activated.setValue("1".getBytes());
			activated.setTimestamp(time);

			Column latitude = new Column();
			latitude.setName("latitude".getBytes());
			latitude.setValue(ByteBufferUtil.bytes(currnetLatLng.latitude).array());
			latitude.setTimestamp(time);

			Column longtitude = new Column();
			longtitude.setName("longtitude".getBytes());
			longtitude.setValue(ByteBufferUtil.bytes(currnetLatLng.longitude).array());
			longtitude.setTimestamp(time);

			Column altitude = new Column();
			altitude.setName("altitude".getBytes());
			altitude.setValue(ByteBufferUtil.bytes(Double.parseDouble(mTxtField.getText().toString())).array());
			altitude.setTimestamp(time);

			Column area = new Column();
			area.setName("area".getBytes());
			area.setValue(ByteBufferUtil.bytes(Math.pow(((mSpinner.getSelectedItemPosition() + 1) * 10), 2)).array());
			area.setTimestamp(time);

			Column title = new Column();
			title.setName("title".getBytes());
			title.setValue(DrawTabActivity.this.title.getBytes());
			title.setTimestamp(time);

			Column memo = new Column();
			memo.setName("memo".getBytes());
			memo.setValue(description.getBytes());
			memo.setTimestamp(time);

			Column picdata = new Column();
			picdata.setName("picdata".getBytes());
			picdata.setValue(bytes2);
			picdata.setTimestamp(time);

			try {
				client.insert(ByteBuffer.wrap(DataHandler.asByteArray(rowkey)), parent, picdata, consistencyLevel);
				client.insert(ByteBuffer.wrap(DataHandler.asByteArray(rowkey)), parent, userid, consistencyLevel);
				client.insert(ByteBuffer.wrap(DataHandler.asByteArray(rowkey)), parent, activated, consistencyLevel);
				client.insert(ByteBuffer.wrap(DataHandler.asByteArray(rowkey)), parent, latitude, consistencyLevel);
				client.insert(ByteBuffer.wrap(DataHandler.asByteArray(rowkey)), parent, longtitude, consistencyLevel);
				client.insert(ByteBuffer.wrap(DataHandler.asByteArray(rowkey)), parent, altitude, consistencyLevel);
				client.insert(ByteBuffer.wrap(DataHandler.asByteArray(rowkey)), parent, area, consistencyLevel);
				client.insert(ByteBuffer.wrap(DataHandler.asByteArray(rowkey)), parent, title, consistencyLevel);
				client.insert(ByteBuffer.wrap(DataHandler.asByteArray(rowkey)), parent, memo, consistencyLevel);
			}
			catch (InvalidRequestException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (UnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (TimedOutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (TException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			mViewFlipper.showPrevious();
			Toast.makeText(DrawTabActivity.this, "Upload Complete.", Toast.LENGTH_LONG).show();
		}
	};

	private OnClickListener mBackToCanvas = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			mViewFlipper.showPrevious();
		}
	};

	private ColumnParent parent;

	// Update tool button
	private void updateModeState() {
		int nCurMode = mSCanvas.getCanvasMode();
		mPenBtn.setSelected(nCurMode == SCanvasConstants.SCANVAS_MODE_INPUT_PEN);
		mEraserBtn.setSelected(nCurMode == SCanvasConstants.SCANVAS_MODE_INPUT_ERASER);
		mTextBtn.setSelected(nCurMode == SCanvasConstants.SCANVAS_MODE_INPUT_TEXT);
		mFillingBtn.setSelected(nCurMode == SCanvasConstants.SCANVAS_MODE_INPUT_FILLING);

		// Reset color picker tool when Eraser Mode
		if (nCurMode == SCanvasConstants.SCANVAS_MODE_INPUT_ERASER)
			mSCanvas.setColorPickerMode(false);
		mColorPickerBtn.setEnabled(nCurMode != SCanvasConstants.SCANVAS_MODE_INPUT_ERASER);
		mColorPickerBtn.setSelected(mSCanvas.isColorPickerMode());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			if (data == null)
				return;

			if (requestCode == REQUEST_CODE_INSERT_IMAGE_OBJECT) {
				Uri imageFileUri = data.getData();
				String imagePath = SPenSDKUtils.getRealPathFromURI(this, imageFileUri);
				insertImageObject(imagePath);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		SubMenu fileMenu = menu.addSubMenu("File");
		fileMenu.add(MENU_FILE_GROUP, MENU_FILE_1, 1, "Menu1");
		fileMenu.add(MENU_FILE_GROUP, MENU_FILE_2, 2, "Menu2");

		SubMenu dataMenu = menu.addSubMenu("Data");
		dataMenu.add(MENU_DATA_GROUP, MENU_DATA_1, 1, "Data1");
		dataMenu.add(MENU_DATA_GROUP, MENU_DATA_2, 2, "Data2");

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		super.onMenuOpened(featureId, menu);

		if (menu == null)
			return true;

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case MENU_FILE_1:
			Toast.makeText(mContext, "File1 menu item selected.", Toast.LENGTH_SHORT).show();
			break;
		case MENU_FILE_2:
			Toast.makeText(mContext, "File2 menu item selected.", Toast.LENGTH_SHORT).show();
			break;
		case MENU_DATA_1:
			Toast.makeText(mContext, "Data1 menu item selected.", Toast.LENGTH_SHORT).show();
			break;
		case MENU_DATA_2:
			Toast.makeText(mContext, "Data2 menu item selected.", Toast.LENGTH_SHORT).show();
			break;
		}
		return true;
	}

	// Call Gallery
	private void callGalleryForInputImage(int nRequestCode) {
		try {
			Intent galleryIntent;
			galleryIntent = new Intent();
			galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
			galleryIntent.setType("image/*");
			galleryIntent.setClassName("com.cooliris.media", "com.cooliris.media.Gallery");
			startActivityForResult(galleryIntent, nRequestCode);
		}
		catch (ActivityNotFoundException e) {
			Intent galleryIntent;
			galleryIntent = new Intent();
			galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
			galleryIntent.setType("image/*");
			startActivityForResult(galleryIntent, nRequestCode);
			e.printStackTrace();
		}
	}

	// Get the minimum image scaled rect which is fit to current screen
	Rect getMiniumCanvasRect(String strImagePath, int nMargin) {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(displayMetrics);
		int nScreenWidth = displayMetrics.widthPixels - nMargin * 2;
		int nScreenHeight = displayMetrics.heightPixels - nMargin * 2;

		// Make more small for screen rotation T.T
		if (nScreenWidth < nScreenHeight)
			nScreenHeight = nScreenWidth;
		else
			nScreenWidth = nScreenHeight;

		int nImageWidth = nScreenWidth;
		int nImageHeight = nScreenHeight;
		if (strImagePath != null) {
			BitmapFactory.Options opts = SPenSDKUtils.getBitmapSize(strImagePath);
			nImageWidth = opts.outWidth;
			nImageHeight = opts.outHeight;
		}

		float fResizeWidth = (float) nScreenWidth / nImageWidth;
		float fResizeHeight = (float) nScreenHeight / nImageHeight;
		float fResizeRatio;

		// Fit to Height
		if (fResizeWidth > fResizeHeight) {
			fResizeRatio = fResizeHeight;
		}
		// Fit to Width
		else {
			fResizeRatio = fResizeWidth;
		}

		return new Rect(0, 0, (int) (nImageWidth * fResizeRatio), (int) (nImageHeight * fResizeRatio));
	}

	// Load SAMM file
	boolean loadSAMMFile(String strFileName) {
		if (mSCanvas.isAnimationMode()) {
			// It must be not animation mode.
		}
		else {
			// set progress dialog
			mSCanvas.setProgressDialogSetting("Loading", "Please wait while loading...", ProgressDialog.STYLE_HORIZONTAL, false);

			// canvas option setting
			SOptionSCanvas canvasOption = mSCanvas.getOption();
			if (canvasOption == null)
				return false;
			canvasOption.mSAMMOption.setConvertCanvasSizeOption(PreferencesOfSAMMOption.getPreferenceLoadCanvasSize(mContext));
			canvasOption.mSAMMOption.setConvertCanvasHorizontalAlignOption(PreferencesOfSAMMOption.getPreferenceLoadCanvasHAlign(mContext));
			canvasOption.mSAMMOption.setConvertCanvasVerticalAlignOption(PreferencesOfSAMMOption.getPreferenceLoadCanvasVAlign(mContext));
			// option setting
			mSCanvas.setOption(canvasOption);

			// show progress for loading data
			if (mSCanvas.loadSAMMFile(strFileName, true, true, true)) {
				// Loading Result can be get by callback function
			}
			else {
				Toast.makeText(this, "Load AMS File(" + strFileName + ") Fail!", Toast.LENGTH_LONG).show();
				return false;
			}
		}
		return true;
	}

	// insert Image Object
	boolean insertImageObject(String imagePath) {
		// Check Valid Image File
		if (!SPenSDKUtils.isValidImagePath(imagePath)) {
			Toast.makeText(this, "Invalid image path or web image", Toast.LENGTH_LONG).show();
			return false;
		}

		RectF rectF = getDefaultImageRect(imagePath);
		SObjectImage sImageObject = new SObjectImage();
		sImageObject.setRect(rectF);
		sImageObject.setImagePath(imagePath);

		// canvas option setting
		SOptionSCanvas canvasOption = mSCanvas.getOption();
		if (canvasOption == null)
			return false;
		canvasOption.mSAMMOption.setContentsQuality(PreferencesOfSAMMOption.getPreferenceSaveImageQuality(mContext));
		// option setting
		mSCanvas.setOption(canvasOption);

		if (mSCanvas.insertSAMMImage(sImageObject, true)) {
			// Toast.makeText(this, "Insert image file("+ imagePath
			// +") Success!", Toast.LENGTH_SHORT).show();
			return true;
		}
		else {
			Toast.makeText(this, "Insert image file(" + imagePath + ") Fail!", Toast.LENGTH_LONG).show();
			return false;
		}
	}

	// get default image rect
	RectF getDefaultImageRect(String strImagePath) {
		// Rect Region : Consider image real size
		BitmapFactory.Options opts = SPenSDKUtils.getBitmapSize(strImagePath);
		int nImageWidth = opts.outWidth;
		int nImageHeight = opts.outHeight;
		int nScreenWidth = mSCanvas.getWidth();
		int nScreenHeight = mSCanvas.getHeight();
		int nBoxRadius = (nScreenWidth > nScreenHeight) ? nScreenHeight / 4 : nScreenWidth / 4;
		int nCenterX = nScreenWidth / 2;
		int nCenterY = nScreenHeight / 2;
		if (nImageWidth > nImageHeight)
			return new RectF(nCenterX - nBoxRadius, nCenterY - (nBoxRadius * nImageHeight / nImageWidth), nCenterX + nBoxRadius, nCenterY + (nBoxRadius * nImageHeight / nImageWidth));
		else
			return new RectF(nCenterX - (nBoxRadius * nImageWidth / nImageHeight), nCenterY - nBoxRadius, nCenterX + (nBoxRadius * nImageWidth / nImageHeight), nCenterY + nBoxRadius);
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

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}
