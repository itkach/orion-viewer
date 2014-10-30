package universe.constellation.orion.viewer.view

import universe.constellation.orion.viewer.PageInfo
import universe.constellation.orion.viewer.LayoutPosition
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import universe.constellation.orion.viewer.geom.Point

public class PageView(val pageInfo: PageInfo, val position: Point) : DrawTask {

    public var layoutInfo: LayoutPosition? = null

    override fun drawOnCanvas(canvas: Canvas,
                              drawContext: DrawContext) {
        canvas.save()
        try {
            canvas.translate(position)
            if (layoutInfo != null) {
                val paint = drawContext.defaultPaint
                paint.setColor(Color.BLACK)
                canvas.drawRect(0F, 0F, layoutInfo!!.x.pageDimension.toFloat(), layoutInfo!!.y.pageDimension.toFloat(), paint)
            }
        } finally {
            canvas.restore()
        }
    }

    public fun transform(shiftX: Int, shiftY: Int) {
        position.shift(shiftX, shiftY)
    }
}

inline fun Canvas.translate(p: Point) {
    this.translate(p.x.toFloat(), p.y.toFloat())
}