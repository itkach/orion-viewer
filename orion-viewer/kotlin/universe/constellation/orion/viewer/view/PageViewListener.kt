package universe.constellation.orion.viewer.view

import android.graphics.Bitmap


trait PageViewListener {

    public fun pageViewUpdated(view: PageView)

    public fun sizeChanged(view: PageView)

    public fun renderPage(view: PageView): IntBitmap
}