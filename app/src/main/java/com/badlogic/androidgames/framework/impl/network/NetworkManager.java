package com.badlogic.androidgames.framework.impl.network;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkManager {
    private final Handler handlerToMainThread;

    private ServerSocket serverSocket;

    private Socket clientSocketConnectedToMyServerSocket;
    private Socket serverSocketMyClientSocketIsConnectedTo;
    private Socket clientSocketConnectedToMyMessagingServerSocket;

    private InetAddress sendToAddress;
    private int port;
    public NsdHelper nsdHelper;

    private final String TAG;
    private final String myServiceName;
    private final String serviceTypeName;

    private Thread connectionServerThread;
    private Thread connectionClientThread;

    private boolean connected;

    private Handler handlerFromMainThread;
    private static PrintWriter printWriter;

    public NetworkManager(Handler handler, Context context, String serviceName, String serviceTypeName) {
        handlerToMainThread = handler;

        nsdHelper = new NsdHelper(context);
        nsdHelper.initializeRegistrationListener();
        nsdHelper.initializeDiscoveryListener();
        
        TAG = serviceName + "Tag";
        myServiceName = serviceName;
        this.serviceTypeName = serviceTypeName;
    }

    public boolean initializeServer() {
        if (!initializeServerSocket()) {
            return false;
        }

        nsdHelper.registerService(port);

        long start = System.nanoTime();
        while (!nsdHelper.serviceRegistered) {
            if (System.nanoTime() - start >= 5e9 || nsdHelper.serviceRegistrationFailed) {
                Log.d(TAG, "initializeServer - cannot register service");
                nsdHelper.serviceRegistrationFailed = false;
                return false;
            }
        }

        if (connectionServerThread == null) { // Todo: Why checking if it is null??!! Investigate to remove this check
            connectionServerThread = new Thread(new ConnectionServerThread());
            connectionServerThread.start();
        }

        return true;
    }

    private boolean initializeServerSocket() {
        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            Log.d(TAG, "initializeServerSocket - " + e.getMessage());
            return false;
        }

        port = serverSocket.getLocalPort();
        return true;
    }

    public boolean findServer() {
        nsdHelper.discoverServices();

        long startTime = System.nanoTime();
        while (!nsdHelper.discoveryStarted) {
            if (System.nanoTime() - startTime > 5e9 || nsdHelper.startingDiscoveryFailed) {
                Log.d(TAG, "findServer - cannot start discovery");
                nsdHelper.startingDiscoveryFailed = false;
                return false;
            }
        }

        return true;
    }

    public void getHostInfo() {
        sendToAddress = nsdHelper.host;
        port = nsdHelper.port;
    }

    public void connectToServer() {
        if (connectionClientThread == null) {
            connectionClientThread = new Thread(new ConnectionClientThread(nsdHelper.host));
            connectionClientThread.start();
        }
    }

    public void stopDiscovery() {
        nsdHelper.stopDiscovery();
    }

    public void unregisterService() {
        nsdHelper.unregisterService();
    }

    public void sendMessage(String message) {
        if (handlerFromMainThread != null) {
            Message msg = handlerFromMainThread.obtainMessage(1, message);
            msg.sendToTarget();
        }
    }

    public void dispose() {
        if (nsdHelper != null) {
            nsdHelper.tearDown();
        }

        try {
            if (serverSocket != null) {
                serverSocket.close();
            }

            if (clientSocketConnectedToMyServerSocket != null) {
                clientSocketConnectedToMyServerSocket.shutdownInput();
                clientSocketConnectedToMyServerSocket.close();
            }

            if (serverSocketMyClientSocketIsConnectedTo != null) {
                serverSocketMyClientSocketIsConnectedTo.shutdownInput();
                serverSocketMyClientSocketIsConnectedTo.close();
            }

            if (clientSocketConnectedToMyMessagingServerSocket != null) {
                clientSocketConnectedToMyMessagingServerSocket.shutdownInput();
                clientSocketConnectedToMyMessagingServerSocket.close();
            }
        } catch (IOException e) {
            //Log.d(TAG, "Cannot close socket");
        }

        if (printWriter != null)
            printWriter.close();
    }

    public class ConnectionServerThread implements Runnable {
        ConnectionServerThread() {
            handlerFromMainThread = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message message) {
                    if (message.what == 1) {
                        if (sendToAddress != null) {
                            MessagingClientTask messagingClientTask = new MessagingClientTask();
                            messagingClientTask.execute(sendToAddress.getHostAddress(), (String) (message.obj));
                        }
                    }
                }
            };
        }

        @Override
        public void run() {
            try {
                while (true) {
                    clientSocketConnectedToMyServerSocket = serverSocket.accept();

                    printWriter = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(clientSocketConnectedToMyServerSocket.getOutputStream())),
                            true);

                    sendToAddress = clientSocketConnectedToMyServerSocket.getInetAddress();

                    connected = true;

                    if (handlerToMainThread != null) {
                        Message msg = handlerToMainThread.obtainMessage(3);
                        msg.sendToTarget();

                        try {
                            Thread messagingServerThread = new Thread(new MessagingServerThread(clientSocketConnectedToMyServerSocket));
                            messagingServerThread.start();

                            while (connected) {

                            }

                            Log.d(TAG, "ConnectionServerThread - run - 1 - connected is false");
                            msg = handlerToMainThread.obtainMessage(4);
                            msg.sendToTarget();

                        } catch (Exception e) {
                            Log.d(TAG, "ConnectionServerThread - run - 2 - " + e.getMessage());
                            msg = handlerToMainThread.obtainMessage(4);
                            msg.sendToTarget();
                        }
                    }
                }
            } catch (IOException e) {
                Log.d(TAG, "ConnectionServerThread - run - 3 - " + e.getMessage());
                Message msg = handlerToMainThread.obtainMessage(4);
                msg.sendToTarget();
            }
        }
    }

    public class ConnectionClientThread extends Thread {
        InetAddress hostAdd;

        ConnectionClientThread(InetAddress hostAddress) {
            hostAdd = hostAddress;

            handlerFromMainThread = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message message) {
                    if (message.what == 1) {
                        if (sendToAddress != null) {
                            MessagingClientTask messagingClientTask = new MessagingClientTask();
                            messagingClientTask.execute(sendToAddress.getHostAddress(), (String) (message.obj));
                        }
                    }
                }
            };
        }

        @Override
        public void run() {
            if (handlerToMainThread != null) {
                try {
                    serverSocketMyClientSocketIsConnectedTo = new Socket(hostAdd, port);
                    try {
                        printWriter = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(serverSocketMyClientSocketIsConnectedTo.getOutputStream())),
                                true);

                        connected = true;

                        Message msg = handlerToMainThread.obtainMessage(2);
                        msg.sendToTarget();

                        try {
                            Thread messagingServerThread = new Thread(new MessagingServerThread(serverSocketMyClientSocketIsConnectedTo));
                            messagingServerThread.start();

                            while (connected) {

                            }

                            //---disconnected from the server---
                            Log.d(TAG, "ConnectionClientThread - run - 1 - connected is false");
                            msg = handlerToMainThread.obtainMessage(5);
                            msg.sendToTarget();
                        } catch (Exception e) {
                            Log.d(TAG, "ConnectionClientThread - run - 2 - " + e.getMessage());
                            msg = handlerToMainThread.obtainMessage(5);
                            msg.sendToTarget();
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "ConnectionClientThread - run - 3 - " + e.getMessage());
                        Message msg = handlerToMainThread.obtainMessage(5);
                        msg.sendToTarget();
                    }
                    //Message msg = handlerToMainThread.obtainMessage(5);
                    //msg.sendToTarget();
                } catch (Exception e) {
                    Log.d(TAG, "ConnectionClientThread - run - 4 - " + e.getMessage());
                    Message msg = handlerToMainThread.obtainMessage(5);
                    msg.sendToTarget();
                }
            }
        }
    }

    public class MessagingServerThread implements Runnable {
        MessagingServerThread(Socket client) {
            clientSocketConnectedToMyMessagingServerSocket = client;
        }

        public void run() {
            if (handlerToMainThread != null) {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(clientSocketConnectedToMyMessagingServerSocket.getInputStream()));

                    String line;
                    while ((line = br.readLine()) != null) {
                        Message msg = handlerToMainThread.obtainMessage(1, line);
                        msg.sendToTarget();
                    }

                    //connected = false;
                    br.close();
                } catch (Exception e) {
                    Log.d(TAG, "MessagingServerThread - run - " + e.getMessage());
                    Message msg = handlerToMainThread.obtainMessage(6);
                    msg.sendToTarget();
                }
            }
        }
    }

    public static class MessagingClientTask extends AsyncTask<String, Void, String> {
        //Socket s;
        //DataOutputStream dos;
        String ip, message;

        @Override
        protected String doInBackground(String... strings) {
            ip = strings[0];
            message = strings[1];

            printWriter.println(message);

            return null;
        }
    }

    public class NsdHelper {
        private NsdManager.RegistrationListener registrationListener;
        private NsdManager.DiscoveryListener discoveryListener;

        private final NsdManager nsdManager;

        String serviceName;
        final String SERVICE_TYPE = "_nsd_" + serviceTypeName + "._tcp.";
        int port;

        //final String TAG = "NsdHelper";

        InetAddress host;

        boolean registeringService;
        public boolean serviceRegistered;
        boolean serviceRegistrationFailed;
        boolean unregisteringService;

        boolean startingDiscovery;
        boolean discoveryStarted;
        boolean startingDiscoveryFailed;
        boolean stoppingDiscovery;

        public boolean serviceResolved;

        NsdHelper(Context context) {
            nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        }

        void registerService(int port) {
            if (serviceRegistered || registeringService || unregisteringService) {
                if (serviceRegistered)
                    Log.d(TAG, "registerService - serviceRegistered");
                if (registeringService)
                    Log.d(TAG, "registerService - registeringService");
                if (unregisteringService)
                    Log.d(TAG, "registerService - unregisteringService");
                return;
            }

            // Create the NsdServiceInfo object, and populate it.
            NsdServiceInfo serviceInfo = new NsdServiceInfo();

            // The name is subject to change based on conflicts
            // with other services advertised on the same network.
            serviceInfo.setServiceName(myServiceName);
            serviceInfo.setServiceType(SERVICE_TYPE);
            serviceInfo.setPort(port);

            registeringService = true;
            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
        }

        void unregisterService() {
            if (!serviceRegistered || registeringService || unregisteringService) {
                if (!serviceRegistered)
                    Log.d(TAG, "unregisterService - not serviceRegistered");
                if (registeringService)
                    Log.d(TAG, "unregisterService - registeringService");
                if (unregisteringService)
                    Log.d(TAG, "unregisterService - unregisteringService");
                return;
            }

            unregisteringService = true;
            nsdManager.unregisterService(registrationListener);
        }

        void discoverServices() {
            if (discoveryStarted || startingDiscovery || stoppingDiscovery) {
                if (discoveryStarted)
                    Log.d(TAG, "discoverServices - discoveryStarted");
                if (startingDiscovery)
                    Log.d(TAG, "discoverServices - startingDiscovery");
                if (stoppingDiscovery)
                    Log.d(TAG, "discoverServices - stoppingDiscovery");
                return;
            }

            startingDiscovery = true;
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        }

        void stopDiscovery() {
            if (!discoveryStarted || startingDiscovery || stoppingDiscovery) {
                if (!discoveryStarted)
                    Log.d(TAG, "stopDiscovery - not discoveryStarted");
                if (startingDiscovery)
                    Log.d(TAG, "stopDiscovery - startingDiscovery");
                if (stoppingDiscovery)
                    Log.d(TAG, "stopDiscovery - stoppingDiscovery");
                return;
            }

            stoppingDiscovery = true;
            nsdManager.stopServiceDiscovery(discoveryListener);
        }

        void initializeRegistrationListener() {
            registrationListener = new NsdManager.RegistrationListener() {
                @Override
                public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                    // Save the service name. Android may have changed it in order to
                    // resolve a conflict, so update the name you initially requested
                    // with the name Android actually used.
                    serviceName = serviceInfo.getServiceName();
                    serviceRegistered = true;
                    registeringService = false;

                    Log.d(TAG, "initializeRegistrationListener - onServiceRegistered");
                }

                @Override
                public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                    // Registration failed! Put debugging code here to determine why.
                    registeringService = false;
                    serviceRegistrationFailed = true;

                    Log.d(TAG, "initializeRegistrationListener - onRegistrationFailed");
                }

                @Override
                public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                    // Service has been unregistered. This only happens when you call
                    // NsdManager.unregisterService() and pass in this listener.
                    serviceName = "";
                    serviceRegistered = false;
                    unregisteringService = false;

                    Log.d(TAG, "initializeRegistrationListener - onServiceUnregistered");
                }

                @Override
                public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                    // Unregistration failed. Put debugging code here to determine why.
                    unregisteringService = false;

                    Log.d(TAG, "initializeRegistrationListener - onUnregistrationFailed");
                }
            };
        }

        void initializeDiscoveryListener() {
            // Instantiate a new DiscoveryListener
            discoveryListener = new NsdManager.DiscoveryListener() {
                // Called as soon as service discovery begins.
                @Override
                public void onDiscoveryStarted(String serviceType) {
                    discoveryStarted = true;
                    startingDiscovery = false;

                    Log.d(TAG, "initializeDiscoveryListener - onDiscoveryStarted");
                }

                @Override
                public void onServiceFound(NsdServiceInfo serviceInfo) {
                    // A service was found! Do something with it.
                    Log.d(TAG, "initializeDiscoveryListener - onServiceFound - " + serviceInfo);
                    if (!serviceInfo.getServiceType().equals(SERVICE_TYPE)) {
                        // Service type is the string containing the protocol and
                        // transport layer for this service.
                        Log.d(TAG, "initializeDiscoveryListener - onServiceFound - Unknown Service Type: " + serviceInfo.getServiceType());
                    } else if (serviceInfo.getServiceName().equals(serviceName)) {
                        // The name of the service tells the user what they'd be
                        // connecting to. It could be "Bob's Chat App".
                        Log.d(TAG, "initializeDiscoveryListener - onServiceFound - Same machine: " + serviceName);
                    } else if (serviceInfo.getServiceName().contains(myServiceName)) {
                        stopDiscovery();
                        nsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
                            @Override
                            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                                // Called when the resolve fails. use the error code to debug.
                                Log.e(TAG, "initializeDiscoveryListener - onServiceFound - onResolveFailed: " + errorCode);
                            }

                            @Override
                            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                                Log.e(TAG, "initializeDiscoveryListener - onServiceFound - onServiceResolved: " + serviceInfo);

                                if (serviceInfo.getServiceName().equals(serviceName)) {
                                    Log.d(TAG, "initializeDiscoveryListener - onServiceFound - onServiceResolved: " + serviceInfo + " - Same IP");
                                    return;
                                }

                                port = serviceInfo.getPort();
                                host = serviceInfo.getHost();
                                serviceResolved = true;
                            }
                        });
                    }
                }

                @Override
                public void onServiceLost(NsdServiceInfo serviceInfo) {
                    // When the network service is no longer available.
                    // Internal bookkeeping code goes here.
                    Log.d(TAG, "initializeDiscoveryListener - onServiceLost - " + serviceInfo);
                }

                @Override
                public void onDiscoveryStopped(String serviceType) {
                    discoveryStarted = false;
                    stoppingDiscovery = false;

                    Log.d(TAG, "initializeDiscoveryListener - onDiscoveryStopped - " + serviceType);
                }

                @Override
                public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                    startingDiscovery = false;
                    startingDiscoveryFailed = true;

                    Log.d(TAG, "initializeDiscoveryListener - onStartDiscoveryFailed - " + errorCode);
                    stopDiscovery();
                }

                @Override
                public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                    stoppingDiscovery = false;

                    Log.d(TAG, "initializeDiscoveryListener - onStopDiscoveryFailed - " + errorCode);
                    stopDiscovery();
                }
            };
        }

        public void tearDown() {
            //nsdManager.unregisterService(registrationListener);
            //nsdManager.stopServiceDiscovery(discoveryListener);
            unregisterService();
            stopDiscovery();
        }
    }
}
