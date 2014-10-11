/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.content.pm.ResolveInfo;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class ChooserActivity extends ResolverActivity {

    private static final String GPLUS_PROP = "persist.smart_chooser_gplus";
    private static final String GPLUS_PKG = "com.google.android.apps.plus";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent intent = getIntent();
        Parcelable targetParcelable = intent.getParcelableExtra(Intent.EXTRA_INTENT);
        if (!(targetParcelable instanceof Intent)) {
            Log.w("ChooseActivity", "Target is not an intent: " + targetParcelable);
            finish();
            return;
        }
        Intent target = (Intent)targetParcelable;
        CharSequence title = intent.getCharSequenceExtra(Intent.EXTRA_TITLE);
        if (title == null) {
            title = getResources().getText(com.android.internal.R.string.chooseActivity);
        }
        Parcelable[] pa = intent.getParcelableArrayExtra(Intent.EXTRA_INITIAL_INTENTS);
        Intent[] initialIntents = null;
        if (pa != null) {
            initialIntents = new Intent[pa.length];
            for (int i=0; i<pa.length; i++) {
                if (!(pa[i] instanceof Intent)) {
                    Log.w("ChooseActivity", "Initial intent #" + i
                            + " not an Intent: " + pa[i]);
                    finish();
                    return;
                }
                initialIntents[i] = (Intent)pa[i];
            }
        }
        super.onCreate(savedInstanceState, target, title, initialIntents, null, false);
    }

    protected void onIntentSelected(ResolveInfo ri, Intent intent, boolean alwaysCheck) {
        String mimeType = getIntent().getType();
        String chosenPackage = intent.getComponent().getPackageName();
        Log.i("HACKATHON", "updating stats for " + mimeType + " choosing " + chosenPackage);
        if(chosenPackage != null) {
            //updateState(mimeType, chosePackage);
            if (chosenPackage.equals(GPLUS_PKG)) {
                String key = GPLUS_PROP;
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                int curValue = prefs.getInt(key, 0);
                Log.i("HACKATHON", "putting gplus value " + (curValue+1));
                prefs.edit().putInt(key, curValue+1).commit();
            }
        }
        
        // NOTE: make sure this call to super is here, or the ChooserActivity will stop working! 
        super.onIntentSelected(ri, intent, alwaysCheck);
    }

    protected List<String> getPopularPackages(String mimeType) {
        String key = GPLUS_PROP;
        int gplusVal = PreferenceManager.getDefaultSharedPreferences(this).getInt(GPLUS_PROP, 0);
        Log.i("HACKATHON", "got gplus value " + gplusVal);
        if (gplusVal > 0) {
            List<String> l = new ArrayList<String>();
            l.add(GPLUS_PKG);
            return l;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
}
