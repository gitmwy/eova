package com;

import com.eova.common.Response;
import com.jfinal.core.JFinal;


/**
 * 鼠标右键->Run As Java Application
 *
 */
public class RunEovaOSS {
    public static void main(String[] args) {
        JFinal.start("src/main/webapp", 801, "/", 0);
    }
}
