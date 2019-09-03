import com.utilities.GapBuffer;
import org.junit.*;

public class GapBufferTest {
    GapBuffer buffer;
    final int bufferSize = 10;

    @Before
    public void setUp() {
        buffer = new GapBuffer(bufferSize);
    }

    @Test
    public void basicInsertTextTest() {
        String content = "abc";
        buffer.insert("abc", 0);

        Assert.assertEquals(content, buffer.toString());
    }

    @Test
    public void gapShrinkTest() {
        String content = "abcedgh";
        int len = content.length();

        buffer.insert(content, 0);

        Assert.assertEquals(buffer.getGapSize(), bufferSize - len);
    }

    @Test
    public void gapExpandTest() {
        String content = "abcedghigh";
        int sizeBeforeInsertion = buffer.getGapSize();

        buffer.insert(content, 0);

        Assert.assertEquals(sizeBeforeInsertion, buffer.getGapSize());
    }
}
