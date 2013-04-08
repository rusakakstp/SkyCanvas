package com.ssm.skycanvas.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.location.Location;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.ssm.skycanvas.activity.R;
import com.ssm.skycanvas.activity.WorldTabActivity;
import com.ssm.skycanvas.data.MarkerData;
import com.ssm.skycanvas.handler.LocationHandler;
import com.sun.java.swing.plaf.windows.resources.windows;

public class AugmentedImageView extends ImageView {

	final int ARROW_LEFT = 0;
	final int AAROW_RIGHT = 1;
	final int ARROW_UP = 2;
	final int ARROW_DOWN = 3;

	private Context context;
	private int flag = 0;
	private Display dis;

	private MarkerData marker;

	private double aziToViewAngle_X = 0;
	private double aziToViewAngleASIN_X = 0;

	private double pitchToViewAngle_Y = 0;
	private double pitchToViewAngleASIN_Y = 0;

	private double aziToViewAngle_Z = 0;
	private double aziToViewAngleASIN_Z = 0;

	private double disDivCamAngle = 0;
	private double disHalf = 0;

	private boolean chCameraAngle = false;

	private double currentAltitude = 0;
	private double currentLatitude = 0;
	private double currentLongtitude = 0;

	private double imageAltitude = 0;
	private double imageLatitude = 0;
	private double imageLongtitude = 0;

	private double zeroToImageAltitude = 0;
	private double zeroToimageLatitude = 0;
	private double zeroToimageLongtitude = 0;

	private double X_arctan = 0;
	private double Y_arctan = 0;
	private double Z_arctan = 0;

	private double zeroToImageLength = 0;

	private Bitmap image;

	private int flag_arrow_side = -1;

	public MarkerData getMarker() {
		return marker;
	}

	public void setMarker(MarkerData marker) {
		this.marker = marker;

		this.imageAltitude = marker.getAltitude();
		this.imageLatitude = marker.getLatitude();
		this.imageLongtitude = marker.getLongtitude();

		// this.setImageBitmap(marker.getPic());
	}

	public double getCurrentAltitude() {
		return currentAltitude;
	}

	public void setCurrentAltitude(double currentAltitude) {
		this.currentAltitude = currentAltitude;
	}

	public double getCurrentLatitude() {
		return currentLatitude;
	}

	public void setCurrentLatitude(double currentLatitude) {
		this.currentLatitude = currentLatitude;
	}

	public double getCurrentLongtitude() {
		return currentLongtitude;
	}

	public void setCurrentLongtitude(double currentLongtitude) {
		this.currentLongtitude = currentLongtitude;
	}

	public double getImageAltitude() {
		return imageAltitude;
	}

	public void setImageAltitude(double imageAltitude) {
		this.imageAltitude = imageAltitude;
	}

	public double getImageLatitude() {
		return imageLatitude;
	}

	public void setImageLatitude(double imageLatitude) {
		this.imageLatitude = imageLatitude;
	}

	public double getImageLongtitude() {
		return imageLongtitude;
	}

	public void setImageLongtitude(double imageLongtitude) {
		this.imageLongtitude = imageLongtitude;
	}

	Point pt;
	private refreshVisible refreshThread;
	private boolean flag_outside;

	// private

	public AugmentedImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub

		this.context = context;

	}

	public void init(Context context) {

		this.context = context;
		dis = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		// CommonUtil.tolog("wm.getWindowManager().getDefaultDisplay().getWidth() = "+wm.getWindowManager().getDefaultDisplay().getWidth());

		// Window wm =

		CommonUtil.tolog(dis.getHeight());
		CommonUtil.tolog(dis.getWidth());
		pt = new Point();

		dis.getSize(pt);
		DisplayMetrics metrics = new DisplayMetrics();

		// pt.x += WorldTabActivity.width;
		// pt.y = WorldTabActivity.height;

		// dis.getMetrics(metrics);
		CommonUtil.tolog("pt.x = " + pt.x);
		CommonUtil.tolog("pt.y = " + pt.y);
		CommonUtil.tolog("pt.y = " + (pt.y + WorldTabActivity.height));

		disHalf = pt.x / 2;
		disDivCamAngle = ((float) pt.x) / WorldTabActivity.horizontalAngle;

		// context.get

	}

	public AugmentedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public AugmentedImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// CommonUtil.tolog("-----------------start ---------------");
		// CommonUtil.tolog("this.getMarker().getMemo() = " +
		// this.getMarker().getMemo());
		//
		// CommonUtil.tolog("widthMeasureSpec = " + widthMeasureSpec);
		//
		// CommonUtil.tolog("heightMeasureSpec = " + heightMeasureSpec);
		//
		// CommonUtil.tolog("-----------------end---------------");
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
		// CommonUtil.tolog("-----------------start ---------------");
		//
		// CommonUtil.tolog("onlayout called");
		//
		// CommonUtil.tolog("-----------------end---------------");
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub

		// CommonUtil.tolog("ondraw-----------");
		int x;
		int y;

		boolean xAngleprod = true;
		boolean yAngleprod = true;

		int width = 150;
		int height = 150;

		double height_ratio = 0;

		float[] result = new float[3];

		try {

			if (flag == 0) {
				refreshThread = new refreshVisible(canvas, this);
				refreshThread.start();
				flag = 1;
			}
			currentAltitude = LocationHandler.ALTITUDE;
			currentLatitude = LocationHandler.LATITUDE;
			currentLongtitude = LocationHandler.LONGTITUDE;

			zeroToimageLongtitude = imageLongtitude - currentLongtitude;
			zeroToimageLatitude = imageLatitude - currentLatitude;
			zeroToImageAltitude = imageAltitude - currentAltitude;

			Location.distanceBetween(currentLatitude, currentLongtitude, imageLatitude, imageLongtitude, result);

			zeroToImageLength = result[0];

			X_arctan = zeroToimageLongtitude / zeroToimageLatitude;
			Y_arctan = zeroToImageAltitude / zeroToImageLength;

			height_ratio = Math.sqrt(getMarker().getArea()) / (zeroToImageLength * (Math.tan(Math.toRadians(WorldTabActivity.verticalAngle / 2))));

			height = (int) ((height_ratio) * (double) pt.y);

			// width = height;Math.sqrt(getMarker().getArea())

			// CommonUtil.tolog("-----------------start ---------------");
			//
			// CommonUtil.tolog("getMarker().getArea() = " +
			// getMarker().getArea());
			// CommonUtil.tolog("Math.sqrt(getMarker().getArea()) = " +
			// Math.sqrt(getMarker().getArea()));
			// CommonUtil.tolog("zeroToImageLength = " + zeroToImageLength);
			// CommonUtil.tolog("MainActivity.verticalAngle/2 = " +
			// WorldTabActivity.verticalAngle/2);
			// CommonUtil.tolog("Math.toRadians(MainActivity.verticalAngle/2 = "
			// + Math.toRadians(WorldTabActivity.verticalAngle/2));
			// CommonUtil.tolog("(Math.tan(Math.toRadians(MainActivity.verticalAngle/2)) = "
			// + (Math.tan(Math.toRadians(WorldTabActivity.verticalAngle/2))));
			// CommonUtil.tolog("height_ratio = " + height_ratio);
			// CommonUtil.tolog("height = " + height);
			// CommonUtil.tolog("pt.y = " + pt.y);
			// CommonUtil.tolog("((double)pt.y*(double)0.1) = " +
			// ((double)pt.y*(double)0.1));
			// CommonUtil.tolog("((double)pt.y*(double)0.8) = " +
			// ((double)pt.y*(double)0.8));
			//
			// CommonUtil.tolog("-----------------end---------------");

			if (height < ((double) pt.y * (double) 0.1)) {
				height = (int) ((double) pt.y * (double) 0.1);
			}
			else if (height > ((double) pt.y * (double) 0.8)) {
				height = (int) ((double) pt.y * (double) 0.8);
			}

			width = height;

			aziToViewAngle_X = Math.toDegrees(Math.atan(X_arctan)) - WorldTabActivity.azimuth;

			// 3 사분면
			if (zeroToimageLatitude < 0 && zeroToimageLongtitude < 0) {
				aziToViewAngle_X = (Math.toDegrees(Math.atan(X_arctan)) - 180) - WorldTabActivity.azimuth;
			}
			// 4 사분면
			else if (zeroToimageLatitude < 0 && zeroToimageLongtitude > 0) {
				aziToViewAngle_X = (Math.toDegrees(Math.atan(X_arctan)) + 180) - WorldTabActivity.azimuth;
			}

			aziToViewAngleASIN_X = Math.sin(Math.toRadians(aziToViewAngle_X));

			pitchToViewAngle_Y = WorldTabActivity.pitch - Math.toDegrees(Math.atan(Y_arctan));
			pitchToViewAngleASIN_Y = Math.sin(Math.toRadians(pitchToViewAngle_Y));

			double root = Math.sqrt(Math.pow(disHalf / Math.sin(Math.toRadians((double) WorldTabActivity.horizontalAngle / (double) 2)), 2) - Math.pow(disHalf, 2)) * Math.tan(Math.toRadians(aziToViewAngle_X));
			double root2 = Math.sqrt(Math.pow(disHalf / Math.sin(Math.toRadians((double) WorldTabActivity.verticalAngle / (double) 2)), 2) - Math.pow(disHalf, 2)) * Math.tan(Math.toRadians(pitchToViewAngle_Y));
			// x = (int) (((disHalf * 2) * aziToViewAngleASIN_X) +
			// disHalf);horizontalAngle
			x = (int) (root + disHalf);
			// y = (int) (((disHalf * 2) * pitchToViewAngleASIN_Y) + disHalf);
			y = (int) (root2 + disHalf);

			// CommonUtil.tolog("-----------------start ---------------");
			//
			// CommonUtil.tolog("this.getMarker().getMemo() = " +
			// this.getMarker().getMemo());
			//
			// // CommonUtil.tolog(" zeroToimageLongtitude = " +
			// zeroToimageLongtitude);
			// // CommonUtil.tolog(" zeroToimageLatitude = " +
			// zeroToimageLatitude);
			// // CommonUtil.tolog(" X_arctan = " + X_arctan);
			// // CommonUtil.tolog(" Math.toDegrees(Math.atan(X_arctan)) = " +
			// Math.toDegrees(Math.atan(X_arctan)));
			//
			// CommonUtil.tolog("aziToViewAngle_X = " + aziToViewAngle_X);
			// CommonUtil.tolog("aziToViewAngleASIN_X = " +
			// aziToViewAngleASIN_X);
			// CommonUtil.tolog("root = " + root);
			// CommonUtil.tolog("WorldTabActivity.verticalAngle = " +
			// WorldTabActivity.verticalAngle);
			// CommonUtil.tolog("x = " + x);
			// CommonUtil.tolog("aziToViewAngle_Y = " + pitchToViewAngle_Y);
			// CommonUtil.tolog("aziToViewAngleASIN_Y = " +
			// pitchToViewAngleASIN_Y);
			// CommonUtil.tolog("root2 = " + root2);
			// CommonUtil.tolog("WorldTabActivity.horizontalAngle = " +
			// WorldTabActivity.horizontalAngle);
			// CommonUtil.tolog("y = " + y);
			// CommonUtil.tolog("width = " + width);
			//
			// CommonUtil.tolog("-----------------end---------------");

			if (aziToViewAngle_X < 90 && aziToViewAngle_X > -90 && x > -width && x < pt.x) {
				// if (x > -width && x < pt.x ) {
				xAngleprod = true;
			}
			else {
				xAngleprod = false;
			}
			if (pitchToViewAngle_Y < 90 && pitchToViewAngle_Y > -90 && y > -height && y < pt.y) {
				// if ( y > -height && y < pt.y) {
				yAngleprod = true;
			}
			else {
				yAngleprod = false;
			}

			if (xAngleprod == true && yAngleprod == true) {

				if (flag_outside == true) {
					flag_outside= false;
					flag_arrow_side = -1;
					
					this.setImageBitmap(marker.getPic());

				}
				// CommonUtil.tolog("==========layout 동작=========");
				this.layout(x, y, x + width, y + height);
				// CommonUtil.tolog("pass if");
				// this.setsc

				// 스레드 스탑
				// 스레드가 인터럽트 걸리지 않았을때 = 스레드가 계속 돌고있을때.
			}
			else {
				// 인터럽트 걸리지 않았을경우
				// 보이지 않는 곳의 좌표가 찍힌 경우
				// 스레드 스타트
				// CommonUtil.tolog("pass not gone3");

				width = 32;
				height = 32;

				// left
				if (x < 0 && x > -2*pt.x) {

					if (flag_outside == false) {

						flag_outside = true;

						if (flag_arrow_side != ARROW_LEFT) {
							flag_arrow_side = ARROW_LEFT;
							this.setImageDrawable(context.getResources().getDrawable(R.drawable.arrow_left));
						}
					}

					this.layout(0, y, width, y + height);

				}

				// right
				else if (x > pt.x && x< pt.x + pt.x) {

					if (flag_outside == false) {

						flag_outside = true;
						if (flag_arrow_side != AAROW_RIGHT) {
							flag_arrow_side = AAROW_RIGHT;
							this.setImageDrawable(context.getResources().getDrawable(R.drawable.arrow_right));
						}
					}

					this.layout(pt.x - width, y, pt.x, y + height);
				}

				// up
				else if (x > 0 && x < pt.x && y < 0) {

					if (flag_outside == false) {

						flag_outside = true;
						if (flag_arrow_side != ARROW_UP) {
							flag_arrow_side = ARROW_UP;
							this.setImageDrawable(context.getResources().getDrawable(R.drawable.arrow_up));
						}
					}

					this.layout(x, 0, x + width, height);

				}

				// down
				else if (x > 0 && x < pt.x && y > pt.y) {

					if (flag_outside == false) {

						flag_outside = true;
						if (flag_arrow_side != ARROW_DOWN) {
							flag_arrow_side = ARROW_DOWN;
							this.setImageDrawable(context.getResources().getDrawable(R.drawable.arrow_down));
						}
					}
					
					this.layout(x, pt.y - height, x + width, pt.y);

				}

				else{
					this.layout(0,0,1,1);
				}
			}

			super.onDraw(canvas);
			// CommonUtil.tolog("ondraw end-----------");
			invalidateCustom(canvas);
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private void invalidateCustom(Canvas canvas) {
		// this.draw(canvas);
		this.invalidate();
		if (flag == 0) {
			// refreshThread.
			// this.get
			flag = 1;
			// CommonUtil.tolog("thread start ttt");

		}
	}

	private class refreshVisible extends Thread {

		Canvas canvas;
		View view;
		boolean flag = true;

		public refreshVisible(Canvas canvas, View view) {
			// TODO Auto-generated constructor stub

			this.canvas = canvas;
			this.view = view;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			while (true && !isInterrupted()) {
				if (!view.isShown() && flag == true) {

					view.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							view.setVisibility(View.VISIBLE);
							view.layout(0, 0, 1, 1);
							// CommonUtil.tolog("post");
							flag = true;
						}

					});
				}
				else {
					flag = true;

				}

				try {
					Thread.sleep(100);
					// CommonUtil.tolog("Thread sleep");
				}
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	// private class checkCameraAngle extends Thread {
	// boolean chCameraAngle = false;
	//
	// public checkCameraAngle(boolean chCameraAngle) {
	// // TODO Auto-generated constructor stub
	// this.chCameraAngle = chCameraAngle;
	// }
	//
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	// super.run();
	//
	// if (MainActivity.verticalAngle != 0.0) {
	// chCameraAngle = true;
	// this.interrupt();
	// }
	// else{
	// chCameraAngle = false;
	// }
	//
	// }
	//
	// }
}
