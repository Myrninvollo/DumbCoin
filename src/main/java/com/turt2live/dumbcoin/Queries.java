package com.turt2live.dumbcoin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Used for getting preset queries from a file
 *
 * @author turt2live
 */
public class Queries {

    public enum Query {
        CREATE_TABLE,
        UPDATE_BALANCE_MOD,
        UPDATE_BALANCE_SET,
        GET_BALANCE,
        GET_ALL_BALANCES,
        REMOVE_LEGACY;
    }

    private Map<Query, String> queries = new HashMap<Query, String>();

    public Queries(DumbCoin plugin, String resourceName) {
        if (plugin == null || resourceName == null) throw new IllegalArgumentException();
        try {
            InputStream stream = plugin.getResource(resourceName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            Map<String, String> vars = new HashMap<String, String>();
            StringBuilder activeLine = new StringBuilder();
            Query lastQuery = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    if (line.startsWith("-- VAR ")) {
                        // -- VAR <name> <val>
                        String[] parts = line.split(" ");
                        if (parts.length != 4)
                            plugin.getLogger().warning("[MySQL] Invalid line: " + line);
                        else
                            vars.put(parts[2], parts[3]);
                    } else if (line.startsWith("-- ACT ")) {
                        // -- ACT <query type>
                        if (lastQuery != null) {
                            queries.put(lastQuery, activeLine.toString().trim());
                        }
                        String[] parts = line.split(" ");
                        if (parts.length != 3)
                            plugin.getLogger().warning("[MySQL] Invalid line: " + line);
                        else {
                            lastQuery = Query.valueOf(parts[2]);
                            activeLine = new StringBuilder();
                        }
                    } else {
                        for (String s : vars.keySet()) {
                            line = line.replaceAll("\\{" + s + "\\}", vars.get(s));
                        }
                        activeLine.append(line).append(" ");
                    }
                }
            }
            if (lastQuery != null) {
                queries.put(lastQuery, activeLine.toString().trim());
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets a particular query from the storage
     *
     * @param query the query to get
     * @return the resulting query, may be null if not defined
     */
    public String getQuery(Query query) {
        return queries.get(query);
    }

}
