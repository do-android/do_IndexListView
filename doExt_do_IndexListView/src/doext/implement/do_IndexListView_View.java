package doext.implement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import core.DoServiceContainer;
import core.helper.DoJsonHelper;
import core.helper.DoScriptEngineHelper;
import core.helper.DoUIModuleHelper;
import core.interfaces.DoIHashData;
import core.interfaces.DoIScriptEngine;
import core.interfaces.DoIUIModuleView;
import core.object.DoInvokeResult;
import core.object.DoMultitonModule;
import core.object.DoUIModule;
import doext.custom.DoIndexListViewDataAdapter;
import doext.custom.DoIndexListViewSideBar;
import doext.custom.DoItemData;
import doext.define.do_IndexListView_IMethod;
import doext.define.do_IndexListView_MAbstract;

/**
 * 自定义扩展UIView组件实现类，此类必须继承相应VIEW类，并实现DoIUIModuleView,do_IndexListView_IMethod接口；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.model.getUniqueKey());
 */
public class do_IndexListView_View extends FrameLayout implements DoIUIModuleView, do_IndexListView_IMethod, OnItemClickListener, OnItemLongClickListener, OnScrollListener {

	/**
	 * 每个UIview都会引用一个具体的model实例；
	 */
	private do_IndexListView_MAbstract model;

	private ListView mListview;
	private DoIndexListViewDataAdapter mAdapter;
	private List<DoItemData> itemData;

	private DoIndexListViewSideBar mSideBar;
	private List<String> indexItemData;

	private Context ctx;

	public do_IndexListView_View(Context context) {
		super(context);
		this.ctx = context;
		this.indexItemData = new ArrayList<String>();
		this.itemData = new ArrayList<DoItemData>();
//		this.setOrientation(LinearLayout.HORIZONTAL);
	}

	/**
	 * 初始化加载view准备,_doUIModule是对应当前UIView的model实例
	 */
	@Override
	public void loadView(DoUIModule _doUIModule) throws Exception {
		this.model = (do_IndexListView_MAbstract) _doUIModule;
		mAdapter = new DoIndexListViewDataAdapter(_doUIModule, ctx);

		mListview = new ListView(ctx);
		mListview.setVerticalScrollBarEnabled(false);
		mListview.setOnScrollListener(this);
		mListview.setDivider(new ColorDrawable(Color.TRANSPARENT));
		mListview.setDividerHeight(0);
		LinearLayout.LayoutParams mListviewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		mListviewLayoutParams.weight = 4;
		this.addView(mListview, mListviewLayoutParams);

        mSideBar = new DoIndexListViewSideBar(ctx, this, indexItemData, dip2px(11), dip2px(16), dip2px(24));
		FrameLayout.LayoutParams mSideBarLayoutParams = new FrameLayout.LayoutParams(dip2px(24), LinearLayout.LayoutParams.MATCH_PARENT);
		mSideBarLayoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
		this.addView(mSideBar, mSideBarLayoutParams);

		TextView mDialogText = new TextView(ctx);
		int size = dip2px(60);
		mDialogText.setHeight(size);
		mDialogText.setWidth(size);
		mDialogText.setGravity(Gravity.CENTER);
		mDialogText.setBackgroundDrawable(createGradientDrawable(Color.parseColor("#90000000"), 20));
		mDialogText.setTextColor(Color.WHITE);
		mDialogText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
		mDialogText.setVisibility(View.INVISIBLE);
		WindowManager mWindowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION,
				WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
		mWindowManager.addView(mDialogText, lp);
		mSideBar.setTextView(mDialogText);
		mSideBar.setListView(mListview);

		mListview.setOnItemClickListener(this);
		mListview.setOnItemLongClickListener(this);

	}

	private GradientDrawable createGradientDrawable(int _fillColor, int _roundRadius) {
		GradientDrawable _drawable = new GradientDrawable();
		_drawable.setColor(_fillColor);
		_drawable.setCornerRadius(_roundRadius);
		return _drawable;
	}

	/**
	 * 动态修改属性值时会被调用，方法返回值为true表示赋值有效，并执行onPropertiesChanged，否则不进行赋值；
	 * 
	 * @_changedValues<key,value>属性集（key名称、value值）；
	 */
	@Override
	public boolean onPropertiesChanging(Map<String, String> _changedValues) {
		if (_changedValues.containsKey("templates")) {
			String value = _changedValues.get("templates");
			if ("".equals(value)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 属性赋值成功后被调用，可以根据组件定义相关属性值修改UIView可视化操作；
	 * 
	 * @_changedValues<key,value>属性集（key名称、value值）；
	 */
	@Override
	public void onPropertiesChanged(Map<String, String> _changedValues) {
		DoUIModuleHelper.handleBasicViewProperChanged(this.model, _changedValues);

		if (_changedValues.containsKey("selectedColor")) {
			try {
				String _bgColor = this.model.getPropertyValue("bgColor");
				String _selectedColor = _changedValues.get("selectedColor");
				Drawable normal = new ColorDrawable(DoUIModuleHelper.getColorFromString(_bgColor, Color.WHITE));
				Drawable selected = new ColorDrawable(DoUIModuleHelper.getColorFromString(_selectedColor, Color.WHITE));
				Drawable pressed = new ColorDrawable(DoUIModuleHelper.getColorFromString(_selectedColor, Color.WHITE));
				mListview.setSelector(getBg(normal, selected, pressed));
			} catch (Exception _err) {
				DoServiceContainer.getLogEngine().writeError("do_ListView selectedColor \n\t", _err);
			}
		}

		if (_changedValues.containsKey("indexBarColors")) {
			mSideBar.setColor(_changedValues.get("indexBarColors"));
		}
		
		if (_changedValues.containsKey("templates")) {
			initViewTemplate(_changedValues.get("templates"));
			mListview.setAdapter(mAdapter);
		}
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if ("bindItems".equals(_methodName)) {
			bindItems(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("refreshItems".equals(_methodName)) {
			refreshItems(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		return false;
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.model.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) {
		return false;
	}

	/**
	 * 释放资源处理，前端JS脚本调用closePage或执行removeui时会被调用；
	 */
	@Override
	public void onDispose() {
		// ...do something
	}

	/**
	 * 重绘组件，构造组件时由系统框架自动调用；
	 * 或者由前端JS脚本调用组件onRedraw方法时被调用（注：通常是需要动态改变组件（X、Y、Width、Height）属性时手动调用）
	 */
	@Override
	public void onRedraw() {
		this.setLayoutParams(DoUIModuleHelper.getLayoutParams(this.model));
	}

	/**
	 * 获取当前model实例
	 */
	@Override
	public DoUIModule getModel() {
		return model;
	}

	/**
	 * 绑定item的数据；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void bindItems(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		String _address = DoJsonHelper.getString(_dictParas, "data", "");
		JSONArray _indexs = DoJsonHelper.getJSONArray(_dictParas, "indexs");
		if (_address == null || _address.length() <= 0)
			throw new Exception("doListView 未指定相关的listview data参数！");
		DoMultitonModule _multitonModule = DoScriptEngineHelper.parseMultitonModule(_scriptEngine, _address);
		if (_multitonModule == null)
			throw new Exception("doListView data参数无效！");
		if (_multitonModule instanceof DoIHashData) {
			DoIHashData _data = (DoIHashData) _multitonModule;
			parseJsonData(_data, _indexs);
			mAdapter.bindData(itemData);
			mSideBar.bindData(indexItemData);
		}
	}

	// 解析Json数据
	private void parseJsonData(DoIHashData _data, JSONArray _indexs) throws Exception {
		indexItemData.clear();
		itemData.clear();
		if (_indexs != null && _indexs.length() > 0) {
			for (int i = 0; i < _indexs.length(); i++) {
				indexItemData.add(_indexs.getString(i));
			}
		} else {
			indexItemData.addAll(_data.getAllKey());
		}

		for (int i = 0; i < indexItemData.size(); i++) {
			String _key = indexItemData.get(i);
			Object _obj = _data.getData(_key);
			if (_obj == null) {
				continue;
			}
			try {
				JSONArray _array = new JSONArray(_obj.toString());
				for (int j = 0; j < _array.length(); j++) {
					DoItemData _itemData = new DoItemData();
					if (j == 0) {
						_itemData.setFrist(true);
						_itemData.setGroup(true);
					}

					if (j == _array.length() - 1) {
						_itemData.setEnd(true);
					}
					_itemData.setGroupId(i);
					_itemData.setData(_array.getJSONObject(j));
					_itemData.setGroupName(_key);
					_itemData.setGroupPosition(j);
					itemData.add(_itemData);
				}
			} catch (Exception e) {
				DoServiceContainer.getLogEngine().writeError("data数据格式不正确！", e);
			}
		}
	}

	/**
	 * 刷新item数据；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void refreshItems(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		mAdapter.notifyDataSetChanged();
		mSideBar.invalidate();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		DoItemData _itemData = itemData.get(position);
		if (_itemData != null) {
			try {
				doListView_LongTouch(_itemData.getGroupName(), _itemData.getGroupPosition());
			} catch (JSONException e) {
				DoServiceContainer.getLogEngine().writeError("longTouch", e);
			}
		}
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		DoItemData _itemData = itemData.get(position);
		if (_itemData != null) {
			try {
				doListView_Touch(_itemData.getGroupName(), _itemData.getGroupPosition());
			} catch (JSONException e) {
				DoServiceContainer.getLogEngine().writeError("touch", e);
			}
		}
	}

	private void doListView_Touch(String groupName, int groupPos) throws JSONException {
		DoInvokeResult _invokeResult = new DoInvokeResult(this.model.getUniqueKey());
		JSONObject _obj = new JSONObject();
		_obj.put("groupID", groupName);
		_obj.put("index", groupPos);
		_invokeResult.setResultNode(_obj);
		this.model.getEventCenter().fireEvent("touch", _invokeResult);
	}

	private void doListView_LongTouch(String groupName, int groupPos) throws JSONException {
		DoInvokeResult _invokeResult = new DoInvokeResult(this.model.getUniqueKey());
		JSONObject _obj = new JSONObject();
		_obj.put("groupID", groupName);
		_obj.put("index", groupPos);
		_invokeResult.setResultNode(_obj);
		this.model.getEventCenter().fireEvent("longTouch", _invokeResult);
	}

	private StateListDrawable getBg(Drawable normal, Drawable selected, Drawable pressed) {
		StateListDrawable bg = new StateListDrawable();
		bg.addState(View.PRESSED_ENABLED_STATE_SET, pressed);
		bg.addState(View.ENABLED_FOCUSED_STATE_SET, selected);
		bg.addState(View.ENABLED_STATE_SET, normal);
		bg.addState(View.FOCUSED_STATE_SET, selected);
		bg.addState(View.EMPTY_STATE_SET, normal);
		return bg;
	}

	private void initViewTemplate(String data) {
		try {
			mAdapter.initTemplates(data.split(","));
		} catch (Exception e) {
			DoServiceContainer.getLogEngine().writeError("解析cell属性错误： \t", e);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	private int lastGroupId;

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (listener != null) {
			if (itemData != null && itemData.size() > 0) {
				DoItemData myItemData = itemData.get(firstVisibleItem);
				int currentGroupId = myItemData.getGroupId();
				if (currentGroupId != lastGroupId) {
					listener.groupChange(currentGroupId);
					lastGroupId = currentGroupId;
				}
			}
		}
	}

	private onGroupChangeListener listener;

	public void setOnGroupChangeListener(onGroupChangeListener ls) {
		this.listener = ls;
	}

	public interface onGroupChangeListener {
		public void groupChange(int groupId);
	}

	public int dip2px(float dipValue) {
		final float scale = ctx.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}
}