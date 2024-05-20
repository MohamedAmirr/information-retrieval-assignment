/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author ehab
 */
public class Test {
    public static int countCharacter(String str, char charToCount) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == charToCount) {
                count++;
            }
        }
        return count;
    }

    public static String removeSubstring(String str, int start, int end) {
        // Check if the start and end indices are valid
        if (start < 0 || end > str.length() || start > end) {
            return str; // Return the original string if indices are not valid
        }

        // Create a StringBuilder object from the string
        StringBuilder sb = new StringBuilder(str);

        // Remove the substring between start and end indices
        sb.delete(start, end);

        // Return the modified string
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        Index5 index = new Index5();
        //|**  change it to your collection directory
        //|**  in windows "C:\\tmp11\\rl\\collection\\"
        String files = System.getProperty("user.dir") + "\\tmp11\\rl\\collection\\";

        File file = new File(files);
        String[] fileList = file.list(); // List files in the directory

        fileList = index.sort(fileList); // Sort files if necessary
        index.N = fileList.length;

        // Build both regular and bi-word indexes
        for (int i = 0; i < fileList.length; i++) {
            fileList[i] = files + fileList[i];
        }
        index.buildIndex(fileList); // Existing method to build a standard index
        index.buildBiWordIndex(fileList); // New method to build a bi-word index
        index.store("index"); // Store the index, possibly enhancing to differentiate between indexes
        index.printDictionary(); // Print dictionary for verification

        // Test with predefined bi-word query
        String testBiWord = "we need";
        System.out.println("Bi-word Model result = \n" + index.searchBiWord(testBiWord.replace("\"", "").replace(" ", "_")));

        // Interactive search handling
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String phrase = "";
        do {
            System.out.println("Enter search phrase (include quotes for bi-word search): ");
            phrase = in.readLine();
            if (phrase.isEmpty()) break; // Exit on empty input

            List<String> result = new ArrayList<>();
            if (countCharacter(phrase, '\"') > 1) {
                while (countCharacter(phrase, '\"') > 1) {
                    int indexOfFirstQoute = phrase.indexOf('\"');
                    int indexOfSecondQoute = phrase.indexOf('\"', indexOfFirstQoute + 1);
                    String wordBetweenQuotations = phrase.substring(indexOfFirstQoute + 1, indexOfSecondQoute);
                    String[] listOfWords = wordBetweenQuotations.split(" ");
                    if (listOfWords.length == 2) {
                        ArrayList<String> resultOfBiWordIndex = new ArrayList<>(Arrays.asList(index.searchBiWord(String.join(" ", listOfWords)).split("\n")));
                        if (result.isEmpty())
                            result.addAll(resultOfBiWordIndex);
                        else {
                            for (int i = 0; i < result.size(); i++) {
                                if (!resultOfBiWordIndex.contains(result.get(i))) {
                                    result.remove(i);
                                    i--;
                                }
                            }
                        }
                    } else {
                        ArrayList<String> resultOfPositionalIndex = new ArrayList<>(Arrays.asList(index.searchPositional(String.join(" ", listOfWords)).split("\n")));
                        if (result.isEmpty())
                            result.addAll(resultOfPositionalIndex);
                        else {
                            for (int i = 0; i < result.size(); i++) {
                                if (!resultOfPositionalIndex.contains(result.get(i))) {
                                    result.remove(i);
                                    i--;
                                }
                            }
                        }
                    }
                    phrase = removeSubstring(phrase, indexOfFirstQoute, indexOfSecondQoute + 1);
                }
            }
            if (phrase.length() > 0) {
                ArrayList<String> resultOfInvertedIndex = new ArrayList<>(Arrays.asList(index.find_24_01(phrase).split("\n")));
                if (result.isEmpty())
                    result.addAll(resultOfInvertedIndex);
                else {
                    for (int i = 0; i < result.size(); i++) {
                        if (!resultOfInvertedIndex.contains(result.get(i))) {
                            result.remove(i);
                            i--;
                        }
                    }
                }
            }
            System.out.println("Boolean Model result = \n");
            for (String word : result) {
                System.out.println(word);
            }
        } while (true);
    }
}
