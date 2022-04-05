package com.komkum.komkum.util.adaper

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

import com.komkum.komkum.ui.book.BookHomeFragment
import com.komkum.komkum.ui.container.*
import com.komkum.komkum.ui.home.HomeFragment
import com.komkum.komkum.ui.library.LibraryFragment
import com.komkum.komkum.ui.podcast.PodcastHomeFragment
import com.komkum.komkum.ui.search.SearchFragment

class HomeViewpagerAdapter(fm : FragmentActivity) : FragmentStateAdapter(fm) {
    override fun getItemCount(): Int {
        return 5
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> MusicContainerFragment()
            1 -> BookContainerFragment()
            2 -> PodcastContainerFragment()
            3 -> SearchContainerFragment()
            4 -> LibraryContainerFragment()
            else -> MusicContainerFragment()
        }
    }
}

//class NonSwipingViewPager : ViewPager2() {
//
//    constructor(context: Context) : super(context) {
//        setMyScroller()
//    }
//
//    constructor(context: Context, attrs: AttributeSet) :
//            super(context, attrs) {
//        setMyScroller()
//    }
//
//    override fun onInterceptTouchEvent(event: MotionEvent): Boolean
//    {
//        // Never allow swiping to switch between pages
//        return false
//    }
//
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        // Never allow swiping to switch between pages
//        return false
//    }
//
//
//    private fun setMyScroller() {
//        try {
//            val viewpager = ViewPager2::class.java
//            val scroller = viewpager.getDeclaredField("mScroller")
//            scroller.isAccessible = true
//            scroller.set(this, MyScroller(context))
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//    }
//
//    inner class MyScroller(context: Context) : Scroller(context, DecelerateInterpolator()) {
//
//        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
//            super.startScroll(startX, startY, dx, dy, 0 /* secs */)
//        }
//    }
//}