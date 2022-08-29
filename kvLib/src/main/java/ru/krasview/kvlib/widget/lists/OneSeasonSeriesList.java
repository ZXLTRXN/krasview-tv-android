package ru.krasview.kvlib.widget.lists;

import java.util.Map;



import android.content.Context;

import ru.krasview.kvlib.ApiConst;

public class OneSeasonSeriesList extends AllSeriesList {
	public OneSeasonSeriesList(Context context, Map<String, Object> map) {
		super(context, map);
	}

	@Override
	protected String getApiAddress() {
		return ApiConst.SEASON + "?id=" + getMap().get("id");
	}
}
