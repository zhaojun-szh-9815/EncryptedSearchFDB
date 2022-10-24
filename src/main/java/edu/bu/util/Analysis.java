package edu.bu.util;

public class Analysis {
    public static long FilesSize = 0;
    public static long MapSize = 0;

    public static void addFilesSize(long size) {
        FilesSize += size;
    }

    public static void setMapSize(long size) {
        MapSize = size;
    }

    public static double computeRate() {
        System.out.println("Map: " + MapSize);
        System.out.println("Files: " + FilesSize);
        return (double) MapSize/ (double) FilesSize;
    }
}
