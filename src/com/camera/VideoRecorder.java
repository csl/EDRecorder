package com.camera;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class VideoRecorder extends Activity{
	  
	  //Create objects of MediaRecorder and Preview class  
	  private MediaRecorder recorder;
	  private Preview mPreview;
	  
	  boolean flag=false; 
	  boolean startedRecording=false;
	  boolean stoppedRecording=false;
	  
	  private String TAG = "VideoRecorder";
	  private GLSurfaceView glsView;
	  
	  // In this method, create an object of MediaRecorder class. Create an object of 
	    // RecorderPreview class(Customized View). Add RecorderPreview class object
	    // as content of UI.     
	  public void onCreate(Bundle savedInstanceState) 
	  {
	     super.onCreate(savedInstanceState);
	     setContentView(R.layout.main);

	     recorder = new MediaRecorder();
	     recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
	     recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	     recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
	     //recorder.setMaxDuration(5000);
	     mPreview = new Preview(VideoRecorder.this, recorder);
	     setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    
	     ((FrameLayout) findViewById(R.id.preview)).addView(mPreview);

	     Button astartClick = (Button) findViewById(R.id.astartClick);
	     astartClick.setOnClickListener(new OnClickListener() {
	    	 public void onClick(View v) 
		     {
	 	        recorder.start();
		        startedRecording=true;
		     }
	     });

	     Button astopClick = (Button) findViewById(R.id.astopClick);
	     astopClick.setOnClickListener(new OnClickListener() {
	    	 public void onClick(View v) 
		     {
			      //stop the recorder
			      recorder.stop();
			      recorder.release();
			      recorder = null;
			      stoppedRecording=true;		    	 
		     }
	     });
	     
	     
	     Button startClick = (Button) findViewById(R.id.startClick);
	     startClick.setOnClickListener(new OnClickListener() {
		     public void onClick(View v) 
		     {
		    	 
		     }
	     });
	     
	     Button stopClick = (Button) findViewById(R.id.stopClick);
	     stopClick.setOnClickListener(new OnClickListener() {
	    	 public void onClick(View v) 
		     {
		    	 
		     }
	     });
	     //glsView = (GLSurfaceView) findViewById(R.id.mSurfaceView1);
	     //glsView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

	   } 
	  
	   /*!
	  <p>
	       Initialize the contents of the Activity's standard options menu. Menu items are to be placed in to menu.
	       This is called on each press of menu button. In this options to start and stop recording are provided. 
	       Option for start recording  has group id 0 and option to stop recording is 1.
	       (first parameter of menu.add method). Start and stop have different group id, if recording is already 
	       started then it shows stop option else it shows start option.
	  </p>*/   
	  @Override
	  public boolean onPrepareOptionsMenu(Menu menu) 
	  {
	    super.onPrepareOptionsMenu(menu);
	    menu.clear(); 
	    //menu.add(0, 0, 0, "Start Recording"); 
	    //menu.add(1, 1, 0, "Stop Recording");
	    menu.add(0, 0, 0, "GoogleMap Tracker");
	    
	    /*
	    menu.setGroupVisible(0, false);
	    menu.setGroupVisible(1, false);
	    
	    if(startedRecording == false)
	      menu.setGroupVisible(0, true);
	    else if(startedRecording == true && stoppedRecording == false)
	      menu.setGroupVisible(1, true);
	    */
	    
	    return true;
	  }
	 
	  
	   /*!
	    <p>
	      This method receives control when Item in menu option is selected. It contains implementations
	      to be performed on selection of menu item. 
	      </p>*/
	      
	  @Override
	  public boolean onOptionsItemSelected(MenuItem item) 
	  {
	    switch (item.getItemId()) 
	    {
	    case 0:
	          Intent open = new Intent();
	          open.setClass(VideoRecorder.this, MyGoogleMap.class);
	          startActivity(open);	    	
		  break;
	    
	    default:
	      break;
	    }
	    return super.onOptionsItemSelected(item);
	  }
	  
	  class Preview extends SurfaceView implements SurfaceHolder.Callback
	  {
	    //Create objects for MediaRecorder and SurfaceHolder.
	    SurfaceHolder mHolder;
	    MediaRecorder tempRecorder;
	 
	    //Create constructor of Preview Class. In this, get an object of 
	      //surfaceHolder class by calling getHolder() method. After that add   
	      //callback to the surfaceHolder. The callback will inform when surface is 
	      //created/changed/destroyed. Also set surface not to have its own buffers.
	    public Preview(Context context,MediaRecorder recorder) 
	    {
	      super(context);
	      tempRecorder=recorder;
	      mHolder=getHolder();
	      mHolder.addCallback(this);
	      mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	      // TODO Auto-generated constructor stub
	    }
	 
	    public Surface getSurface()
	    {
	      return mHolder.getSurface();
	    }
	    
	    // Implement the methods of SurfaceHolder.Callback interface
	 
	      // SurfaceCreated : This method gets called when surface is created.
	      // In this, initialize all parameters of MediaRecorder object.
	    //The output file will be stored in SD Card.
	    
	    public void surfaceCreated(SurfaceHolder holder){
	      
	      tempRecorder.setOutputFile("/sdcard/" + Math.random()%1000 + ".3gpp");
	      tempRecorder.setPreviewDisplay(mHolder.getSurface());
	      try{
	        tempRecorder.prepare();
	      } catch (Exception e) {
	        String message = e.getMessage();
	        tempRecorder.release();
	        tempRecorder = null;
	      }
	    }
	 
	    public void surfaceDestroyed(SurfaceHolder holder) 
	    {
	      if(tempRecorder!=null)
	      {
	        tempRecorder.release();
	        tempRecorder = null;
	      }
	    }
	 
	    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) 
	    {
	    	Log.i(TAG, w + "," + "h");
	 
	    }
	  }   
	}
