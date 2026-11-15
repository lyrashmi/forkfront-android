package com.tbd.forkfront;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class FlowLayout extends ViewGroup
{
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;

	private int mOrientation = HORIZONTAL;
	private int mMinItemSize = 75;

	// ____________________________________________________________________________________
	public FlowLayout(Context context)
	{
		super(context);
	}

	public FlowLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public FlowLayout(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	// ____________________________________________________________________________________
	public void setOrientation(int orientation)
	{
		if(mOrientation != orientation)
		{
			mOrientation = orientation;
			requestLayout();
		}
	}

	public void setMinItemSize(int minItemSize)
	{
		mMinItemSize = minItemSize;
		requestLayout();
	}

	// ____________________________________________________________________________________
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);

		int paddingLeft = getPaddingLeft();
		int paddingRight = getPaddingRight();
		int paddingTop = getPaddingTop();
		int paddingBottom = getPaddingBottom();

		int count = getChildCount();

		if(mOrientation == VERTICAL)
		{
			// Vertical single column stack
			int maxWidth = 0;
			int totalHeight = paddingTop + paddingBottom;

			for(int i = 0; i < count; i++)
			{
				View child = getChildAt(i);
				if(child.getVisibility() == GONE)
					continue;

				measureChild(child, widthMeasureSpec, heightMeasureSpec);
				int w = Math.max(child.getMeasuredWidth(), mMinItemSize);
				int h = Math.max(child.getMeasuredHeight(), mMinItemSize);

				maxWidth = Math.max(maxWidth, w);
				totalHeight += h;
			}

			maxWidth += paddingLeft + paddingRight;
			setMeasuredDimension(
				widthMode == MeasureSpec.EXACTLY ? widthSize : maxWidth,
				totalHeight
			);
			return;
		}

		// HORIZONTAL uniform grid layout
		int targetAvailableWidth = widthSize - paddingLeft - paddingRight;
		if(targetAvailableWidth <= 0)
			targetAvailableWidth = getContext().getResources().getDisplayMetrics().widthPixels - paddingLeft - paddingRight;

		int numCols = Math.max(1, targetAvailableWidth / mMinItemSize);
		int baseColWidth = targetAvailableWidth / numCols;
		int remainder = targetAvailableWidth % numCols;

		List<View> visibleChildren = new ArrayList<>();
		for(int i = 0; i < count; i++)
		{
			View child = getChildAt(i);
			if(child.getVisibility() != GONE)
				visibleChildren.add(child);
		}

		int numVisible = visibleChildren.size();
		int numRows = (numVisible + numCols - 1) / numCols;
		if(numRows == 0)
			numRows = 1;

		int childHeightSpec = MeasureSpec.makeMeasureSpec(mMinItemSize, MeasureSpec.EXACTLY);

		for(int i = 0; i < numVisible; i++)
		{
			int col = i % numCols;
			int colW = baseColWidth + (col < remainder ? 1 : 0);
			View child = visibleChildren.get(i);
			child.measure(
				MeasureSpec.makeMeasureSpec(colW, MeasureSpec.EXACTLY),
				childHeightSpec
			);
		}

		int totalHeight = paddingTop + paddingBottom + (numRows * mMinItemSize);
		setMeasuredDimension(widthSize, totalHeight);
	}

	// ____________________________________________________________________________________
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		int paddingLeft = getPaddingLeft();
		int paddingRight = getPaddingRight();
		int paddingTop = getPaddingTop();

		if(mOrientation == VERTICAL)
		{
			int y = paddingTop;
			int count = getChildCount();
			for(int i = 0; i < count; i++)
			{
				View child = getChildAt(i);
				if(child.getVisibility() == GONE)
					continue;
				int h = child.getMeasuredHeight();
				int w = child.getMeasuredWidth();
				child.layout(paddingLeft, y, paddingLeft + w, y + h);
				y += h;
			}
			return;
		}

		int targetAvailableWidth = (right - left) - paddingLeft - paddingRight;
		if(targetAvailableWidth <= 0)
			targetAvailableWidth = getContext().getResources().getDisplayMetrics().widthPixels - paddingLeft - paddingRight;

		int numCols = Math.max(1, targetAvailableWidth / mMinItemSize);
		int baseColWidth = targetAvailableWidth / numCols;
		int remainder = targetAvailableWidth % numCols;

		int visibleIdx = 0;
		int count = getChildCount();
		for(int i = 0; i < count; i++)
		{
			View child = getChildAt(i);
			if(child.getVisibility() == GONE)
				continue;

			int col = visibleIdx % numCols;
			int row = visibleIdx / numCols;

			int x = paddingLeft + col * baseColWidth + Math.min(col, remainder);
			int colW = baseColWidth + (col < remainder ? 1 : 0);
			int childY = paddingTop + row * mMinItemSize;

			child.layout(x, childY, x + colW, childY + mMinItemSize);
			visibleIdx++;
		}
	}
}
