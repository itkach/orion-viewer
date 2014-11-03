package universe.constellation.orion.viewer.view

import android.graphics.Paint
import universe.constellation.orion.viewer.geom.Dimension

public trait DrawContext {
    val defaultPaint: Paint
    val screenSize: Dimension
}