package app.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ScraperController
{
	private static String address = "localhost";
	private static int port = 5656;

	public static void start()
	{
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(address, port), 3000);
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());

			output.writeUTF("START");
			output.flush();
			String msg = "";
			while(!msg.equals("ACK"))
				msg = input.readUTF();
			input.close();
			output.close();
			socket.close();
		} catch (IOException e) {
		}
	}

	public static void stop()
	{
		try {
			Socket socket = new Socket(address, port);
			socket.connect(new InetSocketAddress(address, port), 3000);
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());

			output.writeUTF("STOP");
			output.flush();
			String msg = "";
			while(!msg.equals("ACK"))
				msg = input.readUTF();
			input.close();
			output.close();
			socket.close();
		} catch (IOException e) {
		}
	}

	public static void setAddress(String address)
	{
		ScraperController.address = address;
	}

	public static void setPort(int port)
	{
		ScraperController.port = port;
	}
}
