/*
 * Copyright 2016 2LinesSoftware Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.twolinessoftware.activities;

import android.text.Editable;
import android.text.TextWatcher;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;

import java.util.List;

public class ButtonsEnabledTextWatcher implements TextWatcher, Validator.ValidationListener{

        private final Validator mInternalValidator;
        private final BaseFragment mBaseFragment;

        public ButtonsEnabledTextWatcher(BaseFragment baseFragment){
            this.mInternalValidator = new Validator(baseFragment);
            this.mInternalValidator.setValidationListener(this);
            this.mBaseFragment = baseFragment;
        }


        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mInternalValidator.validate();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }

        @Override
        public void onValidationSucceeded() {
            mBaseFragment.setButtonsEnabled(true);
        }

        @Override
        public void onValidationFailed(List<ValidationError> errors) {
            mBaseFragment.setButtonsEnabled(false);
        }


    public void check() {
        mInternalValidator.validate();
    }
}
