package com.example.androidclient;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import com.androidplot.xy.*;

/**
 * Wrapper activity demonstrating the use of the new {@link SensorEvent#values
 * rotation vector sensor} ({@link Sensor#TYPE_ROTATION_VECTOR
 * TYPE_ROTATION_VECTOR}).
 * 
 * @see Sensor
 * @see SensorEvent
 * @see SensorManager
 * 
 */
public class MotionControllerActivity extends Activity {

	private Thread conn;
	private Connection connection;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int i = R.layout.activity_motion_controller;
		connection = new Connection((SensorManager)getSystemService(SENSOR_SERVICE));
		this.conn = new Thread(connection);
		setContentView(i);
		Button bl = (Button) findViewById(R.id.button1);
		Button br = (Button) findViewById(R.id.button0);
		Button wheel = (Button) findViewById(R.id.button2);
		//this.conn.start();
		
		wheel.setOnTouchListener(new OnTouchListener(){
			float lastX;
			float lastY;
			float curX;
			float curY;
			long time;
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				int e = arg1.getAction();
				if (e == MotionEvent.ACTION_DOWN)
				{
					lastX = arg1.getX();
					lastY = arg1.getY();
					time = System.currentTimeMillis();
				}
				else if (e == MotionEvent.ACTION_MOVE)
				{
					curX = arg1.getX();
					curY = arg1.getY();
					float distx = (float) ((curX - lastX) / 2.0);
					float disty = (curY - lastY);
					float dist = distx * distx + disty * disty;
					int down;
					if (curY > lastY)
					{
						connection.sendWheel(dist, 1);
					}
					else 
					{
						connection.sendWheel(dist, 0);
					}
				}
				return false;
			}
			
		});
		bl.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				connection.notifyLeftClick();
			}
		});
		br.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				connection.notifyRightClick();
			}
		});
		this.conn.start();

	}

	@Override
	protected void onResume() {
		// Ideally a game should implement onResume() and onPause()
		// to take appropriate action when the activity looses focus
		super.onResume();
	}

	@Override
	protected void onPause() {
		// Ideally a game should implement onResume() and onPause()
		// to take appropriate action when the activity looses focus
		super.onPause();

	}

	public void stop() {
	}



}