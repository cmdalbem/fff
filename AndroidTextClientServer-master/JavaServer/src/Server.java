import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class Server {

	private static ServerSocket serverSocket;
	private static Socket clientSocket;
	private static InputStreamReader inputStreamReader;
	private static BufferedReader bufferedReader;
	private static String message;
	
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private static Calendar cal;
	
	private static float orientX, orientY;
	
	private static int delayX=100, delayY=100;
	
	private static final int MAX_DELAY_MS = 1000;
	private static final int ANGLE_TRESHOLD = 10;
	private static final int MIN_DELAY = 100;
	
	private static final String ROBOT_MODE = "keyboard";
	//private static final String ROBOT_MODE = "mouse";
	
	private static final int SCREEN_WIDTH = 1920;
	private static final int SCREEN_HEIGHT = 1080;
	
	private static final boolean PRINT_KEYS = true;
	
	
	public static final class HorizontalControlThread extends Thread {

		private Robot robot;
		
		HorizontalControlThread() throws AWTException {
			robot = new Robot();
       		robot.setAutoDelay(0);
       		robot.setAutoWaitForIdle(false);
        }

        public void run() {
	   	 	try {
	   	 		int key = 0;
	   	 		boolean press = false;
	
	    		if (orientY < -ANGLE_TRESHOLD) {
	    			key = KeyEvent.VK_D;
	    			press = true;
	    			if(PRINT_KEYS) System.out.println("d");
	    		}
	    		else if (orientY > ANGLE_TRESHOLD) {
	    			key = KeyEvent.VK_A;
	    			press = true;
	    			if(PRINT_KEYS) System.out.println("a");
	    		}
	    		
	    		if (press && delayY > MIN_DELAY) {
	    			robot.keyPress(key);
	    			Thread.sleep(delayY);
	    			robot.keyRelease(key);
	    		}
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        }
    };
    
    public static final class VerticalControlThread extends Thread{
    	
		private Robot robot;

		VerticalControlThread() throws AWTException {
			robot = new Robot();
			robot.setAutoDelay(0);
			robot.setAutoWaitForIdle(false);
         }

         public void run() {
    		try {
    			int key = 0;
    			boolean press = false;
    			 
 				if (orientX < -ANGLE_TRESHOLD) {
 					key = KeyEvent.VK_W;
        			press = true;
        			if(PRINT_KEYS) System.out.println("w");
 					
 				}
 				else if (orientX > ANGLE_TRESHOLD) {
 					key = KeyEvent.VK_S;
        			press = true;
        			if(PRINT_KEYS) System.out.println("s");
 				}
 				
 				if (press && delayX > MIN_DELAY) {
        			robot.keyPress(key);
        			Thread.sleep(delayX);
        			robot.keyRelease(key);
        		}
 				
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} 
    	 }
     };
     
     public static final class AccelerationControllerThread extends Thread{
     	
 		private Robot robot;

 		AccelerationControllerThread() throws AWTException {
 			robot = new Robot();
 			robot.setAutoDelay(50);
 			robot.setAutoWaitForIdle(false);
          }

          public void run() {
     		int key = KeyEvent.VK_SHIFT;
         	robot.keyPress(key);
         	System.out.println("shift");
         	robot.keyRelease(key);
     	 }
      };
      
      public static final class BrakeControllerThread extends Thread{
       	
   		private Robot robot;

   		BrakeControllerThread() throws AWTException {
   			robot = new Robot();
   			robot.setAutoDelay(50);
   			robot.setAutoWaitForIdle(false);
            }

            public void run() {
       		int key = KeyEvent.VK_SPACE;
           	robot.keyPress(key);
           	System.out.println("space");
           	robot.keyRelease(key);
       	 }
        };
	 	
	
	public static void handleMessage(String msg) throws AWTException {
		// Print the message
		cal = Calendar.getInstance();;
		//System.out.println(dateFormat.format(cal.getTime()) + " " + msg);
		
		// Parse
		if (msg.equals("boost")) {
			if (ROBOT_MODE=="keyboard") {
				AccelerationControllerThread th = new AccelerationControllerThread();
				th.start();
			}
		}
		else if (msg.equals("brake")) {
			if (ROBOT_MODE=="keyboard") {
				BrakeControllerThread th = new BrakeControllerThread();
				th.start();
			}
		}		
		else {
			String[] data = msg.split(",");
			orientX = Float.parseFloat(data[0]);
			orientY = Float.parseFloat(data[1]);
			
			//System.out.println("orientX = " + orientX);
			//System.out.println("orientY = " + orientY);
			
			HorizontalControlThread t1 = new HorizontalControlThread();
			VerticalControlThread t2 = new VerticalControlThread();
			
			if (ROBOT_MODE=="keyboard") {
				delayX = (int) Math.abs( (((Math.abs(orientX))/90.) * MAX_DELAY_MS) );
				delayY = (int) Math.abs( (((Math.abs(orientY))/90.) * MAX_DELAY_MS) );
				
				t1.start();
				t2.start();

				/*System.out.println("delayX : " + delayX);
				System.out.println("delayY : " + delayY);*/
			}
			else if (ROBOT_MODE=="mouse") {
				// Set mouse position instead of simulation key presses
				Robot robot = new Robot();
				int x, y;
				
				x = (int) (SCREEN_WIDTH/2 - (orientY/90 * SCREEN_WIDTH/2));
				y = (int) (SCREEN_HEIGHT/2 + (orientX/90 * SCREEN_HEIGHT/2));
	 
				robot.mouseMove(x,y);
			}
		}
		
	}

	
	/////////////////
	// UDP Version //
	/////////////////
	
	public static void main(String[] args) throws AWTException {
		System.out.println("Listening to the port 4444");
		
		while (true) {
			String message;
		    byte[] lMsg = new byte[512];
		    DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
		    DatagramSocket ds = null;
		    try {
		        ds = new DatagramSocket(4444);
		        
		        //disable timeout for testing
		        //ds.setSoTimeout(100000);
		        
		        ds.receive(dp);
		        message = new String(lMsg, 0, dp.getLength());
		        
		        handleMessage(message);
		        
		    } catch (SocketException e) {
		        e.printStackTrace();
		    } catch (IOException e) {
		        e.printStackTrace();
		    } finally {
		        if (ds != null) {
		            ds.close();
		        }
		    }	
		}		
		
	}
	
	/////////////////
	// TCP Version //
	/////////////////
	
//	public static void main(String[] args) {
//		try {
//			serverSocket = new ServerSocket(4444); // Server socket
//
//		} catch (IOException e) {
//			System.out.println("Could not listen on port: 4444");
//		}
//
//		System.out.println("Server started. Listening to the port 4444");
//
//	   while (true) {
//			try {
//
//				clientSocket = serverSocket.accept(); // accept the client connection
//				inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
//				bufferedReader = new BufferedReader(inputStreamReader); // get the client message
//				message = bufferedReader.readLine();
//				
//				handleMessage(message);
//				
//				inputStreamReader.close();
//				clientSocket.close();
//
//				//Robot robot=new Robot();
//				//robot.key
//				
//			} catch (IOException ex) {
//				System.out.println("Problem in message reading");
//			}
//		}
//	}

}