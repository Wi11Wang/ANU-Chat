package wang.bogong.anuchat;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import SearchBox.TagToken.Token;
import SearchBox.TagToken.Tokenizer;

import org.junit.Test;


public class TokenizerTest {

    private static Tokenizer tokenizer;
    private static final String MID = "#apple;|#orange;";
    private static final String ADVANCED = "#apple;&#banana;";
    private static final String BASIC = "#apple;banana;";
    private static final String ADD_AND_SUB = "#&";

    @Test(timeout=1000)
    public void testStartToken() {
        tokenizer = new Tokenizer(ADD_AND_SUB);

        // check the type of the first token
        assertEquals("wrong token type", Token.Type.START, tokenizer.current().getType());

        // check the actual token value"
        assertEquals("wrong token value", "#", tokenizer.current().getToken());
    }

    @Test(timeout=1000)
    public void testAndToken() {
        tokenizer = new Tokenizer(ADD_AND_SUB);

        // extract next token (just to skip first passCase token)
        tokenizer.next();

        // check the type of the first token
        assertEquals("wrong token type", Token.Type.AND, tokenizer.current().getType());

        // check the actual token value
        assertEquals("wrong token value", "&", tokenizer.current().getToken());
    }

    @Test(timeout=1000)
    public void testFirstToken(){
        tokenizer = new Tokenizer(ADVANCED);

        // check the type of the first token
        assertEquals("wrong token type", Token.Type.START, tokenizer.current().getType());
        // check the actual token value
        assertEquals("wrong token value", "#", tokenizer.current().getToken());
    }

    @Test(timeout=1000)
    public void testMidTokenResult() {
        //"#apple;|#orange;"
        tokenizer = new Tokenizer(MID);

        // test first token #
        assertEquals(Token.Type.START, tokenizer.current().getType());

        // test second token (apple)TAG
        tokenizer.next();
        assertEquals(new Token("apple",Token.Type.TAG), tokenizer.current());

        // test third token ;
        tokenizer.next();
        assertEquals(Token.Type.END, tokenizer.current().getType());

        // test forth token |
        tokenizer.next();
        assertEquals(Token.Type.OR, tokenizer.current().getType());

        // test fifth token #
        tokenizer.next();
        assertEquals(Token.Type.START, tokenizer.current().getType());

        // test forth token (banana)TAG
        tokenizer.next();
        assertEquals(new Token("orange",Token.Type.TAG), tokenizer.current());

        // test fifth token ;
        tokenizer.next();
        assertEquals(Token.Type.END, tokenizer.current().getType());

    }

    @Test(timeout=1000)
    public void testBasicTokenResult() {
        //"#apple;|#orange;"
        tokenizer = new Tokenizer(BASIC);

        // test first token #
        assertEquals(Token.Type.START, tokenizer.current().getType());

        // test second token apple(TAG)
        tokenizer.next();
        assertEquals(new Token("apple",Token.Type.TAG), tokenizer.current());

        // test third token ;
        tokenizer.next();
        assertEquals(Token.Type.END, tokenizer.current().getType());

        // test forth token banana(TAG)
        tokenizer.next();
        assertEquals(new Token("banana",Token.Type.TAG), tokenizer.current());

        // test fifth token ;
        tokenizer.next();
        assertEquals(Token.Type.END, tokenizer.current().getType());

    }

    @Test(timeout=1000)
    public void testAdvancedTokenResult(){
        // "#apple;&#banana;"
        tokenizer = new Tokenizer(ADVANCED);

        // test first token #
        assertEquals(Token.Type.START, tokenizer.current().getType());

        // test second token apple(TAG)
        tokenizer.next();
        assertEquals(new Token("apple", Token.Type.TAG), tokenizer.current());

        // test third token ;
        tokenizer.next();
        assertEquals( Token.Type.END, tokenizer.current().getType());

        // test fourth token &
        tokenizer.next();
        assertEquals(Token.Type.AND, tokenizer.current().getType());

        // test fifth token #
        tokenizer.next();
        assertEquals(Token.Type.START, tokenizer.current().getType());

        // test sixth token banana(Tag)
        tokenizer.next();
        assertEquals(new Token("banana", Token.Type.TAG), tokenizer.current());

        // test seventh token ;
        tokenizer.next();
        assertEquals(Token.Type.END, tokenizer.current().getType());
    }

    @Test(timeout=1000)
    public void testExceptionToken() {
        // Test a series of non-identifiable tokens
        assertThrows(Token.IllegalTokenException.class, () -> {
            tokenizer = new Tokenizer("1=1");
            tokenizer.next();
        });

        assertThrows(Token.IllegalTokenException.class, () -> {
            tokenizer = new Tokenizer("$%^12");
            int i = -1;
            while (i++ < "$%^12".length()) {
                System.out.println(i);
                tokenizer.next();
            }
        });
    }
}
