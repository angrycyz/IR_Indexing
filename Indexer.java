import java.util.*;
import java.io.*;

public class Indexer {
    private HashMap<Integer, List<TermInfo>> indexMap = new HashMap<>();
    private HashSet<Integer> fileMap = new HashSet<>();
    private HashSet<Integer> stopWords = new HashSet<>();
    private final String outputDir = "Terms";
    private int totalDocNum = 0;

    public void parseStopWords(String path){

    }

    public void clearStopWords() {
        stopWords.clear();
    }

    public void deleteDir() {
        File folder = new File(outputDir);
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteDir();
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    public void dumpToDisk() {
        File directory = new File(outputDir);

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
                                outputDir + '/' + fileName + ".txt");
                        fileMap.add(termID);
                } else {
                    outputWriter = new PrintWriter(
                            new FileOutputStream(outputDir + '/' + fileName + ".txt",
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

    public void clearIndexMap() {
        indexMap.clear();
    }

    public void clearFileMap() {
        fileMap.clear();
    }

    public void addToIndexMap(TermInfo termInfo) {
        int termID = termInfo.getTermID();

        System.out.printf("termID %s\n", termID);

        if (!indexMap.containsKey(termID)) {
            indexMap.put(termID, new ArrayList<TermInfo>(){{add(termInfo);}});
        } else {
            indexMap.get(termID).add(termInfo);
        }
    }


    public void indexing(List<TermInfo> tokenSeq) {
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
                    indexing(tokenizer.tokenize(textBuffer, docID));
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
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void parseFromDir(String dir) {

        deleteDir();
        clearFileMap();

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
    }

    public static void main(String args[]){
        //../test_dir
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
