package com.example.mobilclicker;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

public class EditedScrollView extends ScrollView {
    public EditedScrollView(Context context) {
        super(context);
    }
    public EditedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public EditedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public EditedScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    @Override
    public void fling(int velocityY) {
        super.fling((int)(velocityY * 5.0)); // speed set to 500%, feels better/more responsive :]
    }
}
