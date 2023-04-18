package com.bbk.catchme.tools;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;

import com.badlogic.androidgames.framework.impl.AndroidGame;

public class Settings {
	private static SharedPreferences prefs;
	private static String prefName = "CatchMeGame";

	public static boolean soundEnabled = true;

	public static void loadPrefs(AndroidGame game) {
		prefs = game.getSharedPreferences(prefName, MODE_PRIVATE);

		soundEnabled = prefs.getBoolean("soundEnabled", true);
	}
	
	public static void savePrefs(AndroidGame game) {
		prefs = game.getSharedPreferences(prefName, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();

		editor.putBoolean("soundEnabled", soundEnabled);

		editor.apply();
	}
}