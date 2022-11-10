package edu.bu.search;

import com.apple.foundationdb.Database;
import com.apple.foundationdb.FDB;
import com.apple.foundationdb.tuple.Tuple;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.crypto.sse.IEX2Lev;
import org.crypto.sse.RR2Lev;
import org.crypto.sse.TextExtractPar;
import org.crypto.sse.TextProc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * add, clear, download, update (index dictionary)
 */
public class ClusionRR2LevOpt {
    private static final FDB fdb = FDB.selectAPIVersion(520);
    private RR2Lev rr2Lev;
    private static final String PASS = "123";
    private static List<byte[]> listSK;

    private static Set<String> keys;
    private Map<String, Collection<byte[]>> levMap = new HashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(ClusionRR2LevOpt.class);

    static {
        try {
            listSK = IEX2Lev.keyGen(256, PASS, "salt/salt", 100000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateRR2Lev(String path) throws Exception {

        ArrayList<File> listOfFile = new ArrayList<File>();
        TextProc.listf(path, listOfFile);

        TextProc.TextProc(false, path);

        int bigBlock = 1000;
        int smallBlock = 100;
        int dataSize = 10000;

        logger.info("\nBeginning of Encrypted Multi-map creation \n");
        logger.info("Number of keywords "+ TextExtractPar.lp1.keySet().size());
        logger.info("Number of pairs "+	TextExtractPar.lp1.keys().size());

        long startTime = System.nanoTime();
        this.rr2Lev = RR2Lev.constructEMMParGMM(listSK.get(0), TextExtractPar.lp1, bigBlock, smallBlock, dataSize);
        long endTime = System.nanoTime();

        long output = endTime - startTime;
        logger.info("Elapsed time in seconds: " + output / 1000000000);

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

    public List<String> queryToken(String keyword) throws Exception{
        if (this.rr2Lev == null) {
            logger.error("RR2Lev is null, download or generate it firstly.");
            return null;
        }

        byte[][] token = RR2Lev.token(listSK.get(0), keyword);
        List<String> res = this.rr2Lev.query(token, rr2Lev.getDictionary(), rr2Lev.getArray());
        return res;
    }

    public boolean uploadRR2Lev() throws Exception {
        if (this.rr2Lev == null) {
            logger.error("RR2Lev is null, generate it firstly.");
            return false;
        }

        MyRR2Lev myRR2Lev = new MyRR2Lev();
        myRR2Lev.setMultiDictionary(this.rr2Lev.getDictionary());

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
            logger.info("Upload RR2Lev: Success");
            return true;
        } catch (Exception e) {
            logger.error("Upload RR2Lev: Fail");
            e.printStackTrace();
            return false;
        }
    }

    public void updateRR2Lev(String path) throws Exception {
        ArrayList<File> listOfFile = new ArrayList<File>();
        TextProc.listf(path, listOfFile);

        TextProc.TextProc(false, path);

        int bigBlock = 1000;
        int smallBlock = 100;
        int dataSize = 10000;

        logger.info("\nBeginning of Encrypted Multi-map creation \n");
        logger.info("Number of keywords "+ TextExtractPar.lp1.keySet().size());
        logger.info("Number of pairs "+	TextExtractPar.lp1.keys().size());

        long startTime = System.nanoTime();
        this.rr2Lev = RR2Lev.constructEMMParGMM(listSK.get(0), TextExtractPar.lp1, bigBlock, smallBlock, dataSize);
        long endTime = System.nanoTime();

        long output = endTime - startTime;
        logger.info("Elapsed time in seconds: " + output / 1000000000);

    }

    /*public void loadKeys() {
        try (
                InputStream is = new FileInputStream(".\\src\\tmp\\keys.txt");
                InputStreamReader isr = new InputStreamReader(is, "Ascii");
                BufferedReader br = new BufferedReader(isr);
        ) {
            this.keys = new HashSet<>();
            String key = br.readLine();
            while (key != null) {
                this.keys.add(key);
                key = br.readLine();
            }
            logger.info("loadKey: success, length = " + keys.size());
        } catch (Exception e) {
            logger.error("Load Keys error");
            e.printStackTrace();
        }
    }*/

    public boolean clearRR2Lev() {
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
            logger.info("Clear RR2Lev: Success");
            return true;
        } catch (Exception e) {
            logger.error("Clear RR2Lev: Fail");
            logger.error(e.getMessage());
            return false;
        }
    }

    public boolean downloadRR2Lev() {
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

            this.rr2Lev = new RR2Lev(myRR2Lev.getMultiDictionary(), myRR2Lev.getArr());
            logger.info("Download RR2Lev: Success");
            return true;
        } catch (Exception e) {
            logger.error("Download RR2Lev: Fail");
            logger.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }



}
