package universe.constellation.orion.viewer.view

/**
 * Created by mike on 11/3/14.
 */

trait TaskAccumulator {

    fun appendTask(p: PageView)

    fun removeTask(p: PageView)

    fun update()

}