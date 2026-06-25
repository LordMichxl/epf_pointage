package sn.epf.pointage.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ReseauUtils {

    private ReseauUtils() {}
    public static String getIpLocalAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "inconnue";
        }
    }
}