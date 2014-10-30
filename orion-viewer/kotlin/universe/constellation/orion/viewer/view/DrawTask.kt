package universe.constellation.orion.viewer.view

import android.graphics.Canvas
import android.graphics.Paint

public trait  DrawTask {

    public fun drawOnCanvas(canvas: Canvas, drawContext: DrawContext)

}