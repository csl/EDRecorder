package com.camera;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.net.ftp.FTPClient;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

public class edlist extends Activity 
{
	String szToday;
	private ArrayList<HashMap<String, Object>> w_list;

	private ListView show_view;
	
	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edlist);
        
        //Display: create ListView class
        show_view = (ListView)findViewById(R.id.listview);
        
        w_list = getFileItems();
        
        SimpleAdapter listitemAdapter=new SimpleAdapter(this,  
        										w_list, 
        										R.layout.no_listview_style,
        										new String[]{"ItemTitle","ItemText"}, 
        										new int[]{R.id.topTextView,R.id.bottomTextView}  
        										);  
        
        show_view.setAdapter(listitemAdapter);              
        show_view.setOnItemClickListener(new OnItemClickListener() 
        {          
        	   @Override  
        	   public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,  
        	     long arg3) 
        	   {

        	        FTPClient client = new FTPClient();
        	        FileInputStream fis = null;
        	        

        	        try {
        	            client.connect("192.168.173.100");
        	            client.login("shulong", "lgaybysp");


        	            if (client.isConnected() == true)
        	            {
	        	            //
	        	            // Create an InputStream of the file to be uploaded
	        	            //
	        	            String filename = VideoRecorder.fp.get(arg2).filename;
	        	            Log.i("TAG", filename);
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
	
	        	        	Toast popup =  Toast.makeText(edlist.this, "傳送成功", Toast.LENGTH_SHORT);
	            	        popup.show();
	            	        
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
        });
        
        Toast popup =  Toast.makeText(edlist.this, "請按上鍵頭跳出", Toast.LENGTH_SHORT);
        popup.show();
    }
	
	public ArrayList<HashMap<String, Object>> getFileItems() 
	{
		ArrayList<HashMap<String, Object>> listitem = new ArrayList<HashMap<String,Object>>();
		
        for (int i=0; i<VideoRecorder.fp.size(); i++) 
		{	
			HashMap<String, Object> map = new HashMap<String, Object>();
			FileTagStruct tmp = VideoRecorder.fp.get(i);
			map.put("ItemTitle", tmp.filename);
			
			String tags = "";
	        for (int j=0; j<tmp.tag.size(); j++) 
			{	
	        	if (tmp.tag.size() == j-1)
	        		tags = tags + VideoRecorder.fp.get(i).tag.get(j) + "sec";
	        	else
	        		tags = tags + VideoRecorder.fp.get(i).tag.get(j) + "sec, ";
			}
	        
			map.put("ItemText", tags);
			listitem.add(map);
		}
		return listitem;
	}
	
    
    //error message
    private void openOptionsDialog(String info)
	{
	    new AlertDialog.Builder(this)
	    .setTitle("message")
	    .setMessage(info)
	    .setPositiveButton("OK",
	        new DialogInterface.OnClickListener()
	        {
	         public void onClick(DialogInterface dialoginterface, int i)
	         {
	         }
	         }
	        )
	    .show();
	}
}
