package aexp.sensors;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import java.util.List;
import java.io.*;

public class SensorMonitor extends Activity implements SensorEventListener {
    static final String LOG_TAG = "SENSORMONITOR";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.monitor );
        Intent i = getIntent();
        if( i != null ) {
            sensorName = i.getStringExtra( "sensorname" );
            Log.d( LOG_TAG,"sensorName: "+sensorName );
            if( sensorName != null ) {
                TextView t = (TextView)findViewById( R.id.sensorname );
                t.setText( sensorName );
            }
        }
        SharedPreferences appPrefs = getSharedPreferences( 
                                        Sensors.PREF_CAPTURE_FILE,
                                        MODE_PRIVATE );
        captureState = appPrefs.getBoolean( Sensors.PREF_CAPTURE_STATE, false );
        String captureStateText = null;
        if( captureState ) {
            File captureFileName = new File( "/sdcard", "capt.csv" );
            captureStateText = "Capture: "+captureFileName.getAbsolutePath();
            try {
                captureFile = new PrintWriter( new FileWriter( captureFileName, false ) );
            } catch( IOException ex ) {
                Log.e( LOG_TAG, ex.getMessage(), ex );
                captureStateText = "Capture: "+ex.getMessage();
            }
        } else
            captureStateText = "Capture: OFF";
        TextView t = (TextView)findViewById( R.id.capturestate );
        t.setText( captureStateText );
    }

    protected void onStart() {
        super.onStart();
        if( sensorName != null ) {
            sensorManager = 
                (SensorManager)getSystemService( SENSOR_SERVICE  );
            List<Sensor> sensors = sensorManager.getSensorList( Sensor.TYPE_ALL );
            Sensor ourSensor = null;
            for( int i = 0 ; i < sensors.size() ; ++i )
                if( sensorName.equals( sensors.get( i ).getName() ) ) {
                    ourSensor = sensors.get( i );
                    break;
                }
            if( ourSensor != null ) {
                    sensorManager.registerListener( 
                            this, 
                            ourSensor,
                            SensorManager.SENSOR_DELAY_UI );
            }
        }
    }

    protected void onPause() {
        super.onStop();
        if( sensorManager != null )
            sensorManager.unregisterListener( this );
        if( captureFile != null ) {
            captureFile.close();
        }
    }

// SensorEventListener
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
        int count = sensorEvent.values.length < fields.length ?
                    sensorEvent.values.length :
                    fields.length;
        for( int i = 0 ; i < count ; ++i ) {
            TextView t = (TextView)findViewById( fields[i] );
            t.setText( "["+i+"]: "+Float.toString( sensorEvent.values[i] ) );
        }
        if( captureFile != null ) {
                for( int i = 0 ; i < sensorEvent.values.length ; ++i ) {
                    if( i > 0 )
                        captureFile.print( "," );
                    captureFile.print( Float.toString( sensorEvent.values[i] ) );
                }
                captureFile.println();
        }
    }

    private String sensorName;
    private boolean captureState = false;
    private SensorManager sensorManager;
    private PrintWriter captureFile;
    final static int fields[] = {
        R.id.f1,
        R.id.f2,
        R.id.f3,
        R.id.f4,
        R.id.f5,
        R.id.f6
    };
}
