package ru.krasview.kvlib.widget.lists;

import java.util.Map;


import android.content.Context;

import ru.krasview.kvlib.ApiConst;

public class LetterShowList extends AllShowList{
	//не убирать
	@Override
	public void setConstData(){
	}

	public LetterShowList(Context context, Map<String, Object> map) {
		super(context, map);
	}

	@Override
	protected String getApiAddress() {
		return ApiConst.LETTER_SHOW + getMap().get("name");
	}
}
