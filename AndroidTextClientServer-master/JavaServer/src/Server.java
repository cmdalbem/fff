

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
	
	private static final int MAX_DELAY_MS = 500;
	private static final int ANGLE_TRESHOLD = 8;
	
	private static final String ROBOT_MODE = "keyboard";
	//private static final String ROBOT_MODE = "mouse";
	
	private static final int SCREEN_WIDTH = 1920;
	private static final int SCREEN_HEIGHT = 1080;
	
	
	public static final class VerticalControlThread extends Thread {

		private Robot robot;
		
		VerticalControlThread() throws AWTException {
			robot = new Robot();
       		robot.setAutoDelay(0);
       		robot.setAutoWaitForIdle(false);
        }

        public void run() {
       	 	while(true) {
       	 	try {
           		// Steer right/Steer left
        		if (orientY < -ANGLE_TRESHOLD) {
        			robot.keyPress(KeyEvent.VK_A);
        		}
        		else if (orientY > ANGLE_TRESHOLD) {
        			robot.keyPress(KeyEvent.VK_D);
        		}
        		
        		System.out.println("tick1");
           		
    			// Sleep
           		Thread.sleep(delayY);
    				
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
       	 	}
        }
    };
	
    public static final class HorizontalControlThread extends Thread{
    	
		private Robot robot;

		 HorizontalControlThread() throws AWTException {
			robot = new Robot();
			robot.setAutoDelay(0);
			robot.setAutoWaitForIdle(false);
         }

         public void run() {
        	 while(true) {
        		 try {				
             		// Up/Down
     				if (orientX < -ANGLE_TRESHOLD) {
     					robot.keyPress(KeyEvent.VK_W);
     				}
     				else if (orientX > ANGLE_TRESHOLD) {
     					robot.keyPress(KeyEvent.VK_S);
     				}
     				
     				System.out.println("tick2");
             		
     				// Sleep
             		Thread.sleep(delayX);
     				
     			} catch (InterruptedException e) {
     				e.printStackTrace();
     			} 
        	 }
        	 
         }
     };
	 	
	
	public static void handleMessage(String msg) {
		// Print the message
		cal = Calendar.getInstance();;
		System.out.println(dateFormat.format(cal.getTime()) + " " + msg);
		
		// Parse
		String[] data = msg.split(",");
		orientX = Float.parseFloat(data[0]);
		orientY = Float.parseFloat(data[1]);
		
		System.out.println("orientX = " + orientX);
		System.out.println("orientY = " + orientY);
		
		try {
			if (ROBOT_MODE=="keyboard") {
				delayX = (int) Math.abs( ((1-(Math.abs(orientX))/90) * MAX_DELAY_MS) );
				delayY = (int) Math.abs( ((1-(Math.abs(orientY))/90) * MAX_DELAY_MS) );

				System.out.println("delayX : " + delayX);
				System.out.println("delayY : " + delayY);
			}
			else if (ROBOT_MODE=="mouse") {
				// Set mouse position instead of simulation key presses
				Robot robot = new Robot();
				int x, y;
				
				x = (int) (SCREEN_WIDTH/2 - (orientY/90 * SCREEN_WIDTH/2));
				y = (int) (SCREEN_HEIGHT/2 + (orientX/90 * SCREEN_HEIGHT/2));
	 
				robot.mouseMove(x,y);
			}
			
        } catch (AWTException ex) {
            //...
        }
		
	}

	
	/////////////////
	// UDP Version //
	/////////////////
	
	public static void main(String[] args) throws AWTException {
		System.out.println("Listening to the port 4444");
		
		HorizontalControlThread t1 = new HorizontalControlThread();
		t1.start();
		
		VerticalControlThread t2 = new VerticalControlThread();
		t2.start();
		
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
