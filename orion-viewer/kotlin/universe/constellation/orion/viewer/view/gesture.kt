package universe.constellation.orion.viewer.view

import android.view.GestureDetector
import android.view.MotionEvent
import universe.constellation.orion.viewer.DocumentWrapper
import universe.constellation.orion.viewer.util.ScreenUtil
import android.content.Context
import universe.constellation.orion.viewer.SimpleLayoutStrategy
import universe.constellation.orion.viewer.RenderThread

/**
 * Created by mike on 11/3/14.
 */

class GestureListener(val pageProducer: PageViewController) : GestureDetector.SimpleOnGestureListener() {

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        val yDelta = -distanceY
        pageProducer.translatePages(yDelta.toInt());
        pageProducer.pageAccumulator.update()
        return true
    }
}

fun initNewGesture(c: Context, doc: DocumentWrapper, accumulator: TaskAccumulator, strategy: SimpleLayoutStrategy, renderer: RenderThread) : GestureListener {
    val pageInfoProvider = PageInfoProvider(doc)
    val lazyPageViewProvider = LazyPageViewProvider(ScreenUtil.getScreenSize(c), pageInfoProvider, strategy)
    val pageProducer = PageViewController(lazyPageViewProvider, accumulator, renderer)
    pageProducer.createPage(0)
    return GestureListener(pageProducer)
}