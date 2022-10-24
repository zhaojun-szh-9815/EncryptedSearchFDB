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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Backup
 */
public class FdbSearchService {
    private static final FDB fdb = FDB.selectAPIVersion(520);
    private FdbRR2Lev fdbRR2Lev = new FdbRR2Lev();
    private static final String PASS = "123";
    private static List<byte[]> listSK;
    private static final String RR2LEV_DICT_KEY = "twoLev_dict";
    private static final String STR_LENGTH = "_length";
    private static ObjectMapper mapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(FdbSearchService.class);

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
        RR2Lev rr2Lev = RR2Lev.constructEMMParGMM(listSK.get(0), TextExtractPar.lp1, bigBlock, smallBlock, dataSize);
        fdbRR2Lev.addRR2LevList(rr2Lev);
        long endTime = System.nanoTime();

        long output = endTime - startTime;
        logger.info("Elapsed time in seconds: " + output / 1000000000);

    }

    public List<String> queryToken(String keyword) throws Exception{
        if (fdbRR2Lev == null) {
            logger.error("RR2Lev is null, download or generate it firstly.");
            return null;
        }

        byte[][] token = RR2Lev.token(listSK.get(0), keyword);
        List<String> res = new ArrayList<>();
        List<RR2Lev> rr2LevList = fdbRR2Lev.getRr2LevList();
        for (RR2Lev rr2Lev : rr2LevList) {
            List<String> tmp = rr2Lev.query(token, rr2Lev.getDictionary(), rr2Lev.getArray());
            res.addAll(tmp);
        }
        return res;
    }

    public List<String> queryLastToken(String keyword) throws Exception{
        if (fdbRR2Lev == null) {
            logger.error("RR2Lev is null, download or generate it firstly.");
            return null;
        }

        byte[][] token = RR2Lev.token(listSK.get(0), keyword);
        List<String> res = new ArrayList<>();
        List<RR2Lev> rr2LevList = fdbRR2Lev.getRr2LevList();
        for (RR2Lev rr2Lev : rr2LevList) {
            res = rr2Lev.query(token, rr2Lev.getDictionary(), rr2Lev.getArray());
        }
        return res;
    }

    public boolean uploadRR2Lev() throws JsonProcessingException {
        if (fdbRR2Lev == null) {
            logger.error("RR2Lev is null, generate it firstly.");
            return false;
        }

        String dict = mapper.writeValueAsString(fdbRR2Lev.getMapList());
        List<String> strList = splitEqually(dict, 10000);

        logger.info("upload: dict length: " + dict.length());

        try (Database db = fdb.open()) {
            int length = strList.size();
            int index = 0;
            while (index < length) {
                String DBIndex = RR2LEV_DICT_KEY + index;
                int curInx = index;
                db.run(transaction -> {
                    transaction.set(Tuple.from(DBIndex).pack(), Tuple.from(strList.get(curInx)).pack());
                    return null;
                });
                logger.debug("upload rr2lev: " + DBIndex);
                index += 1;
            }
            String DBLengthIndex = RR2LEV_DICT_KEY + STR_LENGTH;
            db.run(transaction -> {
                transaction.set(Tuple.from(DBLengthIndex).pack(), Tuple.from(length).pack());
                return null;
            });
            logger.info("upload: rr2lev length: " + length);

            logger.info("Upload RR2Lev: Success");
            return true;
        } catch (Exception e) {
            logger.error("Upload RR2Lev: Fail");
            logger.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRR2Lev(String path) throws Exception  {
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
        RR2Lev rr2Lev = RR2Lev.constructEMMParGMM(listSK.get(0), TextExtractPar.lp1, bigBlock, smallBlock, dataSize);
        long endTime = System.nanoTime();

        boolean flag = downloadRR2Lev();

        fdbRR2Lev.addRR2LevList(rr2Lev);

        logger.info("Update: dictList length = " + fdbRR2Lev.getMapList().size());

        long output = endTime - startTime;
        logger.info("Elapsed time in seconds: " + output / 1000000000);

        return flag;
    }

    public boolean clearRR2Lev() {
        try (Database db = fdb.open()){
            Long length = db.run(tr -> {
                byte[] result = tr.get(Tuple.from(RR2LEV_DICT_KEY + STR_LENGTH).pack()).join();
                return Tuple.fromBytes(result).getLong(0);
            });
            db.run(transaction -> {
                transaction.clear(Tuple.from(RR2LEV_DICT_KEY + STR_LENGTH).pack());
                return null;
            });
            int len = length.intValue();
            logger.info("Clear: dict length = " + len);
            int index = 0;
            while (index < len) {
                String DBIndex = RR2LEV_DICT_KEY + index;
                db.run(transaction -> {
                    transaction.clear(Tuple.from(DBIndex).pack());
                    return null;
                });
                index += 1;
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
        try (Database db = fdb.open()) {

            Long length = db.run(tr -> {
                byte[] result = tr.get(Tuple.from(RR2LEV_DICT_KEY + STR_LENGTH).pack()).join();
                return Tuple.fromBytes(result).getLong(0);
            });
            int len = length.intValue();
            String[] dictArr = new String[len];
            int index = 0;
            while (index < len) {
                String DBIndex = RR2LEV_DICT_KEY + index;
                Object queryResult = db.run(transaction -> {
                    byte[] result = transaction.get(Tuple.from(DBIndex).pack()).join();
                    return Tuple.fromBytes(result).get(0);
                });
                String subStr = (String) queryResult;
                dictArr[index] = subStr;
                index += 1;
            }

            logger.info("dictArr: length = " + dictArr.length + ", each element length = " + dictArr[0].length());

            String str = String.join("", dictArr);

            logger.info("joined str length = " + str.length());

            TypeReference<List<Map<String, Collection<byte[]>>>> tr_dict = new TypeReference<List<Map<String, Collection<byte[]>>>>() {};
            List<Map<String, Collection<byte[]>>> dictionaryList = mapper.readValue(str,tr_dict);
            fdbRR2Lev.setMapList(dictionaryList);
            fdbRR2Lev.convert2RR2Lev();

            logger.info("Download RR2Lev: Success");
            return true;
        } catch (Exception e) {
            logger.error("Download RR2Lev: Fail");
            logger.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> splitEqually(String text, int size) {
        // Give the list the right capacity to start with. You could use an array
        // instead if you wanted.
        List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);

        for (int start = 0; start < text.length(); start += size) {
            ret.add(text.substring(start, Math.min(text.length(), start + size)));
        }
        return ret;
    }

}
