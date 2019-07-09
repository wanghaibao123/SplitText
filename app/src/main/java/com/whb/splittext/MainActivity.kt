package com.whb.splittext

import android.content.Context
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.TextView
import com.whb.splittext.SplitTextViewHelper.BACK
import com.whb.splittext.SplitTextViewHelper.FORE
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var helper = SplitTextViewHelper.SplitTextViewHelperBuilder(tv_one)
            .setSelectColor(Color.RED)
            .setSelectedDirection(FORE)
            .setRegular("\\b[a-zA-Z-]+\\b")
            .setText("Pygmalion，King of Cyprus，was a famous sculptor．He made an ivory image of a woman so lovely that he fell in love with it．Every day he tried to make Galatea up in gold andpurple，for that was the name he had given to this mistress of his heart．")
            .setWordClick { word, textView, x, y, w, h, helper ->
                showPopuWindow(textView, x, y, w, h, word, helper)
            }
            .build()

        var helper2 = SplitTextViewHelper.SplitTextViewHelperBuilder(tv_two)
            .setSelectColor(Color.RED)
            .setSelectedDirection(BACK)
            .setRegular("\\b[a-zA-Z-]+\\b")
            .setText("He embraced and kissed it，but it remaineda statue．In despair he went to Aphrodite‘sshrine for help．Offering rich sacrifice and sending up a passionate prayer，he begged the goddess to give him a wife as graceful as Galatea．")
            .setWordClick { word, textView, x, y, w, h, helper ->
                showPopuWindow(textView, x, y, w, h, word, helper)
            }
            .build()
    }


    /**
     * 弹窗
     */
    fun showPopuWindow(v: TextView, x: Int, y: Int, ww: Int, wh: Int, s: String, helper: SplitTextViewHelper) {
        val contentView = LayoutInflater.from(v.context).inflate(R.layout.playword_layout_popuwindow, null)
        // 判断需要向上弹出还是向下弹出显示
        var screenHeight = getWindowHeight(v.context)
        var isNeedShowUp = (screenHeight - y - wh < dp2px(this, 150f))
        var xpos = 0
        var ypos = 0
        var x1 = x + (ww - dp2px(this, 185f)) / 2
        if (x1 < 0) {
            x1 = 10
        } else if (x1 + dp2px(this, 185f) > getWindowWidth(v.context)) {
            x1 = getWindowWidth(v.context) - dp2px(this, 185f) - 10
        }
        if (isNeedShowUp) {
            xpos = x1
            ypos = y - dp2px(this, 150f) - wh
            contentView.findViewById<View>(R.id.view_top_arrow).visibility = View.GONE
            contentView.findViewById<View>(R.id.view_bottom_arrow).visibility = View.VISIBLE
            var margin =
                contentView.findViewById<View>(R.id.view_bottom_arrow).layoutParams as ViewGroup.MarginLayoutParams
            margin.setMargins(x - x1 + (ww - dp2px(this, 10f)) / 2, 0, 0, 0)
            contentView.layoutParams = margin
        } else {
            xpos = x1
            ypos = y
            contentView.findViewById<View>(R.id.view_top_arrow).visibility = View.VISIBLE
            contentView.findViewById<View>(R.id.view_bottom_arrow).visibility = View.GONE
            var margin =
                contentView.findViewById<View>(R.id.view_top_arrow).layoutParams as ViewGroup.MarginLayoutParams
            margin.setMargins(x - x1 + (ww - dp2px(this, 10f)) / 2, 0, 0, 0)
            contentView.layoutParams = margin
        }
        var mCustomPopWindow = CustomPopWindow.PopupWindowBuilder(v.context)
            .setView(contentView)
            .setFocusable(false)//是否获取焦点，默认为ture
            .setOutsideTouchable(true) //外部获取焦点 并且dimess
            .setOnDissmissListener {
                helper.dismissSelected()
            }
            .size(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            .create()
            .showAtLocation(v, Gravity.TOP or Gravity.LEFT, xpos, ypos)

    }

    /**
     * 获取屏幕高度
     *
     * @param context
     * @return
     */
    fun getWindowHeight(context: Context): Int {
        val wm = context
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return wm.defaultDisplay.height
    }

    /**
     * 获取屏幕高度
     *
     * @param context
     * @return
     */
    fun getWindowWidth(context: Context): Int {
        val wm = context
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return wm.defaultDisplay.width
    }

    /**
     * dp to px
     *
     * @param context
     * @param dpValue dp
     * @return px
     */
    fun dp2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}
