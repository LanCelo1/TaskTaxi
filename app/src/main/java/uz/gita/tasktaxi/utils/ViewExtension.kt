package uz.gita.tasktaxi.utils

import android.app.Activity
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment

fun Activity.makeStatusBarTransparent() {
    window.apply {

        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        statusBarColor = Color.TRANSPARENT
    }
    val windowInsetsController = WindowCompat.getInsetsController(
        window,
        window.decorView
    )
//    window.setFlags(
//        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//    )
//    window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
//    window.statusBarColor = Color.TRANSPARENT
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//    }

//    window.statusBarColor = window.navigationBarColor
//    val windowInsetsController = WindowCompat.getInsetsController(
//        window,
//        window.decorView
//    )
//
}

fun Fragment.getStatusBarHeight(): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) resources.getDimensionPixelSize(resourceId)
    else Rect().apply { requireActivity().window.decorView.getWindowVisibleDisplayFrame(this) }.top
}

fun Fragment.getNavigationBarHeight(): Int {
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0) resources.getDimensionPixelSize(resourceId)
    else Rect().apply { requireActivity().window.decorView.getWindowVisibleDisplayFrame(this) }.bottom
}
