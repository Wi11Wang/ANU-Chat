package SearchBox.TagToken;

import java.util.Objects;

/**
 * Token class to save extracted token from tokenizer.
 * Each token has its surface form saved in {@code token}
 * and type saved in {@code type} which is one of the predefined type in Type enum.
 * The following are the different types of tokens:
 * START: #
 * TAG: String
 * END: ;
 * AND: &
 * OR: |
 */
public class Token {
    public enum Type {TAG, AND, OR, START, END}

    private String token; // Token representation in String form.
    private Type type;    // Type of the token.
    /**
     * The following exception will be thrown if a tokenizer attempts to tokenize something that is not of one
     * of the types of tokens.
     */
    public static class IllegalTokenException extends IllegalArgumentException {
        public IllegalTokenException(String errorMessage) {
            super(errorMessage);
        }
    }

    public Token(String token, Type type) {
        this.token = token;
        this.type = type;
    }

    @Override
    public String toString() {
        if (type == Type.TAG) {
            return "TAG(" + token + ")";
        } else {
            return type + "";
        }
    }

    public String getToken() {
        return token;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true; // Same hashcode.
        if (!(other instanceof Token)) return false; // Null or not the same type.
        return this.type == ((Token) other).getType() && this.token.equals(((Token) other).getToken()); // Values are the same.
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, type);
    }

}
