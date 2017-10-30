package com.smartera.socialhub;

public enum ResponseState {
    SUCCESS  ("SUCCESS"), 
    FAILED("FAILED")
    ;

    private final String stateMsg;

    ResponseState(String stateMsg) {
        this.stateMsg = stateMsg;
    }
    
    public String getStateMsg() {
        return this.stateMsg;
    }
    
}