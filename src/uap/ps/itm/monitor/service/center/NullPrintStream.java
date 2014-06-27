package uap.ps.itm.monitor.service.center;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

public class NullPrintStream extends PrintStream {

	public NullPrintStream(OutputStream out) {
		super(out);
		
	}

	@Override
	public void print(boolean b) {
		
	}

	@Override
	public void print(char c) {
		
	}

	@Override
	public void print(char[] s) {
		
	}

	@Override
	public void print(double d) {
		
	}

	@Override
	public void print(float f) {
	}

	@Override
	public void print(int i) {
	}

	@Override
	public void print(long l) {
	}

	@Override
	public void print(Object obj) {
	}

	@Override
	public void print(String s) {
	}

	@Override
	public PrintStream printf(Locale l, String format, Object... args) {
		return this;
	}

	@Override
	public PrintStream printf(String format, Object... args) {
		return this;
	}

	@Override
	public void println() {
		super.println();
	}

	@Override
	public void println(boolean x) {
	}

	@Override
	public void println(char x) {
	}

	@Override
	public void println(char[] x) {
	}

	@Override
	public void println(double x) {
	}

	@Override
	public void println(float x) {
	}

	@Override
	public void println(int x) {
	}

	@Override
	public void println(long x) {
	}

	@Override
	public void println(Object x) {
	}

	@Override
	public void println(String x) {
	}	

}
