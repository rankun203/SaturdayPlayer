package com.saturdayplayer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.net.ServerSocket;

public class MainActivity extends Activity {

    private ServerSocket mServerSocket;
    public NsdManager.RegistrationListener mRegistrationListener;
    public static final String SERVICE_TYPE = "_http._tcp.";
    public String mServiceName = "SaturdayPlayerGroup";
    NsdServiceInfo serviceInfo;
    public int mLocalPort = -1;
    NsdManager mNsdManager;
    private static final String TAG = "SaturdayPlayer";
    public NsdManager.DiscoveryListener mDiscoveryListener;
    public NsdManager.ResolveListener mResolveListener;
    public NsdServiceInfo mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeServerSocket();
        initializeNsd();
        registerService(mLocalPort);
logMsg("" + mLocalPort + ":" + mServiceName);
    }

    private void initializeNsd() {
        initializeRegistrationListener();
    }

    public void initializeServerSocket() {
logMsg("initializeServerSocket");
        try {
            mServerSocket = new ServerSocket(0);
            mLocalPort = mServerSocket.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void initializeRegistrationListener() {
logMsg("initializeRegistrationListener");
        mRegistrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
logMsg("registrationFailed");
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                logMsg("unregistrationFailed");
            }

            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                mServiceName = serviceInfo.getServiceName();
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                logMsg("serviceUnregistered");
            }
        };
    }
    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                logMsg("Resolve failed.");
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                logMsg("Resolve success.");
                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    logMsg("Same machine.");
                    return;
                }
                mService = serviceInfo;
            }
        };
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void registerService(int port) {
logMsg("registerService");
        serviceInfo = new NsdServiceInfo();

        serviceInfo.setServiceName(mServiceName);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(port);

        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener
        );

    }

    public void clickDiscovery(View v) {
        initializeResolveListener();
        initializeDiscoveryListener();
        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }
    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                logMsg("Discovery failed: Error Code: " + errorCode);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                logMsg("discovery failed: Error Code: " + errorCode);
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                logMsg("Service discovery started.");
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                logMsg("service stopped.");
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                logMsg("Service discovery found: " + serviceInfo);
                if (!serviceInfo.getServiceType().equals(SERVICE_TYPE)) {
                    logMsg("Unknown Service Type: " + serviceInfo.getServiceType());
                } else if (serviceInfo.getServiceName().equals(mServiceName)) {
                    logMsg("Same machine: " + mServiceName);
                } else if (serviceInfo.getServiceName().contains(mServiceName)) {
                    mNsdManager.resolveService(serviceInfo, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                logMsg("service lost.");
                if (serviceInfo == mService) {
                    mService = null;
                }
            }
        };
    }

    public void logMsg(String str) {
        Log.d(TAG, str);
        Toast.makeText(this, str, Toast.LENGTH_SHORT);
    }

    @Override
    protected void onDestroy() {
        mNsdManager.unregisterService(mRegistrationListener);
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        super.onDestroy();
    }

}
