package universe.constellation.orion.viewer.view

import android.graphics.Rect
import universe.constellation.orion.viewer.geom.Point
import android.util.SparseArray
import universe.constellation.orion.viewer.RenderThread
import android.graphics.Bitmap

/**
 * Created by mike on 11/3/14.
 */

public class PageViewController(val pageProvider: LazyPageViewProvider,
                                val pageAccumulator: TaskAccumulator) : PageViewListener {

    var anchor: PageView? = null;

    val visiblePages: SparseArray<PageView> = SparseArray(20);

    val calcRect: Rect = Rect()

    val screenArea = Rect(0, 0, pageProvider.renderingArea.width, pageProvider.renderingArea.height);

    val renderingArea = screenArea;

    val bitmapCache = Cache.create(pageProvider.renderingArea)

    fun createPage(pageNum: Int) {
        val pageView = createPage(pageNum, Point(0, 0))
        anchor = pageView
    }

    fun createPage(pageNum: Int, position: Point): PageView {
        var pageView = visiblePages.get(pageNum)
        if (pageView == null) {
            pageView = pageProvider.getPageView(pageNum, position)
            pageView.pageListener = this
            visiblePages.put(pageNum, pageView)
            pageAccumulator.appendTask(pageView)
        } else {
            pageView.relocate(position)
            if (!pageView.isVisible()) {
                removePageView(pageView)
            }
        }

        ensureHasAnchor(pageView)
        if (pageView.isVisible()) {
            pageView.redraw()
        }

        if (pageView.hasSpaceAfter() && pageView.hasNext()) {
            val rect = pageView.pageArea
            createPage(pageNum + 1, Point(rect.left, rect.bottom + 1))
        }
        println("visible pages " + visiblePages.size())
        return pageView
    }

    public fun viewParamsChanged() {
        val pageNum = anchor!!.pageNum
        forAllPages { recalcPageInfo() }
        createPage(pageNum)
    }

    private fun removePageView(p: PageView) {
        p.destroy()
        visiblePages.remove(p.pageNum)
        pageAccumulator.removeTask(p)
        p.pageListener = null
    }

    private fun ensureHasAnchor(p: PageView) {
        if (!(anchor?.isVisible() ?: false)) {
            println("reseting anchor ${anchor?.pageNum}")
            anchor = null
        }

        if (anchor == null && p.isVisible()) {
            println("Changing anchor to ${p.pageNum}")
            anchor = p;
        }
    }

    inline fun forAllPages(operation: PageView.() -> Unit) {
        for (i in 0..visiblePages.size() - 1) {
            val pageView = visiblePages.valueAt(i)
            pageView.operation()
        }
    }

    fun translatePages(delta: Int) {
        forAllPages {
            transform(0, delta)
        }

        if (anchor != null) {
            createPage(anchor!!.pageNum, anchor!!.position)
        }
    }

    fun PageView.isVisible(): Boolean {
        calcRect.set(this.pageArea)
        return calcRect.intersect(renderingArea) && !calcRect.isEmpty()
    }

    fun PageView.hasSpaceAfter(): Boolean {
        return /*isVisible() &&*/ pageArea.bottom < renderingArea.bottom
    }

    fun PageView.hasSpaceBefore(): Boolean {
        return /*isVisible() &&*/ pageArea.top > renderingArea.top
    }

    fun PageView.hasNext(): Boolean {
        return pageNum < pageProvider.pageCount - 1
    }

    fun PageView.hasPrev(): Boolean {
        return pageNum > 0
    }

    override fun pageViewUpdated(view: PageView) {
        pageAccumulator.update()
    }

    override fun sizeChanged(view: PageView) {
        createPage(view.pageNum, view.position)
    }

    override fun renderPage(view: PageView): IntBitmap {
        val layoutPosition = view.layoutInfo
        if (view.bitmap == null) {
            view.bitmap = bitmapCache.createBitmap(layoutPosition.x.pageDimension, layoutPosition.y.pageDimension)
        }
        println("rendering x = ${layoutPosition.x}, y = ${layoutPosition.y}")
        val curPos = layoutPosition
        val width = view.bitmap!!.width
        val height = view.bitmap!!.height
        val leftTopCorner = pageProvider.layoutStrategy.convertToPoint(curPos)
        pageProvider.pageInfoProvider.doc.renderPage(curPos.pageNumber, view.bitmap, curPos.docZoom, leftTopCorner.x, leftTopCorner.y, leftTopCorner.x + width, leftTopCorner.y + height)
        return view.bitmap!!;
    }
}