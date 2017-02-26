/**
 * Project: OTAUpdates
 *
 * @author berkantkz, TimSchumi
 * License: GNU General Public License, Version 3
 * <p>
 * Copyright 2017 Berkant Korkmaz, Tim Schumacher
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ota.otaupdates;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.AlertDialog;

import eu.chainfire.libsuperuser.Shell;

public class Settings extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        if (MainActivity.sharedPreferences.getBoolean("apptheme_light", false))
            setTheme(R.style.AppTheme_Light);
        else
            setTheme(R.style.AppTheme_Dark);

        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        SwitchPreference enable_auto_install = (SwitchPreference) findPreference("enable_auto_install");
        if (!Shell.SU.available()) {
            enable_auto_install.setEnabled(false);
            enable_auto_install.setChecked(false);
            enable_auto_install.setSummary(getString(R.string.auto_install_root_only));
        }

        SwitchPreference setEnglish = (SwitchPreference) findPreference("force_english");
        setEnglish.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);

                if (MainActivity.sharedPreferences.getBoolean("force_english", false))
                    builder.setTitle(getString(R.string.force_english_window_title));
                else
                    builder.setTitle(getString(R.string.force_default_window_title));

                builder.setMessage(getString(R.string.force_english_window_message));
                builder.setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                });
                builder.setNegativeButton(getString(R.string.button_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog alert = builder.create();
                alert.setCancelable(true);
                alert.show();
                return true;
            }
        });

        SwitchPreference apptheme_light = (SwitchPreference) findPreference("apptheme_light");
        apptheme_light.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
                if (MainActivity.sharedPreferences.getBoolean("apptheme_light", false))
                    setTheme(R.style.AppTheme_Light);
                else
                    setTheme(R.style.AppTheme_Dark);
                builder.setMessage(getString(R.string.switch_apptheme_light_window_message));
                builder.setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                });
                builder.setNegativeButton(getString(R.string.button_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog alert = builder.create();
                alert.setCancelable(true);
                alert.show();
                return true;
            }
        });

    }
}
