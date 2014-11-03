package universe.constellation.orion.viewer.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;

import org.jetbrains.annotations.NotNull;

import universe.constellation.orion.viewer.L;
import universe.constellation.orion.viewer.geom.Dimension;
import universe.constellation.orion.viewer.util.ScreenUtil;

/**
 * Created by mike on 9/14/14.
 */
public class ColorStuff implements DrawContext {

    public final Paint backgroundPaint = new Paint();

    public final Paint defaultPaint = new Paint();

    public final Paint borderPaint = new Paint();

    public final Dimension screenSize;

    public ColorStuff(Context context) {
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStrokeWidth(2);
        borderPaint.setStyle(Paint.Style.STROKE);

        int fontSize = OrionDrawScene.DEFAULT_STATUS_BAR_SIZE - OrionDrawScene.FONT_DELTA;
        defaultPaint.setColor(Color.BLACK);
        defaultPaint.setTextSize(fontSize);

        Typeface tf = Typeface.DEFAULT;
        if (tf != null) {
            defaultPaint.setTypeface(tf);
            defaultPaint.setAntiAlias(true);
        }

        int dim = 64;
        Bitmap bitmap = Bitmap.createBitmap(dim, dim, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint p = new Paint();
        p.setColor(Color.rgb(223, 223, 223));
        int gradsize = 1 << (int)(Math.log(ScreenUtil.calcScreenSize(2, context))/Math.log(2) + 0.1);
        if (gradsize < 2) {
            gradsize = 2;
        }
        L.log("Grad size is " + gradsize);
        p.setShader(new LinearGradient(0, 0, 0, gradsize, Color.rgb(223, 223, 223), Color.rgb(240, 240, 240), Shader.TileMode.MIRROR));
        canvas.drawRect(0, 0, dim, dim, p);

        backgroundPaint.setShader(new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));

        screenSize = ScreenUtil.getScreenSize(context);
    }

    @NotNull
    @Override
    public Paint getDefaultPaint() {
        return defaultPaint;
    }

    @NotNull
    @Override
    public Dimension getScreenSize() {
        return screenSize;
    }
}
