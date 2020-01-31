package com.gov.ducadegliabruzzitreviso.ducaapp.classes;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Class containing a couple utility methods.
 *
 * @author Riccardo De Zen
 */
public class MyUtils {
    public static boolean checkNetwork(Context c) {
        ConnectivityManager connMgr =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        // verifico se esiste una connessione alla Rete attiva
        boolean wifiConnected, mobileConnected;
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }
        return wifiConnected || mobileConnected;
    }

    public static int pixelForAll(int p, Context c) {
        int base_size = p * 3;
        return base_size * c.getResources().getDisplayMetrics().widthPixels / 1080;
    }
}
