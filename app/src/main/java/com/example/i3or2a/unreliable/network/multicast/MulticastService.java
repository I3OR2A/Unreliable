package com.example.i3or2a.unreliable.network.multicast;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by I3OR2A on 2016/6/23.
 */
public class MulticastService {

    private static final String TAG = MulticastService.class.getName();

    private static final int MAXIMUM_PACKETSIZE = 512;

    private static final int MAXIMUM_TIMEOUT = 10000;

    private WifiManager wifiManager;

    private WifiManager.MulticastLock multicastLock;

    private WifiManager.WifiLock wifiLock;

    private PowerManager.WakeLock wakeLock;

    private MulticastSocket multicastSocket;

    private InetAddress inetAddress;

    private NetworkInterface networkInterface;

    private ConnectionHandler connectionHandler;

    private Context context;

    private String ip = "FF7E:230::1234";

    private int port = 12345;

    private OnReceivedListener onReceivedListener;

    private OnErrorListener onErrorListener;

    private OnPreparedListener onPreparedListener;

    private EventHandler mEventHandler;

    private HandlerThread handlerThread;

    private Handler mActionHandler;

    private MediaPlayer mediaPlayer;

    public MulticastService(Context context) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }

        handlerThread = new HandlerThread("handlerThread");
        handlerThread.start();
        if ((looper = handlerThread.getLooper()) != null) {
            mActionHandler = new Handler(looper);
        } else {
            mActionHandler = null;
        }
    }

    public void setOnErrorListener(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    public void setOnReceivedListener(OnReceivedListener onReceivedListener) {
        this.onReceivedListener = onReceivedListener;
    }

    public void setOnPreparedListener(OnPreparedListener onPreparedListener) {
        this.onPreparedListener = onPreparedListener;
    }

    public void start() {
        wifiLock = wifiManager.createWifiLock("wifiLock");
        multicastLock = wifiManager.createMulticastLock("multicastLock");

        stayMulticastAwake(true);
        stayWifiAwake(true);
        stayPowerAwake(true);

        if (connectionHandler == null) {
            connectionHandler = new ConnectionHandler();
            connectionHandler.start();
        }
    }

    public void stop() {
        if (connectionHandler != null) {
            connectionHandler.interrupt();
            connectionHandler = null;
        }
    }

    public void release() {
        stop();
        stayWifiAwake(false);
        stayPowerAwake(false);
        stayMulticastAwake(false);
    }

    public void send() {
        mActionHandler.post(new Runnable() {
            public void run() {
                String string = "hello world";
                byte[] bytes = new byte[MAXIMUM_PACKETSIZE];
                byte[] data = string.getBytes();
                System.arraycopy(data, 0, bytes, 0, data.length);
                DatagramPacket datagramPacket = new DatagramPacket(bytes, MAXIMUM_PACKETSIZE, inetAddress, port);
                try {
                    multicastSocket.send(datagramPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setWakeMode(Context context, int mode) {
        boolean washeld = false;
        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                washeld = true;
                wakeLock.release();
            }
            wakeLock = null;
        }

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(mode | PowerManager.ON_AFTER_RELEASE, MediaPlayer.class.getName());
        wakeLock.setReferenceCounted(false);
        if (washeld) {
            wakeLock.acquire();
        }
    }

    private void stayMulticastAwake(boolean awake) {
        if (multicastLock != null) {
            if (awake && !multicastLock.isHeld()) {
                multicastLock.setReferenceCounted(true);
                multicastLock.acquire();
            } else if (!awake && multicastLock.isHeld()) {
                multicastLock.release();
            }
        }
    }

    private void stayWifiAwake(boolean awake) {
        if (wifiLock != null) {
            if (awake && !wifiLock.isHeld()) {
                wifiLock.acquire();
            } else if (!awake && wifiLock.isHeld()) {
                wifiLock.release();
            }
        }
    }

    private void stayPowerAwake(boolean awake) {
        if (wakeLock != null) {
            if (awake && !wakeLock.isHeld()) {
                wakeLock.acquire();
            } else if (!awake && wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }

    class ConnectionHandler extends Thread {
        private final String TAG = ConnectionHandler.class.getName();

        public ConnectionHandler() {

        }

        public void run() {
            try {
                inetAddress = InetAddress.getByName(ip);
//                networkInterface = getNetworkInterface();
                multicastSocket = new MulticastSocket(null);
                multicastSocket.setReuseAddress(true);
                multicastSocket.bind(new InetSocketAddress(port));
//                multicastSocket.setSoTimeout(MAXIMUM_TIMEOUT);
                multicastSocket.setBroadcast(true);
                multicastSocket.joinGroup(inetAddress);
//                multicastSocket.setNetworkInterface(networkInterface);

                while (!Thread.currentThread().isInterrupted()) {
                    byte[] bytes = new byte[MAXIMUM_PACKETSIZE];
                    DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length);
                    datagramPacket.setLength(MAXIMUM_PACKETSIZE);
                    multicastSocket.receive(datagramPacket);
                    mEventHandler.obtainMessage(SERVICE_RECEIVED, datagramPacket).sendToTarget();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public ArrayList<String> getIPv4Address() {
            ArrayList<String> arrayList = new ArrayList<>();
            try {
                List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface networkInterface : networkInterfaces) {
                    List<InetAddress> inetAddresses = Collections.list(networkInterface.getInetAddresses());
                    for (InetAddress inetAddress : inetAddresses) {
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                            arrayList.add(inetAddress.getHostAddress().toUpperCase());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return arrayList;
        }

        public NetworkInterface getNetworkInterface() {
            ArrayList<String> ipList = getIPv4Address();
            try {
                for (String ip : ipList) {
                    if (ip.startsWith("192.168")) {
                        return NetworkInterface.getByInetAddress(Inet4Address.getByName(ip));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public void interrupt() {
            super.interrupt();

            if (multicastSocket != null)
                try {
                    multicastSocket.leaveGroup(inetAddress);
                    multicastSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private static final int SERVICE_NOP = 0;
    private static final int SERVICE_PREPARED = 3;
    private static final int SERVICE_RECEIVED = 1;
    private static final int SERVICE_ERROR = 2;

    class EventHandler extends Handler {
        private final String TAG = EventHandler.class.getName();

        private MulticastService multicastService;

        public EventHandler(MulticastService multicastService, Looper looper) {
            super(looper);
            this.multicastService = multicastService;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SERVICE_NOP:
                    break;
                case SERVICE_PREPARED:
                    if (onPreparedListener != null) {
                        onPreparedListener.onPrepared(multicastService);
                    }
                    break;
                case SERVICE_RECEIVED:
                    if (onReceivedListener != null && msg.obj instanceof DatagramPacket) {
                        onReceivedListener.onReceived(multicastService, (DatagramPacket) msg.obj);
                    }
                    break;
                case SERVICE_ERROR:
                    if (onErrorListener != null) {
                        onErrorListener.onError(multicastService, msg.arg1, msg.arg2);
                    }
                    break;
                default:
                    Log.e(TAG, "Unknown message type " + msg.what);
            }
        }
    }
}
