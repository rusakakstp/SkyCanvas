package com.ssm.skycanvas.view;

import java.awt.TextField;

import com.ssm.skycanvas.activity.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CustomDialog extends Dialog implements android.view.View.OnClickListener {

	private EditText edit_title;
	private EditText edit_description;
	
	private String strTitle;
	private String strDescription;
	
	
	private Button btn_ok;
	private Button btn_cancel;
	private boolean flag_ok = false;

	public CustomDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub

		setContentView(R.layout.custom_dialog);

		edit_title = (EditText) findViewById(R.id.dialog_title);
		edit_description = (EditText) findViewById(R.id.dialog_description);

		btn_ok = (Button) findViewById(R.id.dialog_btn_ok);
		btn_ok.setOnClickListener(this);
		btn_cancel = (Button) findViewById(R.id.dialog_btn_cancel);
		btn_cancel.setOnClickListener(this);

	}


	public String getStrTitle() {
		return strTitle;
	}

	public void setStrTitle(String strTitle) {
		this.strTitle = strTitle;
	}


	public String getStrDescription() {
		return strDescription;
	}

	public void setStrDescription(String strDescription) {
		this.strDescription = strDescription;
	}


	public boolean isFlag_ok() {
		return flag_ok;
	}

	public void setFlag_ok(boolean flag_ok) {
		this.flag_ok = flag_ok;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.dialog_btn_ok) {
			this.flag_ok = true;
			setStrTitle(edit_title.getText().toString());
			setStrDescription(edit_description.getText().toString());
			this.dismiss();
		}
		else {
			this.flag_ok = false;
			this.dismiss();
		}
	}

}
