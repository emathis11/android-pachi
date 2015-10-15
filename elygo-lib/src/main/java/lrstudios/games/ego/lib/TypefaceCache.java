package lrstudios.games.ego.lib;

import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.util.Hashtable;


public class TypefaceCache {
    private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

    public static Typeface get(AssetManager assetMgr, String assetPath) {
        synchronized (cache) {
            if (!cache.containsKey(assetPath)) {
                Typeface typeface = Typeface.createFromAsset(assetMgr, assetPath);
                cache.put(assetPath, typeface);
            }
            return cache.get(assetPath);
        }
    }
}