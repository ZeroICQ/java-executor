package com.github.zeroicq.executor.test;

public class State {
    public enum Status {PROCESSED, NOT_PROCESSED}

    public Status status = Status.NOT_PROCESSED;
}
