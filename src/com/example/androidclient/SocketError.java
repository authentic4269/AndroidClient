package com.example.androidclient;

import android.content.Context;
import android.widget.Toast;

public class SocketError {
	
	Context context;
	public SocketError(Context c)
	{
		context = c;
	}
	
	public void run(String message) {
		Toast.makeText(context, "An error occurred while connecting to the server: " + message, Toast.LENGTH_SHORT);
	}
}
