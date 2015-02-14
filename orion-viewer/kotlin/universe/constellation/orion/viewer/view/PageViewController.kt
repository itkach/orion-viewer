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

    val screenArea = Rect(0, 0, pageProvider.screenArea.width, pageProvider.screenArea.height);

    val renderingArea = Rect(screenArea);

    val bitmapCache = Cache.create(pageProvider.screenArea)

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
            if (!pageView.isVisibleOrNext()) {
                removePageView(pageView)
            }
        }

        ensureHasAnchor(pageView)
        if (pageView.isVisible()) {
            pageView.redraw()
        }

        println("Cached pages number ${visiblePages.size()} + ${pageView.pageArea} + ${pageView.isVisible()}")
        if ((pageView.hasSpaceAfter() || pageView.isVisible()) && pageView.hasNext()) {
            val rect = pageView.pageArea
            createPage(pageNum + 1, Point(rect.left, rect.bottom + 1))
        }
        return pageView
    }

    public fun viewParamsChanged(yDelta: Int) {
        val pageNum = anchor?.pageNum ?: 0
        renderingArea.set(0, 0, pageProvider.layoutStrategy.viewWidth, pageProvider.layoutStrategy.viewHeight)
        forAllCachedPages { recalcPageInfo() }
        createPage(pageNum)

    }

    private fun removePageView(p: PageView) {
        p.destroy()
        visiblePages.remove(p.pageNum)
        pageAccumulator.removeTask(p)
        p.pageListener = null
    }

    private fun ensureHasAnchor(p: PageView) {
        if (!(anchor?.isVisible() ?: true)) {
            println("Changing anchor to null: ${anchor?.pageNum}")
            anchor = null
        }

        if (anchor == null && p.isVisible()) {
            println("Changing anchor to ${p.pageNum}")
            anchor = p;
        }
    }

    inline fun forAllCachedPages(operation: PageView.() -> Unit) {
        for (i in 0..visiblePages.size() - 1) {
            val pageView = visiblePages.valueAt(i)
            pageView.operation()
        }
    }

    public fun changePage(next: Boolean) {
        translatePages((if (!next) 1 else - 1) * renderingArea.height())
        pageAccumulator.update()
    }

    fun translatePages(delta: Int) {
        println("translating to $delta")
        forAllCachedPages {
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

    fun PageView.isVisibleOrNext(): Boolean {
        return isVisible() || visiblePages.get(pageNum - 1)?.isVisible() ?: false
    }

    fun PageView.hasSpaceAfter(): Boolean {
        return pageArea.bottom < renderingArea.bottom
    }

    fun PageView.hasSpaceBefore(): Boolean {
        return pageArea.top > renderingArea.top
    }

    fun PageView.hasNext(): Boolean {
        return pageNum < pageProvider.lastPage
    }

    fun PageView.hasPrev(): Boolean {
        return pageNum > pageProvider.firstPage
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