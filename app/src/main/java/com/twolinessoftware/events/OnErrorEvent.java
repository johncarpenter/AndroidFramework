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

import android.support.annotation.StringRes;

import com.twolinessoftware.R;

/**
 * Created by John on 2015-04-01.
 */
public class OnErrorEvent {

    public enum Error{

        COMMUNICATION(R.string.error_communication_generic),
        LOGIN_ERROR(R.string.error_invalid_userpass),
        VALIDATION_EMPTY_FIELDS(R.string.error_validation_fields),
        VALIDATION_PASSWORD_LENGTH(R.string.error_password_length),
        VALIDATION_INVALID_EMAIL(R.string.error_invalid_email),
        REGISTER_EMAIL_TAKEN(R.string.error_register_email_taken),
        REGISTER_EMAIL_INVALID(R.string.error_register_invalid_email),
        REQUIRES_LOGIN(R.string.error_requires_login),
        UNKNOWN_SERVER(R.string.error_connection_failed),
        GOOGLE_SERVICES(R.string.common_google_play_services_error_notification_requested_by_msg);


        private final int m_displayErrorResId;

        Error(@StringRes int errorStringResId) {
            m_displayErrorResId = errorStringResId;
        }

        public int getDisplayError(){
            return m_displayErrorResId;
        }
    }

    private Error m_error;

    public OnErrorEvent(Error error) {
        this.m_error = error;
    }

    public Error getError() {
        return m_error;
    }
}
