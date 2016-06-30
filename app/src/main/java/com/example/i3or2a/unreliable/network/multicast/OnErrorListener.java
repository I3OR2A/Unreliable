package com.example.i3or2a.unreliable.network.multicast;

/**
 * Created by I3OR2A on 2016/6/23.
 */
public interface OnErrorListener {
    void onError(MulticastService multicastService, int what, int extra);
}
