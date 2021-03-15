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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ericbt.musicplayer.R;
import com.ericbt.musicplayer.utils.DebugUtils;

import java.util.Timer;
import java.util.TimerTask;

public class NagScreenActivity extends Activity {
    private static final String REMAINING_WAIT_TIME = "REMAINING_WAIT_TIME";

    private TextView seconds;

    private Handler handler = new Handler();

    private int remainingWaitTime;

    private Button play;

    final int totalWaitTime = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DebugUtils.enableStrictMode(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nag_screen);

        play = (Button) findViewById(R.id.play);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final Button upgrade = (Button) findViewById(R.id.upgrade);

        upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NagScreenActivity.this, UpgradeActivity.class));
            }
        });

        if (savedInstanceState != null) {
            remainingWaitTime = savedInstanceState.getInt(REMAINING_WAIT_TIME);
        } else {
            remainingWaitTime = totalWaitTime;
        }

        seconds = (TextView) findViewById(R.id.seconds);

        seconds.setText(String.valueOf(remainingWaitTime));

        createTimer();
    }

    private void createTimer() {
        final Timer timer = new Timer();

        if (remainingWaitTime > 0) {
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            remainingWaitTime--;

                            seconds.setText(String.valueOf(remainingWaitTime));

                            if (remainingWaitTime == 0) {
                                timer.cancel();

                                play.setEnabled(true);
                            }
                        }
                    });
                }
            }, 1000, 1000);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(REMAINING_WAIT_TIME, remainingWaitTime);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

}
