package com.whb.splittext;

import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.text.*;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by whb on 2019-07-08.
 * Email:18720982457@163.com
 */
public class SplitTextViewHelper {

    public static final String FORE = "fore";
    public static final String BACK = "back";

    private TextView mTextView;
    @ColorInt
    public int mSelectColor;

    public String mRegular = "\\b[a-zA-Z-]+\\b";

    @SelectedDirectionType
    public String mSelectedDirection = BACK;

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({FORE, BACK})
    @interface SelectedDirectionType {
    }

    public WordClick mWordClick;

    public CharSequence mText;

    private BackgroundColorSpan mSelectedBackSpan = null;
    private ForegroundColorSpan mSelectedForeSpan = null;

    public SplitTextViewHelper(TextView textView) {
        this.mTextView = textView;
    }

    public void init() {
        mTextView.setText(mText, TextView.BufferType.SPANNABLE);
        mTextView.setHighlightColor(Color.TRANSPARENT);
        SplitWord();
        mTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * 分词并且添加点击事件
     */
    public void SplitWord() {
        Spannable spans = (Spannable) mTextView.getText();
        Matcher matcher = Pattern.compile(mRegular, Pattern.CASE_INSENSITIVE).matcher(spans);
        while (matcher.find()) {
            ClickableSpan clickSpan = clickableSpan();
            spans.setSpan(clickSpan, matcher.start(0), matcher.end(0), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * 创建点击事件
     */
    private ClickableSpan clickableSpan() {
        return new ClickableSpan() {

            @Override
            public void onClick(@NonNull View widget) {
                try {
                    TextView tv = (TextView) widget;
                    String s = tv.getText().subSequence(tv.getSelectionStart(), tv.getSelectionEnd()).toString();
                    setSelectedSpan((TextView) widget);
                    Log.d("tapped on:", s);
                    int[] array = new int[4];
                    meaurePosition(this, widget, array);
                    if (mWordClick != null) {
                        mWordClick.onWordClick(s, (TextView) widget, array[0], array[1], array[2], array[3], SplitTextViewHelper.this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
            }
        };
    }

    /**
     * 单词点击位置
     */
    public void meaurePosition(ClickableSpan spanned, View widget, int[] array) {
        TextView parentTextView = (TextView) widget;
        Rect parentTextViewRect = new Rect();
        // Initialize values for the computing of clickedText position
        SpannableString completeText = (SpannableString) parentTextView.getText();
        Layout textViewLayout = parentTextView.getLayout();
        int startOffsetOfClickedText = completeText.getSpanStart(spanned);
        int endOffsetOfClickedText = completeText.getSpanEnd(spanned);
        float startXCoordinatesOfClickedText = textViewLayout.getPrimaryHorizontal(startOffsetOfClickedText);
        float endXCoordinatesOfClickedText = textViewLayout.getPrimaryHorizontal(endOffsetOfClickedText);
        // Get the rectangle of the clicked text
        int currentLineStartOffset = textViewLayout.getLineForOffset(startOffsetOfClickedText);
        int currentLineEndOffset = textViewLayout.getLineForOffset(endOffsetOfClickedText);
        boolean keywordIsInMultiLine = currentLineStartOffset != currentLineEndOffset;
        textViewLayout.getLineBounds(currentLineStartOffset, parentTextViewRect);
        // Update the rectangle position to his real position on screen
        int[] parentTextViewLocation = new int[2];
        parentTextView.getLocationOnScreen(parentTextViewLocation);
        int parentTextViewTopAndBottomOffset = (parentTextViewLocation[1] - parentTextView.getScrollY() + parentTextView.getCompoundPaddingTop());
        parentTextViewRect.top += parentTextViewTopAndBottomOffset;
        parentTextViewRect.bottom += parentTextViewTopAndBottomOffset;
        parentTextViewRect.left += (parentTextViewLocation[0] +
                startXCoordinatesOfClickedText +
                parentTextView.getCompoundPaddingLeft() - parentTextView.getScrollX());
        parentTextViewRect.right = (int) (parentTextViewRect.left + endXCoordinatesOfClickedText - startXCoordinatesOfClickedText);
        int x = parentTextViewRect.left;
        int y = parentTextViewRect.bottom;
        if (keywordIsInMultiLine) {
            x = parentTextViewRect.left;
        }
        array[0] = x;
        array[1] = y;
        array[2] = parentTextViewRect.width();
        array[3] = parentTextViewRect.height();
    }


    /**
     * 点击事件状态改变
     */
    private void setSelectedSpan(TextView tv) {
        if (TextUtils.equals(mSelectedDirection, BACK) && mSelectedBackSpan == null) {
            mSelectedBackSpan = new BackgroundColorSpan(mSelectColor);
            ((Spannable) tv.getText()).setSpan(mSelectedBackSpan, tv.getSelectionStart(), tv.getSelectionEnd(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (TextUtils.equals(mSelectedDirection, FORE) && mSelectedForeSpan == null) {
            mSelectedForeSpan = new ForegroundColorSpan(mSelectColor);
            ((Spannable) tv.getText()).setSpan(mSelectedForeSpan, tv.getSelectionStart(), tv.getSelectionEnd(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            ((Spannable) tv.getText()).removeSpan(mSelectedBackSpan);
            ((Spannable) tv.getText()).removeSpan(mSelectedForeSpan);
            mSelectedBackSpan = null;
            mSelectedForeSpan = null;
        }
    }

    /**
     * 去除点击状态
     */
    public void dismissSelected() {
        ((Spannable) mTextView.getText()).removeSpan(mSelectedBackSpan);
        ((Spannable) mTextView.getText()).removeSpan(mSelectedForeSpan);
        mSelectedBackSpan = null;
        mSelectedForeSpan = null;
    }

    public interface WordClick {
        void onWordClick(String word, TextView textView, int x, int y, int w, int h, SplitTextViewHelper helper);
    }

    public static class SplitTextViewHelperBuilder {
        private SplitTextViewHelper helper;

        public SplitTextViewHelperBuilder(TextView view) {
            helper = new SplitTextViewHelper(view);
        }

        public SplitTextViewHelperBuilder setSelectColor(@ColorInt int color) {
            helper.mSelectColor = color;
            return this;
        }

        public SplitTextViewHelperBuilder setRegular(String regular) {
            helper.mRegular = regular;
            return this;
        }

        public SplitTextViewHelperBuilder setSelectedDirection(@SelectedDirectionType String selectedDirection) {
            helper.mSelectedDirection = selectedDirection;
            return this;
        }

        public SplitTextViewHelperBuilder setWordClick(WordClick wordClick) {
            helper.mWordClick = wordClick;
            return this;
        }

        public SplitTextViewHelperBuilder setText(CharSequence text) {
            helper.mText = text;
            return this;
        }

        public SplitTextViewHelper build() {
            helper.init();
            return helper;
        }

    }
}
