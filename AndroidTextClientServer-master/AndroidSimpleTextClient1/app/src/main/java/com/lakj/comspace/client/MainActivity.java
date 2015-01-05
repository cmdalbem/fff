package com.lakj.comspace.client;

import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {

	private Socket client;
	private PrintWriter printwriter;
	private EditText ipAddressField;

    private TextView aTextView;
    private Switch aSwitch;
    private Button accButton1, accButton2;
    private SensorManager sManager;

    private ProgressBar leftProgBar, rightProgBar, upProgBar, downProgBar;

    private static int refreshRateMs = 20;
    private static final String DEFAULT_SERVER_ADDRESS = "192.168.43.106";

    private float orientX, orientY, orientZ;

    // Handler for the delayed messages, which will work as a timer
    private Handler timer = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            SendMessage sendMessageTask = new SendMessage();
            sendMessageTask.execute();

            Log.w("log","tick");

            // Order new refresh
            if(aSwitch.isChecked()) {
                timer.sendEmptyMessageDelayed(0, refreshRateMs);
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

        leftProgBar = (ProgressBar) findViewById(R.id.leftProgBar);
        rightProgBar = (ProgressBar) findViewById(R.id.rightProgBar);
        upProgBar = (ProgressBar) findViewById(R.id.upProgBar);
        downProgBar = (ProgressBar) findViewById(R.id.downProgBar);

        leftProgBar.setRotation(180);
        upProgBar.setRotation(270);
        downProgBar.setRotation(90);

//        aSeekBar = (SeekBar) findViewById((R.id.seekBar));
//        aSeekBar.setProgress(refreshRateMs);

        timer.sendEmptyMessageDelayed(0, refreshRateMs);

        accButton1 = (Button) findViewById(R.id.brakeButton);
        accButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SendMessage sendMessageTask = new SendMessage();
                sendMessageTask.execute("brake");
            }
        });

        accButton2 = (Button) findViewById(R.id.boostButton);
        accButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SendMessage sendMessageTask = new SendMessage();
                sendMessageTask.execute("boost");
            }
        });
    }

    public void onSwitchClicked(View view) {
        // Is the toggle on?
        boolean on = ((Switch) view).isChecked();

        if (on) {
            timer.sendEmptyMessageDelayed(0, refreshRateMs);
        } else {
            // Nothing.
        }
    }

	private class SendMessage extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
            String msg;

            if(params.length==0) {
                msg = orientX + "," + orientY + "," + orientZ;
            }
            else {
                msg = params[0];
            }

            Log.w("log","Sending message: " + msg);
            String serverIp = ipAddressField.getText().toString();

            // TCP
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

            // Send UDP packet
            DatagramSocket ds = null;
            try {
                ds = new DatagramSocket();
                InetAddress serverAddr = InetAddress.getByName(serverIp);
                DatagramPacket dp;
                dp = new DatagramPacket(msg.getBytes(), msg.length(), serverAddr, 4444);
                ds.send(dp);
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
        // If sensor is unreliable, return void
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }

        orientX = event.values[2];
        orientY = event.values[1];
        orientZ = event.values[0];

        // Show values on TextView
        aTextView.setText("Orientation X (Roll) :" + Float.toString(orientX) + "\n" +
                "Orientation Y (Pitch) :" + Float.toString(orientY) + "\n" +
                "Orientation Z (Yaw) :" + Float.toString(orientZ));

        // Show values on ProgressBars
        int progressY = (int) Math.abs(orientY/90 * 100);
        int progressX = (int) Math.abs(orientX/90 * 100);

        if(orientY >= 0) {
            leftProgBar.setProgress(progressY);
            rightProgBar.setProgress(0);
        }
        else {
            leftProgBar.setProgress(0);
            rightProgBar.setProgress(progressY);
        }

        if(orientX >= 0) {
            upProgBar.setProgress(0);
            downProgBar.setProgress(progressX);
        }
        else {
            upProgBar.setProgress(progressX);
            downProgBar.setProgress(0);
        }
    }

}
