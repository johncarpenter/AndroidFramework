package com.twolinessoftware.events;

/**
 *
 */
public class OnCommunicationStatusEvent {

    public enum Status{
        idle,
        busy,
        unavailable
    }

    private Status status;

    public OnCommunicationStatusEvent(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

}
