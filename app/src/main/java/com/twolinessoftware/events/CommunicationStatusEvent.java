package com.twolinessoftware.events;

/**
 *
 */
public class CommunicationStatusEvent {

    public enum Status{
        idle,
        busy,
        unavailable
    }

    private Status status;

    public CommunicationStatusEvent(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

}
