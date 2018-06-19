import java.util.*;

public class Tokenizer {

    public int getTermID(String term) {
        return term.hashCode();
    }

    public int getDocID(String docNo) {
        return docNo.hashCode();
    }

    public List<TermInfo> tokenize(StringBuffer textBuffer, String docNo, HashMap<Integer, Integer> idfDocMap) {
        String[] wordSeq = new String(textBuffer).split("[^\\w']+");
        List<TermInfo> seq = new ArrayList<>();
        HashMap<Integer, Integer> termMap = new HashMap<>();;


        int pos = 0;
        for (String word: wordSeq) {
            word = word.replaceAll("'", "").toLowerCase();
            if (word.length() > 0) {
                int termID = getTermID(word);
                seq.add(new TermInfo(termID, getDocID(docNo), docNo, pos, 0));
                termMap.put(termID, termMap.getOrDefault(termID, 0) + 1);
                pos += 1;
            }
        }


        for (TermInfo termInfo: seq) {
            termInfo.setTf(1 + Math.log(termMap.get(termInfo.getTermID()))/Math.log(2));
//            termInfo.print();
        }

        for (int termID: termMap.keySet()) {
            idfDocMap.put(termID, idfDocMap.getOrDefault(termID, 0) + 1);
        }

        return seq;
    }

    public static void main(String[] args) {
        Tokenizer tokenizer = new Tokenizer();
        StringBuffer text = new StringBuffer("\"I love burger,'' I really r'eally hate ONIon!\" She said");
//        tokenizer.tokenize(text, "AP11111-938");
    }
}
