package ru.krasview.kvlib.widget.lists;

import java.util.HashMap;
import java.util.Map;

import ru.krasview.kvlib.indep.consts.TypeConsts;
import ru.krasview.kvlib.widget.List;

import android.content.Context;

public class MainList extends List {
	public MainList(Context context) {
		super(context, null);
	}

	@Override
	public void setConstData() {
		Map<String, Object> m;
		if(account.isKrasviewAccount()) {
			m = new HashMap<String, Object>();
			m.put("type", "my_shows_all");
			m.put("name", "Я смотрю");
			data.add(m);
		}

		m = new HashMap<String, Object>();
		m.put("type", "faves");
		m.put("section", "series");
		m.put("name", "Избранные сериалы");
		data.add(m);

		m = new HashMap<String, Object>();
		m.put("type", "faves");
		m.put("section", "movie");
		m.put("name", "Избранные фильмы");
		data.add(m);

		m = new HashMap<String, Object>();
		m.put("type", TypeConsts.MOVIE);
		m.put("name", "Фильмы");
		data.add(m);

		m = new HashMap<String, Object>();
		m.put("type", TypeConsts.ALL_SHOW);
		m.put("name", "Сериалы");
		data.add(m);
	}
}
