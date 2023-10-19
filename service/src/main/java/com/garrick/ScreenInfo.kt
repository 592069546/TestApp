package com.garrick

import android.graphics.Rect
import com.garrick.Ln.w

data class ScreenInfo(
    /**
     * Device (physical) size, possibly cropped
     *
     * Return the video size as if locked video orientation was not set.
     *
     * @return the unlocked video size
     */
    val contentRect: Rect, // device size, possibly cropped

    /**
     * Video size, possibly smaller than the device size, already taking the device rotation and crop into account.
     *
     *
     * However, it does not include the locked video orientation.
     */
    val unlockedVideoSize: Size,
    /**
     * Device rotation, related to the natural device orientation (0, 1, 2 or 3)
     */
    val deviceRotation: Int,
    /**
     * The locked video orientation (-1: disabled, 0: normal, 1: 90° CCW, 2: 180°, 3: 90° CW)
     */
    private val lockedVideoOrientation: Int
) {

    /**
     * Return the actual video size if locked video orientation is set.
     *
     * @return the actual video size
     */
    val videoSize: Size
        get() = if (videoRotation % 2 == 0) {
            unlockedVideoSize
        } else unlockedVideoSize.rotate()

    /**
     * Return the rotation to apply to the device rotation to get the requested locked video orientation
     *
     * @return the rotation offset
     */
    val videoRotation: Int
        get() {
            return if (lockedVideoOrientation == -1) {
                // no offset
                0
            } else (deviceRotation + 4 - lockedVideoOrientation) % 4
        }

    /**
     * Return the rotation to apply to the requested locked video orientation to get the device rotation
     *
     * @return the (reverse) rotation offset
     */
    val reverseVideoRotation: Int
        get() {
            return if (lockedVideoOrientation == -1) {
                // no offset
                0
            } else (lockedVideoOrientation + 4 - deviceRotation) % 4
        }

    fun withDeviceRotation(newDeviceRotation: Int): ScreenInfo {
        if (newDeviceRotation == deviceRotation) {
            return this
        }
        // true if changed between portrait and landscape
        val orientationChanged = (deviceRotation + newDeviceRotation) % 2 != 0
        val newContentRect: Rect
        val newUnlockedVideoSize: Size
        if (orientationChanged) {
            newContentRect = contentRect.flipRect()
            newUnlockedVideoSize = unlockedVideoSize.rotate()
        } else {
            newContentRect = contentRect
            newUnlockedVideoSize = unlockedVideoSize
        }
        return ScreenInfo(newContentRect, newUnlockedVideoSize, newDeviceRotation, lockedVideoOrientation)
    }

    companion object {
        @JvmStatic
        fun computeScreenInfo(rotation: Int, deviceSize: Size, crop: Rect?, maxSize: Int, lockedVideoOrientation: Int): ScreenInfo {
            val videoOrientation = if (lockedVideoOrientation == Device.LOCK_VIDEO_ORIENTATION_INITIAL) {
                // The user requested to lock the video orientation to the current orientation
                rotation
            } else lockedVideoOrientation
            var contentRect = Rect(0, 0, deviceSize.width, deviceSize.height)
            if (crop != null) {
                val cropRect = if (rotation % 2 != 0) { // 180s preserve dimensions
                    // the crop (provided by the user) is expressed in the natural orientation
                    crop.flipRect()
                } else crop
                if (!contentRect.intersect(cropRect)) {
                    // intersect() changes contentRect so that it is intersected with crop
                    w("Crop rectangle (${cropRect.formatCrop()}) does not intersect device screen (${deviceSize.toRect().formatCrop()})")
                    contentRect = Rect() // empty
                }
            }
            val videoSize = computeVideoSize(contentRect.width(), contentRect.height(), maxSize)
            return ScreenInfo(contentRect, videoSize, rotation, videoOrientation)
        }

        private fun Rect.formatCrop(): String = "${width()}:${height()}:$left:$top"

        private fun computeVideoSize(w: Int, h: Int, maxSize: Int): Size {
            // Compute the video size and the padding of the content inside this video.
            // Principle:
            // - scale down the great side of the screen to maxSize (if necessary);
            // - scale down the other side so that the aspect ratio is preserved;
            // - round this value to the nearest multiple of 8 (H.264 only accepts multiples of 8)
            var width = w and 7.inv() // in case it's not a multiple of 8
            var height = h and 7.inv()
            if (maxSize > 0) {
                if (BuildConfig.DEBUG && maxSize % 8 != 0) {
                    throw AssertionError("Max size must be a multiple of 8")
                }
                val portrait = height > width
                var major = if (portrait) height else width
                var minor = if (portrait) width else height
                if (major > maxSize) {
                    val minorExact = minor * maxSize / major
                    // +4 to round the value to the nearest multiple of 8
                    minor = minorExact + 4 and 7.inv()
                    major = maxSize
                }
                width = if (portrait) minor else major
                height = if (portrait) major else minor
            }
            return Size(width, height)
        }

        private fun Rect.flipRect(): Rect = Rect(top, left, bottom, right)
    }
}