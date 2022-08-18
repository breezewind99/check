package com.cnettech;

import com.cnettech.util.Monitoring;

public class Check {
    public static void main(String[] args) {
        System.out.println("---- Program START ----");
        Monitoring serverRec = new Monitoring();
        serverRec.start();
    }
}
