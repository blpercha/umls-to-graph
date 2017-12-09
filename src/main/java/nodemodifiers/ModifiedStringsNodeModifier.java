package nodemodifiers;

import graph.Node;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class ModifiedStringsNodeModifier implements NodeModifier {
    @Override
    public void modify(Node node) {
        Set<String> variableStrings = new HashSet<>();
        for (String string : node.getStrings()) {
            variableStrings.addAll(addApostrophes(string));
            variableStrings.addAll(addVariableWords(string));
        }
        node.addStrings(variableStrings);
    }

    private Set<String> addApostrophes(final String title) {
        final Set<String> result = new HashSet<>();
        final int pos = title.indexOf("'s");
        if (pos != -1 && (pos + 1 >= title.length() - 1 || !Character.isLetterOrDigit(title.charAt(pos + 2)))) {
            result.add(title.substring(0, pos) + title.substring(pos + 1));
            result.add(title.substring(0, pos) + title.substring(pos + 2));
        }
        return result;
    }

    private Set<String> addVariableWords(final String title) {
        final Set<String> result = new HashSet<>();
        final String[] data = title.split(Pattern.quote(", "));
        if (data.length == 2) {
            result.add(data[0] + " " + data[1]);
            result.add(data[1] + " " + data[0]);
        }
        return result;
    }
}
