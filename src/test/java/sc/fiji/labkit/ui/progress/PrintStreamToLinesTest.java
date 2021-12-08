
package sc.fiji.labkit.ui.progress;

import sc.fiji.labkit.ui.utils.progress.PrintStreamToLines;
import org.junit.Test;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PrintStreamToLinesTest {

	@Test
	public void testSplit() {
		List<String> lines = new ArrayList<>();
		PrintStream out = PrintStreamToLines.printStreamToLines(lines::add);
		out.print("Hello\nWorld\n");
		assertEquals(Arrays.asList("Hello", "World"), lines);
	}

	@Test
	public void testPrefix() {
		List<String> lines = new ArrayList<>();
		PrintStream out = PrintStreamToLines.printStreamToLines(lines::add);
		out.print("Hello");
		out.print(" ");
		out.print("World!\n");
		assertEquals(Arrays.asList("Hello World!"), lines);
	}

	@Test
	public void testEmptyLines() {
		List<String> lines = new ArrayList<>();
		PrintStream out = PrintStreamToLines.printStreamToLines(lines::add);
		out.print("\n\na\n\n");
		assertEquals(Arrays.asList("", "", "a", ""), lines);
	}
}
