/*
 * Copyright 2016 2LinesSoftware Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.twolinessoftware.views;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.twolinessoftware.R;
import com.twolinessoftware.utils.ViewUtils;

/**
 * Created by johncarpenter on 2016-06-01.
 */

public class PasswordEditText extends TextInputEditText {

    private IconDrawable mDrawableLeft;

    private boolean mIsShowingPassword = false;

    /**
     * Password visibility toggle
     */
    private IconDrawable mDrawableRight;

    public PasswordEditText(Context context) {
        super(context);
        init();
    }

    public PasswordEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PasswordEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        // @todo should move this to an attr
        mDrawableLeft = new IconDrawable(getContext(), MaterialIcons.md_lock_open).color(getThemePrimaryColor()).actionBarSize();
        setPasswordVisibilityIndicators();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_UP && mDrawableRight != null) {
                Rect bounds = mDrawableRight.getBounds();
                final int x = (int) event.getX();

                if (x >= (getRight() - (ViewUtils.dpToPx(getContext(),48)))){
                    togglePasswordVisibility();
                    event.setAction(MotionEvent.ACTION_CANCEL);
                }
            }


        return super.onTouchEvent(event);
    }

  

    private void setPasswordVisibilityIndicators(Drawable left, Drawable top, Drawable right, Drawable bottom){
        if(mIsShowingPassword){
            mDrawableRight = new IconDrawable(getContext(), MaterialIcons.md_visibility_off).color(ContextCompat.getColor(getContext(), R.color.pal_disabled_dark)).actionBarSize();
        }else{
            mDrawableRight = new IconDrawable(getContext(), MaterialIcons.md_visibility).color(ContextCompat.getColor(getContext(),R.color.pal_disabled_dark)).actionBarSize();
        }

        super.setCompoundDrawables(mDrawableLeft, null, mDrawableRight, null);

    }

    private void setPasswordVisibilityIndicators(){

        Drawable[] drawables = getCompoundDrawables();

        setPasswordVisibilityIndicators(drawables[0], drawables[1], drawables[2], drawables[3]);


    }

    private void togglePasswordVisibility() {
        if (mIsShowingPassword) {
            setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
        mIsShowingPassword = !mIsShowingPassword;
        setPasswordVisibilityIndicators();
    }

    private int getThemePrimaryColor(){
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }

    @Override
    public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        setPasswordVisibilityIndicators(left,top,right,bottom);
    }

}
