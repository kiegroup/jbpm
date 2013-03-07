package org.jbpm.concurrency.util;

public class GreenLight {

    boolean isGreen = false;

    public void setIsGreen(boolean isGreen) {
        this.isGreen = isGreen;
    }

    public boolean isGreen() {
        return isGreen;
    }
}
