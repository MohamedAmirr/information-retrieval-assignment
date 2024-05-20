/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ehab
 */

public class Posting {

    public Posting next = null;
    int docId;
    int dtf = 1;
    Set<Integer> positions;


    public void addPositions(int position) {
        positions.add(position);
    }

    Posting(int id, int t) {
        docId = id;
        dtf = t;
    }

    Posting(int id) {
        positions = new HashSet<>();
        docId = id;
    }
}