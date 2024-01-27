package wang.bogong.anuchat;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import SearchBox.TagSearchPaser.SearchParser;
import SearchBox.TagToken.Tokenizer;

public class ParserTest {

    /*
        We suggest you test your parser by also running it in your own terminal and checking the output.

        We advise you write additional tests to increase the confidence of your implementation. Simply getting these
	    tests correct does not mean your solution is robust enough pass the marking tests.
     */


    private static final String SIMPLE_CASE = "#tag;";
    private static final String MID_CASE = "#yay;";
    private static final String COMPLEX_CASE = "#yay;&#banan;";
    private static final String FOURTHCASE = "#yay;|#air;";
    private static final String FIFTHCASE = "#yay;&#banan;|#air;";
    private static final String SIXTHCASE = "#yay;|#banan;|#banan;";
    private static final String LONGCASE = "#yay;|#banan;|#banan;|#banan;";
    private static final String LONGANDCASE = "#yay;&#banan;&#banan;|#banan;";


    private static final String ERRORCASE = ";+";


    private static final String[] testExample = new String[]{"2+1", "2-1", "2*1", "2/1"};


    @Test(timeout=1000)
    public void testSimleAdd() {
        Tokenizer simpleTokenizer = new Tokenizer(SIMPLE_CASE);
        String t1 = new SearchParser(simpleTokenizer).attributeTagsIntoBoxes();
        assertEquals("tag", t1);
    }

    @Test(timeout=1000)
    public void testSimpleAdd() {
        Tokenizer simpleTokenizer = new Tokenizer(SIMPLE_CASE);
        String t1 = new SearchParser(simpleTokenizer).getFirstTag();
        assertEquals("tag", t1);
    }

    @Test(timeout=1000)
    public void testMidAdd() {
        Tokenizer midTokenizer = new Tokenizer(MID_CASE);
        String t1 = new SearchParser(midTokenizer).attributeTagsIntoBoxes();
        assertEquals("yay", t1);
    }

    @Test(timeout=1000)
    public void testComplexAdd() {
        Tokenizer simpleTokenizer = new Tokenizer(COMPLEX_CASE);
        String t1 = new SearchParser(simpleTokenizer).attributeTagsIntoBoxes();
        assertEquals("yaybanan", t1);
    }

    @Test(timeout=1000)
    public void testComplexAndBox() {
        Tokenizer simpleTokenizer = new Tokenizer(COMPLEX_CASE);
        String t1 = new SearchParser(simpleTokenizer).getAndBox().andBoxToString();
        assertEquals("banan", t1);
    }

    @Test(timeout=1000)
    public void testFourthAdd() {
        Tokenizer simpleTokenizer = new Tokenizer(FOURTHCASE);
        String t1 = new SearchParser(simpleTokenizer).attributeTagsIntoBoxes();
        assertEquals("yayair", t1);
    }

    @Test(timeout=1000)
    public void testComplexOr() {
        Tokenizer simpleTokenizer = new Tokenizer(FOURTHCASE);
        String t1 = new SearchParser(simpleTokenizer).getOrBox().orBoxToString();
        assertEquals("air", t1);
    }

    @Test(timeout=1000)
    public void testFifthAdd() {
        Tokenizer simpleTokenizer = new Tokenizer(FIFTHCASE);
        String t1 = new SearchParser(simpleTokenizer).attributeTagsIntoBoxes();
        assertEquals("yaybananair", t1);
    }

    @Test(timeout=1000)
    public void testSixthAdd() {
        Tokenizer simpleTokenizer = new Tokenizer(SIXTHCASE);
        String t1 = new SearchParser(simpleTokenizer).attributeTagsIntoBoxes();
        assertEquals("yaybananbanan", t1);
    }

    @Test(timeout=1000)
    public void testLongAdd() {
        Tokenizer simpleTokenizer = new Tokenizer(LONGCASE);
        String t1 = new SearchParser(simpleTokenizer).attributeTagsIntoBoxes();
        assertEquals("yaybananbananbanan", t1);
    }

    @Test(timeout=1000)
    public void testOnlyOrAdd() {
        Tokenizer simpleTokenizer = new Tokenizer(LONGCASE);
        String t1 = new SearchParser(simpleTokenizer).getOrBox().orBoxToString();
        assertEquals("bananbananbanan", t1);
    }

    @Test(timeout=1000)
    public void testOnlyAndAdd() {
        Tokenizer simpleTokenizer = new Tokenizer(LONGANDCASE);
        String t1 = new SearchParser(simpleTokenizer).getAndBox().andBoxToString();
        assertEquals("bananbanan", t1);
    }
}

