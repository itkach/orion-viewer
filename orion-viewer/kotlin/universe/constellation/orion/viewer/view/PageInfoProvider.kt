package universe.constellation.orion.viewer.view

import universe.constellation.orion.viewer.geom.Dimension
import universe.constellation.orion.viewer.DocumentWrapper
import android.util.SparseArray
import universe.constellation.orion.viewer.PageInfo
import universe.constellation.orion.viewer.geom.Point
import android.os.AsyncTask
import universe.constellation.orion.viewer.SimpleLayoutStrategy

/**
 * Created by mike on 10/30/14.
 */

public class PageInfoProvider(val doc: DocumentWrapper) {

    val pageCache = SparseArray<PageInfo>(100)

    public fun getPageInfo(pageNum: Int): PageInfo {
        var pageInfo: PageInfo? = null
        synchronized (pageCache) {
            pageInfo = pageCache.get(pageNum)
        }

        if (pageInfo == null) {
            pageInfo = doc.getPageInfo(pageNum)
            synchronized(pageCache) {
                pageCache.put(pageNum, pageInfo)
            }
        }
        return pageInfo!!
    }

    public fun getPageInfoIfExists(pageNum: Int): PageInfo? {
        return synchronized (pageCache) { pageCache.get(pageNum) }
    }

    public val pageCount: Int = doc.getPageCount();
}

public class LazyPageViewProvider(val screenArea: Dimension, val pageInfoProvider: PageInfoProvider, val layoutStrategy: SimpleLayoutStrategy) {

    val pageCount = pageInfoProvider.doc.getPageCount()
    
    val firstPage: Int = 0
    
    val lastPage: Int = pageCount - 1 

    public fun getPageView(pageNum: Int, pagePosition: Point): PageView {
        var pageInfo = pageInfoProvider.getPageInfoIfExists(pageNum)

        if (pageInfo == null) {
            pageInfo = PageInfo(screenArea.width, screenArea.height, pageNum)
            val pageView = PageView(pageInfo!!.pageNum, screenArea, pagePosition, layoutStrategy)

            runAsyncTask(PageInfoAsyncTask(pageInfoProvider, pageView))

            return pageView
        } else {
            val pageView = PageView(pageInfo!!.pageNum, screenArea, pagePosition, layoutStrategy)
            pageView.updatePageInfo(pageInfo!!, true)
            return pageView
        }
    }

    private fun runAsyncTask(task: PageInfoAsyncTask) {
        //TODO serial for < HONEYCOMB
        task.execute()
    }
}

public class PageInfoAsyncTask(val infoProvider: PageInfoProvider, val pageView: PageView) : AsyncTask<PageView, Int, PageInfo>() {
    override fun doInBackground(vararg params: PageView?): PageInfo? {
        return infoProvider.getPageInfo(pageView.pageNum)
    }

    override fun onPostExecute(result: PageInfo?) {
        pageView.updatePageInfo(result!!, true)
    }
}

