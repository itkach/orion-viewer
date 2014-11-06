package universe.constellation.orion.viewer.view

import universe.constellation.orion.viewer.PageInfo
import universe.constellation.orion.viewer.LayoutPosition
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import universe.constellation.orion.viewer.geom.Point
import universe.constellation.orion.viewer.geom.Dimension
import kotlin.properties.Delegates
import android.graphics.Rect
import universe.constellation.orion.viewer.SimpleLayoutStrategy
import android.os.AsyncTask
import android.graphics.Bitmap

enum class State {
    UNINITIALIZED
    VISIBLE
    NOT_VISIBLE
    DESTROYED
}
public class PageView(val pageNum: Int, var dim: Dimension, val position: Point, val layoutStrategy: SimpleLayoutStrategy) : DrawTask {

    var bitmap : IntBitmap? = null;

    var pageListener: PageViewListener? = null

    var state : State = State.UNINITIALIZED

    public var layoutInfo: LayoutPosition = LayoutPosition()

    public val pageArea: Rect = Rect(0, 0, 0, 0)
        get() {
            $pageArea.set(position.x, position.y, position.x + dim.width, position.y + dim.height)
            return $pageArea
        }

    private val stuffTempRect = Rect()

    override fun drawOnCanvas(canvas: Canvas,
                              drawContext: DrawContext) {
        canvas.save()
        try {
            canvas.translate(position)
            val paint = drawContext.defaultPaint
            val color = paint.getColor()
            paint.setColor(Color.BLACK)

            if (state == State.VISIBLE) {
                stuffTempRect.set(0, 0, dim.width, dim.height)

                if (bitmap != null && !bitmap!!.isDead) {
                    with(bitmap!!) {
                        canvas.drawBitmap(stuffTempRect, paint)
                    }
                }
            }

            paint.setStyle(Paint.Style.STROKE)
            canvas.drawText("Loading page " + pageNum, dim.width / 2F, dim.height /2F, paint)
            canvas.drawRect(0F, 0F, dim.width.toFloat(), dim.height.toFloat(), paint)
            paint.setColor(color)
        } finally {
            canvas.restore()
        }
    }

    public fun transform(shiftX: Int, shiftY: Int) {
        position.shift(shiftX, shiftY)
    }

    public fun relocate(p: Point) {
        position.x  = p.x
        position.y  = p.y
    }

    public fun updatePageInfo(newInfo: PageInfo) {
        state = State.NOT_VISIBLE
        layoutStrategy.getPageInfo(layoutInfo, true, newInfo)
        dim.width = layoutInfo.x.pageDimension
        dim.height = layoutInfo.y.pageDimension
        println("page info updated: " + newInfo.pageNum + " " + dim)
        pageListener?.sizeChanged(this)

        addRenderTask()
    }

    fun updateBitmap(b: IntBitmap) {
        state = State.VISIBLE
        bitmap = b
        pageListener?.pageViewUpdated(this)
        if (state == State.DESTROYED) {
            b.destroy()
        }
    }

    fun destroy() {
        bitmap?.destroy()
        state = State.DESTROYED
    }

}

inline fun Canvas.translate(p: Point) {
    this.translate(p.x.toFloat(), p.y.toFloat())
}

fun PageView.addRenderTask() {
    println("rendering page " + pageNum)
    val p = object : AsyncTask<Int, Int, IntBitmap>() {

        override fun doInBackground(vararg params: Int?): IntBitmap? {
            return pageListener?.renderPage(this@addRenderTask)
            println("null listener " + pageNum)
        }

        override fun onPostExecute(result: IntBitmap?) {
            if (result != null) {
                this@addRenderTask.updateBitmap(result)
            } else {
                println("error null bitmap " + pageNum)
            }
        }
    }

    p.execute(1)
}