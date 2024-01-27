package SearchBox.TagSearchPaser;

import java.util.ArrayList;

import SearchBox.TagToken.Token;
import SearchBox.TagToken.Tokenizer;

public class SearchParser {
    AndBox andBox = new AndBox(new ArrayList<>());
    OrBox orBox = new OrBox(new ArrayList<>());
    PlaintextBox plaintextBox = new PlaintextBox(new ArrayList<>());

    Tokenizer input;

    String firstTag = "";

    public SearchParser(Tokenizer input) {
        this.input = input;
    }

    public void updateBoxesTags() {
        if (input.hasNext()) {
            Token currentToken = input.current();

            switch (currentToken.getType()) {

                /**
                 * [#]: tagSearch+1 -> tagSearch = 1 notes: start a tag
                 * [;]: (only has started before)tagSearch-1 -> tagSearch = 0 notes: end a tag
                 *    : (if haven't start) plain+1 -> tagSearch = 1 add a plain text into the box
                 * [&]: and+1
                 * [|]: or+1
                 */
                case START:
                    input.next();
                case TAG:
                    firstTag = input.current().getToken();
                    input.next();
                case END:
                    if (!input.hasNext()) {
                        break;
                    }
                    else {
                        input.next();
                        input = keepTagGoingIterationsOne(input);
                    }
            }
        }
    }

    /**
     * Author: Xinyi Li
     * <p>
     * (parser) It will attribute all tags in an input into three boxes(or/and/plain text) and return the first tag for search.
     *
     * @return a list of tokens
     */
    public String attributeTagsIntoBoxes() {
        updateBoxesTags();
        return firstTag + andBox.andBoxToString() + orBox.orBoxToString() + plaintextBox.textBoxToString();
    }

    public Tokenizer keepTagGoingIterationsOne(Tokenizer tkTow) {
        if (tkTow.hasNext()) {
            Token currentToken = tkTow.current();
            switch (currentToken.getType()) {
                /**
                 * [#]: tagSearch+1 -> tagSearch = 1 notes: start a tag
                 * [;]: (only has started before)tagSearch-1 -> tagSearch = 0 notes: end a tag
                 *    : (if haven't start) plain+1 -> tagSearch = 1 add a plain text into the box
                 * [&]: and+1
                 * [|]: or+1
                 */
                case AND:
                    addNextTagToAndBox(tkTow);
                    tkTow.next();
                    tkTow = keepTagGoingIterationsOne(tkTow); // iterator
                case OR:
                    addNextTagToOrBox(tkTow);
                    tkTow.next();
                    tkTow = keepTagGoingIterationsTwo(tkTow); // iterator
            }
        }
        return tkTow;
    }

    public Tokenizer keepTagGoingIterationsTwo(Tokenizer tkTow) {
        if (tkTow.hasNext()) {
            Token currentToken = tkTow.current();
            switch (currentToken.getType()) {
                /**
                 * [#]: tagSearch+1 -> tagSearch = 1 notes: start a tag
                 * [;]: (only has started before)tagSearch-1 -> tagSearch = 0 notes: end a tag
                 *    : (if haven't start) plain+1 -> tagSearch = 1 add a plain text into the box
                 * [&]: and+1
                 * [|]: or+1
                 */
                case OR:
                    addNextTagToOrBox(tkTow);
                    tkTow.next();
                    tkTow = keepTagGoingIterationsTwo(tkTow); // iterator
            }
        }
        return tkTow;
    }

    public void addNextTagToAndBox(Tokenizer tk) {
        if (tk.hasNext()) {
            tk.next();
        }
        if (tk.hasNext()) {
            tk.next();
            andBox.addAnd(tk.current().getToken());
            tk.next();
        }
    }

    public void addNextTagToOrBox(Tokenizer tk) {
        if (tk.hasNext()) {
            tk.next();
        }
        if (tk.hasNext()) {
            tk.next();
            orBox.addOr(tk.current().getToken());
            tk.next();
        }
    }

    public String getFirstTag() {
        updateBoxesTags();
        return firstTag;
    }

    public AndBox getAndBox() {
        updateBoxesTags();
        return andBox;
    }

    public OrBox getOrBox() {
        updateBoxesTags();
        return orBox;
    }
    public PlaintextBox getPlainTextBox() {
        updateBoxesTags();
        return plaintextBox;
    }
}