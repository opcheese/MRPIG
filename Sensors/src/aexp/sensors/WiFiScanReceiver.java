package aexp.sensors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;

public class WiFiScanReceiver extends BroadcastReceiver {

	
	Sensors wifiDemo;
	String soob;

	  public WiFiScanReceiver(Sensors wifiDemo) {
	    super();
	    this.wifiDemo = wifiDemo;
	  }
	  static Object guard = new Object();
	@Override
	public void onReceive(Context context, Intent intent) {
		List<ScanResult> results = wifiDemo.wifi.getScanResults();
		Log.e( "Super1", "tut" );
		String res = ""; 
		File captureFileName = new File( Environment.getExternalStorageDirectory().getPath(), "1wifi.csv" );
		PrintWriter captureFile;
		synchronized (guard) {
			EditText tw = (EditText)wifiDemo.findViewById(R.id.editText1);
        	CharSequence cs =  tw.getText();
		
		try {
			captureFile = new PrintWriter( new FileWriter(captureFileName, true ) );
			captureFile.print(cs+":");
			for (ScanResult result : results) {
				captureFile.print(result.toString()+"|");
				Log.e( "Super", result.toString() );
			    }
			captureFile.println();
			captureFile.close();
			  
		} catch (IOException e) {
			Log.e( "SuperE", "!!" );
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	

	}

}
