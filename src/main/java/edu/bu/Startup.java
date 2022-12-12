package edu.bu;

import edu.bu.fdb.FDBService;
import edu.bu.search.ClusionDlsD;
import edu.bu.util.Analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.security.Security;
import java.util.List;

public class Startup {
    public static void main(String[] args) {
        File dir = new File("./downloads");
        dir.mkdir();
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("Enter operation (upload / download / clear / search): ");
                String op = reader.readLine().trim();
                ClusionDlsD dlsd = new ClusionDlsD();
                if("upload".equals(op)) {
                    System.out.print("Enter path: ");
                    String path = reader.readLine().trim();
                    FDBService.UploadAll(path);
                    dlsd.generateDlsD(path);
                    long filesSize = dlsd.computeFilesSize(new File(path));
                    Analysis.addFilesSize(filesSize);
                    dlsd.uploadDlsD();
                }
                if("download".equals(op)) {
                    dlsd.downloadDlsD();
                    Analysis.computeRate();
                }
                if("clear".equals(op)) {
                    System.out.print("Enter path: ");
                    String path = reader.readLine().trim();
                    FDBService.ClearAll(path);
                    dlsd.clearDlsD();
                }
                if("search".equals(op)) {
                    while (true) {
                        System.out.println("Enter keyword (finish by enter quit):");
                        String keyword = reader.readLine().trim();

                        if ("quit".equals(keyword)) {
                            System.out.println("end.");
                            break;
                        }

                        List<String> strings = dlsd.queryToken(keyword);
                        System.out.println(strings);

                        System.out.println("Download the files? Enter the filename you want to download, or * to download all.\nOr leave it empty to exit.");
                        String download = reader.readLine().trim();
                        if("*".equals(download)){
                            for(String str: strings) {
                                FDBService.DownloadAFile(str);
                            }
                        } else if(!"".equals(download)){
                            if(strings.contains(download)){
                                FDBService.DownloadAFile(download);
                            } else {
                                System.out.println(download + " not found.");
                            }
                        }
                    }
                }
                System.out.print("exit?(Y/N)");
                String check = reader.readLine();
                if("Y".equals(check)) {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
