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

package com.twolinessoftware.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.util.UUID;

/**
 * Created by johncarpenter on 2016-04-26.
 */
public abstract class DataModel {

    @SerializedName("uid")
    @JsonProperty("uid")
    private String mUid;

    @SerializedName("created")
    private DateTime mCreated;

    @SerializedName("updated")
    private DateTime mUpdated;


    public DataModel() {
        setUid(UUID.randomUUID().toString());
    }

    public DataModel(String uid) {
        mUid = uid;
    }

    public void setCreated(DateTime created) {
        mCreated = created;
    }

    public DateTime getCreated() {
        return mCreated;
    }

    public void setUpdated(DateTime updated) {
        this.mUpdated = updated;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

}
