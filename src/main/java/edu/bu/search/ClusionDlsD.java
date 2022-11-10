package edu.bu.search;

import com.apple.foundationdb.Database;
import com.apple.foundationdb.FDB;
import com.apple.foundationdb.tuple.Tuple;
import org.crypto.sse.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;


public class ClusionDlsD {
    private static final FDB fdb = FDB.selectAPIVersion(520);
    private static DlsD dlsd;
    private static final String pass = "123";
    private static byte[] sk;
    private static byte[] key1;
    private static byte[] key2;
    private static Set<String> keys;
    private Map<String, Collection<byte[]>> levMap = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(ClusionDlsD.class);

    static {
        try {
            sk = DlsD.keyGen(256, pass, "salt/salt", 100);
            key1 = new byte[sk.length / 2];
            key2 = new byte[sk.length / 2];
            System.arraycopy(sk, 0 , key1, 0, sk.length / 2);
            System.arraycopy(sk, sk.length / 2, key2, 0, sk.length / 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long computeFilesSize(File path) {
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

    public void generateDlsD(String path) throws Exception {

        ArrayList<File> listOfFile = new ArrayList<File>();

        TextProc.listf(path, listOfFile);
        TextProc.TextProc(false, path);
        System.out.println("\nBeginning of Encrypted Multi-map creation \n");
        this.dlsd = DlsD.constructEMMParGMM(key1, key2, TextExtractPar.lp1);
        System.out.println("size of dict pre-search (w, id) pairs " + this.dlsd.getOld_dictionary().size()+ " Unique keywords "+TextExtractPar.lp1.keySet().size()+"\n");


    }

    public List<String> queryToken(String keyword) throws Exception{
        if (this.dlsd == null) {
            logger.error("DlsD is null, download or generate it firstly.");
            return null;
        }

        String[][] stoken = DlsD.token(key1, key2, keyword);
        List<String> res = DlsD.resolve(key2, DlsD.query(stoken, dlsd));
        return res;
    }

    public boolean uploadDlsD() throws Exception {
        if (this.dlsd == null) {
            logger.error("DlsD is null, generate it firstly.");
            return false;
        }

        MyRR2Lev myRR2Lev = new MyRR2Lev();
        myRR2Lev.setMultiDictionary(this.dlsd.getOld_dictionary());

        try (
                Database db = fdb.open();
                PrintWriter printWriter = new PrintWriter(".\\src\\tmp\\keys.txt", "Ascii");
            ) {
            Map<String, Collection<byte[]>> dict = myRR2Lev.getDictionary();
            this.keys = dict.keySet();
            logger.info("Upload: dict size = " + dict.size() + " key size = " + this.keys.size());
            for (String key : this.keys) {
                printWriter.println(key);
            }
            db.run(transaction -> {
                for (String key : this.keys) {
                    transaction.set(Tuple.from(key).pack(), Tuple.from(dict.get(key)).pack());
                }
                return null;
            });
            logger.info("Upload DlsD: Success");
            return true;
        } catch (Exception e) {
            logger.error("Upload DlsD: Fail");
            e.printStackTrace();
            return false;
        }
    }

    public boolean clearDlsD() {
        if (this.keys == null || this.keys.isEmpty()) {
            logger.error("Clear: No keys set!");
            //loadKeys();
        }
        try (Database db = fdb.open()){
            db.run(transaction -> {
                for (String s : this.keys) {
                    transaction.clear(Tuple.from(s).pack());
                }
                return null;
            });
            File file = new File(".\\src\\tmp\\keys.txt");

            if (file.delete()) {
                logger.info("KeySet deleted successfully");
            }
            else {
                logger.error("Failed to delete the KeySet");
            }
            logger.info("Clear DlsD: Success");
            return true;
        } catch (Exception e) {
            logger.error("Clear DlsD: Fail");
            logger.error(e.getMessage());
            return false;
        }
    }

    public boolean downloadDlsD() {
        if (this.keys == null || this.keys.isEmpty()) {
            logger.error("Download: No keys set!");
            //loadKeys();
        }

        try (Database db = fdb.open()) {

            db.run(tr -> {
                for (String key : this.keys) {
                    byte[] result = tr.get(Tuple.from(key).pack()).join();
                    this.levMap.put(key, (LinkedList) Tuple.fromBytes(result).get(0));
                }
                return null;
            });

            logger.info("Download Lev Map: " + this.levMap.size());
            MyRR2Lev myRR2Lev = new MyRR2Lev();
            myRR2Lev.setDictionary(this.levMap);

            this.dlsd.setOld_dictionary(myRR2Lev.getMultiDictionary());
            logger.info("Download DlsD: Success");
            return true;
        } catch (Exception e) {
            logger.error("Download DlsD: Fail");
            logger.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }



}
