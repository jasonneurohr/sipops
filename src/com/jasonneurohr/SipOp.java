package com.jasonneurohr;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * <h1>SipOp</h1>
 * The SipOp class provides methods for exchanging various SIP messages
 * with a target SIP device. TCP port 5060 is assumed for all exchanges.
 * <p>
 * To-do List<br>
 * - Delayed offer ACK<br>
 * </p>
 * @author Jason Neurohr
 *
 */
public class SipOp {
    private Socket sipSocket = null; // Client socket
    private DataOutputStream os = null; // Output stream
    private DataInputStream is = null; // Input stream
    private String responseTag = "";
    private boolean okReceived = false;
    private String destinationIp;
    private String destinationUserPart;
    private String sourceIp;
    private String callId;

    /**
     * No-arg constructor sets the callId for the instance
     */
    SipOp() {
        this.callId = callId();
    }

    /**
     * Constructs a SipOp instance with provided parameters and a callId
     *
     * @param destinationIp       The target SIP device
     * @param destinationUserPart The user part of the SIP uri (preceding the '@')
     * @param sourceIp            The source IP
     */
    SipOp(String destinationIp, String destinationUserPart, String sourceIp) {
        this.destinationIp = destinationIp;
        this.destinationUserPart = destinationUserPart;
        this.sourceIp = sourceIp;
        this.callId = callId();
    }

    /**
     * This returns the destination IP for the SipOp instance
     *
     * @return String This returns the destination IP for the SipOp instance
     */
    public String getDestinationIp() {
        return destinationIp;
    }

    /**
     * Sets the destination IP
     *
     * @param destinationIp the destination IP
     */
    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    /**
     * This returns the destination user URI part
     *
     * @return String This returns the destination user URI part
     */
    public String getDestinationUriPart() {
        return destinationUserPart;
    }

    /**
     * Sets the destination User URI part
     *
     * @param destinationUserPart The destination user URI part
     */
    public void setDestinationUriPart(String destinationUserPart) {
        this.destinationUserPart = destinationUserPart;
    }

    /**
     * Returns the source IP
     *
     * @return String This returns the source IP
     */
    public String getSourceIp() {
        return sourceIp;
    }

    /**
     * Sets the source IP
     *
     * @param sourceIp The source IP
     */
    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    /**
     * Returns the call ID
     *
     * @return String This returns the call ID
     */
    public String getCallId() {
        return callId;
    }

    /**
     * Sets the call ID
     *
     * @param callId This sets the call ID
     */
    public void setCallId(String callId) {
        this.callId = callId;
    }

    /**
     * This method accepts a mode parameter of (a) early, (b) delayed, or (c) options and
     * sends the corresponding SIP messages to the target
     * *
     *
     * @param mode The SIP message type
     */
    public void newInviteOp(String mode) {
        // Try to open a socket
        // Try to open input and output streams
        try {
            sipSocket = new Socket(destinationIp, 5060);
            os = new DataOutputStream(sipSocket.getOutputStream());
            is = new DataInputStream(sipSocket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: hostname");
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: hostname");
        }

        // If everything has been initialized then we want to write some data
        // to the socket we have opened a connection to
        if (sipSocket != null && os != null && is != null) {
            try {
                if (mode.toLowerCase().equals("early")) {
                    sendEarlyOfferInvite(destinationIp, destinationUserPart, sourceIp, os, "1", callId);
                } else if (mode.toLowerCase().equals("delayed")) {
                    sendDelayedOfferInvite(destinationIp, destinationUserPart, sourceIp, os, "1", callId);
                } else if (mode.toLowerCase().equals("options")) {

                    // No need to ACK for OPTIONS
                    // Send OPTIONS, output response and return
                    sendOptions(destinationIp, sourceIp, os, callId);
                    String responseLine;
                    while ((responseLine = is.readLine()) != null && responseLine.length() > 0) {
                        System.out.println("Server: " + responseLine);
                    }
                    return;

                } else {
                    System.out.println("Invalid mode. Enter \"early\", \"delayed\" or \"options\"");
                    return;
                }

                String responseLine;

                while ((responseLine = is.readLine()) != null) {
                    System.out.println("Server: " + responseLine);

                    // Received a 200 OK
                    if (responseLine.contains("SIP/2.0 200 OK")) {
                        okReceived = true;
                    }

                    // Grab the returned tag so we can send the ACK
                    if (okReceived && responseLine.contains("To:")) {
                        responseTag = responseLine.split("tag=")[1];
                        if (mode.toLowerCase().equals("early")) {
                            sendAck(destinationIp, destinationUserPart, sourceIp, os, responseTag, callId);
                        } else if (mode.toLowerCase().equals("delayed")) {
                            // TODO
                        }
                        okReceived = false;
                        break;
                    }
                }
                os.close(); // close the output stream
                is.close(); // close the input stream
                sipSocket.close(); // close the socket
            } catch (UnknownHostException e) {
                System.err.println("Trying to connect to unknown host: " + e);
            } catch (IOException e) {
                System.err.println("IOException:  " + e);
            }
        }
    }

    /**
     * Sends a SIP early offer INVITE message to the target SIP device
     *
     * @param destinationIp       The target SIP device
     * @param destinationUserPart The user part of the SIP uri (preceding the '@')
     * @param sourceIp            The source IP
     * @param os                  The output stream
     * @param cseq                The SIP command sequence
     */
    private void sendEarlyOfferInvite(String destinationIp, String destinationUserPart,
                                      String sourceIp, DataOutputStream os, String cseq, String callId) {
        try {
            // Early offer (sdp included)
            os.writeBytes("INVITE sip:" + destinationUserPart + "@" + destinationIp + ":5060;transport=tcp SIP/2.0\r\n");
            os.writeBytes("Via: SIP/2.0/TCP " + sourceIp + ":5060;branch=1234\r\n");
            os.writeBytes("From: <sip:99999@" + sourceIp + ">;tag=456\r\n");
            os.writeBytes("To: <sip:" + destinationUserPart + "@" + destinationIp + ":5060>\r\n");
            os.writeBytes("Call-ID: " + callId + "@" + sourceIp + "\r\n");
            os.writeBytes("CSeq: " + cseq + " INVITE\r\n");
            os.writeBytes("Content-Type: application/sdp\r\n");
            os.writeBytes("Contact: <sip:99999@" + sourceIp + ":5060;transport=tcp>\r\n");
            os.writeBytes("User-Agent: SIP Probe\r\n");
            os.writeBytes("Max-Forwards: 10\r\n");
            os.writeBytes("Supported: replaces,timer\r\n");
            os.writeBytes("P-Asserted-Identity: <sip:99999@" + sourceIp + ">\r\n");
            os.writeBytes("Allow: INVITE,BYE,CANCEL,ACK,REGISTER,SUBSCRIBE,NOTIFY,MESSAGE,INFO,REFER,OPTIONS,PUBLISH,PRACK\r\n");
            os.writeBytes("Content-Type: application/sdp\r\n");
            os.writeBytes("Content-Length: 207\r\n\r\n");
            os.writeBytes("o=SP 12345 IN IP4 " + sourceIp + "\r\n");
            os.writeBytes("s=-\r\n");
            os.writeBytes("p=11111\r\n");
            os.writeBytes("t=0 0\r\n");
            os.writeBytes("m=audio " + randPort() + " RTP/AVP 8 101\r\n");
            os.writeBytes("c=IN IP4 " + sourceIp + "\r\n");
            os.writeBytes("a=rtpmap:8 PCMA/8000\r\n");
            os.writeBytes("a=rtpmap:101 telephone-event/8000\r\n");
            os.writeBytes("a=fmtp:101 0-15\r\n");
            os.writeBytes("a=ptime:20\r\n");
            os.writeBytes("a=sendrecv\r\n\r\n");
        } catch (java.io.IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Sends a SIP delayed offer INVITE message to the target SIP device
     *
     * @param destinationIp       The target SIP device
     * @param destinationUserPart The user part of the SIP uri (preceding the '@')
     * @param sourceIp            The source IP
     * @param os                  The output stream
     * @param cseq                The SIP command sequence
     */
    private void sendDelayedOfferInvite(String destinationIp, String destinationUserPart,
                                        String sourceIp, DataOutputStream os, String cseq, String callId) {
        try {
            // Early offer (sdp included)
            os.writeBytes("INVITE sip:" + destinationUserPart + "@" + destinationIp + ":5060;transport=tcp SIP/2.0\r\n");
            os.writeBytes("Via: SIP/2.0/TCP " + sourceIp + ":5060;branch=1234\r\n");
            os.writeBytes("From: <sip:99999@" + sourceIp + ">;tag=456\r\n");
            os.writeBytes("To: <sip:" + destinationUserPart + "@" + destinationIp + ":5060>\r\n");
            os.writeBytes("Call-ID: " + callId + "@" + sourceIp + "\r\n");
            os.writeBytes("CSeq: " + cseq + " INVITE\r\n");
            os.writeBytes("Content-Type: application/sdp\r\n");
            os.writeBytes("Contact: <sip:99999@" + sourceIp + ":5060;transport=tcp>\r\n");
            os.writeBytes("User-Agent: SIP Probe\r\n");
            os.writeBytes("Max-Forwards: 10\r\n");
            os.writeBytes("Supported: replaces,timer\r\n");
            os.writeBytes("P-Asserted-Identity: <sip:99999@" + sourceIp + ">\r\n");
            os.writeBytes("Allow: INVITE,BYE,CANCEL,ACK,REGISTER,SUBSCRIBE,NOTIFY,MESSAGE,INFO,REFER,OPTIONS,PUBLISH,PRACK\r\n");
            os.writeBytes("Content-Type: application/sdp\r\n");
            os.writeBytes("Content-Length: 0\r\n\r\n");
        } catch (java.io.IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Sends a SIP ACK message to the target device in an early offer exchange
     *
     * @param destinationIp       The target SIP device
     * @param destinationUserPart The user part of the SIP uri (preceding the '@')
     * @param sourceIp            The source IP
     * @param os                  The output stream
     * @param responseTag
     */
    private void sendAck(String destinationIp, String destinationUserPart,
                         String sourceIp, DataOutputStream os, String responseTag, String callId) {
        try {
            os.writeBytes("ACK sip:" + destinationIp + ":5060;transport=tcp SIP/2.0\r\n");
            os.writeBytes("Via: SIP/2.0/TCP " + sourceIp + ":5060;branch=1234\r\n");
            os.writeBytes("From: <sip:99999@" + sourceIp + ">;tag=456\r\n");
            os.writeBytes("To: <sip:" + destinationUserPart + "@" + destinationIp + ":5060>;tag=" + responseTag + "\r\n");
            os.writeBytes("CSeq: 1 ACK\r\n");
            os.writeBytes("Call-ID: " + callId + "@" + sourceIp + "\r\n");
            os.writeBytes("Contact: <sip:99999@" + sourceIp + ":5060;transport=tcp>\r\n");
            os.writeBytes("User-Agent: SIP Probe\r\n");
            os.writeBytes("Allow: INVITE,ACK,BYE,CANCEL,OPTIONS,INFO,MESSAGE,SUBSCRIBE,NOTIFY,PRACK,UPDATE,REFER\r\n");
            os.writeBytes("Max-Forwards: 10\r\n");
            os.writeBytes("Content-Length: 0\r\n\r\n");
        } catch (java.io.IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Sends a SIP OPTIONS message to the target device
     *
     * @param destinationIp The target SIP device
     * @param sourceIp      The source IP
     * @param os            The output stream
     */
    private void sendOptions(String destinationIp, String sourceIp, DataOutputStream os, String callId) {
        try {
            os.writeBytes("OPTIONS sip:" + destinationIp + ":5060;transport=tcp SIP/2.0\r\n");
            os.writeBytes("Via: SIP/2.0/TCP " + sourceIp + ":5060;branch=1234\r\n");
            os.writeBytes("From: \"SIP Probe\"<sip:99999@" + sourceIp + ":5060>;tag=5678\r\n");
            os.writeBytes("To: <sip:" + destinationIp + ":5060>\r\n");
            os.writeBytes("Call-ID: " + callId + "\r\n");
            os.writeBytes("CSeq: 1 OPTIONS\r\n");
            os.writeBytes("Max-Forwards: 0\r\n");
            os.writeBytes("Content-Length: 0\r\n\r\n");
        } catch (java.io.IOException e) {
            System.out.println(e);
        }
    }

    /**
     * This returns a random high range port number
     *
     * @return String This returns a random high range port number
     */
    private String randPort() {
        int randomNum;
        randomNum = (int) (Math.random() * (65535 - 1024) + 1);
        return ((Integer) randomNum).toString();
    }

    /**
     * This returns a random 5 digit number for use as the call ID in the SIP messages
     *
     * @return String This returns a random 5 digit number
     */
    private String callId() {
        int randomNum;
        randomNum = (int) (Math.random() * (99999 - 10000) + 1);
        return ((Integer) randomNum).toString();
    }
}
