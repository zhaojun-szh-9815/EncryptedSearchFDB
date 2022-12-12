package edu.bu.util;

import java.io.File;

public class Analysis {
    public static long FilesSize = 0;
    public static long MapSize = 0;

    public static long KeyValueSize = 0;

    public static void addFilesSize(long size) {
        FilesSize += size;
    }

    public static void setMapSize(long size) {
        MapSize = size;
    }

    public static void getKeyValueSize() throws Exception{
        File f = new File(".\\dict.txt");
        KeyValueSize = f.length();
    }

    public static void computeRate() throws Exception {
        getKeyValueSize();
        System.out.println("Map Key-Value: " + (double)KeyValueSize/1024.0 + " kb");
        System.out.println("Map: " + (double)MapSize/1024.0 + " kb");
        System.out.println("Files: " + (double)FilesSize/1024.0 + " kb");
        System.out.println("Key-Value | Files: " + (double) KeyValueSize/ (double) FilesSize);
        System.out.println("Map | Files: " + (double) MapSize/ (double) FilesSize);
    }
}
