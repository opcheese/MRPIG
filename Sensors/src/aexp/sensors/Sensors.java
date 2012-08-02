package aexp.sensors;


import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


public class Sensors extends ListActivity  implements SensorEventListener,  OnClickListener
{
    public static final String PREF_CAPTURE_STATE = "captureState";
    public static final String PREF_CAPTURE_FILE = "captureStatePrefs";
    static final String LOG_TAG = "SENSORMONITOR";
    static final int MENU_CAPTURE_ON = 1;
    static final int MENU_CAPTURE_OFF = 2;
    
    private String sensorName;
    
    private SensorManager sensorManager;
    private PrintWriter captureFile;
    
    @Override
	public void onDestroy() {
    	 super.onDestroy();
		unregisterReceiver(receiver);
	}

    public void onClick(View view) {		

		
    	Log.e("Supers", "onClick() wifi.startScan()");
		boolean res = wifi.startScan();
		Log.e("Superf",Boolean.toString(res));
		
	}
    WifiManager wifi;
    BroadcastReceiver receiver;
    boolean first = true;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if (first)
        {first = false;
        
        SensorManager sensorManager = 
                (SensorManager)getSystemService( SENSOR_SERVICE  );
        ArrayList<SensorItem> items = new ArrayList<SensorItem>();
        List<Sensor> sensors = sensorManager.getSensorList( Sensor.TYPE_ALL );
        for( int i = 0 ; i < sensors.size() ; ++i )
        {
        	for (int j=0;j<3;j++)
            items.add( new SensorItem( sensors.get( i ),j ) );
        }
        sensorAdapter = new ArrayAdapter( this,
                                R.layout.sensor_row,
                                R.id.text1,
                                items );
        setListAdapter( sensorAdapter );
        SharedPreferences appPrefs = getSharedPreferences( 
                                        PREF_CAPTURE_FILE,
                                        MODE_PRIVATE );
        captureState = appPrefs.getBoolean( PREF_CAPTURE_STATE, false );
        
        
        String captureStateText = null;
        if( captureState ) {
          
        } else
            captureStateText = "Capture: OFF";
        
        
        
        Button buttonScan = (Button) findViewById(R.id.button1);
		buttonScan.setOnClickListener(this);

		// Setup WiFi
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		// Get WiFi status
		
		// Register Broadcast Receiver
		if (receiver == null)
			receiver = new WiFiScanReceiver(this);

		registerReceiver(receiver, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		
	
        }
        
    }
    
    protected void onStart() {
        super.onStart();
       {
            sensorManager = 
                (SensorManager)getSystemService( SENSOR_SERVICE  );
            List<Sensor> sensors = sensorManager.getSensorList( Sensor.TYPE_ALL );
            Sensor ourSensor = null;
            for( int i = 0 ; i < sensors.size() ; ++i )
               
           
                sensorManager.registerListener( 
                        this, 
                        sensors.get( i ),
                        SensorManager.SENSOR_DELAY_UI );
       
               
                }
            
        }
    
    
    protected void onPause() {
        super.onPause();
        SharedPreferences appPrefs = getSharedPreferences( 
                                        PREF_CAPTURE_FILE,
                                        MODE_PRIVATE );
        SharedPreferences.Editor ed = appPrefs.edit();
        ed.putBoolean( PREF_CAPTURE_STATE, captureState );
        ed.commit();
        
        if( sensorManager != null )
            sensorManager.unregisterListener( this );
        if( captureFile != null ) {
            captureFile.close();
        }
    }
    
    public void onAccuracyChanged (Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        StringBuilder b = new StringBuilder();
        for( int i = 0 ; i < sensorEvent.values.length ; ++i ) {
            if( i > 0 )
                b.append( " , " );
            b.append( Float.toString( sensorEvent.values[i] ) );
        }
        Log.d( LOG_TAG, "onSensorChanged: ["+b+"]" );
        int count = sensorEvent.values.length < 3 ?
                    sensorEvent.values.length :
                    3;
        ListAdapter la =  this.getListAdapter();
        for( int i = 0 ; i < count ; ++i ) {
        
           int arg0 = 0;
           while (!la.getItem( arg0).toString().startsWith(sensorEvent.sensor.getName() + "["+i+"]"))
           {
        	   arg0++;
           }
           ((SensorItem)la.getItem( arg0)).val = Double.toString( sensorEvent.values[i] );            
        }
        ((BaseAdapter)la).notifyDataSetChanged();
        if( captureFile != null ) {
        	EditText tw = (EditText)this.findViewById(R.id.editText1);
        	CharSequence cs =  tw.getText();
        	  captureFile.print(cs+":");
              captureFile.print(sensorEvent.sensor.getName()+":");
                for( int i = 0 ; i < sensorEvent.values.length ; ++i ) {               	
                  
                	if( i > 0 )
                        captureFile.print( "," );
                    captureFile.print( Float.toString( sensorEvent.values[i] ) );
                }
                captureFile.println();
        }
    }
    

    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_CAPTURE_ON, 1, R.string.capture_on );
		menu.add(0, MENU_CAPTURE_OFF, 2, R.string.capture_off );
        return result;
    }
int nCap = 0;
    public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
        switch( id ) {
            case MENU_CAPTURE_ON:
            	EditText tw = (EditText)this.findViewById(R.id.editText1);
            	CharSequence cs =  tw.getText();
            	  File captureFileName = new File( Environment.getExternalStorageDirectory().getPath(),java.net.URLEncoder.encode("capture"+(nCap++)+cs)+".csv" );
                  //captureStateText = "Capture: "+captureFileName.getAbsolutePath();
                  try {
                      captureFile = new PrintWriter( new FileWriter( captureFileName, false ) );
                  } catch( IOException ex ) {
                      Log.e( LOG_TAG, ex.getMessage(), ex );
                    //  captureStateText = "Capture: "+ex.getMessage();
                  }
                captureState = true;
                break;

            case MENU_CAPTURE_OFF:
                captureState = false;
                captureFile.close();
                break;
        }
        return true;
    }

    protected void onListItemClick(
            ListView l,
            View v,
            int position,
            long id) {
//        Sensor sensor = sensorAdapter.getItem( position ).getSensor();
//        String sensorName = sensor.getName();
//        Intent i = new Intent();
//        i.setClassName( "aexp.sensors","aexp.sensors.SensorMonitor" );
//        i.putExtra( "sensorname",sensorName );
//        startActivity( i );
    }

    private ArrayAdapter<SensorItem> sensorAdapter;
    private boolean captureState = false;

    class SensorItem {
        SensorItem( Sensor sensor,int num ) {
            this.sensor = sensor;
            this.num = num;
            this.val="0";
        }

        public String toString() {
            return sensor.getName() + "["+num+"]"+val;
        }

        Sensor getSensor() {
            return sensor;
        }
        public String val;
        private Sensor sensor;
        private int num;
    }
}
