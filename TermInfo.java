public class TermInfo {
    public TermInfo(int termID, int docID, String docNo, int pos, double tf) {
        this.termID = termID;
        this.docID = docID;
        this.docNo = docNo;
        this.pos = pos;
        this.tf = tf;
    }

    public int getTermID() {
        return termID;
    }

    public void setTermID(int termID) {
        this.termID = termID;
    }

    private int termID;

    public int getDocID() {
        return docID;
    }

    public void setDocID(int docID) {
        this.docID = docID;
    }

    private int docID;

    public String getDocNo() {
        return docNo;
    }

    public void setDocNo(String docNo) {
        this.docNo = docNo;
    }

    private String docNo;

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    private int pos;

    public void print() {
        System.out.printf(
                "TermID: %d, DocID: %d, DocNo: %s, Pos: %d, tf: %d\n",
                termID, docID, docNo, pos, tf);
    }

    public double getTf() {
        return tf;
    }

    public void setTf(double tf) {
        this.tf = tf;
    }

    private double tf;
}
