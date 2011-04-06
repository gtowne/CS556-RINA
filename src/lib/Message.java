package lib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

public class Message {
		public static final int DNS_REQ = 1;
		public static final int DNS_RSP = 2;
		public static final int HTTP_GET = 3;
		public static final int HTTP_RSP = 4;
		public static final int CDAP_IDD_REQ = 5;
		public static final int CDAP_IDD_RSP = 6;
		public static final int CDAP_CONNECT_REQ = 7;
		public static final int CDAP_CONNECT_RSP = 8;
		public static final int CDAP_UPDATE_RIB_REQ = 9;
		public static final int CDAP_UPDATE_RIB_RSP = 10;
		public static final int CDAP_IDD_UPDATE_REQ = 13;
		public static final int CDAP_IDD_SERVADD_REQ = 14;
		public static final int CDAP_UPDATE_RSP = 15;

		public int type;
		public int length;
		public String text1;
		public String text2;
		public int lookupType;
		public int errorCode;
		public int address;

		private Message(){};

		public static Message readFromSocket(Socket tcpSocket) {
			DataInputStream input = null;
			try {
				input = new DataInputStream(tcpSocket.getInputStream());
			} catch (IOException e) {e.printStackTrace();}

			try {
				return (new Message())._parse(input);
			} catch (EOFException e) {
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		public static Message parseMessage(byte[] inMessage) {
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(inMessage));
			
			try {
				return (new Message())._parse(in);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		public static byte[] newCDAP_IDD_REQ(int lookupType, String query) {
			int messageLength = 4 + query.length() + 2; // 1 4-byte int field
			
			messageLength += query.length() + 2;
			
			ByteArrayOutputStream _out = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(_out);
			try {
				// write the type
				out.writeInt(CDAP_IDD_REQ);

				// write the message length
				out.writeInt(messageLength);
				
				out.writeInt(lookupType);
				
				out.writeUTF(query);

			} catch (IOException e) {e.printStackTrace();}
			
			return _out.toByteArray();
			
		}
		
		
		public static byte[] newCDAP_IDD_UPDATE_REQ(String difName, String nmsURL) {
			ByteArrayOutputStream _out = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(_out);
			try{
				out.writeInt(CDAP_IDD_UPDATE_REQ);
				out.writeInt(difName.length() + 2 + nmsURL.length() + 2);
				out.writeUTF(difName);
				out.writeUTF(nmsURL);
			}catch(Exception e) {e.printStackTrace();}
			
			return _out.toByteArray();
		}
		
		public static byte[] newCDAP_IDD_SERVADD_REQ(String difName, String serviceURL) {
			ByteArrayOutputStream _out = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(_out);
			try{
				out.writeInt(CDAP_IDD_SERVADD_REQ);
				out.writeInt(difName.length() + 2 + serviceURL.length() + 2);
				out.writeUTF(difName);
				out.writeUTF(serviceURL);
			}catch(Exception e) {e.printStackTrace();}
			
			return _out.toByteArray();
		}
		
		/**
		 * 
		 * @param 0 if success, 1 if failure
		 * @return
		 */
		public static byte[] newCDAP_UPDATE_RSP(int status) {
			ByteArrayOutputStream _out = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(_out);
			try{
				out.writeInt(CDAP_UPDATE_RSP);
				out.writeInt(4);
				out.writeInt(status);
			}catch(Exception e) {e.printStackTrace();}
			
			return _out.toByteArray();
		}

		public static byte[] newCDAP_IDD_RSP(int status, String DIFName, String NMSName) {

			int messageLength = 12; // 5 4-byte integer fields

			int dnLength = 0;
			if (DIFName != null) {
				dnLength = DIFName.length();
			}
			if (dnLength > 0) {
				messageLength += dnLength + 2;
			}

			int nnLength = 0;
			if (NMSName != null) {
				nnLength = NMSName.length();
			}
			if (nnLength > 0) {
				messageLength += nnLength + 2;
			}

			ByteArrayOutputStream _out = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(_out);
			try {
				// write the type
				out.writeInt(CDAP_IDD_RSP);

				// write the message length
				out.writeInt(messageLength);
				
				out.writeInt(status);
				
				out.writeInt(dnLength);
				
				out.writeInt(nnLength);
				
				if (dnLength > 0) {
					out.writeUTF(DIFName);
				}
				
				if (nnLength > 0) {
					out.writeUTF(NMSName);
				}

			} catch (IOException e) {e.printStackTrace();}
			
			return _out.toByteArray();
		}

		private Message _parse (DataInputStream input) throws Exception {
			// Read type
			type = input.readInt();
			// Read Length
			length = input.readInt();

			switch (type) {


			case CDAP_IDD_REQ:
				lookupType = input.readInt();
				text1 = input.readUTF();
				break;


			case CDAP_IDD_RSP:
				errorCode = input.readInt();
				int DIFNameLength = input.readInt();
				int NMSNameLength = input.readInt();
				if (DIFNameLength >0) {
					text1 = input.readUTF();
				}

				if (NMSNameLength > 0) { 
					text2 = input.readUTF();
				}
				break;

			case CDAP_UPDATE_RIB_REQ:
				address = input.readInt();
				byte[] remaining = new byte[length - 4];
				input.readFully(remaining);
				break;


			case CDAP_UPDATE_RIB_RSP:
				errorCode = input.readInt();
				break;


			case CDAP_CONNECT_RSP:
				
				break;

			case CDAP_IDD_SERVADD_REQ:
				text1 = input.readUTF();
				text2 = input.readUTF();
				break;
				
			case CDAP_IDD_UPDATE_REQ:
				text1 = input.readUTF();
				text2 = input.readUTF();
				break;
				
			case CDAP_UPDATE_RSP:
				errorCode = input.readInt();
				break;


				// Default case handles
				// - DNS_REQ, DNS_RSP, HTTP_GET, HTTP_RSP, CDAP_CONNECT_REQ, 
			default:
				// read text field
				text1 = input.readUTF();
			}

			return this;
		}
}
