/*******************************************************************************
 * Copyright (C) 2014 Travis Ralston (turt2live)
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

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
     *
     * @return the resulting query, may be null if not defined
     */
    public String getQuery(Query query) {
        return queries.get(query);
    }

}
