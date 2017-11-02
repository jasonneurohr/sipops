package com.jasonneurohr;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * <h1>SipOp</h1>
 * The SipOp class provides methods for exchanging various SIP messages
 * with a target SIP device. TCP port 5060 is assumed for all exchanges.
 *
 * @author Jason Neurohr
 */
public class SipOp {
    private Socket sipSocket = null; // Client socket
    private BufferedOutputStream os = null;
    private DataInputStream is = null; // Input stream
    private String responseTag = "";
    private boolean okReceived = false;
    private String destinationSipUa;
    private String destinationUriDomainPart;
    private String destinationUriUserPart;
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
     * @param destinationUriDomainPart The target SIP device
     * @param destinationUriUserPart   The user part of the SIP uri (preceding the '@')
     * @param sourceIp                 The source IP
     */
    SipOp(String destinationSipUa, String destinationUriUserPart, String destinationUriDomainPart, String sourceIp) {
        this.destinationSipUa = destinationSipUa;
        this.destinationUriUserPart = destinationUriUserPart;
        this.destinationUriDomainPart = destinationUriDomainPart;
        this.sourceIp = sourceIp;
        this.callId = callId();
    }

    /**
     * This returns the destination SIP UA for the SipOp instance
     *
     * @return String This returns the destination SIP UA for the SipOp instance
     */
    public String getDestinationSipUa() {
        return destinationSipUa;
    }

    /**
     * Sets the destination SIP UA
     *
     * @param destinationSipUa the destination SIP UA
     */
    public void setDestinationSipUa(String destinationSipUa) {
        this.destinationSipUa = destinationSipUa;
    }

    /**
     * This returns the destination URI user part for the SipOp instance
     *
     * @return String This returns the destination URI user part for the SipOp instance
     */
    public String getDestinationUriUserPart() {
        return destinationUriUserPart;
    }

    /**
     * Sets the destination URI user part
     *
     * @param destinationUriUserPart the destination URI user part
     */
    public void setDestinationUriUserPart(String destinationUriUserPart) {
        this.destinationUriUserPart = destinationUriUserPart;
    }

    /**
     * This returns the destination URI domain part for the SipOp instance
     *
     * @return String This returns the destination URI domain part for the SipOp instance
     */
    public String getDestinationUriDomainPart() {
        return destinationUriDomainPart;
    }

    /**
     * Sets the destination URI domain part
     *
     * @param destinationUriDomainPart the destination URI domain part
     */
    public void setDestinationUriDomainPart(String destinationUriDomainPart) {
        this.destinationUriDomainPart = destinationUriDomainPart;
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
            sipSocket = new Socket(destinationSipUa, 5060);
            os = new BufferedOutputStream(sipSocket.getOutputStream());
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
                    sendEarlyOfferInvite(destinationUriDomainPart, destinationUriUserPart, sourceIp, os, "1", callId);
                } else if (mode.toLowerCase().equals("delayed")) {
                    sendDelayedOfferInvite(destinationUriDomainPart, destinationUriUserPart, sourceIp, os, "1", callId);
                } else if (mode.toLowerCase().equals("options")) {

                    // No need to ACK for OPTIONS
                    // Send OPTIONS, output response and return
                    sendOptions(destinationUriDomainPart, sourceIp, os, callId);
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
                            sendAck(destinationUriDomainPart, destinationUriUserPart, sourceIp, os, responseTag, callId);
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
     * @param destinationUriDomainPart The target SIP device
     * @param destinationUriUserPart   The user part of the SIP uri (preceding the '@')
     * @param sourceIp                 The source IP
     * @param os                       The output stream
     * @param cseq                     The SIP command sequence
     */
    private void sendEarlyOfferInvite(String destinationUriDomainPart, String destinationUriUserPart,
                                      String sourceIp, BufferedOutputStream os, String cseq, String callId) {
        try {
            String earlyOfferMessage = "INVITE sip:" + destinationUriUserPart + "@" + destinationUriDomainPart + ":5060;transport=tcp SIP/2.0\r\n" +
                    "Via: SIP/2.0/TCP " + sourceIp + ":5060;branch=1234\r\n" +
                    "From: <sip:99999@" + sourceIp + ">;tag=456\r\n" +
                    "To: <sip:" + destinationUriUserPart + "@" + destinationUriDomainPart + ":5060>\r\n" +
                    "Call-ID: " + callId + "@" + sourceIp + "\r\n" +
                    "CSeq: " + cseq + " INVITE\r\n" +
                    "Content-Type: application/sdp\r\n" +
                    "Contact: <sip:99999@" + sourceIp + ":5060;transport=tcp>\r\n" +
                    "User-Agent: SIP Probe\r\n" +
                    "Max-Forwards: 10\r\n" +
                    "Supported: replaces,timer\r\n" +
                    "P-Asserted-Identity: <sip:99999@" + sourceIp + ">\r\n" +
                    "Allow: INVITE,BYE,CANCEL,ACK,REGISTER,SUBSCRIBE,NOTIFY,MESSAGE,INFO,REFER,OPTIONS,PUBLISH,PRACK\r\n" +
                    "Content-Type: application/sdp\r\n" +
                    "Content-Length: 207\r\n\r\n" +
                    "o=SP 12345 IN IP4 " + sourceIp + "\r\n" +
                    "s=-\r\n" +
                    "p=11111\r\n" +
                    "t=0 0\r\n" +
                    "m=audio " + randPort() + " RTP/AVP 8 101\r\n" +
                    "c=IN IP4 " + sourceIp + "\r\n" +
                    "a=rtpmap:8 PCMA/8000\r\n" +
                    "a=rtpmap:101 telephone-event/8000\r\n" +
                    "a=fmtp:101 0-15\r\n" +
                    "a=ptime:20\r\n" +
                    "a=sendrecv\r\n\r\n";

            os.write(earlyOfferMessage.getBytes());
            os.flush();

        } catch (java.io.IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Sends a SIP delayed offer INVITE message to the target SIP device
     *
     * @param destinationUriDomainPart The target SIP device
     * @param destinationUriUserPart   The user part of the SIP uri (preceding the '@')
     * @param sourceIp                 The source IP
     * @param os                       The output stream
     * @param cseq                     The SIP command sequence
     */
    private void sendDelayedOfferInvite(String destinationUriDomainPart, String destinationUriUserPart,
                                        String sourceIp, BufferedOutputStream os, String cseq, String callId) {
        try {
            String delayedOfferMessage = "INVITE sip:" + destinationUriUserPart + "@" + destinationUriDomainPart + ":5060;transport=tcp SIP/2.0\r\n" +
                    "Via: SIP/2.0/TCP " + sourceIp + ":5060;branch=1234\r\n" +
                    "From: <sip:99999@" + sourceIp + ">;tag=456\r\n" +
                    "To: <sip:" + destinationUriUserPart + "@" + destinationUriDomainPart + ":5060>\r\n" +
                    "Call-ID: " + callId + "@" + sourceIp + "\r\n" +
                    "CSeq: " + cseq + " INVITE\r\n" +
                    "Content-Type: application/sdp\r\n" +
                    "Contact: <sip:99999@" + sourceIp + ":5060;transport=tcp>\r\n" +
                    "User-Agent: SIP Probe\r\n" +
                    "Max-Forwards: 10\r\n" +
                    "Supported: replaces,timer\r\n" +
                    "P-Asserted-Identity: <sip:99999@" + sourceIp + ">\r\n" +
                    "Allow: INVITE,BYE,CANCEL,ACK,REGISTER,SUBSCRIBE,NOTIFY,MESSAGE,INFO,REFER,OPTIONS,PUBLISH,PRACK\r\n" +
                    "Content-Type: application/sdp\r\n" +
                    "Content-Length: 0\r\n\r\n";

            os.write(delayedOfferMessage.getBytes());
            os.flush();

        } catch (java.io.IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Sends a SIP ACK message to the target device in an early offer exchange
     *
     * @param destinationUriDomainPart The target SIP device
     * @param destinationUriUserPart   The user part of the SIP uri (preceding the '@')
     * @param sourceIp                 The source IP
     * @param os                       The output stream
     * @param responseTag
     */
    private void sendAck(String destinationUriDomainPart, String destinationUriUserPart,
                         String sourceIp, BufferedOutputStream os, String responseTag, String callId) {
        try {
            String ackMessage = "ACK sip:" + destinationUriDomainPart + ":5060;transport=tcp SIP/2.0\r\n" +
                    "Via: SIP/2.0/TCP " + sourceIp + ":5060;branch=1234\r\n" +
                    "From: <sip:99999@" + sourceIp + ">;tag=456\r\n" +
                    "To: <sip:" + destinationUriUserPart + "@" + destinationUriDomainPart + ":5060>;tag=" + responseTag + "\r\n" +
                    "CSeq: 1 ACK\r\n" +
                    "Call-ID: " + callId + "@" + sourceIp + "\r\n" +
                    "Contact: <sip:99999@" + sourceIp + ":5060;transport=tcp>\r\n" +
                    "User-Agent: SIP Probe\r\n" +
                    "Allow: INVITE,ACK,BYE,CANCEL,OPTIONS,INFO,MESSAGE,SUBSCRIBE,NOTIFY,PRACK,UPDATE,REFER\r\n" +
                    "Max-Forwards: 10\r\n" +
                    "Content-Length: 0\r\n\r\n";

            os.write(ackMessage.getBytes());
            os.flush();

        } catch (java.io.IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Sends a SIP OPTIONS message to the target device
     *
     * @param destinationUriDomainPart The target SIP device
     * @param sourceIp                 The source IP
     * @param os                       The output stream
     */
    private void sendOptions(String destinationUriDomainPart, String sourceIp, BufferedOutputStream os, String callId) {
        try {
            String optionsMessage = "OPTIONS sip:" + destinationUriDomainPart + ":5060;transport=tcp SIP/2.0\r\n" +
                    "Via: SIP/2.0/TCP " + sourceIp + ":5060;branch=1234\r\n" +
                    "From: \"SIP Probe\"<sip:99999@" + sourceIp + ":5060>;tag=5678\r\n" +
                    "To: <sip:" + destinationUriDomainPart + ":5060>\r\n" +
                    "Call-ID: " + callId + "\r\n" +
                    "CSeq: 1 OPTIONS\r\n" +
                    "Max-Forwards: 0\r\n" +
                    "Content-Length: 0\r\n\r\n";

            os.write(optionsMessage.getBytes());
            os.flush();

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
