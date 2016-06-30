package com.example.i3or2a.unreliable.network.multicast;

import java.net.DatagramPacket;

/**
 * Created by I3OR2A on 2016/6/23.
 */
public interface OnReceivedListener {
    void onReceived(MulticastService multicastService, DatagramPacket datagramPacket);
}
