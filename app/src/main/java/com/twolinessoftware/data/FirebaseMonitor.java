/*
 * Copyright (c) 2016. Petrofeed Inc
 *
 *  All Rights Reserved.
 *
 *  NOTICE:  All information contained herein is, and remains
 *  the property of Petrofeed Inc and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to Petrofeed Inc
 *  and its suppliers and may be covered by U.S. and Foreign Patents,
 *  patents in process, and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from Petrofeed Inc.
 *
 */

package com.twolinessoftware.data;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

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

                        for ( DataSnapshot snapshot : dataSnapshot.getChildren() ) {
                            subscriber.onNext(new FirebaseChange<>(FirebaseChange.State.Data, snapshot.getValue(mTypeParameterClass)));
                        }
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                };

                firebaseRef.addListenerForSingleValueEvent(listener);

                subscriber.add(Subscriptions.create(() -> firebaseRef.removeEventListener(listener)));
            }

        });
    }

}
