package org.eclipse.jdi.internal.spy;/*
 * JDI class Implementation
 *
 * (BB)
 * (C) Copyright IBM Corp. 2000
 */
 


import java.io.*;
import java.net.*;

/**
 * This class can be used to spy all JDWP packets. It should be configured 'in between' the debugger
 * application and the VM (or J9 debug proxy).
 * Its parameters are:
 *  1) The port number to which the debugger application connects;
 *  2) The port number on which the VM or proxy waits for a JDWP connection;
 *  3) The file where the trace is written to.
 *
 * Note that if this program is used for tracing JDWP activity of Leapfrog, the
 * 'debug remote program' option must be used, and the J9 proxy must first be started up by hand
 * on the port to which Leapfrog will connect.
 * The J9 proxy that is started up by Leapfrog is not used and will return immediately.
 */ 
public class TcpipSpy extends Thread {
		
	private static final byte[] handshakeBytes = "JDWP-Handshake".getBytes();
	private String fDescription;
	private DataInputStream fDataIn;
	private DataOutputStream fDataOut;
	private static PrintStream out = System.out;
	
	public TcpipSpy(String description, InputStream in, OutputStream out) throws IOException {
		fDescription = description;
		fDataIn = new DataInputStream(new BufferedInputStream(in));
		fDataOut = new DataOutputStream(new BufferedOutputStream(out));
	}

	public void run() {
		try {
			// Skip handshake.
			int handshakeLength;
			
			handshakeLength = handshakeBytes.length;
			while (handshakeLength-- > 0) {
				int b = fDataIn.read();
				fDataOut.write(b);
			}
			out.println(fDescription + "performed handshake.");
			out.flush();
			fDataOut.flush();
			
			// Print all packages.
			while (true) {
				int length = fDataIn.readInt();
				fDataOut.writeInt(length);
				length -= 4;
				out.println();
				out.println(fDescription + " remaining length:" + length + ".");
				while (length-- > 0) {
					int b = fDataIn.readUnsignedByte();
					fDataOut.write(b);
					if (b <= 0xf)
						out.print(" 0");
					else
						out.print(" ");

					out.print(Integer.toHexString(b));
				}
				out.flush();
				fDataOut.flush();
			}
		} catch (Exception e) {
			out.println(fDescription + " ERROR: " + e);
		}
	}

	public static void main(String[] args) {
		int inPort = 0;
		int outPort = 0;
		String outputFile = null;
		try {
			inPort = Integer.parseInt(args[0]);
			outPort = Integer.parseInt(args[1]);
			outputFile = args[2];
		}
		catch (Exception e) {
			out.println("Usage: TcpipSpy <inPort> <outPort> <outputFile>");
			System.exit(-1);
		}
		
		try {
			File file = new File(outputFile);
			out.println("TcpipSpy: logging output to " + file.getAbsolutePath());
			out = new PrintStream(new BufferedOutputStream(new FileOutputStream(file)));
		}
		catch (Exception e) {
			out.println("Cannot open " + outputFile);
			System.exit(-2);
		}
		out.println("Waiting in port " + inPort + ", connecting to port " + outPort);
		out.println();
		try {
			ServerSocket serverSock = new ServerSocket(inPort);
			Socket inSock = serverSock.accept();
			Socket outSock = new Socket(InetAddress.getLocalHost(), outPort);
			new TcpipSpy("From debugger:", inSock.getInputStream(), outSock.getOutputStream()).start();
			new TcpipSpy("From VM:", outSock.getInputStream(), inSock.getOutputStream()).start();
		} catch (Exception e) {
			out.println(e);
		}
	}
}