package lrstudios.games.ego.lib.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import lrstudios.games.ego.lib.R;


public class Preferences extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
