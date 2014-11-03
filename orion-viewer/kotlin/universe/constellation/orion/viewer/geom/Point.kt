package universe.constellation.orion.viewer.geom

public class Point(var x: Int, var y: Int) {

    fun shift(x: Int, y: Int) {
        this.x += x
        this.y += y
    }

    fun relocate(p: Point) {
        this.x = p.x
        this.y = p.y
    }

}