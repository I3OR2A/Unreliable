package com.example.i3or2a.unreliable.network.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.DatePicker;

/**
 * Created by I3OR2A on 2016/6/30.
 */
public class WifiDirectService {

    private static final String TAG = WifiDirectService.class.getName();

    private WifiP2pManager mWifiP2pManager;

    private WifiP2pManager.Channel mChannel;

    private Context mContext;

    private IntentFilter mIntentFilter;

    private BroadcastReceiver mWifiDirectBroadcastReceiver;

    private EventHandler mEventHandler;

    private boolean isWifiP2pEnabled = false;

    public void setWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    public WifiDirectService(Context context) {
        mContext = context;
        mWifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(mContext, Looper.getMainLooper(), null);
        mWifiDirectBroadcastReceiver = new WifiDirectBroadcastReceiver(this, mWifiP2pManager, mChannel);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction((WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION));
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }
    }

    public void registerReceiver() {
        mContext.registerReceiver(mWifiDirectBroadcastReceiver, mIntentFilter);
    }

    public void unregisterReceiver() {
        mContext.unregisterReceiver(mWifiDirectBroadcastReceiver);
    }

    private WifiP2pManager.ChannelListener mChannelListener = new WifiP2pManager.ChannelListener() {
        public void onChannelDisconnected() {

        }
    };

    public WifiP2pManager.ChannelListener getChannelListener() {
        return this.mChannelListener;
    }

    private WifiP2pManager.PeerListListener mPeerListListener = new WifiP2pManager.PeerListListener() {
        public void onPeersAvailable(WifiP2pDeviceList peers) {

        }
    };

    public WifiP2pManager.PeerListListener getPeerListListener() {
        return this.mPeerListListener;
    }

    private WifiP2pManager.ConnectionInfoListener mConnectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        public void onConnectionInfoAvailable(WifiP2pInfo info) {

        }
    };

    public WifiP2pManager.ConnectionInfoListener getConnectionInfoListener() {
        return mConnectionInfoListener;
    }

    interface OnDataChangedListener {
        void onReset();

        void onUpdate(WifiP2pDevice wifiP2pDevice);
    }

    private OnDataChangedListener mOnDataChangedListener = new OnDataChangedListener() {
        public void onReset() {

        }

        public void onUpdate(WifiP2pDevice wifiP2pDevice) {

        }

    };

    public OnDataChangedListener getOnDataChangedListener() {
        return this.mOnDataChangedListener;
    }

    public static class EventHandler extends Handler {
        private final String TAG = EventHandler.class.getName();

        private WifiDirectService mWifiDireectService;

        public EventHandler(WifiDirectService wifiDirectService, Looper looper) {
            super(looper);
            this.mWifiDireectService = wifiDirectService;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                default:
                    Log.e(TAG, "Unknown message type " + msg.what);
            }
        }
    }
}
