package ru.krasview.kvlib.indep;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import ru.krasview.kvlib.indep.consts.AuthRequestConst;
import ru.krasview.kvlib.interfaces.FatalErrorExitListener;
import ru.krasview.secret.ApiConst;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class HTTPClient extends KVHttpClient {
	static Context mContext = null;
	static FatalErrorExitListener exitListener = null;

	public static void setContext(Context context) {
		mContext = context;
	}

	public static void setExitListener(FatalErrorExitListener l) {
		exitListener = l;
	}

	public static String getXML(String address, String params, int request_auth_type) {
		address = addParams(address, params);
		String auth_address = address;
		AuthAccount account = AuthAccount.getInstance();
		switch(request_auth_type) {
		case AuthRequestConst.AUTH_NONE:
			auth_address = address;
			break;
		case AuthRequestConst.AUTH_KRASVIEW:
			if(!account.isKrasviewAccount()) {
				return "";
			}
			auth_address = address + "hash=" + account.getHash();
			break;
		case AuthRequestConst.AUTH_TV:
			if(!account.isTVAccount()) {
				return null;
			} else if(account.isSocialNetworkAccount()) {
				auth_address = address + "hash=" + account.getTvHash();
			} else {
				try {
					auth_address = address
						+ "login=" + URLEncoder.encode(account.getLogin(), StandardCharsets.UTF_8.toString())
						+ "&password=" + URLEncoder.encode(account.getPassword(), StandardCharsets.UTF_8.toString());
				} catch (UnsupportedEncodingException e) {
					return null;
				}
			}
			break;
			default:
				throw new IllegalStateException("Unexpected value: " + request_auth_type);
		}
		String result = getXML(auth_address);
		if(result.equals("wrong hash")) {
			if(account.isSocialNetworkAccount()) {
				exitFromApplication();
				return "";
			}
			try {
				String hash = getXML(ApiConst.KRASVIEW_AUTH, "login="
			                     + URLEncoder.encode(account.getLogin(), StandardCharsets.UTF_8.toString())
			                     + "&password=" + URLEncoder.encode(account.getPassword(), StandardCharsets.UTF_8.toString()));
				account.setHash(hash);
				if(account.getHash().equals("error")) {
					exitFromApplication();
					return "";
				}
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(mContext);
				prefs.edit().putString("pref_hash", account.getHash()).commit();
				auth_address = address + "hash=" + account.getHash();
				result = getXML(auth_address);
			} catch (UnsupportedEncodingException e) {
				return null;
			}
		}
		return result;
	}

	private static void exitFromApplication() {
		if(exitListener != null) {
			exitListener.onError();
		}
	}
}
