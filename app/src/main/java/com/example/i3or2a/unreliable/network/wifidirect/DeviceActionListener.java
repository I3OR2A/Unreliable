package com.example.i3or2a.unreliable.network.wifidirect;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Created by I3OR2A on 2016/6/30.
 */
public interface DeviceActionListener {
    void showDetails(WifiP2pDevice wifiP2pDevice);

    void cancelDisconnect();

    void connnect(WifiP2pConfig wifiP2pConfig);

    void disconnect();
}
