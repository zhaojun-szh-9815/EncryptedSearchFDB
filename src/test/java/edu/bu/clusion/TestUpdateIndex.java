package edu.bu.clusion;

import org.crypto.sse.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class TestUpdateIndex {

    private static BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));
    private static RR2Lev twolev;
    private static RR2Lev new2lev;
    private static List<byte[]> listSK;

    static {
        try {
            Printer.addPrinter(new Printer(Printer.LEVEL.EXTRA));

            String pass = "123";

            listSK = IEX2Lev.keyGen(256, pass, "salt/salt", 100000);

            String pathName = ".\\src\\test\\files";

            ArrayList<File> listOfFile = new ArrayList<File>();
            TextProc.listf(pathName, listOfFile);

            TextProc.TextProc(false, pathName);

            // The two parameters depend on the size of the dataset. Change
            // accordingly to have better search performance
            int bigBlock = 1000;
            int smallBlock = 100;
            int dataSize = 10000;

            // Construction of the global multi-map
            System.out.println("\nBeginning of Encrypted Multi-map creation \n");
            System.out.println("Number of keywords "+ TextExtractPar.lp1.keySet().size());
            System.out.println("Number of pairs "+	TextExtractPar.lp1.keys().size());
            //start
            long startTime = System.nanoTime();
            twolev = RR2Lev.constructEMMParGMM(listSK.get(0), TextExtractPar.lp1, bigBlock, smallBlock, dataSize);
            //end
            long endTime = System.nanoTime();

            //time elapsed
            long output = endTime - startTime;
            System.out.println("Elapsed time in seconds: " + output / 1000000000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception{

        queryDict();
        extractNewDir();
        updateDict();
        queryDict();

    }

    public static void updateDict() {
        twolev.dictionary.putAll(new2lev.dictionary);
    }

    public static void extractNewDir() throws Exception {
        System.out.println("Beginning of Encrypted Multi-map creation");

        String pathName = ".\\src\\test\\additionalFiles";

        ArrayList<File> listOfFile = new ArrayList<File>();
        TextProc.listf(pathName, listOfFile);

        TextProc.TextProc(false, pathName);

        // The two parameters depend on the size of the dataset. Change
        // accordingly to have better search performance
        int bigBlock = 1000;
        int smallBlock = 100;
        int dataSize = 10000;

        // Construction of the global multi-map
        System.out.println("\nBeginning of Encrypted Multi-map creation \n");
        System.out.println("Number of keywords "+ TextExtractPar.lp1.keySet().size());
        System.out.println("Number of pairs "+	TextExtractPar.lp1.keys().size());
        //start
        long startTime = System.nanoTime();
        new2lev = RR2Lev.constructEMMParGMM(listSK.get(0), TextExtractPar.lp1, bigBlock, smallBlock, dataSize);
        //end
        long endTime = System.nanoTime();

        //time elapsed
        long output = endTime - startTime;
        System.out.println("Elapsed time in seconds: " + output / 1000000000);
    }

    public static void queryDict() throws Exception{
        while (true) {
            System.out.println("Enter the keyword to search for:");
            String keyword = keyRead.readLine();
            if ("".equals(keyword)) {
                break;
            }
            byte[][] token = RR2Lev.token(listSK.get(0), keyword);
            System.out.println("Final Result: " + twolev.query(token, twolev.getDictionary(), twolev.getArray()));
        }
    }

    public static void printMultiMap() {
        Set<String> keys = twolev.dictionary.keySet();
        System.out.println("size of keys: " + keys.size());
        Iterator<String> keyIterator = keys.iterator();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            // System.out.println(key + ": ");
            Collection<byte[]> value = twolev.dictionary.get(key);
//            System.out.println("--value: length: " + value.size());
//            System.out.println("--value: " + value.stream().map(bytes -> bytes.toString()).collect(Collectors.toList()));
            if (value.size() > 1) {
                System.out.println("--value: " + value.stream().map(bytes -> bytes.toString()).collect(Collectors.toList()));
            }
        }
    }

}
