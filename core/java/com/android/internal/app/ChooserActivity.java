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

    IntentLearner intentdb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Log.i("HACKATHON", "Creating IntentLearner Object " );
        intentdb = new IntentLearner(this);

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
        String mimeType = mSourceIntent.getType();
        String chosenPackage = intent.getComponent().getPackageName();
        Log.i("HACKATHON", "updating stats for " + mimeType + " choosing " + chosenPackage);
        if(chosenPackage != null) {

            Log.i("HACKATHON", "Updating App usage for "+ mimeType + " and " + chosenPackage + " in IntentDB " );
            intentdb.UpdateAppUsage(chosenPackage, mimeType, null);

        }
        
        // NOTE: make sure this call to super is here, or the ChooserActivity will stop working! 
        super.onIntentSelected(ri, intent, alwaysCheck);
    }

    protected List<String> getPopularPackages(String mimeType) {
        List<String> l;

        Log.i("HACKATHON", "Get List of Apps for "+ mimeType +" from IntentDB " );
        l = intentdb.GetListofApps(mimeType);
        if (l.size() > 0) {
            Log.i("HACKATHON", "Top App from intentdb is " + l.get(0));
        } else {
            Log.i("HACKATHON", "No top apps");
        }

        if (l.isEmpty())
        {
            return Collections.emptyList();
        }
        else
        {
            return l;
        }
    }

    @Override
    protected void onResume() {
        hideSystemUI();
        super.onResume();
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
