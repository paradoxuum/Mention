package me.condolence.command;

import java.util.ArrayList;
import java.util.List;

public class TabComplete {
    public static List<String> getMatching(String[] args, List<String> toMatch) {
        String lastArg = args[args.length - 1];
        List<String> matching = new ArrayList<>();
        if (!lastArg.isEmpty()) {
            for (String s : toMatch) {
                if (s.regionMatches(true, 0, lastArg, 0, lastArg.length())) {
                    matching.add(s);
                }
            }
        } else {
            matching.addAll(toMatch);
        }
        return matching;
    }
}
