package doext.custom;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import core.helper.DoUIModuleHelper;
import doext.implement.do_IndexListView_View;
import doext.implement.do_IndexListView_View.onGroupChangeListener;

public class DoIndexListViewSideBar extends LinearLayout implements onGroupChangeListener {
	private SectionIndexer sectionIndexter = null;
	private ListView list;
	private TextView mDialogText;
	private int m_nItemHeight;
	private int m_nItmeWidth;
	private int fontSize;

	private int bgColor = Color.TRANSPARENT;
	private int pressBgColor = Color.parseColor("#C0C0C0");
	private int textColor = Color.BLACK;
	private int lumpColor = Color.TRANSPARENT;

	private List<String> indexDatas;

	private int pos;

	private boolean isFlag;

	public DoIndexListViewSideBar(Context context) {
		super(context);
	}

	public void setColor(String _colosStr) {
		// bgColor 默认透明,pressBgColor 灰色,textColor 黑色,lumpColor 默认透明
		String[] colorStrs = _colosStr.split(",");
		if (colorStrs != null && colorStrs.length == 4) {
			this.bgColor = DoUIModuleHelper.getColorFromString(colorStrs[0], Color.TRANSPARENT);
			this.pressBgColor = DoUIModuleHelper.getColorFromString(colorStrs[1], Color.parseColor("#C0C0C0"));
			this.textColor = DoUIModuleHelper.getColorFromString(colorStrs[2], Color.BLACK);
			this.lumpColor = DoUIModuleHelper.getColorFromString(colorStrs[3], Color.TRANSPARENT);
		}
		this.setBackgroundColor(bgColor);
	}

	public DoIndexListViewSideBar(Context context, do_IndexListView_View _indexListView, List<String> datas, int fontSize, int itemHeight, int itemWidth) {
		super(context);
		this.fontSize = fontSize;
		this.m_nItemHeight = itemHeight;
		this.m_nItmeWidth = itemWidth;
		this.indexDatas = datas;
		if (_indexListView != null) {
			_indexListView.setOnGroupChangeListener(this);
		}
		this.setBackgroundColor(bgColor);
	}

	public void bindData(List<String> datas) {
		this.indexDatas = datas;
	}

	public void setListView(ListView _list) {
		list = _list;
		sectionIndexter = (SectionIndexer) _list.getAdapter();
	}

	public void setTextView(TextView mDialogText) {
		this.mDialogText = mDialogText;
	}

	int marginTop;

	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		isFlag = true;
		int _y = (int) event.getY();
		int idx;
		if (_y < marginTop) {
			idx = 0;
		} else if (_y > marginTop + indexDatas.size() * m_nItemHeight) {
			idx = indexDatas.size() - 1;
		} else {
			idx = (_y - marginTop) / m_nItemHeight;
			if (idx >= indexDatas.size())
				idx = indexDatas.size() - 1;
		}
		pos = idx;
		// 没有数据
		if (idx < 0) {
			return true;
		}

		if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
			mDialogText.setVisibility(View.VISIBLE);
			mDialogText.setText("" + indexDatas.get(idx));
			if (sectionIndexter == null) {
				sectionIndexter = (SectionIndexer) list.getAdapter();
			}
			int position = sectionIndexter.getPositionForSection(idx);
//			if (position == -1) {
//				return true;
//			}

			list.setSelection(position);
			this.setBackgroundColor(pressBgColor);
			this.invalidate();
		} else {
			this.setBackgroundColor(bgColor);
			mDialogText.setVisibility(View.INVISIBLE);
		}
		return true;
	}

	@SuppressLint("DrawAllocation")
	protected void onDraw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setColor(textColor);
		paint.setTextSize(fontSize);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setAntiAlias(true);
		float widthCenter = getMeasuredWidth() / 2;
		marginTop = (this.getHeight() - m_nItemHeight * indexDatas.size()) / 2;
		if (marginTop <= 0)
			marginTop = 0;
		for (int i = 0; i < indexDatas.size(); i++) {
			if (pos == i) {
				paint.setColor(lumpColor);
				canvas.drawRect(0, marginTop + (i * m_nItemHeight) + (m_nItemHeight / 3), m_nItmeWidth, marginTop + ((i + 1) * m_nItemHeight) + (m_nItemHeight / 4), paint);// 长方形
			}
			paint.setColor(textColor);
			canvas.drawText(indexDatas.get(i), widthCenter, marginTop + m_nItemHeight + (i * m_nItemHeight), paint);
		}
		super.onDraw(canvas);
	}

	@Override
	public void groupChange(int groupId) {
		if (isFlag) {
			isFlag = false;
			return;
		}
		pos = groupId;
		invalidate();
	}
}