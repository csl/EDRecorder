package com.camera;

import java.util.ArrayList;

//6. �M��[�J��

/** Class to hold our location information */
public class FileTagStruct {

	public String filename;
	public ArrayList<Integer> tag;
	public boolean upload;
	
	FileTagStruct()
	{
		filename = "";
		tag = new ArrayList<Integer>();
		upload = false;
	}
	

}
