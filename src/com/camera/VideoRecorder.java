package com.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
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
import android.widget.TextView;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.net.ftp.FTPClient;

public class VideoRecorder extends Activity implements SensorListener  {
	  
	  //Create objects of MediaRecorder and Preview class  
	  private MediaRecorder recorder;
	  private Preview mPreview;
	  private TextView timer_view;
	  
	  private int SEC = 15;
	  private int CUT_SEC = 60;

	  boolean flag=false; 
	  boolean startedRecording=false;
	  boolean stoppedRecording=false;
	  
	  private String TAG = "VideoRecorder";
	  private GLSurfaceView glsView;
	  private Timer timer;
	  private Timer cut_timer;

	  private long startTime;
	  private long stopTime;
	  private FileTagStruct newtag;
	  static ArrayList<FileTagStruct> fp;
	  
	  final AtomicBoolean started = new AtomicBoolean(false);
	  
	  SensorManager sensorMgr;
	  private long lastUpdate;
	  float last_x, last_y, last_z;
	  boolean upload_t;
	  
	  boolean auto_load = false;
	  
	  private static final int SHAKE_THRESHOLD = 5000;  
	  // In this method, create an object of MediaRecorder class. Create an object of 
	    // RecorderPreview class(Customized View). Add RecorderPreview class object
	    // as content of UI.     
	  public void onCreate(Bundle savedInstanceState) 
	  {
	     super.onCreate(savedInstanceState);
	     setContentView(R.layout.main);
	     
	     upload_t = false;

	     //12. 震動時加入
	     sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);  
		 sensorMgr.registerListener(this, SensorManager.SENSOR_ACCELEROMETER, SensorManager.SENSOR_DELAY_GAME);  

		 //1. 最簡易的錄影功能
	     recorder = new MediaRecorder();
	     //recorder.setMaxDuration(5000);
	     mPreview = new Preview(VideoRecorder.this, recorder);

	     //4. 清單管使用
	     fp = new ArrayList<FileTagStruct>();

	     
		 //1. 最簡易的錄影功能
	     setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	     ((FrameLayout) findViewById(R.id.preview)).addView(mPreview);
	     timer_view = (TextView) findViewById(R.id.timer_view);
	     Button astartClick = (Button) findViewById(R.id.astartClick);
	     astartClick.setOnClickListener(new OnClickListener() {
	    	 public void onClick(View v) 
		     {
	    		//2.開始&結束錄影功能 
	    		startRec();	    		
	    		//3.循環錄影 
	   	     	timer = new Timer();
		        timer.schedule(new DateTask(), SEC * 1000, SEC * 1000);
	    		//7. 若沒tag則錄影檔案清除 
	   	     	cut_timer = new Timer();
		        cut_timer.schedule(new Cut_DateTask(), CUT_SEC * 1000, CUT_SEC * 1000);
		        //5. 加入tag功能
		        startTime = System.currentTimeMillis();
		     }
	     });

	     Button astopClick = (Button) findViewById(R.id.astopClick);
	     astopClick.setOnClickListener(new OnClickListener() {
	    	 public void onClick(View v) 
		     {
		    		//2.開始&結束錄影功能 
		    		stopRec();
		    		timer.cancel();
		     }
	     });
	     
	     Button tag = (Button) findViewById(R.id.tag);
	     tag.setOnClickListener(new OnClickListener() {
	    	 public void onClick(View v) 
		     {
 		         //5. 加入tag功能
	    		 stopTime = System.currentTimeMillis() - startTime;
	    		 if (newtag != null)
	    		 {
	    			 //ms -> sec
	    			 //6. 結構加入tag支援
		    		 newtag.tag.add((int) stopTime / 1000);
		    		 timer_view.setText(Integer.toString((int) stopTime/1000) + " sec");
	    		 }
	    		 //upload
	    		 if (auto_load == true)
	    			 upload_t = true;
	    		
		     }
	     });
	     
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
	    menu.add(0, 0, 0, "GoogleMap Tracker");
        //4. 清單管使用
	    menu.add(1, 1, 1, "TAG");
	    menu.add(2, 2, 2, "EXIT");
	    
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
	    case 1:
	    	if (fp.size() != 0)
	    	{
	          open = new Intent();
	          open.setClass(VideoRecorder.this, edlist.class);
	          startActivity(open);
	    	}
		  break;
	    case 2:
	    	  //11. 結束程式修正
	          android.os.Process.killProcess(android.os.Process.myPid());
	          finish(); 
	          break;
	    
	    default:
	      break;
	    }
	    return super.onOptionsItemSelected(item);
	  }

	  
      //12. 震動時加入
	  public void onSensorChanged(int sensor, float[] values) 
	  {
		  if (sensor == SensorManager.SENSOR_ACCELEROMETER) {
		  long curTime = System.currentTimeMillis();

		  //13. 設定時間防止震動太快
		  if ((curTime - lastUpdate) > 100) {
			  long diffTime = (curTime - lastUpdate);
			  lastUpdate = curTime;
	
			  float  x = values[SensorManager.DATA_X];
			  float y = values[SensorManager.DATA_Y];
			  float z = values[SensorManager.DATA_Z];
	
			  float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;
	
			  if (speed > SHAKE_THRESHOLD) 
			  {
				  Log.d("sensor", "shake detected w/ speed:" + speed);
				  Toast.makeText(this, "shake", Toast.LENGTH_SHORT).show();
	    		 //tag
	    		 stopTime = System.currentTimeMillis() - startTime;
	    		 if (newtag != null)
	    		 {
	    			 //ms -> sec
		    		 newtag.tag.add((int) stopTime / 1000);
		    		 timer_view.setText(Integer.toString((int) stopTime/1000) + " sec");
	    		 }

	    		 //upload
	    		 if (auto_load == true)
	    			 upload_t = true;
				     
	    		 
			  }
			  last_x = x;
			  last_y = y;
			  last_z = z;
			  }
		  }
	  }
	  
	  //2.開始&結束錄影功能 
	  public void startRec()
	  {
          //8. 改由時間的檔名進行儲存影像
   	      int year, month, day;
          int shour, sminute, sec;
          final Calendar c = Calendar.getInstance();
          
          year = c.get(Calendar.YEAR);
          month = c.get(Calendar.MONTH) + 1;
          day = c.get(Calendar.DAY_OF_MONTH);          
          shour = c.get(Calendar.HOUR_OF_DAY);
          sminute = c.get(Calendar.MINUTE);
          sec = c.get(Calendar.SECOND);

 	     recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
	     recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	     recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
	        
	     recorder.setOutputFile("/sdcard/" + year + "_" + month +  "_" + day + "_" + shour + "_" + sminute  + "_" +  sec + ".3gpp");
	     
         //5. 加入tag功能
	     newtag = new FileTagStruct();
	     newtag.filename = year + "_" + month +  "_" + day + "_" + shour + "_" + sminute  + "_" +  sec + ".3gpp";
	     
	     recorder.setPreviewDisplay(mPreview.getSurface());
	     
	      try{
	    	  recorder.prepare();
	          recorder.start();
	      } catch (Exception e) {
	    	e.printStackTrace();
	        recorder.release();
	        recorder = null;
	      }
	        startedRecording=true;
		  
	  }

		//2.開始&結束錄影功能 
	  public void stopRec()
	  {
	      try{
		      recorder.stop();
		      recorder.reset();
	      } catch (Exception e) {
	        recorder.release();
	        recorder = null;
	      }

	      fp.add(newtag);
     	  newtag = null;
	  }
	  
	  //7. 若沒tag則錄影檔案清除 
	  public class Cut_DateTask extends TimerTask 
	  {
		    public void run() 
		    {
		    	if (fp.size() == 0) return;
		    	
		    	FileTagStruct other = null;
		

		    	for (int i=0; i<fp.size(); i++)
		    	{
		    		other = fp.get(i);
		    		if (other.tag.size() != 0) continue;
		    		File file = new File("/sdcard/" + other.filename);
		    		boolean dd = file.delete();
		    		fp.remove(i);
		    	}
		    }
	  }	  
	  
   	  //3.循環錄影 
	  public class DateTask extends TimerTask 
	  {
		    public void run() 
		    {
		   	      int year, month, day;
		          int shour, sminute, sec;
		          final Calendar c = Calendar.getInstance();
		          
			      try{
				      recorder.stop();
				      recorder.reset();
			      } catch (Exception e) {
			        recorder.release();
			        recorder = null;
			      }
			      
			      if (newtag.upload == false && upload_t == true)
		    	  {
		    			 newtag.upload = true;
		          }

		         try {
		        	 //add info tag
		        	 fp.add(newtag);
		        	 newtag = null;
		        	 
		        	 //9. 修改循環錄影switch的時間
		             Thread.sleep(2000);
		         }
		         catch(InterruptedException e) {
		         }
		          
		          //8. 改由時間的檔名進行儲存影像
		          year = c.get(Calendar.YEAR);
		          month = c.get(Calendar.MONTH) + 1;
		          day = c.get(Calendar.DAY_OF_MONTH);          
		          shour = c.get(Calendar.HOUR_OF_DAY);
		          sminute = c.get(Calendar.MINUTE);
		          sec = c.get(Calendar.SECOND);

		 	     recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
			     recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			     recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
			        
			     recorder.setOutputFile("/sdcard/" + year + "_" + month +  "_" + day + "_" + shour + "_" + sminute  + "_" +  sec + ".3gpp");
			     
			    
			     newtag = new FileTagStruct();
			     newtag.filename = year + "_" + month +  "_" + day + "_" + shour + "_" + sminute  + "_" +  sec + ".3gpp";
			     recorder.setPreviewDisplay(mPreview.getSurface());
			     
			      try{
			    	  recorder.prepare();
			          recorder.start();
			      } catch (Exception e) {
			    	e.printStackTrace();
			        recorder.release();
			        recorder = null;
			      }

			      startedRecording=true;
			      
		          startTime = System.currentTimeMillis();
		          
		          if (upload_t == true)
		          {
					    	 Thread t = new upload(newtag.filename);
					         t.start(); 

			    			 upload_t = false;

		          }
		    }
	  }
	  
	  public class upload extends Thread 
	  {
		  
		    String filename;

		  	upload(String mfilename)
		  	{
		  		filename = mfilename;
		  	}
	  
		    public void run() 
		    {
	    	    //10. ftp上傳功能
		    	FTPClient client = new FTPClient();
		        FileInputStream fis = null;
		        
		        
		        try {
		            client.connect("ftp.myweb.hinet.net");
		            client.login("a85056250", "2oliouoi");


		            if (client.isConnected() == true)
		            {
	    	            //
	    	            // Create an InputStream of the file to be uploaded
	    	            //
	    	            Log.i("TAG", filename);
	    	            
	    	            try {
	   		             Thread.sleep(2000);
		   		         }
		   		         catch(InterruptedException e) {
		   		         }
	    	            
	    	            fis = new FileInputStream("/sdcard/" + filename);

	    	            //
	    	            // Store file to server
	    	            //
	    	            client.storeFile(filename, fis);
	    	            client.logout();
		            }     
	    	        } catch (IOException e) {
	    	            e.printStackTrace();
	    	        } finally {
	        	        
	    	            try {
	    	                if (fis != null) {
	    	                    fis.close();
	    	                }
	    	                client.disconnect();
	    	            } catch (IOException e) {
	    	                e.printStackTrace();
	    	            }
	    	        }				       
		    }
	  }
	  
	  
      //1. 最簡易的錄影功能
	  class Preview extends SurfaceView implements SurfaceHolder.Callback
	  {
	    //Create objects for MediaRecorder and SurfaceHolder.
	    SurfaceHolder mHolder;
	    MediaRecorder tempRecorder;
	 
	    //Create constructor of Preview Class. In this, get an object of 
	      //surfaceHolder class by calling getHolder() method. After that add   
	      //callback to the surfaceHolder. The callback will inform when surface is 
	      //created/changed/destroyed. Also set surface not to have its own buffers.
	    public Preview(Context context, MediaRecorder recorder) 
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
	    
	    public void surfaceCreated(SurfaceHolder holder)
	    {
	    }
	 
	    public void surfaceDestroyed(SurfaceHolder holder) 
	    {
	      if(tempRecorder!=null)
	      {
	        //tempRecorder.release();
	        //tempRecorder = null;
	      }
	    }
	 
	    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) 
	    {
	    	Log.i(TAG, w + "," + "h");
	 
	    }
	  }
	  
	
	public void onAccuracyChanged(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}   
	}
