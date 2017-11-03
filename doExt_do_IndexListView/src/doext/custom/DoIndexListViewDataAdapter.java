package doext.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import core.DoServiceContainer;
import core.helper.DoJsonHelper;
import core.helper.DoTextHelper;
import core.interfaces.DoIUIModuleView;
import core.object.DoSourceFile;
import core.object.DoUIContainer;
import core.object.DoUIModule;

public class DoIndexListViewDataAdapter extends BaseAdapter implements SectionIndexer {

	private DoUIModule model;
	private Context ctx;

	public DoIndexListViewDataAdapter(DoUIModule _module, Context context) {
		this.model = _module;
		this.ctx = context;
	}

	private Map<String, String> viewTemplates = new HashMap<String, String>();
	private List<String> cellTemplates = new ArrayList<String>();
	private SparseIntArray datasPositionMap = new SparseIntArray();
	private List<DoItemData> itemData;

	public void bindData(List<DoItemData> _itemData) {
		this.itemData = _itemData;
		notifyDataSetChanged();
	}

	public void initTemplates(String[] templates) throws Exception {
		cellTemplates.clear();
		for (String templateUi : templates) {
			if (templateUi != null && !templateUi.equals("")) {
				DoSourceFile _sourceFile = model.getCurrentPage().getCurrentApp().getSourceFS().getSourceByFileName(templateUi);
				if (_sourceFile != null) {
					viewTemplates.put(templateUi, _sourceFile.getTxtContent());
					cellTemplates.add(templateUi);
				} else {
					throw new Exception("试图使用一个无效的页面文件:" + templateUi);
				}
			}
		}
	}

	@Override
	public void notifyDataSetChanged() {
		int _size = itemData.size();
		for (int i = 0; i < _size; i++) {
			try {
				JSONObject childData = itemData.get(i).getData();
				Integer _index = DoTextHelper.strToInt(DoJsonHelper.getString(childData, "template", "0"), 0);
				if (_index >= cellTemplates.size() || _index < 0) {
					DoServiceContainer.getLogEngine().writeError("索引不存在", new Exception("索引 " + _index + " 不存在"));
					_index = 0;
				}
				datasPositionMap.put(i, _index);
			} catch (Exception e) {
				DoServiceContainer.getLogEngine().writeError("解析data数据错误： \t", e);
			}
		}
		super.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (itemData == null) {
			return 0;
		}
		return itemData.size();
	}

	@Override
	public Object getItem(int position) {
		return itemData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		return datasPositionMap.get(position);
	}

	@Override
	public int getViewTypeCount() {
		return cellTemplates.size();
	}

	@Override
	public boolean isEnabled(int position) {
		return !itemData.get(position).isGroup();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View _childView = null;
		try {

			DoItemData myItemData = itemData.get(position);
			JSONObject childData = myItemData.getData();
			DoIUIModuleView _doIUIModuleView = null;
			int _index = DoTextHelper.strToInt(DoJsonHelper.getString(childData, "template", "0"), 0);
			if (_index >= cellTemplates.size() || _index < 0) {
				DoServiceContainer.getLogEngine().writeError("索引不存在", new Exception("索引 " + _index + " 不存在"));
				_index = 0;
			}
			String templateUI = cellTemplates.get(_index);
			if (convertView == null) {
				String content = viewTemplates.get(templateUI);
				DoUIContainer _doUIContainer = new DoUIContainer(model.getCurrentPage());
				_doUIContainer.loadFromContent(content, null, null);
				_doUIContainer.loadDefalutScriptFile(templateUI);// @zhuozy效率问题，listview第一屏可能要加载多次模版、脚本，需改进需求设计；
				_doIUIModuleView = _doUIContainer.getRootView().getCurrentUIModuleView();
			} else {
				_doIUIModuleView = (DoIUIModuleView) convertView;
			}
			if (_doIUIModuleView != null) {
				_doIUIModuleView.getModel().setModelData(childData);

				_childView = (View) _doIUIModuleView;
				// 设置headerView 的 宽高
				_childView.setLayoutParams(new AbsListView.LayoutParams((int) _doIUIModuleView.getModel().getRealWidth(), (int) _doIUIModuleView.getModel().getRealHeight()));
				if (_childView instanceof ViewGroup) {
					((ViewGroup) _childView).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
				}
			}
		} catch (Exception e) {
			DoServiceContainer.getLogEngine().writeError("解析data数据错误： \t", e);
		}
		if (_childView == null) {
			return new View(ctx);
		}
		return _childView;
	}

	@Override
	public int getPositionForSection(int section) {
		for (int i = 0; i < itemData.size(); i++) {
			if (section == itemData.get(i).getGroupId()) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public int getSectionForPosition(int position) {
		return 0;
	}

	@Override
	public Object[] getSections() {
		return null;
	}
}