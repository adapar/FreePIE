package com.example.freepie.android.imu;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.CyclicBarrier;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class UdpSenderTask implements SensorEventListener {

	float[] acc;
	float[] mag;
	float[] gyr;
	float[] imu;
	
	float[] orientation = new float[3];	
	
	float[] inR = new float[16];
	float[] I = new float[16];

	
	DatagramSocket socket;
	InetAddress endPoint;
	int port;
	boolean sendOrientation;
	boolean sendRaw;
	byte sendFlag;
	
	ByteBuffer buffer;
	CyclicBarrier sync;
	
	Thread worker;
	boolean running;

	public void start(TargetSettings target) {
		final SensorManager sensorManager = target.getSensorManager();		
		sendRaw = target.getSendRaw();
		sendOrientation = target.getSendOrientation();
		
		sendFlag = (byte)((sendRaw ? 0x01 : 0x00) | (sendOrientation ? 0x02 : 0x00)); 
		
		sync = new CyclicBarrier(2);
			
		buffer = ByteBuffer.allocate(49);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		try {	
			endPoint = InetAddress.getByName(target.getToIp());
			port = target.getPort();
			socket = new DatagramSocket();
		}
		catch(Exception e) {			
		}
					
		running = true;
		worker = new Thread(new Runnable() { 
            public void run(){
        		while(running) {
        			try {
        			sync.await();
        			} catch(Exception e) {}
        			
        			Send();
        		}
        		try  {
        			socket.disconnect();
        		}
        		catch(Exception e)  {}
        	}
		});	
		worker.start();

		
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);
		
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
				SensorManager.SENSOR_DELAY_FASTEST);		
		
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_FASTEST);	
	}
	
	public void onSensorChanged(SensorEvent sensorEvent) {
	    switch (sensorEvent.sensor.getType()) {  
	        case Sensor.TYPE_ACCELEROMETER:
	            acc = sensorEvent.values.clone();
	            break;
	        case Sensor.TYPE_MAGNETIC_FIELD:
	            mag = sensorEvent.values.clone();
	            break;
	            
	        case Sensor.TYPE_GYROSCOPE:
	            gyr = sensorEvent.values.clone();
	            break;
	    }

	    if (sendOrientation && sensorEvent.accuracy != SensorManager.SENSOR_STATUS_UNRELIABLE && acc != null && mag != null) {
	        boolean success = SensorManager.getRotationMatrix(inR, I, acc, mag);
	        if (success) {
	            SensorManager.getOrientation(inR, orientation);
	            imu = orientation.clone();
	        }
	    }
		
		if(sync.getNumberWaiting() > 0)
			sync.reset();			
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	public void stop() {
		running = false;
	}

	private void Send() {
		buffer.clear();			
		
		buffer.put(sendFlag);
		
		if(sendRaw && acc != null && mag != null && gyr != null) {
			//Acc
			buffer.putFloat(acc[0]);
			buffer.putFloat(acc[1]);
			buffer.putFloat(acc[2]);
			
			//Gyro
			buffer.putFloat(gyr[0]);
			buffer.putFloat(gyr[1]);
			buffer.putFloat(gyr[2]);	
			
			//Mag
			buffer.putFloat(mag[0]);
			buffer.putFloat(mag[1]);
			buffer.putFloat(mag[2]);
		}
		
		if(sendOrientation && imu != null) {		
			//Orientation
			buffer.putFloat(imu[0]);
			buffer.putFloat(imu[1]);
			buffer.putFloat(imu[2]);
		}		
      				
		byte[] arr = buffer.array();
	    DatagramPacket p = new DatagramPacket(arr, arr.length, endPoint, port);	    
	    try {
	    	socket.send(p);
	    }
	    catch(IOException w) {
	    	
	    }
	}
}