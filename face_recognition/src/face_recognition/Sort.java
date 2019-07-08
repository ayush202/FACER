/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package face_recognition;

/**
 *
 * @author ayush
 */
public class Sort implements Comparable<Sort>{
   String label,rep;

    public Sort(String label, String rep) {
        this.label = label;
        this.rep = rep;
    }

    @Override
    public int compareTo(Sort o) {
        return this.label.compareTo(o.label);
    }
}
