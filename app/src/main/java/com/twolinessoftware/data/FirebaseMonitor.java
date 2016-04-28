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

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.twolinessoftware.ErrorException;

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

    public Observable<FirebaseChange<T>> monitorChanges(Firebase firebaseRef) {
        return Observable.create(new Observable.OnSubscribe<FirebaseChange<T>>() {
            @Override
            public void call(Subscriber<? super FirebaseChange<T>> subscriber) {


                ChildEventListener eventListener = firebaseRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        subscriber.onNext(new FirebaseChange<>(FirebaseChange.State.Added, dataSnapshot.getValue(mTypeParameterClass)));
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        subscriber.onNext(new FirebaseChange<>(FirebaseChange.State.Changed, dataSnapshot.getValue(mTypeParameterClass)));
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        subscriber.onNext(new FirebaseChange<>(FirebaseChange.State.Removed, dataSnapshot.getValue(mTypeParameterClass)));
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        subscriber.onError(firebaseError.toException());
                    }
                });
                subscriber.add(Subscriptions.create(() -> firebaseRef.removeEventListener(eventListener)));

            }

        });
    }


    public Observable<FirebaseChange<T>> once(Firebase firebaseRef) {
        return Observable.create(new Observable.OnSubscribe<FirebaseChange<T>>() {
            @Override
            public void call(Subscriber<? super FirebaseChange<T>> subscriber) {

                ValueEventListener listener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if ( dataSnapshot == null || !dataSnapshot.exists() ) {
                            subscriber.onNext(new FirebaseChange<T>(FirebaseChange.State.Empty, null));
                            return;
                        }

                        for ( DataSnapshot snapshot : dataSnapshot.getChildren() ) {
                            subscriber.onNext(new FirebaseChange<>(FirebaseChange.State.Data, snapshot.getValue(mTypeParameterClass)));
                        }

                        subscriber.onCompleted();
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        subscriber.onError(new ErrorException(ErrorException.Code.FIREBASE_ERROR_GENERIC));
                    }
                };

                firebaseRef.addListenerForSingleValueEvent(listener);

                subscriber.add(Subscriptions.create(() -> firebaseRef.removeEventListener(listener)));
            }

        });
    }

}
