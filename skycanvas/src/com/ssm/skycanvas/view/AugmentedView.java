package com.ssm.skycanvas.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import android.content.Context;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;

public class AugmentedView extends ViewGroup {
	
	ArrayList<AugmentedView.ViewNFlag> viewNflag;
	private View view;
	
	
	public AugmentedView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub

		// new reLayout(this).start();
		viewNflag = new ArrayList<AugmentedView.ViewNFlag>();
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
//		CommonUtil.tolog("-----------------start ---------------");
//		
//		CommonUtil.tolog("widthMeasureSpec = " + widthMeasureSpec);
//
//		CommonUtil.tolog("heightMeasureSpec = " + heightMeasureSpec);
//		
//		CommonUtil.tolog("-----------------end---------------");
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		int count = this.getChildCount();
		int viewIndex = 0;
		View tempView;
		
		for (int i = 0; i < count; i++) {
			
			tempView = this.getChildAt(i);
			for (int j = 0; j < viewNflag.size(); j++) {
				if (viewNflag.get(j).getView() == tempView) {
					viewIndex = j;
				}
			}
			
			if (viewNflag.get(viewIndex).getFlag() == 0) {
				tempView.layout(0, 0, 1, 1);
				viewNflag.get(viewIndex).setFlag(1);
				CommonUtil.tolog("onlayout augview");
			}
		}
		
	}

	@Override
	protected void onAttachedToWindow() {
		// TODO Auto-generated method stub
		super.onAttachedToWindow();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		this.invalidate();
	}
	@Override
	public void addView(View child, LayoutParams params) {
		// TODO Auto-generated method stub
		super.addView(child, params);
		
		viewNflag.add(new ViewNFlag(child,0));
		CommonUtil.tolog("addview");
	}
	@Override
	public void addView(View child, int index, LayoutParams params) {
		// TODO Auto-generated method stub
		super.addView(child, index, params);
		
		viewNflag.add(new ViewNFlag(child,0));
		CommonUtil.tolog("addview");
	}
	
	@Override
	public void addView(View child) {
		// TODO Auto-generated method stub
		super.addView(child);
		
		viewNflag.add(new ViewNFlag(child,0));
		CommonUtil.tolog("addview");
	}
	
	
	private class ViewNFlag {
		View view=null;
		int flag=0;
		
		public ViewNFlag(View view, int flag) {
			// TODO Auto-generated constructor stub
			this.view=view;
			this.flag = flag;
		}
		
		
		public View getView() {
			return view;
		}
		public void setView(View view) {
			this.view = view;
		}
		public int getFlag() {
			return flag;
		}
		public void setFlag(int flag) {
			this.flag = flag;
		}
		
		
	}
	

}
