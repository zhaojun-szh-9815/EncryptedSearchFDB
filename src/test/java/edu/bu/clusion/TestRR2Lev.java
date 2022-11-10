package edu.bu.clusion;

import org.crypto.sse.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TestRR2Lev {
    public static void main(String[] args) throws Exception {

        Printer.addPrinter(new Printer(Printer.LEVEL.EXTRA));

        BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Enter your password :");

        // String pass = keyRead.readLine();
        String pass = "123";
        System.out.println("password: " + pass);

        List<byte[]> listSK = IEX2Lev.keyGen(256, pass, "salt/salt", 100000);

        System.out.println("Enter the relative path name of the folder that contains the files to make searchable");

        // String pathName = keyRead.readLine();
        String pathName = ".\\src\\test\\files";
        System.out.println("pathName: " + pathName);

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
        RR2Lev twolev = RR2Lev.constructEMMParGMM(listSK.get(0), TextExtractPar.lp1, bigBlock, smallBlock, dataSize);
        //end
        long endTime = System.nanoTime();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(twolev.getDictionary());
        System.out.println("Size of Dictionary: " + baos.size() / 1024 + " kb");
        oos.close();
        baos.close();

        //time elapsed
        long output = endTime - startTime;
        System.out.println("Elapsed time in seconds: " + output / 1000000000);

        while (true) {

            System.out.println("Enter the keyword to search for:");
            String keyword = keyRead.readLine();
            byte[][] token = RR2Lev.token(listSK.get(0), keyword);

            System.out.println("Final Result: " + twolev.query(token, twolev.getDictionary(), twolev.getArray()));

        }

    }
}

