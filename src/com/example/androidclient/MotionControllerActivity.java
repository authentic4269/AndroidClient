package com.example.androidclient;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.method.Touch;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;

import android.graphics.*;
import android.util.*;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

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
		showServers();
	}
	
	private class st {
		
		public String name;
		public String ip;
		public st(String name, String ip)
		{
			this.name = name; this.ip = ip;
		}
	}
	
	private class MyListAdapter extends ArrayAdapter<st> {

		private ArrayList<st> servers;
		private Context c;
		
		@Override
		public View getView(int i, View v, ViewGroup p)
		{
			LayoutInflater inf = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowview = inf.inflate(R.layout.server_option, p, false);
			TextView name = (TextView) rowview.findViewById(R.id.name);
			TextView ip = (TextView) rowview.findViewById(R.id.ip);
			name.setText(servers.get(i).name + ": ");
			ip.setText(servers.get(i).ip);
			return rowview;
		}
		
		public MyListAdapter(ArrayList<st> l,
				int activityListItem, Context context) {
			super(context, activityListItem, l);
			servers = l;
			c = context;
		}

		@Override
		public int getCount() {
			return servers.size();
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}
		


	}
	
	private void showServers() {
		int s = R.layout.activity_server;
		setContentView(s);
		ListView list = (ListView) findViewById(R.id.listView1);
		final ArrayList<st> l = new ArrayList<st>();
		String names[] = {"Christo Macbook", "Mitch Acer", "Mitch Macbook"};
		String ips[] = {"158.130.169.198", "158.130.167.201", "158.130.161.209"};
		for (int i = 0; i < names.length; i++)
		{
			l.add(new st(names[i], ips[i]));
		}
		list.setAdapter(new MyListAdapter(l, R.layout.server_option, this.getApplicationContext()));
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				final st item = (st) arg0.getItemAtPosition(arg2);
				startController(item.ip);
			}
			
		});
	}

	public void startController(String servername) {
		connection = new Connection((SensorManager)getSystemService(SENSOR_SERVICE), servername,
				new SocketError(this.getApplicationContext()));
		int i = R.layout.activity_motion_controller;
		this.conn = new Thread(connection);
		this.conn.start();
		setContentView(i);
		Button bl = (Button) findViewById(R.id.button1);
		Button br = (Button) findViewById(R.id.button0);
		Button wheel = (Button) findViewById(R.id.button2);
		Button touch = (Button) findViewById(R.id.touch);
		
		
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
						connection.sendWheel(dist * 2, 1);
					}
					else 
					{
						connection.sendWheel(dist * 2, 0);
					}
					lastX = curX;
					lastY = curY;
					
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
		/*touch.setOnTouchListener(new OnTouchListener() {
			private SparseArray<PointF> activepointers = new SparseArray<PointF>();
			private float dist = 0;
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				int pointeridx = arg1.getActionIndex();
				int pointerId = arg1.getPointerId(pointeridx);
				int maskedAction = arg1.getActionMasked();
				switch (maskedAction) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_DOWN:
					PointF f = new PointF();
					f.x = arg1.getX(pointeridx);
					f.y = arg1.getY(pointeridx);
					activepointers.put(pointerId,  f);
					if (activepointers.size() == 2)
					{
						PointF p0 = activepointers.get(0);
						PointF p1 = activepointers.get(1);
						float xd = p1.x - p0.x;
						float yd = p1.y - p0.y;
						dist = (xd * xd) + (yd * yd);
					}
					return true;
				case MotionEvent.ACTION_MOVE:
					int j = 0;
					for (int size = 2; j < size; j++)
					{
						PointF point = activepointers.get(arg1.getPointerId(j));
						if (point != null)
						{
							point.x = arg1.getX(j);
							point.y = arg1.getY(j);
						}
					}
					if (activepointers.size() == 2)
					{
						PointF p0 = activepointers.get(0);
						PointF p1 = activepointers.get(1);
						float xd = p1.x - p0.x;
						float yd = p1.y - p0.y;
						float newdist = (xd * xd) + (yd * yd);
						connection.notifyZoom(newdist - dist);
						dist = newdist;
						
					}
					return true;
					activepointers.remove(pointerId);
				default: {
				}
					
				}
				return false;
				
			}
			
		});*/
		
		
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