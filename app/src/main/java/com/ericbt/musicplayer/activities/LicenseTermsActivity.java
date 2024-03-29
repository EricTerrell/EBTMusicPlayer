/*
  EBT Music Player
  (C) Copyright 2022, Eric Bergman-Terrell

  This file is part of EBT Music Player.

    EBT Music Player is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    EBT Music Player is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with EBT Music Player.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.ericbt.musicplayer.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RadioButton;

import com.ericbt.musicplayer.Preferences;
import com.ericbt.musicplayer.R;
import com.ericbt.musicplayer.StringLiterals;
import com.ericbt.musicplayer.utils.DebugUtils;

public class LicenseTermsActivity extends Activity {
    private boolean allowCancel;

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DebugUtils.enableStrictMode(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_license_terms);

        allowCancel = getIntent().getExtras().getBoolean(StringLiterals.ALLOW_CANCEL);

        if (allowCancel) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        final RadioButton acceptLicenseTerms = findViewById(R.id.AcceptLicenseTerms);
        acceptLicenseTerms.setChecked(Preferences.userAcceptedTerms(this));

        final RadioButton rejectLicenseTerms = findViewById(R.id.RejectLicenseTerms);
        rejectLicenseTerms.setChecked(!Preferences.userAcceptedTerms(this));

        Button okButton = findViewById(R.id.OKButton);

        okButton.setOnClickListener(v -> {
            final boolean userAcceptedTerms = acceptLicenseTerms.isChecked();

            Preferences.putUserAcceptedTerms(LicenseTermsActivity.this, userAcceptedTerms);

            if (!userAcceptedTerms) {
                AlertDialog.Builder userRejectedTermsDialogBuilder = new AlertDialog.Builder(LicenseTermsActivity.this);
                userRejectedTermsDialogBuilder.setTitle(String.format("Rejected %s License Terms", getString(R.string.app_name)));
                userRejectedTermsDialogBuilder.setMessage(String.format("You rejected the %s license terms. Please uninstall %s immediately.", getString(R.string.app_name), getString(R.string.app_name)));
                userRejectedTermsDialogBuilder.setPositiveButton("OK", (dialog, which) -> {
                    alertDialog.dismiss();

                    finish();

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(StringLiterals.EXIT, true);
                    startActivity(intent);
                });

                userRejectedTermsDialogBuilder.setCancelable(false);

                alertDialog = userRejectedTermsDialogBuilder.create();
                alertDialog.show();
            }
            else {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (allowCancel) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = false;

        if (item.getItemId() == android.R.id.home) {
            finish();

            result = true;
        }

        return result;
    }
}
