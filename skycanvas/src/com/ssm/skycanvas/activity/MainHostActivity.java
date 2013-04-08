package com.ssm.skycanvas.activity;

import com.ssm.skycanvas.handler.DataHandler;
import com.ssm.skycanvas.handler.LocationHandler;
import com.ssm.skycanvas.view.CommonUtil;

import android.app.FragmentManager;
import android.app.LocalActivityManager;
import android.app.TabActivity;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

public class MainHostActivity extends TabActivity implements OnClickListener, OnTabChangeListener {

	private TabHost tabhost;
	private DataHandler hand;
	private LocalActivityManager mlam;

	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// this.setTheme(android.R.style.Holo_ButtonBar);
		setContentView(R.layout.activity_tab);

		tabhost = getTabHost();
		// TabHost tabhost = (TabHost)findViewById(R.id.tabhost);
		// tabhost.setup(getLocalActivityManager());

		tabhost.addTab(tabhost.newTabSpec("tag").setIndicator("Draw Canvas").setContent(new Intent(this, DrawTabActivity.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)));
//		tabhost.addTab(tabhost.newTabSpec("tag1").setIndicator("S World").setContent(new Intent(this, WorldTabActivity.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)));
		tabhost.addTab(tabhost.newTabSpec("tag2").setIndicator("Map").setContent(new Intent(this, MapTabActivity.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)));

		tabhost.getTabWidget().setVisibility(View.GONE);
		tabhost.setOnTabChangedListener(this);
		
		Button btn_draw = (Button) findViewById(R.id.tab_btn_1);
		Button btn_sworld = (Button) findViewById(R.id.tab_btn_2);
		Button btn_setting = (Button) findViewById(R.id.tab_btn_3);

		btn_draw.setOnClickListener(this);
		btn_sworld.setOnClickListener(this);
		btn_setting.setOnClickListener(this);

		LinearLayout linear = (LinearLayout) findViewById(R.id.tab_btn_linear);
		WorldTabActivity.width -= linear.getLayoutParams().width;
		linear.bringToFront();

		LocationHandler gpsLocHandler = new LocationHandler(this, LocationManager.GPS_PROVIDER);
		gpsLocHandler.setDataHandle(null);
		LocationHandler netLocHandler = new LocationHandler(this, LocationManager.NETWORK_PROVIDER);
		netLocHandler.setDataHandle(null);

		mlam = new LocalActivityManager(this, true);
		mlam.dispatchCreate(savedInstanceState);
		tabhost.setup(mlam);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mlam.dispatchResume();
		CommonUtil.tolog("==========Main on resume============");
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mlam.dispatchPause(isFinishing());
		CommonUtil.tolog("==========Main on onPause============");
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		if (v.getId() == R.id.tab_btn_1) {
//			mlam.dispatchPause(isFinishing());
//			mlam.dispatchResume();
			tabhost.setCurrentTab(0);
		}
		else if (v.getId() == R.id.tab_btn_2) {
			Intent intet = new Intent(this, WorldTabActivity.class);
			startActivity(intet);
		}
		else if (v.getId() == R.id.tab_btn_3) {
//			mlam.dispatchPause(isFinishing());
//			mlam.dispatchResume();
			tabhost.setCurrentTab(1);
		}

	}

	@Override
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		
//		CommonUtil.tolog("==========on resume============");
//		if (tabId.equals(arg0)) {
//			
//		}
		
	}
}
