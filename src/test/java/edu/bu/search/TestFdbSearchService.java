package edu.bu.search;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class TestFdbSearchService {
    public static void main(String[] args) throws Exception{
        FdbSearchService service1 = new FdbSearchService();

        service1.generateRR2Lev(".\\src\\test\\files");
        service1.uploadRR2Lev();

        FdbSearchService service2 = new FdbSearchService();
        service2.downloadRR2Lev();

        BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.println("Enter keyword :");
            String keyword = keyRead.readLine();

            if ("quit".equals(keyword)) {
                break;
            }

            List<String> strings = service2.queryToken(keyword);
            System.out.println(strings);
        }

        service2.updateRR2Lev(".\\src\\test\\additionalFiles");
        while (true) {
            System.out.println("Enter keyword :");
            String keyword = keyRead.readLine();

            if ("quit".equals(keyword)) {
                break;
            }

            List<String> strings = service2.queryLastToken(keyword);
            System.out.println(strings);
        }

        service2.uploadRR2Lev();

        FdbSearchService service3 = new FdbSearchService();
        service3.downloadRR2Lev();

        while (true) {
            System.out.println("Enter keyword :");
            String keyword = keyRead.readLine();

            if ("quit".equals(keyword)) {
                break;
            }

            List<String> strings = service3.queryToken(keyword);
            System.out.println(strings);
        }

        service3.clearRR2Lev();
    }
}
