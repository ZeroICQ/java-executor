package com.github.zeroicq.executor.test;

public class ConditionState {
    public enum Status {START_STATE, STATE1}

    public Status status = Status.START_STATE;

    public void advanceState() {
        status = Status.STATE1;
    }

    public void resetState() {
        status = Status.START_STATE;
    }
}
