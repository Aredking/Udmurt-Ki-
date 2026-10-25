package com.udmurt.learn.android;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.udmurt.learn.Platform;
import com.udmurt.learn.UdmurtGame;

public class AndroidLauncher extends AndroidApplication implements Platform {

    private static final String TAILSCALE_PACKAGE = "com.tailscale.ipn";
    private static final String PLAY_URL =
            "https://play.google.com/store/apps/details?id=com.tailscale.ipn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useAccelerometer = false;
        configuration.useCompass = false;
        configuration.useImmersiveMode = true;
        initialize(new UdmurtGame(this), configuration);
    }

    @Override
    public boolean isAndroid() {
        return true;
    }

    @Override
    public boolean hasTailscale() {
        try {
            getPackageManager().getPackageInfo(TAILSCALE_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void installTailscale() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + TAILSCALE_PACKAGE)));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                            "https://play.google.com/store/apps/details?id=" + TAILSCALE_PACKAGE)));
                }
            }
        });
    }

    @Override
    public void openTailscale() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent launch = getPackageManager().getLaunchIntentForPackage(TAILSCALE_PACKAGE);
                if (launch != null) {
                    startActivity(launch);
                } else {
                    installTailscale();
                }
            }
        });
    }
}
