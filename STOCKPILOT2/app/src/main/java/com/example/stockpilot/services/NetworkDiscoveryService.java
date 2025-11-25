package com.example.stockpilot.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

 

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetworkDiscoveryService extends IntentService {

    public static final String ACTION_DISCOVERY_RESULT = "com.example.stockpilot.DISCOVERY_RESULT";
    public static final String EXTRA_IP_ADDRESS = "ip_address";
    public static final String EXTRA_ERROR_MESSAGE = "error_message";

    private static final String TAG = "NetworkDiscoveryService";
    private static final String DISCOVERY_MESSAGE = "STOCKPILOT_DISCOVERY_REQUEST";
    private static final int DISCOVERY_PORT = 9876;

    public NetworkDiscoveryService() {
        super("NetworkDiscoveryService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);

            byte[] sendData = DISCOVERY_MESSAGE.getBytes();
            byte[] recvBuf = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);

            // Send discovery to global and interface-specific broadcast addresses
            sendGlobalBroadcast(socket, sendData);
            sendInterfaceBroadcasts(socket, sendData);

            // Tight receive loop under ~500ms total
            long deadlineNs = System.nanoTime() + 500_000_000L; // 500ms
            while (System.nanoTime() < deadlineNs) {
                try {
                    socket.setSoTimeout(50);
                    socket.receive(receivePacket);
                    String serverIp = receivePacket.getAddress().getHostAddress();
                    broadcastSuccess(serverIp);
                    socket.close();
                    return;
                } catch (IOException ignored) {
                    // Keep trying until deadline
                }
            }

            socket.close();
            broadcastError("Discovery timed out");
        } catch (Exception e) {
            e.printStackTrace();
            broadcastError(e.getMessage());
        }
    }

    private void sendGlobalBroadcast(DatagramSocket socket, byte[] payload) {
        try {
            DatagramPacket packet = new DatagramPacket(payload, payload.length,
                    InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT);
            socket.send(packet);
        } catch (Exception ignored) { }
    }

    private void sendInterfaceBroadcasts(DatagramSocket socket, byte[] payload) {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface nif = interfaces.nextElement();
                if (!nif.isUp() || nif.isLoopback()) continue;
                for (java.net.InterfaceAddress ia : nif.getInterfaceAddresses()) {
                    InetAddress broadcast = ia.getBroadcast();
                    if (broadcast == null) continue;
                    try {
                        DatagramPacket packet = new DatagramPacket(payload, payload.length, broadcast, DISCOVERY_PORT);
                        socket.send(packet);
                    } catch (Exception ignored) { }
                }
            }
        } catch (SocketException ignored) { }
    }

    private void broadcastSuccess(String ipAddress) {
        Intent intent = new Intent(ACTION_DISCOVERY_RESULT);
        intent.putExtra(EXTRA_IP_ADDRESS, ipAddress);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastError(String errorMessage) {
        Intent intent = new Intent(ACTION_DISCOVERY_RESULT);
        intent.putExtra(EXTRA_ERROR_MESSAGE, errorMessage);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}