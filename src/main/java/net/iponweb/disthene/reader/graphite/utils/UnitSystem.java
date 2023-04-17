package net.iponweb.disthene.reader.graphite.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrei Ivanov
 */
public enum UnitSystem {
    BINARY("binary"),
    SI("si"),
    NONE("");

    private final List<Unit> prefixes = new ArrayList<>();

    UnitSystem(String system) {
        switch (system) {
            case "binary":
                prefixes.add(new Unit("Pi", Math.pow(1024.0, 5)));
                prefixes.add(new Unit("Ti", Math.pow(1024.0, 4)));
                prefixes.add(new Unit("Gi", Math.pow(1024.0, 3)));
                prefixes.add(new Unit("Mi", Math.pow(1024.0, 2)));
                prefixes.add(new Unit("Ki", 1024.0));
                break;
            case "si":
                prefixes.add(new Unit("P", Math.pow(1000.0, 5)));
                prefixes.add(new Unit("T", Math.pow(1000.0, 4)));
                prefixes.add(new Unit("G", Math.pow(1000.0, 3)));
                prefixes.add(new Unit("M", Math.pow(1000.0, 2)));
                prefixes.add(new Unit("K", 1000.0));
                break;
            default:
                break;
        }
    }

    public List<Unit> getPrefixes() {
        return prefixes;
    }
}
