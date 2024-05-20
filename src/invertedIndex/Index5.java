package invertedIndex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.*;

public class Index5 {
    int N = 0;
    public Map<Integer, SourceRecord> sources;

    public HashMap<String, DictEntry> index;

    public Index5() {
        sources = new HashMap<Integer, SourceRecord>();
        index = new HashMap<String, DictEntry>();
    }

    public void setN(int n) {
        N = n;
    }


    //---------------------------------------------
    /*
     This function receives linked list with datatype of Posting class,
     and it receives the head of it,
     then iterates from the head of linked list printing ID of each document.
     It prints the IDs of the documents that a word appears in them.
     The data that will be printed from this function will be in this form
     [ID1, ID2, ID3, ID4]
     */
    public void printPostingList(Posting p) {
        System.out.print("[");
        while (p != null) {
            System.out.print("" + p.docId);
            p = p.next;
            if (p != null)
                System.out.print(',');
        }
        System.out.println("]");
    }

    //---------------------------------------------
    /*
     This function prints the frequency and the IDs of the documents that each word appears in them.
     */
    public void printDictionary() {
        Iterator it = index.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            DictEntry dd = (DictEntry) pair.getValue();
            System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "]       =--> ");
            printPostingList(dd.pList);
        }
        System.out.println("------------------------------------------------------");
        System.out.println("*** Number of terms = " + index.size());
    }

    //----------------------------------------------------------------------------
    /*
    This function links each word in the string 'ln' to the corresponding file using 'fid'.
     */
    public int indexOneLine(String ln, int fid, int numOfPrevFileWords) {
        int flen = 0;

        String[] words = ln.split("\\W+");
        flen += words.length;
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].toLowerCase();

            if (stopWord(words[i]))
                continue;

            words[i] = stemWord(words[i]);

            if (!index.containsKey(words[i])) {
                index.put(words[i], new DictEntry());
            }

            if (!index.get(words[i]).postingListContains(fid)) {
                index.get(words[i]).doc_freq += 1;
                if (index.get(words[i]).pList == null) {
                    index.get(words[i]).pList = new Posting(fid);
                    index.get(words[i]).last = index.get(words[i]).pList;
                    index.get(words[i]).last.addPositions(numOfPrevFileWords + i + 1);
                } else {
                    index.get(words[i]).last.next = new Posting(fid);
                    index.get(words[i]).last = index.get(words[i]).last.next;
                    index.get(words[i]).last.addPositions(numOfPrevFileWords + i + 1);
                }
            } else {
                index.get(words[i]).last.dtf += 1;
                index.get(words[i]).last.addPositions(numOfPrevFileWords + i + 1);
            }
            index.get(words[i]).term_freq += 1;
            if (words[i].equalsIgnoreCase("lattice")) {
                System.out.println("  <<" + index.get(words[i]).getPosting(1) + ">> " + ln);
            }
        }
        return flen;
    }

    //-----------------------------------------------
    /*
    This function gives a unique ID for each file and calls indexOneLine function to
    index each word to the corresponding file.
     */
    public void buildIndex(String[] files) {  // from disk not from the internet
        int fid = 0;
        for (String fileName : files) {
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                if (!sources.containsKey(fileName)) {
                    sources.put(fid, new SourceRecord(fid, fileName, fileName, "notext"));
                }
                String ln;
                int flen = 0;
                while ((ln = file.readLine()) != null) {
                    flen += indexOneLine(ln, fid, flen);
                }
                sources.get(fid).length = flen;

            } catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            fid++;
        }
        //   printDictionary();
    }

    //-----------------------------------------------
    /*
    This function links each two words in the string 'bigram' to the corresponding file using 'fileId'.
     */
    public int indexBiWord(String bigram, int fileId) {
        bigram = bigram.toLowerCase();

        String[] words = bigram.split("\\W+");
        for (String word : words) {
            if (stopWord(word)) {
                return 0;  // If either word in the bigram is a stop word, skip indexing this bigram
            }
        }

        // Stemming each word in the bigram
        String stemmedBigram = "";
        for (String word : words) {
            stemmedBigram += stemWord(word) + " ";
        }
        stemmedBigram = stemmedBigram.trim();  // Remove trailing space

        // Check if the stemmed bigram is not in the dictionary and add it if absent
        if (!index.containsKey(stemmedBigram)) {
            index.put(stemmedBigram, new DictEntry());
        }

        // Add document id to the posting list
        if (!index.get(stemmedBigram).postingListContains(fileId)) {
            index.get(stemmedBigram).doc_freq += 1;  // Increment document frequency
            if (index.get(stemmedBigram).pList == null) {
                index.get(stemmedBigram).pList = new Posting(fileId);
                index.get(stemmedBigram).last = index.get(stemmedBigram).pList;
            } else {
                index.get(stemmedBigram).last.next = new Posting(fileId);
                index.get(stemmedBigram).last = index.get(stemmedBigram).last.next;
            }
        } else {
            index.get(stemmedBigram).last.dtf += 1;  // Increment term frequency in document
        }

        // Increment total term frequency in the collection
        index.get(stemmedBigram).term_freq += 1;

        // If the bigram includes a specific word, e.g., "lattice", you can debug or log here
        if (stemmedBigram.contains("lattice")) {
            System.out.println("  <<" + index.get(stemmedBigram).getPosting(1) + ">> " + bigram);
        }

        return 2;  // Return 2 because each bigram is made of two words
    }

    public void buildBiWordIndex(String[] files) {
        int fid = 0; // File identifier
        for (String fileName : files) {
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                if (!sources.containsKey(fileName)) {
                    sources.put(fid, new SourceRecord(fid, fileName, fileName, "notext")); // Assumes SourceRecord manages these details
                }
                String ln;
                String prevWord = null; // Store the previous word for bi-word indexing
                int flen = 0; // Length of the file in terms of words processed
                while ((ln = file.readLine()) != null) {
                    String[] words = ln.split("\\W+"); // Split the line into words
                    for (int i = 0; i < words.length; i++) {
                        if (prevWord != null) {
                            String bigram = prevWord + "_" + words[i];
                            flen += indexBiWord(bigram, fid); // Function to index a bi-word and increment file length by word count
                        }
                        prevWord = words[i]; // Update the previous word to the current for the next iteration
                    }
                }
                sources.get(fid).length = flen; // Update the source record with the file length
            } catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            fid++; // Increment file identifier for the next file
        }
    }


    //----------------------------------------------------------------------------
    /*
    It checks if the passed word is a stop word or not.
     */
    boolean stopWord(String word) {
        if (word.equals("the") || word.equals("to") || word.equals("be") || word.equals("for") || word.equals("from") || word.equals("in")
                || word.equals("a") || word.equals("into") || word.equals("by") || word.equals("or") || word.equals("and") || word.equals("that")) {
            return true;
        }
        if (word.length() < 2) {
            return true;
        }
        return false;

    }

    //----------------------------------------------------------------------------
    /*
    It returns the root of the passed word.
    form of a word before any inflectional affixes are added
     */
    String stemWord(String word) { //skip for now
        return word;
//        Stemmer s = new Stemmer();
//        s.addString(word);
//        s.stem();
//        return s.toString();
    }

    //----------------------------------------------------------------------------
    /*
    This function receives two linked list of documents IDs,
    and gets the IDs that found in both linked lists.
     */
    Posting intersect(Posting pL1, Posting pL2) {
        Posting answer = null;
        Posting last = null;
        while (pL1 != null && pL2 != null) {
            if (pL1.docId == pL2.docId) {
                if (answer == null) {
                    answer = new Posting(pL1.docId);
                    last = answer;
                } else {
                    last.next = new Posting(pL1.docId);
                    last = last.next;
                }
                pL1 = pL1.next;
                pL2 = pL2.next;
            } else if (pL1.docId < pL2.docId)
                pL1 = pL1.next;
            else
                pL2 = pL2.next;
        }
        return answer;
    }

    /*
    This function receives a string, and finds the documents that this string appears in them.
     */
    public String find_24_01(String phrase) { // any mumber of terms non-optimized search 
        String result = "";
        String[] words = phrase.split("\\W+");
        int len = words.length;

        //fix this if word is not in the hash table will crash...
        Posting posting = index.containsKey(words[0]) ? index.get(words[0]).pList : null;

        // If no postings, return an indication such as "No results found."
        if (posting == null) {
            return "No results found for '" + words[0];
        }

        int i = 1;
        while (i < len) {
            if (!index.containsKey(words[i])) {
                return "No results found for '" + words[i];
            }
            posting = intersect(posting, index.get(words[i].toLowerCase()).pList);
            i++;
        }
        while (posting != null) {
            //System.out.println("\t" + sources.get(num));
            result += "\t" + posting.docId + " - " + sources.get(posting.docId).title + " - " + sources.get(posting.docId).length + "\n";
            posting = posting.next;
        }
        return result;
    }

    public String searchBiWord(String biWordPhrase) {
        String result = "";
        // Normalize and prepare the bi-word phrase for lookup
        biWordPhrase = biWordPhrase.toLowerCase().replace(" ", "_");

        // Retrieve the posting list for the bi-word directly
        Posting posting = index.containsKey(biWordPhrase) ? index.get(biWordPhrase).pList : null;

        // If no postings, return an indication such as "No results found."
        if (posting == null) {
            return "No results found for '" + biWordPhrase.replace("_", " ") + "'.";
        }

        // Iterate through the postings and build the result string
        while (posting != null) {
            String docTitle = sources.get(posting.docId).title;
            int docLength = sources.get(posting.docId).length;
            result += "\t" + posting.docId + " - " + docTitle + " - " + docLength + "\n";
            posting = posting.next;
        }
        return result;
    }

    public String searchPositional(String positionalPhrase) {
        String result = "";
        // Normalize and prepare the bi-word phrase for lookup
        String[] positionalPhraseList = positionalPhrase.toLowerCase().split(" ");

        List<Posting> postingList = new ArrayList<>();
        for (String phrase : positionalPhraseList) {
            if (index.containsKey(phrase)) {
                postingList.add(index.get(phrase).pList);
            } else {
                if (stopWord(phrase))
                    postingList.add(null); // null value indicates a stop word to skip it
                else
                    return "No results found for " + positionalPhrase;
            }
        }

        if (!postingList.isEmpty()) {
            Posting firstPosting = postingList.get(0);
            while (firstPosting != null) {
                for (int position : firstPosting.positions) {
                    boolean found = true;
                    for (int wordNum = 1; wordNum < postingList.size(); wordNum++) {
                        if (postingList.get(wordNum) == null)
                            continue;
                        Posting currPosting = postingList.get(wordNum);

                        while (currPosting != null && currPosting.docId != firstPosting.docId) {
                            currPosting = currPosting.next;
                        }

                        if (currPosting == null) {
                            found = false;
                            break;
                        }

                        if (!currPosting.positions.contains(position + wordNum)) {
                            found = false;
                            break;
                        }
                    }
                    if (found) {
                        String docTitle = sources.get(firstPosting.docId).title;
                        int docLength = sources.get(firstPosting.docId).length;
                        result += "\t" + firstPosting.docId + " - " + docTitle + " - " + docLength + "\n";
                        break;
                    }
                }
                firstPosting = firstPosting.next;
            }
        } else {
            return "No results found for " + positionalPhrase;
        }
        return result;
    }


    //---------------------------------
    /*
    This function receives a list of words, and sorts them using bubble sort.
     */
    String[] sort(String[] words) {  //bubble sort
        boolean sorted = false;
        String sTmp;
        //-------------------------------------------------------
        while (!sorted) {
            sorted = true;
            for (int i = 0; i < words.length - 1; i++) {
                int compare = words[i].compareTo(words[i + 1]);
                if (compare > 0) {
                    sTmp = words[i];
                    words[i] = words[i + 1];
                    words[i + 1] = sTmp;
                    sorted = false;
                }
            }
        }
        return words;
    }

    //---------------------------------
    // This function stores the inverted index into a hard disk
    public void store(String storageName) {
        try {
            String pathToStorage = System.getProperty("user.dir") + "\\tmp11\\rl\\" + storageName;
            Writer wr = new FileWriter(pathToStorage);
            for (Map.Entry<Integer, SourceRecord> entry : sources.entrySet()) {
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue().URL + ", Value = " + entry.getValue().title + ", Value = " + entry.getValue().text);
                wr.write(entry.getKey().toString() + ",");
                wr.write(entry.getValue().URL.toString() + ",");
                wr.write(entry.getValue().title.replace(',', '~') + ",");
                wr.write(entry.getValue().length + ","); //String formattedDouble = String.format("%.2f", fee );
                wr.write(String.format("%4.4f", entry.getValue().norm) + ",");
                wr.write(entry.getValue().text.toString().replace(',', '~') + "\n");
            }
            wr.write("section2" + "\n");

            Iterator it = index.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                DictEntry dd = (DictEntry) pair.getValue();
                //  System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "] <" + dd.term_freq + "> =--> ");
                wr.write(pair.getKey().toString() + "," + dd.doc_freq + "," + dd.term_freq + ";");
                Posting p = dd.pList;
                while (p != null) {
                    //    System.out.print( p.docId + "," + p.dtf + ":");
                    wr.write(p.docId + "," + p.dtf + ":");
                    p = p.next;
                }
                wr.write("\n");
            }
            wr.write("end" + "\n");
            wr.close();
            System.out.println("=============EBD STORE=============");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //=========================================
    /*
    Checks if the passed file name exists.
     */
    public boolean storageFileExists(String storageName) {
        java.io.File f = new java.io.File(System.getProperty("user.dir") + "\\tmp11\\rl\\" + storageName);
        if (f.exists() && !f.isDirectory())
            return true;
        return false;

    }

    //----------------------------------------------------
    /*
    Receives a file name and write a word "end" inside it.
     */
    public void createStore(String storageName) {
        try {
            String pathToStorage = System.getProperty("user.dir") + "\\tmp11\\" + storageName;
            Writer wr = new FileWriter(pathToStorage);
            wr.write("end" + "\n");
            wr.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //----------------------------------------------------
    //load index from hard disk into memory
    public HashMap<String, DictEntry> load(String storageName) {
        try {
            String pathToStorage = System.getProperty("user.dir") + "\\tmp11\\rl\\" + storageName;
            sources = new HashMap<Integer, SourceRecord>();
            index = new HashMap<String, DictEntry>();
            BufferedReader file = new BufferedReader(new FileReader(pathToStorage));
            String ln = "";
            int flen = 0;
            while ((ln = file.readLine()) != null) {
                if (ln.equalsIgnoreCase("section2")) {
                    break;
                }
                String[] ss = ln.split(",");
                int fid = Integer.parseInt(ss[0]);
                try {
                    System.out.println("**>>" + fid + " " + ss[1] + " " + ss[2].replace('~', ',') + " " + ss[3] + " [" + ss[4] + "]   " + ss[5].replace('~', ','));

                    SourceRecord sr = new SourceRecord(fid, ss[1], ss[2].replace('~', ','), Integer.parseInt(ss[3]), Double.parseDouble(ss[4]), ss[5].replace('~', ','));
                    //   System.out.println("**>>"+fid+" "+ ss[1]+" "+ ss[2]+" "+ ss[3]+" ["+ Double.parseDouble(ss[4])+ "]  \n"+ ss[5]);
                    sources.put(fid, sr);
                } catch (Exception e) {

                    System.out.println(fid + "  ERROR  " + e.getMessage());
                    e.printStackTrace();
                }
            }
            while ((ln = file.readLine()) != null) {
                //     System.out.println(ln);
                if (ln.equalsIgnoreCase("end")) {
                    break;
                }
                String[] ss1 = ln.split(";");
                String[] ss1a = ss1[0].split(",");
                String[] ss1b = ss1[1].split(":");
                index.put(ss1a[0], new DictEntry(Integer.parseInt(ss1a[1]), Integer.parseInt(ss1a[2])));
                String[] ss1bx;   //posting
                for (int i = 0; i < ss1b.length; i++) {
                    ss1bx = ss1b[i].split(",");
                    if (index.get(ss1a[0]).pList == null) {
                        index.get(ss1a[0]).pList = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).pList;
                    } else {
                        index.get(ss1a[0]).last.next = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).last.next;
                    }
                }
            }
            System.out.println("============= END LOAD =============");
            //    printDictionary();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return index;
    }
}

//=====================================================================
