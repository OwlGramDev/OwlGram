package it.owlgram.android.entities.syntax_highlight.prism4j;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

public class NoSuchGrammarException
    extends NoSuchElementException {

    public NoSuchGrammarException(String grammarName) {
        super("No such grammar " + grammarName);
    }

    public static Supplier<NoSuchGrammarException> supply(String grammarName) {
        return () -> new NoSuchGrammarException(grammarName);
    }
}
