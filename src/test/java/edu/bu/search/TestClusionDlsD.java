package edu.bu.search;

import edu.bu.util.Analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

public class TestClusionDlsD {

    public static void main(String[] args) throws Exception {

        ClusionDlsD dlsd1 = new ClusionDlsD();

//        dlsd1.generateDlsD(".\\src\\test\\files");
//        long filesSize = dlsd1.computeFilesSize(new File(".\\src\\test\\files"));
        dlsd1.generateDlsD("E:\\IntelliJProject\\FDBDS_maildir");
        long filesSize = dlsd1.computeFilesSize(new File("E:\\IntelliJProject\\FDBDS_maildir"));

        Analysis.addFilesSize(filesSize);
        dlsd1.uploadDlsD();

        ClusionDlsD dlsd2 = new ClusionDlsD();
        dlsd2.downloadDlsD();

        Analysis.computeRate();

        BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.println("Enter keyword :");
            String keyword = keyRead.readLine();

            if ("quit".equals(keyword)) {
                break;
            }

            List<String> strings = dlsd2.queryToken(keyword);
            System.out.println(strings);
        }

        dlsd2.clearDlsD();
    }
}
