package com.jasonneurohr;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * <h1>SipOp</h1>
 * The SipOp class provides methods for exchanging various SIP messages
 * with a target SIP device. TCP port 5060 is assumed for all exchanges.
 *
 * @author Jason Neurohr
 */
public class SipOp {
    private Socket sipSocket = null; // Client socket
    private SSLSocketFactory sslSocketFactory = null;
    private SSLSocket sslSocket = null;
    private BufferedOutputStream os = null;
    private Scanner is = null;
    private String responseTag = "";
    private boolean okReceived = false;
    private boolean useSipTls = false;
    private String destinationPort;
    private String keyStorePath;
    private String destinationSipUa;
    private String destinationUriDomainPart;
    private String destinationUriUserPart;
    private String sourceIp;
    private String callId;
    private String nextLine;
    private boolean ackSent = false;
    private Integer contentLength = 0;
    private int amountRead = 0;
    private boolean startCounting = false;

    /**
     * No-arg constructor sets the callId for the instance
     */
    SipOp() {
        this.callId = callId();
    }

    /**
     * @param destinationSipUa The target SIP device
     * @param sourceIp         The source IP
     */
    SipOp(String destinationSipUa, String sourceIp) {
        this.destinationSipUa = destinationSipUa;
        this.sourceIp = sourceIp;
    }

    /**
     * @param destinationSipUa The target SIP device
     * @param sourceIp         The source IP
     */
    SipOp(String destinationSipUa, String sourceIp, boolean useSipTls, String keyStorePath) {
        this.destinationSipUa = destinationSipUa;
        this.sourceIp = sourceIp;
        this.keyStorePath = keyStorePath;
        this.useSipTls = useSipTls;
        if (useSipTls) {
            this.destinationPort = "5061";
        } else {
            this.destinationPort = "5060";
        }
    }

    /**
     * Constructs a SipOp instance with provided parameters and a callId
     *
     * @param destinationSipUa         The target SIP device
     * @param destinationUriUserPart   The user part of the SIP uri (preceding the '@')
     * @param destinationUriDomainPart The domain part of the SIP uri (following the '@')
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
     * Constructs a SipOp instance with provided parameters and a callId
     *
     * @param destinationSipUa         The target SIP device
     * @param destinationUriUserPart   The user part of the SIP uri (preceding the '@')
     * @param destinationUriDomainPart The domain part of the SIP uri (following the '@')
     * @param sourceIp                 The source IP
     */
    SipOp(String destinationSipUa, String destinationUriUserPart, String destinationUriDomainPart,
          String sourceIp, boolean useSipTls, String keyStorePath) {
        this.destinationSipUa = destinationSipUa;
        this.destinationUriUserPart = destinationUriUserPart;
        this.destinationUriDomainPart = destinationUriDomainPart;
        this.sourceIp = sourceIp;
        this.callId = callId();
        this.keyStorePath = keyStorePath;
        this.useSipTls = useSipTls;
        if (useSipTls) {
            this.destinationPort = "5061";
        } else {
            this.destinationPort = "5060";
        }
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
            if (!useSipTls) {
                sipSocket = new Socket(destinationSipUa, 5060);
                os = new BufferedOutputStream(sipSocket.getOutputStream());
                is = new Scanner(new BufferedInputStream(sipSocket.getInputStream()));
            }

            if (useSipTls) {
                System.setProperty("javax.net.ssl.trustStore", keyStorePath);
                sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                sslSocket = (SSLSocket) sslSocketFactory.createSocket(destinationSipUa, 5061);
                os = new BufferedOutputStream(sslSocket.getOutputStream());
                is = new Scanner(new BufferedInputStream(sslSocket.getInputStream()));
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: hostname");
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: hostname");
        }

        // If everything has been initialized then we want to write some data
        // to the socket we have opened a connection to
        if ((sipSocket != null || sslSocket != null) && os != null && is != null) {

            try {
                if (mode.toLowerCase().equals("early")) {
                    sendEarlyOfferInvite(destinationUriDomainPart, destinationUriUserPart,
                            sourceIp, os, "1", callId, destinationPort);
                } else if (mode.toLowerCase().equals("delayed")) {
                    sendDelayedOfferInvite(destinationUriDomainPart, destinationUriUserPart,
                            sourceIp, os, "1", callId, destinationPort);
                } else if (mode.toLowerCase().equals("options")) {

                    // No need to ACK for OPTIONS
                    // Send OPTIONS, output response and return
                    sendOptions(destinationSipUa, sourceIp, os, callId, destinationPort);
                    System.out.println("Received:");
                    while (is.hasNext()) {
                        System.out.println(is.nextLine());
                    }
                    return;
                } else {
                    return;
                }

                while (is.hasNext()) {
                    // Get the next line of input
                    nextLine = is.nextLine();
                    System.out.println(nextLine);

                    // If received a 200 OK, set receive flag to true
                    if (!ackSent && nextLine.contains("SIP/2.0 200 OK")) {
                        okReceived = true;
                    }

                    // If okReceived is true and the line contains the To field
                    // Grab the ID for later messages
                    if (!ackSent && okReceived && nextLine.contains("To:")) {
                        responseTag = nextLine.split("tag=")[1];
                    }

                    // If okReceived is true and the line contains Content-Length
                    // Capture the length of the SDP, if its greater then 0
                    // Set startCounting to true
                    if (!ackSent && okReceived && nextLine.contains("Content-Length")) {
                        contentLength = (Integer.parseInt(nextLine.split("Content-Length: ")[1]));
                        if (contentLength > 0) {
                            startCounting = true;
                        }
                    }

                    // If startCounting is true increment the amount read
                    // by the length of the line
                    if (!ackSent && startCounting) {
                        amountRead += nextLine.length();
                    }

                    // If the okReceived is true and amount Read == contentLength - 3
                    // Send the ACK in response to the received 200OK
                    // Set the ackSent flag to true
                    if (!ackSent && okReceived && amountRead == (contentLength - 3)) {
                        sendAck(destinationUriDomainPart, destinationUriUserPart,
                                sourceIp, os, responseTag, callId, "1", destinationPort);
                        ackSent = true;
                        break;

                    }
                }

                System.out.println("Tearing down socket");
                // Tearing down the socket will cause a FIN, ACK to be sent immediately
                // This could cause unexpected events on the far end, for example
                // A Cisco Meeting Server will output messages similar to the below to the syslog
                //
                // Nov  6 09:51:50 user.info CMS1 host:server:  INFO : SIP trace: connection 12: read failure, code 104
                // Nov  6 09:51:50 user.info CMS1 host:server:  INFO : SIP trace: connection 12: shutting down...

                os.close(); // close the output stream
                is.close(); // close the input stream
                if (!useSipTls) {
                    sipSocket.close(); // close the socket
                } else if (useSipTls) {
                    sslSocket.close();
                }
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
     * @param callId                   The callID
     */
    private void sendEarlyOfferInvite(String destinationUriDomainPart, String destinationUriUserPart,
                                      String sourceIp, BufferedOutputStream os, String cseq, String callId,
                                      String destinationPort) {

        try {

            String sdpContent = "v=0\r\n" +
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
                    "a=recvonly\r\n\r\n";

            String earlyOfferMessage = "INVITE sip:" + destinationUriUserPart + "@" + destinationUriDomainPart + ":" + destinationPort + ";transport=tcp SIP/2.0\r\n" +
                    "Via: SIP/2.0/TCP " + sourceIp + ":" + destinationPort + ";branch=1234\r\n" +
                    "From: <sip:99999@" + sourceIp + ">;tag=456\r\n" +
                    "To: <sip:" + destinationUriUserPart + "@" + destinationUriDomainPart + ":" + destinationPort + ">\r\n" +
                    "Call-ID: " + callId + "@" + sourceIp + "\r\n" +
                    "CSeq: " + cseq + " INVITE\r\n" +
                    "Content-Type: application/sdp\r\n" +
                    "Contact: <sip:99999@" + sourceIp + ":" + destinationPort + ";transport=tcp>\r\n" +
                    "User-Agent: SIP Probe\r\n" +
                    "Max-Forwards: 10\r\n" +
                    "Supported: replaces,timer\r\n" +
                    "P-Asserted-Identity: <sip:99999@" + sourceIp + ">\r\n" +
                    "Allow: INVITE,BYE,CANCEL,ACK,REGISTER,SUBSCRIBE,NOTIFY,MESSAGE,INFO,REFER,OPTIONS,PUBLISH,PRACK\r\n" +
                    "Content-Type: application/sdp\r\n" +
                    "Content-Length: " + sdpContent.length() + "\r\n\r\n" +
                    sdpContent;

            System.out.println();
            System.out.println("Sending:");
            System.out.println(earlyOfferMessage);

            os.write(earlyOfferMessage.getBytes());
            os.flush();

        } catch (java.io.IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Sends a SIP early offer INVITE message to the target SIP device.
     * Includes the responseTag such that a RE-INVITE can be sent
     *
     * @param destinationUriDomainPart The target SIP device
     * @param destinationUriUserPart   The user part of the SIP uri (preceding the '@')
     * @param sourceIp                 The source IP
     * @param os                       The output stream
     * @param cseq                     The SIP command sequence
     * @param callId                   The callID
     * @param responseTag              The tag returned from the far end SIP UA
     */
    private void sendEarlyOfferInvite(String destinationUriDomainPart, String destinationUriUserPart,
                                      String sourceIp, BufferedOutputStream os, String cseq, String callId,
                                      String destinationPort, String responseTag) {
        try {

            String sdpContent = "v=0\r\n" +
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
                    "a=recvonly\r\n\r\n";

            String earlyOfferMessage = "INVITE sip:" + destinationUriUserPart + "@" + destinationUriDomainPart + ":" + destinationPort + ";transport=tcp SIP/2.0\r\n" +
                    "Via: SIP/2.0/TCP " + sourceIp + ":" + destinationPort + ";branch=1234\r\n" +
                    "From: <sip:99999@" + sourceIp + ">;tag=456\r\n" +
                    "To: <sip:" + destinationUriUserPart + "@" + destinationUriDomainPart + ":" + destinationPort + ">;tag=" + responseTag + "\r\n" + // TODO: utilise To header from earlier messaging
                    "Call-ID: " + callId + "@" + sourceIp + "\r\n" +
                    "CSeq: " + cseq + " INVITE\r\n" +
                    "Content-Type: application/sdp\r\n" +
                    "Contact: <sip:99999@" + sourceIp + ":" + destinationPort + ";transport=tcp>\r\n" +
                    "User-Agent: SIP Probe\r\n" +
                    "Max-Forwards: 10\r\n" +
                    "Supported: replaces,timer\r\n" +
                    "P-Asserted-Identity: <sip:99999@" + sourceIp + ">\r\n" +
                    "Allow: INVITE,BYE,CANCEL,ACK,REGISTER,SUBSCRIBE,NOTIFY,MESSAGE,INFO,REFER,OPTIONS,PUBLISH,PRACK\r\n" +
                    "Content-Type: application/sdp\r\n" +
                    "Content-Length: " + sdpContent.length() + "\r\n\r\n" +
                    sdpContent;

            System.out.println();
            System.out.println("Sending:");
            System.out.println(earlyOfferMessage);

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
                                        String sourceIp, BufferedOutputStream os, String cseq, String callId,
                                        String destinationPort) {
        try {
            String delayedOfferMessage = "INVITE sip:" + destinationUriUserPart + "@" + destinationUriDomainPart + ":" + destinationPort + ";transport=tcp SIP/2.0\r\n" +
                    "Via: SIP/2.0/TCP " + sourceIp + ":" + destinationPort + ";branch=1234\r\n" +
                    "From: <sip:99999@" + sourceIp + ">;tag=456\r\n" +
                    "To: <sip:" + destinationUriUserPart + "@" + destinationUriDomainPart + ":" + destinationPort + ">\r\n" +
                    "Call-ID: " + callId + "@" + sourceIp + "\r\n" +
                    "CSeq: " + cseq + " INVITE\r\n" +
                    "Content-Type: application/sdp\r\n" +
                    "Contact: <sip:99999@" + sourceIp + ":" + destinationPort + ";transport=tcp>\r\n" +
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
     * @param responseTag              The tag returned from the far end SIP UA
     * @param callId                   The callID
     * @param cseq                     The SIP command sequence
     */
    private void sendAck(String destinationUriDomainPart, String destinationUriUserPart,
                         String sourceIp, BufferedOutputStream os, String responseTag, String callId, String cseq,
                         String destinationPort) {
        try {
            String ackMessage = "ACK sip:" + destinationUriDomainPart + ":" + destinationPort + ";transport=tcp SIP/2.0\r\n" +
                    "Via: SIP/2.0/TCP " + sourceIp + ":" + destinationPort + ";branch=1234\r\n" +
                    "From: <sip:99999@" + sourceIp + ">;tag=456\r\n" +
                    "To: <sip:" + destinationUriUserPart + "@" + destinationUriDomainPart + ":" + destinationPort + ">;tag=" + responseTag + "\r\n" + // TODO: utilise To header from earlier messaging
                    "CSeq: " + cseq + " ACK\r\n" +
                    "Call-ID: " + callId + "@" + sourceIp + "\r\n" +
                    "Contact: <sip:99999@" + sourceIp + ":" + destinationPort + ";transport=tcp>\r\n" +
                    "User-Agent: SIP Probe\r\n" +
                    "Allow: INVITE,ACK,BYE,CANCEL,OPTIONS,INFO,MESSAGE,SUBSCRIBE,NOTIFY,PRACK,UPDATE,REFER\r\n" +
                    "Max-Forwards: 10\r\n" +
                    "Content-Length: 0\r\n\r\n";

            System.out.println();
            System.out.println("Sending:");
            System.out.println(ackMessage);

            os.write(ackMessage.getBytes());
            os.flush();

        } catch (java.io.IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Sends a SIP OPTIONS message to the target device
     *
     * @param destinationSipUa The target SIP device
     * @param sourceIp         The source IP
     * @param os               The output stream
     */
    private void sendOptions(String destinationSipUa, String sourceIp, BufferedOutputStream os, String callId,
                             String destinationPort) {
        try {
            String optionsMessage = "OPTIONS sip:" + destinationSipUa + ":" + destinationPort + ";transport=tcp SIP/2.0\r\n" +
                    "Via: SIP/2.0/TCP " + sourceIp + ":" + destinationPort + ";branch=1234\r\n" +
                    "From: \"SIP Probe\"<sip:99999@" + sourceIp + ":" + destinationPort + ">;tag=5678\r\n" +
                    "To: <sip:" + destinationSipUa + ":" + destinationPort + ">\r\n" +
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
