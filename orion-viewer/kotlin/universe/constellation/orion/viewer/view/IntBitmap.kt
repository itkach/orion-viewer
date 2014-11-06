package universe.constellation.orion.viewer.view

import universe.constellation.orion.viewer.geom.Dimension
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect


public class IntBitmap(val width: Int, val height: Int, val offset: Int, val cache : Cache) {

    val endOffset = offset + intCounts()

    val array = cache.array

    var isDead = false
        private set

    public fun intCounts(): Int = width * height

    public fun Canvas.drawBitmap(x: Float, y: Float, width: Int, height: Int, paint: Paint ) {
        this.drawBitmap(cache.array, offset, this@IntBitmap.width, 0, 0, width, height, true, paint);
    }

    public fun Canvas.drawBitmap(r: Rect, paint: Paint) {
        drawBitmap(r.left.toFloat(), r.top.toFloat(), r.width(), r.height(), paint);
    }

    fun destroy() {
        isDead = true
    }
}


public class Cache(screenSize : Dimension) {

    val array: IntArray = IntArray(screenSize.width * screenSize.height * 4)
    val intervals = linkedListOf<IntBitmap>()

    val totalSize = array.size

    //TODO wrong cycling
    public fun createBitmap(width: Int, height: Int): IntBitmap {
        var offset = findFreeOffset(height * width)
        if (offset == -1) {
            val mutableIterator = intervals.iterator()
            for (i in mutableIterator) {
                if (i.isDead) {
                    mutableIterator.remove()
                }
            }
            offset = findFreeOffset(height * width)
        }

        if (offset == -1) {
            throw RuntimeException("not enough memory: total memory " + array.size)
        }

        val bitmap = IntBitmap(width, height, offset, this)
        val insertAtBegining = (intervals.first?.offset ?: 0) > offset

        val insertIndex = if (insertAtBegining) 0 else intervals.size
        println("insertIndex $insertIndex offset $offset")
        intervals.add(insertIndex, bitmap)

        return bitmap;
    }

    private fun findFreeOffset(needBytes: Int): Int {
        val firstFreeAtEnd = intervals.last?.endOffset ?: 0
        val firstOccupied = intervals.first?.offset ?: 0

        return if (firstFreeAtEnd + needBytes < totalSize) {
            firstFreeAtEnd
        } else if (needBytes < firstOccupied) {
            firstOccupied - needBytes
        } else {
            -1
        }
    }
}
