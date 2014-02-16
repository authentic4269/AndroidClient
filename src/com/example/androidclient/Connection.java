package com.example.androidclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class Connection implements Runnable, SensorEventListener {
	private SensorManager mSensorManager;
	private Sensor mRotationVectorSensor;
	private Sensor mLinearAccelerationSensor;
	private SensorManager manager;
	private PrintWriter out;
	private BufferedReader in;
	private int count;
	private boolean notify = false;
	
	public Connection(SensorManager m)
	{
		manager = m;

	}
	
	public void run() {
		try {
			mRotationVectorSensor = manager
					.getDefaultSensor(Sensor.TYPE_ORIENTATION);
			mLinearAccelerationSensor = manager
					.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

			count = 0;

            // Make connection and initialize streams 
			// christo 158.130.169.198
			// acer 158.130.167.201
			// macbook 158.130.161.209
            Socket socket = new Socket("158.130.161.209", 3000);
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		manager.registerListener(this, mRotationVectorSensor, SensorManager.SENSOR_DELAY_GAME);
		manager.registerListener(this, mLinearAccelerationSensor, SensorManager.SENSOR_DELAY_GAME);
	}
	
	public void notifyLeftClick()
	{
		JSONObject ob = new JSONObject();
		int code;
		if (count > 3)
		{
			
			code = 3;
		}
		else 
		{
			count++;
			code = 2;
		}

		try {
			ob.put("type", code);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		out.println(ob.toString());
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		
	}

	public void onSensorChanged(SensorEvent event) {
		// we received a sensor event. it is a good practice to check
		// that we received the proper event
		JSONObject ob = new JSONObject();
		int i;
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			try {
				ob.put("type", 0);
				JSONArray arr = new JSONArray();
				for (i = 0; i < 3; i++)
					arr.put(event.values[i]);
				ob.put("data", arr);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			try {
				ob.put("type", 1);
				JSONArray arr = new JSONArray();
				for (i = 0; i < 3; i++)
					arr.put(event.values[i]);
				ob.put("data", arr);
				ob.put("time", System.currentTimeMillis());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		out.println(ob.toString());
	}

	public void notifyRightClick() {
		JSONObject ob = new JSONObject();
		try {
			ob.put("type", 4);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		out.println(ob.toString());
	}

	public void sendWheel(float dist, int i) {
		JSONObject ob = new JSONObject();
		try {
			ob.put("type", 5);
			ob.put("data", (int) (dist / 10.0));
			ob.put("down", i);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	

}
