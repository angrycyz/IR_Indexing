import java.util.*;
import java.io.*;

public class QueryProcessor {
    private final String RESULT_PATH = "QueryResult.txt";
    private HashMap<Integer, Double> idfMap = new HashMap<>();
    private HashMap<Integer, String> docMap = new HashMap<>();
    public final String DOCMAP_PATH = "DocMap.txt";
    public final String IDFMAP_PATH = "IDFMap.txt";
    public final String OUTPUT_DIR = "../Terms";
    private int retrieveNum = 10;

    public void setRetrieveNum(int num) {
        retrieveNum = num;
    }

    public String[] getTokens(String line) {
        String[] tokens =  line.split("[^\\w']+");
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].replaceAll("'", "").toLowerCase();
        }
        return tokens;
    }

    public void fillDocMap() {
        File file = new File(OUTPUT_DIR + "/" + DOCMAP_PATH);
        try {
            Scanner input = new Scanner(file);
            while (input.hasNextLine()) {
                String line = input.nextLine();
                String[] pairs = line.substring(1, line.length() - 1).split("[, \\n]+");
                for (String pair: pairs) {
                    /* if the doc name contains =,
                     * search for first index and split string
                     */
                    String[] pair_lst = pair.split("=");
                    docMap.put(Integer.parseInt(pair_lst[0]), pair_lst[1]);
                }
            }
            input.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void fillIdfMap() {
        File file = new File(OUTPUT_DIR + "/" + IDFMAP_PATH);
        try {
            Scanner input = new Scanner(file);
            while (input.hasNextLine()) {
                String line = input.nextLine();
                String[] pairs = line.substring(1, line.length() - 1).split("[, \\n]+");
                for (String pair: pairs) {
                    /* if the doc name contains =,
                     * search for first index and split string
                     */
                    String[] pair_lst = pair.split("=");
                    idfMap.put(Integer.parseInt(pair_lst[0]), Double.parseDouble(pair_lst[1]));
                }
            }
            input.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public HashSet<String> getStopWords(String path) {
        HashSet<String> stopWords = new HashSet<>();
        File file = new File(path);
        try {
            Scanner input = new Scanner(file);
            while (input.hasNextLine()) {
                String line = input.nextLine();
                stopWords.add(line);
            }
            input.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return stopWords;
    }

    public void processQuery(String path, String stopwordsPath) {
        File queryFile = new File(path);
        HashSet<String> stopWords = getStopWords(stopwordsPath);

        fillDocMap();
        fillIdfMap();

        try {
            PrintWriter outputWriter = new PrintWriter("searchResult.txt", "UTF-8");
            Scanner input = new Scanner(queryFile);
            while (input.hasNextLine()) {
                String line = input.nextLine();
                String[] tokens = getTokens(line);
                search(tokens, stopWords);
            }
            input.close();
            outputWriter.close();
        } catch(IOException e) {
            e.printStackTrace();
        }

    }

    public void search(String[] tokens, HashSet<String> stopWords) {

        Map<Integer, Double> tfIdfMap = new TreeMap<>();
        Tokenizer tokenizer = new Tokenizer();

        for (String token: tokens) {
            if (stopWords.contains(token)) {
                continue;
            }

            int termID = tokenizer.getTermID(token);

            if (!idfMap.containsKey(termID)) {
                continue;
            }

            String fileName;
            if (termID < 0) {
                fileName = "_" + Integer.toString(-termID);
            } else {
                fileName = Integer.toString(termID);
            }

            try {
//                System.out.println(fileName);
                File file = new File(OUTPUT_DIR + "/" + fileName + ".txt");
                Scanner input = new Scanner(file);
                HashSet<Integer> proDocSet = new HashSet<>();
                while (input.hasNextLine()) {
                    String line = input.nextLine();
                    String[] lst = line.split(" ");
                    double tf = Double.parseDouble(lst[2]);
                    double idf = idfMap.get(Integer.parseInt(lst[0]));
                    int docID = Integer.parseInt(lst[1]);

                    if (!proDocSet.contains(docID)) {
                        tfIdfMap.put(docID,
                                tfIdfMap.getOrDefault(docID, 0.0)
                                        + tf * idf);
                        proDocSet.add(docID);
                    }
                }

                input.close();

            } catch(FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        System.out.println(tfIdfMap);
        getKlargest(retrieveNum, tfIdfMap);
    }

    /* if the docs keep changing, deleting, adding new one, we should use heap
     * otherwise we can also use sorted array/list
     */
    public void getKlargest(int k, Map<Integer, Double> tfIdfMap) {
        try {
            int i = 0;
            PrintWriter printWriter = new PrintWriter(RESULT_PATH);
            for (Integer key: tfIdfMap.keySet()) {
                if (i >= k) {
                    break;
                }
                printWriter.println(docMap.get(key));
            }
            printWriter.close();
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        //../AP_DATA/ap89_collection
        if (args.length == 0) {
            System.out.println("Please give the query path and stopwords path, separate by space...");
            Scanner scanner = new Scanner(System.in);
            try {
                while (true) {
                    if (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        String[] line_arg = line.trim().split("\\s+");
                        if (line_arg.length == 2) {
                            Indexer indexer = new Indexer();
                            QueryProcessor queryProcessor = new QueryProcessor();
                            /* actually if we want the query processor runs independently
                             * we should do indexer entirely based on disk but not memory.
                             * which means the map from the indexer should also be dumped
                             * into disk. We take it out and put into new map when the
                             * query processor begins to work.
                             */
                            queryProcessor.processQuery(line_arg[0], line_arg[1]);
                        } else {
                            System.err.println("Invalid arguments number");
                        }
                        break;
                    }
                }
            } catch(IllegalStateException | NoSuchElementException e) {
                e.printStackTrace();
            }
        } else if (args.length == 2) {
            Indexer indexer = new Indexer();
            QueryProcessor queryProcessor = new QueryProcessor();
            queryProcessor.processQuery(args[0], args[1]);
        } else {
            System.err.println("Too many arguments");
        }
    }
}
