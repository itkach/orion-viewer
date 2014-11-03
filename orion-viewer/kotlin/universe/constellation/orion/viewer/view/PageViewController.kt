package universe.constellation.orion.viewer.view

import android.graphics.Rect
import universe.constellation.orion.viewer.geom.Point
import android.util.SparseArray

/**
 * Created by mike on 11/3/14.
 */

public class PageViewController(val pageProvider: LazyPageViewProvider, val pageAccumulator: TaskAccumulator) : PageViewListener {

    var anchor: PageView? = null;

    val visiblePages: SparseArray<PageView> = SparseArray(20);

    val calcRect: Rect = Rect()

    val renderingArea = Rect(0, 0, pageProvider.renderingArea.width, pageProvider.renderingArea.height);

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
        }

        if (pageView.hasSpaceAfter() && pageView.hasNext()) {
            val rect = pageView.pageArea
            createPage(pageNum + 1, Point(rect.left, rect.bottom + 1))
        }

        return pageView
    }

    fun translatePages(delta: Int) {
        for (i in 0..visiblePages.size() - 1) {
            val pageView = visiblePages.get(i)
            pageView.transform(0, delta)
        }
        //anchor!!.transform(0, delta)
        createPage(anchor!!.pageNum, anchor!!.position)
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

    override fun pageLoaded(view: PageView) {
        createPage(view.pageNum, view.position)
    }

    override fun sizeChanged(view: PageView) {
        createPage(view.pageNum, view.position)
    }
}