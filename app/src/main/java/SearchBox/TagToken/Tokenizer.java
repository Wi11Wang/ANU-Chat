package SearchBox.TagToken;

import java.util.Scanner;

public class Tokenizer {
    private String buffer;          // String to be transformed into tokens each time next() is called.
    private Token currentToken;     // The current token. The next token is extracted when next() is called.

    /**
     * Once running, provide a mathematical string to the terminal
     */
    public static void main(String[] args) {
        // Create a scanner to get the user's input.
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNext()) {
            String input = scanner.nextLine();
            if (input.equals("q"))
                break;
            Tokenizer tokenizer = new Tokenizer(input);
            while (tokenizer.hasNext()) {
                System.out.print(tokenizer.current() + " ");
                tokenizer.next();
            }
            System.out.println();
        }
    }


    public Tokenizer(String text) {
        buffer = text;          // save input text (string)
        next();                 // extracts the first token.
    }

    /**
     * This function will find and extract a next token from {@code _buffer} and
     * save the token to {@code currentToken}.
     */
    public void next() {
        buffer = buffer.trim();     // remove whitespace

        if (buffer.isEmpty()) {
            currentToken = null;    // if there's no string left, set currentToken null and return
            return;
        }

        char firstChar = buffer.charAt(0);
        if (firstChar == '&')
            currentToken = new Token("&", Token.Type.AND);
        else if (firstChar == '|')
            currentToken = new Token("|", Token.Type.OR);
        else if (firstChar == '#')
            currentToken = new Token("#", Token.Type.START);
        else if (firstChar == ';')
            currentToken = new Token(";", Token.Type.END);
        else {
            String str = "";
            for (int i=0; i < buffer.length(); i++) {
                char word = buffer.charAt(i);
                if (Character.isDigit(word) || Character.isLetter(word)) {
                    str = str + word;
                } else {
                    break;
                }
            }
            if (!"".equals(str))
                currentToken = new Token(str, Token.Type.TAG);
            else
                throw new Token.IllegalTokenException("Insert VALID Token!");
        }

        // Remove the extracted token from buffer
        int tokenLen = currentToken.getToken().length();
        buffer = buffer.substring(tokenLen);
    }

    /**
     * Returns the current token extracted by {@code next()}
     * @return type: Token
     */
    public Token current() {
        return currentToken;
    }

    /**
     * Check whether there still exists another tokens in the buffer or not
     * @return type: boolean
     */
    public boolean hasNext() {
        return currentToken != null;
    }
}