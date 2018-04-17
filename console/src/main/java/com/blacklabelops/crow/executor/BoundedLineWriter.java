package com.blacklabelops.crow.executor;

import java.io.IOException;
import java.io.Writer;

public abstract class BoundedLineWriter extends Writer {

	private char[] lineBuffer;

	private int bufferPosition;

	private int maxLineSize = 8192;

	public BoundedLineWriter() {
		super();
	}

	public BoundedLineWriter(int lineLimitChars) {
		super();
		this.maxLineSize = lineLimitChars;
	}

	protected abstract void writeLine(String line);

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		synchronized (lock) {
			if (this.lineBuffer == null) {
				this.lineBuffer = new char[maxLineSize];
				this.bufferPosition = -1;
			}
			boolean peek = false;
			int writePosition = -1;
			for (int i = 0; i < len; i++) {
				boolean eol = false;
				char c = cbuf[off + i];
				this.lineBuffer[++this.bufferPosition] = c;
				if (c == '\r') {
					if (peek) {
						eol = true;
					}
					writePosition = this.bufferPosition;
					peek = true;
				} else if (c == '\n') {
					eol = true;
					peek = false;
					writePosition = -1;
				} else {
					if (peek) {
						eol = true;
					}
					peek = false;
				}
				boolean bufferFull = this.bufferPosition == (maxLineSize - 1);
				if (eol || bufferFull) {
					if (writePosition != -1 && !bufferFull) {
						int length = 1 + writePosition;
						this.writeLine(String.valueOf(this.lineBuffer, 0, length));
						this.lineBuffer[0] = c;
						this.bufferPosition = 0;
						writePosition = -1;
					} else {
						int length = 1 + this.bufferPosition;
						this.writeLine(String.valueOf(this.lineBuffer, 0, length));
						this.bufferPosition = -1;
						writePosition = -1;
					}
					eol = false;
				}
			}

		}

	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void close() throws IOException {
		synchronized (lock) {
			if (this.lineBuffer != null) {
				if (this.bufferPosition != -1) {
					this.writeLine(String.valueOf(this.lineBuffer, 0, this.bufferPosition + 1));
				}
			}
		}
	}

}
