package lib.internet_dif;

import java.io.*;

public class InetDIFPacket {
	public enum Type {INIT, DATA, CLOSE, ERROR};
	
	public Type type;
	public int connID;
	public String senderName;
	public String receiverName;
	public int proposedConnID;

	public byte[] header;
	public byte[] payload;
	
	public byte[] data;
	
	public static InetDIFPacket parsePacket(byte[] unformattedData) {
		try {
			return new InetDIFPacket(unformattedData);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static InetDIFPacket initPacket(int proposedConnID, String senderName, String receiverName) {
		InetDIFPacket packet = null;
		try {packet = new InetDIFPacket(Type.INIT, proposedConnID, senderName, receiverName, null);}
		catch(IOException e){}
		
		return packet;
	}
	
	public static InetDIFPacket dataPacket(int connID, String senderName, String receiverName, byte[] payload) {
		InetDIFPacket packet = null;
		try {packet = new InetDIFPacket(Type.DATA, connID, senderName, receiverName, payload);}
		catch(IOException e){}
		
		return packet;
	}
	
	public InetDIFPacket(byte[] unformattedData) throws IOException {
		data = unformattedData;
		DataInputStream dataReader = new DataInputStream(new ByteArrayInputStream(unformattedData));
		
		// read conn ID
		connID = dataReader.readInt();
		
		// read receiver name
		receiverName = dataReader.readUTF();
		
		// read sender name
		senderName = dataReader.readUTF();
		
		// read message type
		int typeInt = dataReader.readInt();
		switch (typeInt) {
		case 0:
			type = Type.INIT;
			break;
		case 1:
			type = Type.DATA;
			break;
		case 2:
			type = Type.CLOSE;
			break;
		default:
			type = Type.ERROR;
		}
		
		if (type == Type.INIT) {
			proposedConnID = dataReader.readInt();
		}
		
		// read remaining data into the payload
		payload = new byte[dataReader.available()];
		dataReader.readFully(payload);
	}

	public InetDIFPacket(Type type, int connID, String senderName,
			String receiverName, byte[] payload) throws IOException {
		super();
		this.type = type;
		this.connID = connID;
		this.senderName = senderName;
		this.receiverName = receiverName;
		this.payload = payload;
		
		ByteArrayOutputStream _headerBuilder = new ByteArrayOutputStream();
		DataOutputStream headerBuilder = new DataOutputStream(_headerBuilder);
		
		switch (type) {
		case INIT:
			// write connID
			headerBuilder.writeInt(0);
			// write receiver name length
			//headerBuilder.writeInt(receiverName.length());
			// write receiver name in UTF-8
			headerBuilder.writeUTF(receiverName);
			// write sender name length
			//headerBuilder.writeInt(senderName.length());
			// write sender name in UTF-8
			headerBuilder.writeUTF(senderName);
			// write message type
			headerBuilder.writeInt(0);
			// write proposed connID
			headerBuilder.write(connID);
			
			header = _headerBuilder.toByteArray();
			data = header;
			break;
			
		case DATA:
			// write connID
			headerBuilder.writeInt(connID);
			// write receiver name length
			//headerBuilder.writeInt(receiverName.length());
			// write receiver name in UTF-8
			headerBuilder.writeUTF(receiverName);
			// write sender name length
			//headerBuilder.writeInt(senderName.length());
			// write sender name in UTF-8
			headerBuilder.writeUTF(senderName);
			// write message type
			headerBuilder.writeInt(1);
			
			// copy header and payload into new array
			header = _headerBuilder.toByteArray();
			data = new byte[header.length + payload.length];
			int j = 0;
			for (int i = 0; i < data.length; i++) {
				if (i < header.length) {
					data[i] = header[i];
				} else {
					data[i] = payload[j];
					j++;
				}
			}
			
			break;
			
		case CLOSE:
			// write connID
			headerBuilder.writeInt(connID);
			// write receiver name length
			//headerBuilder.writeInt(receiverName.length());
			// write receiver name in UTF-8
			headerBuilder.writeUTF(receiverName);
			// write sender name length
			//headerBuilder.writeInt(senderName.length());
			// write sender name in UTF-8
			headerBuilder.writeUTF(senderName);
			// write message type
			headerBuilder.writeInt(2);
			break;
			
		}
	
	}
	
	
 }
