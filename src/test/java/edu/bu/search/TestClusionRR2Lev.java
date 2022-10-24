package edu.bu.search;

import edu.bu.util.Analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

public class TestClusionRR2Lev {

    public static void main(String[] args) throws Exception{

        ClusionRR2Lev rr2Lev1 = new ClusionRR2Lev();

        rr2Lev1.generateRR2Lev(".\\src\\test\\files");
        long filesSize = rr2Lev1.computeFilesSize(new File(".\\src\\test\\files"));
        Analysis.addFilesSize(filesSize);
        rr2Lev1.uploadRR2Lev();

        ClusionRR2Lev rr2Lev2 = new ClusionRR2Lev();
        rr2Lev2.downloadRR2Lev();

        double rate = Analysis.computeRate();
        System.out.println("Rate = " + rate);

        BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.println("Enter keyword :");
            String keyword = keyRead.readLine();

            if ("quit".equals(keyword)) {
                break;
            }

            List<String> strings = rr2Lev2.queryToken(keyword);
            System.out.println(strings);
        }

        rr2Lev2.updateRR2Lev(".\\src\\test\\additionalFiles");
        filesSize = rr2Lev1.computeFilesSize(new File(".\\src\\test\\additionalFiles"));
        Analysis.addFilesSize(filesSize);
        rr2Lev2.uploadRR2Lev();

        ClusionRR2Lev rr2Lev3 = new ClusionRR2Lev();
        rr2Lev3.downloadRR2Lev();

        rate = Analysis.computeRate();
        System.out.println("Rate = " + rate);

        while (true) {
            System.out.println("Enter keyword :");
            String keyword = keyRead.readLine();

            if ("quit".equals(keyword)) {
                break;
            }

            List<String> strings = rr2Lev3.queryToken(keyword);
            System.out.println(strings);
        }

        rr2Lev3.clearRR2Lev();

    }

}