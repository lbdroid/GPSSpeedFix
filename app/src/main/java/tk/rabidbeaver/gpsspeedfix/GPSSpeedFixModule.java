package tk.rabidbeaver.gpsspeedfix;

import android.location.Location;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class GPSSpeedFixModule implements IXposedHookLoadPackage {
    private static final String TAG = "GPSSpeedFix";
    private static final String SERVICE = "com.android.server.location.GpsLocationProvider";
    private Location[] mLocation = new Location[5];
    private float lastAcceptableBearing = 0;
    private float distance = 0;
    private long time = 0;
    private float speed = 0;
    private int minSamples = 0;

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!"android".equals(lpparam.packageName)) return;

        try {
            XposedBridge.hookAllMethods(XposedHelpers.findClass(SERVICE, lpparam.classLoader), "reportLocation", reportLocationHook);
        } catch (Throwable t) {
            Log.d(TAG, t.getMessage());
        }
    }

    private final XC_MethodHook reportLocationHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
            Location l = new Location("SpeedFixer");
            l.setLatitude((double)param.args[1]);
            l.setLongitude((double)param.args[2]);
            l.setAltitude((double)param.args[3]);
            l.setTime((long)param.args[7]);

            if (mLocation[0] != null){
                distance = mLocation[0].distanceTo(l); // distance in meters
                time = l.getTime() - mLocation[0].getTime(); // time is in milliseconds
                time /= 1000; // now the time is in seconds
                param.args[4] = distance / time; // speed in meters per second.
            } // else we just leave the speed from HAL alone.

            if (minSamples < 5){
                mLocation[minSamples] = l;
                minSamples++;
                lastAcceptableBearing = (float)param.args[5];
            } else {
                for (int i=0; i<4; i++) mLocation[i] = mLocation[i+1];
                mLocation[4] = l;
            }

            if (speed < 1) param.args[5] = lastAcceptableBearing;
            else lastAcceptableBearing = (float)param.args[5];
        }
    };
}
