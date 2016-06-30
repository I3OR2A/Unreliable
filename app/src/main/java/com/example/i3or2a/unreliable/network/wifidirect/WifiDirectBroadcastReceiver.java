package com.example.i3or2a.unreliable.network.wifidirect;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.net.NetworkInterface;

/**
 * Created by I3OR2A on 2016/6/30.
 */
public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = WifiDirectBroadcastReceiver.class.getName();

    private WifiDirectService wifiDirectService;

    private WifiP2pManager wifiP2pManager;

    private WifiP2pManager.Channel channel;

    public WifiDirectBroadcastReceiver(WifiDirectService wifiDirectService, WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel) {
        this.wifiDirectService = wifiDirectService;
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                wifiDirectService.setWifiP2pEnabled(true);
            } else {
                wifiDirectService.setWifiP2pEnabled(false);
                if (wifiDirectService.getOnDataChangedListener() != null) {
                    wifiDirectService.getOnDataChangedListener().onReset();
                }
            }
            Log.d(TAG, "P2P state changed - " + state);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (wifiP2pManager != null) {
                wifiP2pManager.requestPeers(channel, wifiDirectService.getPeerListListener());
            }
            Log.d(TAG, "P2P peers changed");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "P2P connection changed");
            if (wifiP2pManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                wifiP2pManager.requestConnectionInfo(channel, wifiDirectService.getConnectionInfoListener());
            } else {
                if (wifiDirectService.getOnDataChangedListener() != null) {
                    wifiDirectService.getOnDataChangedListener().onReset();
                }
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "P2P this device changed");
            WifiP2pDevice wifiP2pDevice = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            if(wifiDirectService.getOnDataChangedListener() != null){
                wifiDirectService.getOnDataChangedListener().onUpdate(wifiP2pDevice);
            }
        }
    }
}
