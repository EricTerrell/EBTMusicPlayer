/*
  EBT Music Player
  (C) Copyright 2021, Eric Bergman-Terrell

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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.ericbt.musicplayer.R;
import com.ericbt.musicplayer.utils.DebugUtils;
import com.ericbt.musicplayer.utils.Logger;

public class UpgradeActivity extends Activity {
    private Logger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger = new Logger(this);

        super.onCreate(savedInstanceState);

        DebugUtils.enableStrictMode(this);

        setContentView(R.layout.activity_upgrade);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        final Button upgrade = (Button) findViewById(R.id.upgrade);

        upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String paidVersionURL = getResources().getString(R.string.paid_version_url);
                logger.log(String.format("paidVersionURL: %s", paidVersionURL));

                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(paidVersionURL));
                startActivity(intent);
            }
        });
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
