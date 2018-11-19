package com.quickaction3d;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.Toast;

/**
 * Gallery3D like QuickAction. 
 * 
 * This example shows how to use Gallery3D like QuickAction.
 * 
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 * 
 * Contributors:
 * - Kevin Peck <kevinwpeck@gmail.com>
 * 
 */
public class ExampleActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_new_quick_action_3d);
		
		final QuickAction quickAction = QuickActionFactory.createQuickAction(this, true,QuickAction.HORIZONTAL);
		
		//show on btn1
		Button btn1 = (Button) this.findViewById(R.id.btn1);
		btn1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				quickAction.show(v);
			}
		});

		Button btn2 = (Button) this.findViewById(R.id.btn2);
		btn2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				quickAction.show(v);
			}
		});
		
		Button btn3 = (Button) this.findViewById(R.id.btn3);
		btn3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				quickAction.show(v);
				quickAction.setAnimStyle(QuickAction.ANIM_REFLECT);
			}
		});
	}
}