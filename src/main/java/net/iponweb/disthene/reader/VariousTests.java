package net.iponweb.disthene.reader;

import com.google.gson.Gson;

import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class VariousTests {

    public static void main(String[] args) throws Exception {
        Gson gson = new Gson();
        Double values[] = new Double[100];
/*
        for(int i = 0; i < values.length; i++) {
            values[i] = (double) i;
        }
*/
        List paths = PathsService.getInstance().getPaths("bidswitch", "userverlua-eu-gce-1.userver.requests.path.*");
        System.out.println(gson.toJson(paths));

    }

}
