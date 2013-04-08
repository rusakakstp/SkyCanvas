package com.ssm.skycanvas.handler;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.ssm.skycanvas.data.MarkerData;
import com.ssm.skycanvas.view.AugmentedImageView;
import com.ssm.skycanvas.view.AugmentedView;
import com.ssm.skycanvas.view.CommonUtil;

public class AugmentedViewHandler {

	private Context context;
	private View view;
	private AugmentedView augView;
	private ArrayList<AugmentedImageView> imageArray;
	
	private int index = 0;
	

	public ArrayList<AugmentedImageView> getImageArray() {
		return imageArray;
	}

	public void setImageArray(ArrayList<AugmentedImageView> imageArray) {
		this.imageArray = imageArray;
	}

	public AugmentedViewHandler(Context context) {
		// TODO Auto-generated constructor stub

		this.context = context;

		augView = new AugmentedView(context);
		augView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		augView.setAlwaysDrawnWithCacheEnabled(true);
		augView.buildLayer();
		augView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
		augView.setPersistentDrawingCache(ViewGroup.PERSISTENT_ALL_CACHES);
		
		imageArray = new ArrayList<AugmentedImageView>();
	}
	
	public void addview(MarkerData marker){
		
		AugmentedImageView image2 = new AugmentedImageView(context);
		image2.setImageBitmap(marker.getPic());
		image2.setOnClickListener((OnClickListener) context);
		image2.setMarker(marker);
		image2.init(context);
		imageArray.add(image2);
		
		augView.addView(image2);
		
//		marker.getPic().recycle();
		index++;
	}
	
	public void removeView(MarkerData marker){
		Object[] augArray =  imageArray.toArray();
		
		for (Object image :augArray) {
			if (((AugmentedImageView) image).getMarker() == marker) {
				augView.removeView(((AugmentedImageView) image));
			}
		}
		
	}

	public AugmentedView getAugView() {
		return augView;
	}

	public void setAugView(AugmentedView augView) {
		this.augView = augView;
	}
	
//	public void addViewToAugView(View view)	{
//		getAugView().addView(view, new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//		CommonUtil.tolog("child count = "+getAugView().getChildCount());
//	}

	private class refreshThread extends Thread{
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
		}
	}
}