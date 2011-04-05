package idd.testing;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import lib.Message;

public class TestClient {
	public static void main(String args[]) {
		try {
			
			
			
			Socket iddSock = new Socket("127.0.0.1", 8888);
			
			DataOutputStream out = new DataOutputStream(iddSock.getOutputStream());
			
			out.write(Message.newCDAP_IDD_UPDATE_REQ("dif.one", "dif.one.nms"));
			Message response = Message.readFromSocket(iddSock);
			System.out.println("Response type: " + response.type);
			System.out.println("Response status: " + response.errorCode);
			
			out.write(Message.newCDAP_IDD_UPDATE_REQ("dif.two", "dif.two.nms"));
			response = Message.readFromSocket(iddSock);
			System.out.println("Response type: " + response.type);
			System.out.println("Response status: " + response.errorCode);
			
			out.write(Message.newCDAP_IDD_UPDATE_REQ("dif.three", "dif.three.nms"));
			response = Message.readFromSocket(iddSock);
			System.out.println("Response type: " + response.type);
			System.out.println("Response status: " + response.errorCode);
			
			out.write(Message.newCDAP_IDD_SERVADD_REQ("dif.one", "dif.one.serviceone"));
			response = Message.readFromSocket(iddSock);
			System.out.println("Response type: " + response.type);
			System.out.println("Response status: " + response.errorCode);
			
			out.write(Message.newCDAP_IDD_SERVADD_REQ("dif.two", "dif.one.serviceone"));
			response = Message.readFromSocket(iddSock);
			System.out.println("Response type: " + response.type);
			System.out.println("Response status: " + response.errorCode);
			
			out.write(Message.newCDAP_IDD_REQ(1, "dif.one.serviceone"));
			response = Message.readFromSocket(iddSock);
			System.out.println("Response type: " + response.type);
			System.out.println("DIF name: " + response.text1);
			
			out.write(Message.newCDAP_IDD_REQ(2, "dif.one.serviceone"));
			response = Message.readFromSocket(iddSock);
			System.out.println("Response type: " + response.type);
			System.out.println("DIF name: " + response.text1);
			System.out.println("NMS URL: " + response.text2);
			
			out.write(Message.newCDAP_IDD_REQ(3, "dif.three"));
			response = Message.readFromSocket(iddSock);
			System.out.println("Response type: " + response.type);
			System.out.println("NMS URL: " + response.text2);
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
