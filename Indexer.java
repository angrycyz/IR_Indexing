import java.util.*;
import java.io.*;

public class Indexer {
    private HashMap<Integer, List<TermInfo>> indexMap = new HashMap<>();
    private HashSet<Integer> fileMap = new HashSet<>();
    private HashMap<Integer, Double> idfMap = new HashMap<>();
    private HashMap<Integer, String> docMap = new HashMap<>();
    private HashMap<Integer, Integer> idfDocMap = new HashMap<>();
    public final String DOCMAP_PATH = "DocMap.txt";
    public final String IDFMAP_PATH = "IDFMap.txt";
    public final String OUTPUT_DIR = "../Terms";
    private int totalDocNum = 0;

    private void clearIndexMap() {
        indexMap.clear();
    }

    private void clearFileMap() {
        fileMap.clear();
    }

    private void clearIdfMap() {
        idfMap.clear();
    }

    private void clearIdfDocMap() {
        idfDocMap.clear();
    }

    private void clearDocMap() {
        docMap.clear();
    }

    public double getIdf(int termID) {
        return idfMap.get(termID);
    }

    public HashMap<Integer, Double> getIdfMap() {
        return idfMap;
    }

    private void dumpDocMap() {
        File directory = new File(OUTPUT_DIR);

        /*create output directory */
        if (!directory.exists()) {
            directory.mkdir();
        }

        try {
            PrintWriter outputWriter;
            outputWriter = new PrintWriter(OUTPUT_DIR + "/" + DOCMAP_PATH);
            outputWriter.print(docMap.toString());
            outputWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        clearDocMap();
    }


    private void dumpIdfMap() {
        File directory = new File(OUTPUT_DIR);

        /*create output directory */
        if (!directory.exists()) {
            directory.mkdir();
        }

        try {
            PrintWriter outputWriter;
            outputWriter = new PrintWriter(OUTPUT_DIR + "/" + IDFMAP_PATH);
            outputWriter.print(idfMap.toString());
            outputWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        clearIdfMap();
    }

    private void calcIdfMap() {
        for (int termID: idfDocMap.keySet()) {
            idfMap.put(termID, Math.log(totalDocNum/idfDocMap.get(termID))/Math.log(2));
        }
        clearIdfDocMap();
    }

//    public void calcIdfFromDir() {
//        File directory = new File(OUTPUT_DIR);
//        File[] files = directory.listFiles();
//
//        if (files != null) {
//            for (File file : files) {
//                if (file.isFile()) {
//                    System.out.println("Parsing " + file.getName() + "...");
//                    calcIdfFromFile(file);
//                }
//            }
//        } else {
//            System.err.println("Invalid Path.");
//        }
//    }
//
//    public void calcIdfFromFile(File file) {
//        HashMap<Integer, Integer> termDocMap = new HashMap<>();
//        try {
//            Scanner input = new Scanner(file);
//            while (input.hasNextLine()) {
//                String line = input.nextLine();
//                int docID = Integer.parseInt(line.split(" ")[1]);
//                termDocMap.put(docID, termDocMap.getOrDefault(docID, 0) + 1);
//            }
//            input.close();
//
//            for (int docID: termDocMap.keySet()) {
//                idfMap.put(docID, Math.log(totalDocNum/termDocMap.get(docID))/Math.log(2));
//            }
//
//        } catch(FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

    private void deleteDir() {
        File folder = new File(OUTPUT_DIR);
        File[] files = folder.listFiles();
        if (files!=null) {
            for(File f: files) {
                if (f.isDirectory()) {
                    deleteDir();
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    private void dumpToDisk() {
        File directory = new File(OUTPUT_DIR);

        /*create output directory */
        if (!directory.exists()) {
            directory.mkdir();
        }

        /* iterate through all term and put to its corresponding file */
        for (Integer termID: indexMap.keySet()) {
            List<TermInfo> lst = indexMap.get(termID);
            String fileName;
            if (termID < 0) {
                fileName = "_" + Integer.toString(-termID);
            } else {
                fileName = Integer.toString(termID);
            }
            try {
                PrintWriter outputWriter;
                if (!fileMap.contains(termID)) {
                        outputWriter = new PrintWriter(
                                OUTPUT_DIR + '/' + fileName + ".txt");
                        fileMap.add(termID);
                } else {
                    outputWriter = new PrintWriter(
                            new FileOutputStream(OUTPUT_DIR + '/' + fileName + ".txt",
                                    true));
                }
                for (TermInfo termInfo: lst) {
                    outputWriter.println(termInfo.getTermID() + " " +
                            termInfo.getDocID() + " " + termInfo.getTf()
                            + " " + termInfo.getPos());
                }
                outputWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private void addToIndexMap(TermInfo termInfo) {
        int termID = termInfo.getTermID();

        if (!indexMap.containsKey(termID)) {
            indexMap.put(termID, new ArrayList<TermInfo>(){{add(termInfo);}});
        } else {
            indexMap.get(termID).add(termInfo);
        }
    }


    public void indexing(List<TermInfo> tokenSeq) {
        if (tokenSeq.size() > 0) {
            docMap.put(tokenSeq.get(0).getDocID(), tokenSeq.get(0).getDocNo());
        }

        for (TermInfo termInfo: tokenSeq) {
            addToIndexMap(termInfo);
        }

        dumpToDisk();
        clearIndexMap();
    }

    public void parseFromFile(File file) {
        StringBuffer textBuffer = new StringBuffer("");
        String docID = "";
        boolean textBegins = false;
        try {
            Scanner input = new Scanner(file);
            while (input.hasNextLine()) {
                String line = input.nextLine();
                if (line.startsWith("</DOC>")) {
                    totalDocNum += 1;
                    Tokenizer tokenizer = new Tokenizer();
                    indexing(tokenizer.tokenize(textBuffer, docID, idfDocMap));
                    textBuffer.setLength(0);
                    docID = "";
                }
                if (line.startsWith("<DOCNO>") && line.endsWith("</DOCNO>")) {
                    docID = line.substring(line.indexOf("<DOCNO>") + 7, line.indexOf("</DOCNO>")).trim();
                }
                if (line.startsWith("<TEXT>")) {
                    textBegins = true;
                }
                if (line.startsWith("</TEXT>")) {
                    textBegins = false;
                }
                if (!line.startsWith("<TEXT>") && textBegins) {
                    textBuffer.append(line);
                }
            }

            input.close();

        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void parseFromDir(String dir) {

        deleteDir();

        File directory = new File(dir);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    System.out.println("Parsing " + file.getName() + "...");
                    parseFromFile(file);
                }
            }
        } else {
            System.err.println("Invalid Path.");
        }

        calcIdfMap();

        dumpDocMap();
        dumpIdfMap();

        clearFileMap();
        clearIndexMap();

    }

    public static void main(String args[]){
        //../AP_DATA/ap89_collection
        if (args.length == 0) {
            System.out.println("Please give the corpus directory...");
            Scanner scanner = new Scanner(System.in);
            try {
                while (true) {
                    if (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        String[] line_arg = line.trim().split("\\s+");
                        if (line_arg.length == 1) {
                            Indexer indexer = new Indexer();
                            indexer.parseFromDir(line_arg[0]);
                        } else {
                            System.err.println("Invalid arguments number");
                        }
                        break;
                    }
                }
            } catch(IllegalStateException | NoSuchElementException e) {
                e.printStackTrace();
            }
        } else if (args.length == 1) {
            Indexer indexer = new Indexer();
            indexer.parseFromDir(args[0]);
        } else {
            System.err.println("Too many arguments");
        }
    }
}
