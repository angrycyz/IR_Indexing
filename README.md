------------------------------------------
# INDEXING AND QUERYING A TREC DATA SET
------------------------------------------

This repository contains 4 classes: **Tokenizer, Indexer, QueryProcessor, TermInfo, Similarity**.

------------------------------------------
HOW TO RUN THE PROGRAM
------------------------------------------

1) run the Indexer use:

*java Indexer [arg1]*

*arg1* is the directory path which contains all the documents waiting to be indexed.

e.g *java Indexer ../AP_DATA/ap89_collection* 

2) run the QueryProcessor:

*java QueryProcessor [arg1] [arg2]*

*arg1* is the file path which contains all queries, *arg2* is the path and name of the stoplist file.

e.g  *java QueryProcessor ../AP_DATA/query_desc.51-100.short.txt ../AP_DATA/stoplist.txt*

------------------------------------------
HOW EACH CLASS WORKS
------------------------------------------

### Indexer

This class take the documents directory path as the input and index all the docments. The basic idea of this class is firstly parse from the documents to get the tokenized results, and for all the tokenized terms, it create files for each of these terms with file name being the term id. Everytime a new document come in, the indexer will search for files for each of these tokenized terms and append the new index to the end of the file. The document id to document name is stored using a HashMap, so is the idf for all terms.  These two maps will finally be dumped into disk as independent files. So the QueryProcessor could directly import these files into memory and start processing queries. 

Indexing on sequence start from *indexing(List<TermInfo> tokenSeq)* function. The write to disk step can be found in *dumpToDisk()* function. The idf will be calculated after all files are processed. This can be found in *parseFromDir(String dir)* function.

All the outputs are stored in **../Terms** directory.

### QueryProcessor

As we store all indexes in independent files accordin to its term id, the query processor becomes super easy. It takes the query file path and stoplist file path as the input, and search for index file for tokenized query terms, also, it eliminates the stopwords according to the stoplist.  For one query, each term can be indexed with many documents, I create a TreeMap to store the cumulated **tf * idf** value and sort it after all terms has been processed. For now I hard code the retrieved document number to 10, but it could also be set as input from the user. 

Query processing start from *processQuery(String path, String stopwordsPath)* function. It firstly read the maps we dumped from Indexer into memory, and for each query it reads term indexes from file and update the **tfIdfMap**. Finally it stores the query results in **../QueryResult.txt** file.

### Tokenizer

This class will be used to tokenize strings. The **tokenize** function take the strings and its doc name to generate the term id and doc id as a sequence. the sequence returned is a list of TermInfo. This class will be instantiated in Indexer.

### TermInfo

This class will be instantiated in Indexer and Tokenizer. It contains 5 fields including **termID, docID, docNo, pos, tf** as well as getter and setter of these fields.

### Similarity

This class includes 2 methods. *calcJaccardCoef(String s1, String s2)* is used to calculate the Jaccard Coefficients. *calcLevinsteinDist(String s1, String s2)* is used to calculate the edit distance

------------------------------------------
WHY I CHOOSE THIS WAY FOR INDEXING
------------------------------------------

See above for explanation of how to implement indexer.

The first reason is we don't need to traverse all indexes and merge all the indexes, the indexer has really low requirement on memory. And if we run queries, this allows us to run queries independently and the query processor will be very fast.
