/*
 * Copyright (c) 2015. 2Lines Software,Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.twolinessoftware.events;

import android.content.Intent;

/**
 * Created by John on 2015-04-02.
 */
public class OnAccountLoggedInEvent {
  ;
    private final Intent m_intent;

    private boolean m_newAccount;

    public OnAccountLoggedInEvent(Intent loginIntent, boolean isNewAccount) {
        this.m_intent = loginIntent;
        this.m_newAccount = isNewAccount;
    }


    public Intent getIntent() {
        return m_intent;
    }

    public boolean isNewAccount() {
        return m_newAccount;
    }

    public void setNewAccount(boolean isNewAccount) {
        this.m_newAccount = isNewAccount;
    }
}
