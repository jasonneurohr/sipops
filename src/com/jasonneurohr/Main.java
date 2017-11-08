package com.jasonneurohr;

/**
 * Initialises a SipOp instance and passes command line parameters
 *
 * @author Jason Neurohr
 */
public class Main {

    public static void main(String[] args) {
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                args[i] = args[i].toLowerCase();
            }

            if (args[0].equals("early") || args[0].equals("delayed")) {
                if (args.length < 5) {
                    printHelp();
                }
                if (args.length == 5) {
                    // Not enough args to be secure
                    SipOp sipOp = new SipOp(args[1], args[2], args[3], args[4]);
                    sipOp.newInviteOp(args[0]);
                } else if (args.length == 7) {
                    SipOp sipOp = new SipOp(args[1], args[2], args[3], args[4], true, args[6]);
                    sipOp.newInviteOp(args[0]);
                } else {
                    printHelp();
                }
            } else if (args[0].equals("options")) {
                if (args.length == 3) {
                    SipOp sipOp = new SipOp(args[1], args[2]);
                    sipOp.newInviteOp(args[0]);
                } else if (args.length == 5) {
                    SipOp sipOp = new SipOp(args[1], args[2], true, args[4]);
                    sipOp.newInviteOp(args[0]);
                } else {
                    printHelp();
                }
            } else {
                printHelp();
            }
        } else {
            printHelp();
        }
    }

    public static void printHelp() {
        System.out.println();
        System.out.println("Syntax:");
        System.out.println("Note: \"secure\" is an optional argument to use SIP TLS (Port 5061)");
        System.out.println("If you use this option, you must specify a Java Keystore Path");
        System.out.println();
        System.out.println("Early offer INVITE:\t java -jar .\\SipOps.jar early <destination UA> <URI user part> <URI domain part> <Source IP> [secure] [Keystore Path]");
        System.out.println("Delayed offer INVITE:\t java -jar .\\SipOps.jar delayed <destination UA> <URI user part> <URI domain part> <Source IP> [secure] [Keystore Path]");
        System.out.println("OPTIONS:\t\t java -jar .\\SipOps.jar options <destination UA> <Source IP> [secure] [Keystore Path]");
        System.out.println();
        System.out.println("Sample Usage:");
        System.out.println();
        System.out.println("Early Offer INVITE:");
        System.out.println("java -jar .\\SipOps.jar early 192.168.44.122 1 192.168.44.122 192.168.44.32");
        System.out.println("java -jar .\\SipOps.jar early 192.168.44.122 1 192.168.44.122 192.168.44.32 secure C:\\myJavaKeystore.jks");
        System.out.println();
        System.out.println("Delayed Offer INVITE:");
        System.out.println("java -jar .\\SipOps.jar delayed 192.168.44.122 1 192.168.44.122 192.168.44.32");
        System.out.println("java -jar .\\SipOps.jar delayed 192.168.44.122 1 192.168.44.122 192.168.44.32 secure C:\\myJavaKeystore.jks");
        System.out.println();
        System.out.println("OPTIONS:");
        System.out.println("java -jar .\\SipOps.jar options 192.168.44.122 192.168.44.32");
        System.out.println("java -jar .\\SipOps.jar options 192.168.44.122 192.168.44.32 secure C:\\myJavaKeystore.jks");
        System.out.println();
    }
}
