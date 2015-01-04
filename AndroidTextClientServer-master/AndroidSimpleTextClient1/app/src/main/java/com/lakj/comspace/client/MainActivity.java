package com.lakj.comspace.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {

	private Socket client;
	private PrintWriter printwriter;
	private EditText ipAddressField;

    private TextView aTextView;
    private Switch aSwitch;
    private SeekBar aSeekBar;
    private SensorManager sManager;

    private static int refreshRateMs = 20;
    private static final String DEFAULT_SERVER_ADDRESS = "192.168.43.106";

    private float orientX, orientY, orientZ;

    // Handler for the delayed messages, which will work as a timer
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // Do task here
            SendMessage sendMessageTask = new SendMessage();
            sendMessageTask.execute();

            Log.w("log","tick");

            // Order new refresh
            if(aSwitch.isChecked()) {
                mHandler.sendEmptyMessageDelayed(0, refreshRateMs);
            }
        }
    };


	@Override
	protected void onCreate(Bundle savedInstanceState) {
        Log.w("log","Initializing...");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ipAddressField = (EditText) findViewById(R.id.ipAddressField); // reference to the text field
        ipAddressField.setText(DEFAULT_SERVER_ADDRESS);

        //get the TextView from the layout file
        aTextView = (TextView) findViewById(R.id.aTextView);
        //get a hook to the sensor service
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        aSwitch = (Switch) findViewById(R.id.switch1);

        aSeekBar = (SeekBar) findViewById((R.id.seekBar));
        aSeekBar.setProgress(refreshRateMs);

        mHandler.sendEmptyMessageDelayed(0, refreshRateMs);


//        upButton = (Button) findViewById(R.id.upArrow);
//        upButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                messsage = "up";
//                SendMessage sendMessageTask = new SendMessage();
//                sendMessageTask.execute();
//            }
//        });
    }

    public void onSwitchClicked(View view) {
        // Is the toggle on?
        boolean on = ((Switch) view).isChecked();

        if (on) {
            mHandler.sendEmptyMessageDelayed(0, refreshRateMs);
        } else {
            // Nothing.
        }
    }

	private class SendMessage extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
            String msg = orientX + "," + orientY + "," + orientZ;
            Log.w("log","Sending message: " + msg);
            String serverIp = ipAddressField.getText().toString();

//			try {
//				client = new Socket(serverIp, 4444); // connect to the server
//				printwriter = new PrintWriter(client.getOutputStream(), true);
//
//                printwriter.write(msg); // write the message to output stream
//
//				printwriter.flush();
//				printwriter.close();
//				client.close(); // closing the connection
//
//			} catch (UnknownHostException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}

            DatagramSocket ds = null;
            try {
                ds = new DatagramSocket();
                InetAddress serverAddr = InetAddress.getByName(serverIp);
                DatagramPacket dp;
                dp = new DatagramPacket(msg.getBytes(), msg.length(), serverAddr, 4444);
                ds.send(dp);
            } catch (SocketException e) {
                e.printStackTrace();
            }catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (ds != null) {
                    ds.close();
                }
            }

			return null;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.slimple_text_client, menu);
		return true;
	}

    //when this Activity starts
    @Override
    protected void onResume()
    {
        super.onResume();
		/*register the sensor listener to listen to the gyroscope sensor, use the
		 * callbacks defined in this class, and gather the sensor information as
		 * quick as possible*/
        sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
    }

    //When this Activity isn't visible anymore
    @Override
    protected void onStop()
    {
        //unregister the sensor listener
        sManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1)
    {
        //Do nothing
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        //if sensor is unreliable, return void
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }

        orientX = event.values[2];
        orientY = event.values[1];
        orientZ = event.values[0];

        //else it will output the Roll, Pitch and Yawn values
        aTextView.setText("Orientation X (Roll) :" + Float.toString(orientX) + "\n" +
                "Orientation Y (Pitch) :" + Float.toString(orientY) + "\n" +
                "Orientation Z (Yaw) :" + Float.toString(orientZ));
    }

}
