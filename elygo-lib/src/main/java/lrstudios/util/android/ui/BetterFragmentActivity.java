/*
 * This file is part of Elygo-lib.
 * Copyright (C) 2012   Emmanuel Mathis [emmanuel *at* lr-studios.net]
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lrstudios.util.android.ui;

import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import lrstudios.util.android.AndroidUtils;


public class BetterFragmentActivity extends AppCompatActivity {
    private static final String TAG = BetterFragmentActivity.class.getSimpleName();

    private Menu _optionsMenu;


    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        _optionsMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            finish();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    /**
     * Returns the {@link Menu} containing the current items of the Action Bar.
     */
    protected Menu getOptionsMenu() {
        return _optionsMenu;
    }

    protected void showInfoDialog(String message) {
        showInfoDialog(null, message);
    }

    protected void showInfoDialog(int messageId) {
        showInfoDialog(null, getString(messageId));
    }

    protected void showInfoDialog(int titleId, int messageId) {
        showInfoDialog(getString(titleId), getString(messageId));
    }

    protected void showInfoDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, AndroidUtils.getEmptyDialogOnClickListener())
                .show();
    }

    /**
     * Shortcut for Toast.makeText(...).
     */
    protected void showToast(int titleResId) {
        Toast.makeText(BetterFragmentActivity.this, titleResId, Toast.LENGTH_SHORT).show();
    }

    /**
     * Shortcut for Toast.makeText(...).
     */
    protected void showToast(String title) {
        Toast.makeText(BetterFragmentActivity.this, title, Toast.LENGTH_SHORT).show();
    }

    /**
     * Enables or disables the specified item of the Action Bar.
     * Return false if the item was not found.
     */
    protected boolean setOptionItemEnabled(int itemMenuId, boolean enabled) {
        if (_optionsMenu == null)
            return false;

        MenuItem item = _optionsMenu.findItem(itemMenuId);
        if (item == null)
            return false;

        item.setEnabled(enabled);
        return true;
    }

    protected boolean setOptionItemVisible(int itemMenuId, boolean visible) {
        if (_optionsMenu == null)
            return false;

        MenuItem item = _optionsMenu.findItem(itemMenuId);
        if (item == null)
            return false;

        item.setVisible(visible);
        return true;
    }

    /**
     * Disables the specified item of the Action Bar.
     * Return false if the item was not found.
     */
    protected boolean disableOptionItem(int itemMenuId) {
        return setOptionItemEnabled(itemMenuId, false);
    }

    /**
     * Enables the specified item of the Action Bar.
     * Return false if the item was not found.
     */
    protected boolean enableOptionItem(int itemMenuId) {
        return setOptionItemEnabled(itemMenuId, true);
    }

    /**
     * Removes the specified item from the Action Bar.
     */
    protected void removeOptionItem(int itemMenuId) {
        _optionsMenu.removeItem(itemMenuId);
    }

    protected void log(Object... args) {
        StringBuilder builder = new StringBuilder();
        for (Object arg : args)
            builder.append(arg.toString()).append(" ");
        builder.setLength(builder.length() - 1);
        Log.v(TAG, builder.toString());
    }
}
