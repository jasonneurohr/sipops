package com.jasonneurohr;

/**
 * Initialises a SipOp instance and passes command line parameters
 *
 * @author Jason Neurohr
 */
public class Main {

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Missing parameters");
            System.out.println("java -jar SipOps.jar [early|delayed|option] <destination SIP UA> <destination URI user part> < destination URI domain part>"
                    + " <source IP>");
        } else {

            SipOp sipOp = new SipOp(args[1], args[2], args[3], args[4]);

            if (args[0].toLowerCase().equals("early") || args[0].toLowerCase().equals("delayed") ||
                    args[0].toLowerCase().equals("options")){
                sipOp.newInviteOp(args[0]);
            } else {
                System.out.println("Invalid mode. Enter \"early\", \"delayed\" or \"options\"");
            }
        }
    }
}
