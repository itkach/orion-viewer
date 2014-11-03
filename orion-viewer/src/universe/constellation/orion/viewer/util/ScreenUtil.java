package universe.constellation.orion.viewer.util;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import universe.constellation.orion.viewer.Common;
import universe.constellation.orion.viewer.geom.Dimension;

public class ScreenUtil {

    public static double calcScreenSize(int originalSize, DisplayMetrics metrics) {
        Common.d("Device dpi: " + metrics.density);
        return (originalSize * metrics.density + 0.5);
    }

    public static double calcScreenSize(int originalSize, Context activity) {
        return calcScreenSize(originalSize, activity.getResources().getDisplayMetrics());
    }

    public static Dimension getScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point p = new Point();
            display.getSize(p);
            return new Dimension(p.x, p.y);
        } else {
            return new Dimension(display.getWidth(), display.getHeight());
        }
    }

}
