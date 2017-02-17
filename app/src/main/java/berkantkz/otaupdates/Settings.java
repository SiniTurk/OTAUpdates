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

package berkantkz.otaupdates;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.AlertDialog;

import eu.chainfire.libsuperuser.Shell;

public class Settings extends PreferenceActivity {

    public static Activity SettingsActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        SettingsActivity = Settings.this;

        CheckBoxPreference enable_auto_install = (CheckBoxPreference) findPreference("enable_auto_install");
        if (Shell.SU.available() == false) {
            enable_auto_install.setEnabled(false);
            enable_auto_install.setSummary("Only rooted/root granted devices are supported");
        }

        CheckBoxPreference setEnglish = (CheckBoxPreference) findPreference("force_english");
        setEnglish.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                force_english_dialog();
                return true;
            }
        });

    }

    public void force_english_dialog() {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Force English");
                builder.setMessage("Relaunch is required to take affect, you can relaunch now or later. Would you like to restart now?");
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
            }

}
