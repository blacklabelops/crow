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

public class BoundedLineWriterEolTest extends BoundedLineWriter {

	private List<String> output;

	private List<String> expected;

	public BoundedLineWriterEolTest() {
		super(true);
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
	public void testWrite_Linefeed_NoEnding() throws IOException {
		expected.add("\n");
		this.write("\n");
		this.close();
		assertEquals(Integer.valueOf(0), Integer.valueOf(this.output.size()));
	}

	@Test
	public void testWrite_CarriageReturn_NoEnding() throws IOException {
		expected.add("\r");
		this.write("\r");
		this.close();
		assertEquals(Integer.valueOf(0), Integer.valueOf(this.output.size()));
	}

	@Test
	public void testWrite_Combined_NoEnding() throws IOException {
		this.write("\r\n");
		this.close();
		assertEquals(Integer.valueOf(0), Integer.valueOf(this.output.size()));
	}

	@Test
	public void testWrite_CarriageReturnInMiddle_NoEnding() throws IOException {
		expected.add("Hello");
		expected.add("DuDu");
		this.write("Hello\rDuDu");
		this.close();
		assertThat(output, is(expected));
	}

	@Test
	public void testWrite_OnlyCarriageReturnLinefeedEnding_NoEnding() throws IOException {
		this.write("\r\n");
		this.close();
		assertEquals(Integer.valueOf(0), Integer.valueOf(this.output.size()));
	}

	@Test
	public void testWrite_SimpleString() throws IOException {
		expected.add("Hello");
		this.write("Hello");
		this.close();
		assertThat(output, is(expected));
	}

	@Test
	public void testWrite_SimpleStringWithLineEnding() throws IOException {
		expected.add("Hello");
		this.write("Hello\n");
		this.close();
		assertThat(output, is(expected));
	}

	@Test
	public void testWrite_TwoDifferentLineEndings() throws IOException {
		expected.add("Hello");
		expected.add("Du");
		this.write("Hello\nDu\r");
		this.close();
		assertThat(output, is(expected));
	}

	@Test
	public void testWrite_DoubleLineEndings() throws IOException {
		expected.add("Hello");
		expected.add("Du");
		this.write("Hello\r\nDu\r\n");
		this.close();
		assertThat(output, is(expected));
	}

	@Test
	public void testWrite_OneEnding() throws IOException {
		expected.add("Hello");
		expected.add("DuDu");
		this.write("Hello\r\nDuDu");
		this.close();
		assertThat(output, is(expected));
	}

	@Test
	public void testWrite_MultipleEndings() throws IOException {
		expected.add("Hello");
		expected.add("DuDu");
		expected.add("dddddddd");
		expected.add("qwdqw");
		this.write("Hello\r\nDuDu\ndddddddd\rqwdqw");
		this.close();
		assertThat(output, is(expected));
	}

}
