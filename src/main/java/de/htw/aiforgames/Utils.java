package de.htw.aiforgames;

public class Utils {
    public static int floorLog2(long n){
        return 63 - Long.numberOfLeadingZeros(n);
    }
}
