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

package com.twolinessoftware.data;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.twolinessoftware.ErrorException;
import com.twolinessoftware.utils.GsonUtil;

import org.json.JSONObject;

import java.util.HashMap;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

/**
 * Created by johncarpenter on 2016-01-26.
 */
public class FirebaseMonitor<T> {

    final Class<T> mTypeParameterClass;

    public FirebaseMonitor(Class<T> typeParameterClass) {
        this.mTypeParameterClass = typeParameterClass;
    }

    public Observable<DataChange<T>> monitorChanges(DatabaseReference firebaseRef) {
        return Observable.create(new Observable.OnSubscribe<DataChange<T>>() {
            @Override
            public void call(Subscriber<? super DataChange<T>> subscriber) {


                ChildEventListener eventListener = firebaseRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        subscriber.onNext(new DataChange<>(DataChange.State.Added, buildFromSnapshot(dataSnapshot)));
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        subscriber.onNext(new DataChange<>(DataChange.State.Changed, buildFromSnapshot(dataSnapshot)));
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        subscriber.onNext(new DataChange<>(DataChange.State.Removed, buildFromSnapshot(dataSnapshot)));
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {
                        subscriber.onError(firebaseError.toException());
                    }
                });
                subscriber.add(Subscriptions.create(() -> firebaseRef.removeEventListener(eventListener)));

            }

        });
    }


    public Observable<DataChange<T>> once(DatabaseReference firebaseRef) {
        return Observable.create(new Observable.OnSubscribe<DataChange<T>>() {
            @Override
            public void call(Subscriber<? super DataChange<T>> subscriber) {

                ValueEventListener listener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot == null || !dataSnapshot.exists()) {
                            subscriber.onNext(new DataChange<T>(DataChange.State.Empty, null));
                            return;
                        }

                        subscriber.onNext(new DataChange<>(DataChange.State.Data, buildFromSnapshot(dataSnapshot)));

                        subscriber.onCompleted();
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {
                        subscriber.onError(new ErrorException(ErrorException.Code.FIREBASE_ERROR_GENERIC));
                    }
                };

                firebaseRef.addListenerForSingleValueEvent(listener);

                subscriber.add(Subscriptions.create(() -> firebaseRef.removeEventListener(listener)));
            }

        });
    }

    private T buildFromSnapshot(DataSnapshot dataSnapshot) {

        HashMap<String, JSONObject> dataSnapshotValue = (HashMap<String, JSONObject>) dataSnapshot.getValue();
        String jsonString = new Gson().toJson(dataSnapshotValue);

        return GsonUtil.buildGsonAdapter().fromJson(jsonString, mTypeParameterClass);

    }

}
