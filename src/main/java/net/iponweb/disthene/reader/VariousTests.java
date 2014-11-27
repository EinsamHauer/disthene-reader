package net.iponweb.disthene.reader;

import com.google.gson.Gson;

/**
 * @author Andrei Ivanov
 */
public class VariousTests {

    public static void main(String[] args) {
        Gson gson = new Gson();
        Double values[] = new Double[100];
/*
        for(int i = 0; i < values.length; i++) {
            values[i] = (double) i;
        }
*/
        System.out.println(gson.toJson(values));

    }

}
