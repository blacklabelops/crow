package com.blacklabelops.crow.console.executor;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.blacklabelops.crow.console.executor.BoundedLineWriter;

public class BoundedLineWriterArrayBoundsTest extends BoundedLineWriter {

	private List<String> output;

	private List<String> expected;

	public BoundedLineWriterArrayBoundsTest() {
		super(2);
	}

	@Before
	public void setup() {
		output = new LinkedList<>();
		expected = new LinkedList<>();
	}

	@Override
	protected void writeLine(String line) {
		output.add(line);
	}

	@Test
	public void testWrite_NoInput() throws IOException {
		this.write("");
		this.close();
		assertEquals(Integer.valueOf(0), Integer.valueOf(this.output.size()));
	}

	@Test
	public void testWrite_LinfeedCarriageReturnOverBufferEnds() throws IOException {
		expected.add("H\r");
		expected.add("\n");
		this.write("H\r\n");
		this.close();
		assertThat(output, is(expected));
	}

	@Test
	public void testWrite_OnlyLinefeedEnding() throws IOException {
		expected.add("\n");
		this.write("\n");
		this.close();
		assertThat(output, is(expected));
	}

	@Test
	public void testWrite_OnlyCarriageReturnEnding() throws IOException {
		expected.add("\r");
		this.write("\r");
		this.close();
		assertThat(output, is(expected));
	}

	@Test
	public void testWrite_OnlyCarriageReturnLinefeedEnding() throws IOException {
		expected.add("\r\n");
		this.write("\r\n");
		this.close();
		assertThat(output, is(expected));
	}

	@Test
	public void testWrite_SimpleString() throws IOException {
		expected.add("He");
		expected.add("ll");
		expected.add("o");
		this.write("Hello");
		this.close();
		assertThat(output, is(expected));
	}

	@Test
	public void testWrite_SimpleStringWithLineEnding() throws IOException {
		expected.add("He");
		expected.add("ll");
		expected.add("o\n");
		this.write("Hello\n");
		this.close();
		assertThat(output, is(expected));
	}

	@Test
	public void testWrite_TwoDifferentLineEndings() throws IOException {
		expected.add("He");
		expected.add("ll");
		expected.add("o\n");
		expected.add("Du");
		expected.add("\r");
		this.write("Hello\nDu\r");
		this.close();
		assertThat(output, is(expected));
	}

	@Test
	public void testWrite_DoubleLineEndings() throws IOException {
		expected.add("He");
		expected.add("ll");
		expected.add("o\r");
		expected.add("\n");
		expected.add("Du");
		expected.add("\r\n");
		this.write("Hello\r\nDu\r\n");
		this.close();
		assertThat(output, is(expected));
	}

	@Test
	public void testWrite_OneEnding() throws IOException {
		expected.add("He");
		expected.add("ll");
		expected.add("o\r");
		expected.add("\n");
		expected.add("Du");
		expected.add("Du");
		this.write("Hello\r\nDuDu");
		this.close();
		assertThat(output, is(expected));
	}

}
