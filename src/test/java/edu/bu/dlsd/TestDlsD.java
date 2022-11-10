package edu.bu.dlsd;

import edu.bu.util.Analysis;
import org.crypto.sse.DlsD;
import org.crypto.sse.Printer;
import org.crypto.sse.TextExtractPar;
import org.crypto.sse.TextProc;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestDlsD {
    public static void main(String[] args) throws Exception {
        Printer.addPrinter(new Printer(Printer.LEVEL.EXTRA));

        BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter your password :");
        // String pass = keyRead.readLine();
        String pass = "123";
        System.out.println("password: " + pass);
        byte[] sk = DlsD.keyGen(256, pass, "salt/salt", 100);

        byte[] key1 = new byte[sk.length / 2];
        byte[] key2 = new byte[sk.length / 2];
        System.arraycopy(sk, 0 , key1, 0, sk.length / 2);
        System.arraycopy(sk, sk.length / 2, key2, 0, sk.length / 2);
        System.out.println("Enter the relative path name of the folder that contains the files to make searchable");
        // String pathName = keyRead.readLine();
        String pathName = ".\\src\\test\\files";
        System.out.println("pathName: " + pathName);

        long fileSize = computeFilesSize(new File(pathName));

        ArrayList<File> listOfFile = new ArrayList<File>();

        TextProc.listf(pathName, listOfFile);
        TextProc.TextProc(false, pathName);
        System.out.println("\nBeginning of Encrypted Multi-map creation \n");
        DlsD emm = DlsD.constructEMMParGMM(key1, key2, TextExtractPar.lp1);
        System.out.println("size of dict pre-search (w, id) pairs " + emm.getOld_dictionary().size()+ " Unique keywords "+TextExtractPar.lp1.keySet().size()+"\n");

        System.out.println("Size of Files: " + fileSize / 1024 + " kb");


        // create an object of the class named Class
        Class obj = DlsD.class;

        // access the private variable
        Field field = obj.getDeclaredField("state_global_version");
        // make private field accessible
        field.setAccessible(true);

        while (true) {

            // get value of field
            // and convert it in string
            Integer state_global_version = (Integer) field.get(obj);

            System.out.println("\n\n\t NEW ROUND and Global State ="+state_global_version+"\n\n");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(emm.getNew_dictionary());
            System.out.println("Size of new Dictionary: " + baos.size() / 1024 + " kb");
            oos.reset();
            oos.writeObject(emm.getOld_dictionary());
            System.out.println("Size of old Dictionary: " + baos.size() / 1024 + " kb");
            oos.close();
            baos.close();

            System.out.println("\nEnter the keyword to search for:");
            String keyword = keyRead.readLine();

            //generation of the search token
            String[][] stoken = DlsD.token(key1, key2, keyword);
            System.out.println(DlsD.resolve(key2, DlsD.query(stoken, emm)));

            // update phase
            System.out.println("Enter the keyword to add/delete:");
            String label = keyRead.readLine();
            System.out.println("Enter the doc ID to add/delete:");
            String value  = keyRead.readLine();
            System.out.println("Enter the operation (+/-):");
            String op  = keyRead.readLine();
            byte[][] tokenUp = DlsD.tokenUp(key1, key2, label, value, op);
            DlsD.update(tokenUp, emm);

            //restructuring
            int parameter = 3;
            emm.deamortized_restruct(key1, key2, emm, parameter);

            System.out.println("To exit , enter Y else enter N");
            String exit  = keyRead.readLine();
            if (exit.equals("Y")) {
                break;
            }
        }
    }

    public static long computeFilesSize(File path) {
        List<File> files = Arrays.asList(path.listFiles());
        long filesSize = 0;
        for(File file : files) {
            if(file.isFile()) {
                filesSize += file.length();
            } else {
                filesSize += computeFilesSize(file);
            }
        }
        return filesSize;
    }
}
