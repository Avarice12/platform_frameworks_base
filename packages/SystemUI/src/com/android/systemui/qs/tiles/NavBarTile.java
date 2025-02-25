/*
 * Copyright (C) 2015 The Dirty Unicorns Project
 * Copyright (C) 2018 Benzo Rom
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

package com.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.view.View;

import androidx.annotation.Nullable;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dagger.qualifiers.Background;
import com.android.systemui.dagger.qualifiers.Main;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.plugins.qs.QSTile.State;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.qs.logging.QSLogger;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.KeyguardStateController;;

import javax.inject.Inject;

/** Quick settings tile: NavBarTile **/
public class NavBarTile extends QSTileImpl<BooleanState> {


    @Inject
    public NavBarTile(QSHost host,
            @Background Looper backgroundLooper,
            @Main Handler mainHandler,
            FalsingManager falsingManager,
            MetricsLogger metricsLogger,
            StatusBarStateController statusBarStateController,
            ActivityStarter activityStarter,
            QSLogger qsLogger,
            BroadcastDispatcher broadcastDispatcher,
            KeyguardStateController keyguardStateController
        ) {
        super(host, backgroundLooper, mainHandler, falsingManager, metricsLogger,
                statusBarStateController, activityStarter, qsLogger);
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleClick(@Nullable View view) {
        setEnabled(!mState.value);
        refreshState();
    }

    @Override
    public Intent getLongClickIntent() {
        return new Intent().setComponent(new ComponentName(
            "com.android.settings", "com.android.settings.Settings$NavigationSettingsActivity"));
    }

    private void setEnabled(boolean enabled) {
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.FORCE_SHOW_NAVBAR,
                enabled ? 1 : 0);
    }

   private boolean isNavbarEnabled() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.FORCE_SHOW_NAVBAR, 1) == 1;
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        if (state.slash == null) {
            state.slash = new SlashState();
        }
        state.value = isNavbarEnabled();
        state.label = mContext.getString(R.string.quick_settings_navbar_title);
        state.icon = ResourceIcon.get(R.drawable.ic_qs_navbar);
        if (isNavbarEnabled()) {
            state.state = Tile.STATE_ACTIVE;
        } else {
            state.state = Tile.STATE_INACTIVE;
        }
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_navbar_title);
    }

    @Override
    protected String composeChangeAnnouncement() {
        if (mState.value) {
            return mContext.getString(R.string.accessibility_quick_settings_navbar_changed_on);
        } else {
            return mContext.getString(R.string.accessibility_quick_settings_navbar_changed_off);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.SYBERIA;
    }

    @Override
    public void handleSetListening(boolean listening) {
        // Do nothing
    }
}
