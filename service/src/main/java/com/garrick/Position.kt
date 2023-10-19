package com.garrick

data class Position constructor(val point: Point, val screenSize: Size) {

    constructor(x: Int, y: Int, screenWidth: Int, screenHeight: Int) : this(Point(x, y), Size(screenWidth, screenHeight))

    fun rotate(rotation: Int): Position = when (rotation) {
        1 -> Position(Point(screenSize.height - point.y, point.x), screenSize.rotate())
        2 -> Position(Point(screenSize.width - point.x, screenSize.height - point.y), screenSize)
        3 -> Position(Point(point.y, screenSize.width - point.x), screenSize.rotate())
        else -> this
    }
}