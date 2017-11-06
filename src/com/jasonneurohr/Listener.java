package com.jasonneurohr;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * <h1>Listener</h1>
 * A sample Listener class.
  *
 * @author Jason Neurohr
 */
public class Listener {
    private ServerSocket serverSocket = null;
    private Socket clientSocket = null;
    private BufferedOutputStream os = null;
    private Scanner is = null;

    public static void main(String[] args) {
        Listener listener = new Listener();
        listener.startListener();
    }

    public void startListener() {
        try {
            serverSocket = new ServerSocket(5060);
            clientSocket = serverSocket.accept();
            os = new BufferedOutputStream(clientSocket.getOutputStream());
            is = new Scanner(new BufferedInputStream(clientSocket.getInputStream()));

            while (is.hasNext()) {
                System.out.println(is.nextLine());
            }

        } catch (java.io.IOException e) {
            System.out.println(e);
        }
    }
}
