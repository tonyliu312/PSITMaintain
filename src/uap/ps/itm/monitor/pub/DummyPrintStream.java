package uap.ps.itm.monitor.pub;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import uap.ps.itm.monitor.service.loganalyze.LogXmlFormater;

/**
 * @author 赵国滨 2010-8-30
 * 
 * 流重定向
 * 
 */
public class DummyPrintStream extends PrintStream {

	private static Logger logger = null;

	W w;

	public DummyPrintStream(OutputStream out, boolean debug) {
		super(out);
		if (debug) {
			w = new DW();
		} else {
			w = new EW();
		}
	}

	@Override
	public void print(boolean b) {
		w.write("" + b);
	}

	@Override
	public void print(char c) {
		w.write("" + c);
	}

	@Override
	public void print(char[] s) {
		w.write(String.valueOf(s));
	}

	@Override
	public void print(double d) {
		w.write("" + d);
	}

	@Override
	public void print(float f) {
		w.write("" + f);
	}

	@Override
	public void print(int i) {
		w.write("" + i);
	}

	@Override
	public void print(long l) {
		w.write("" + l);
	}

	@Override
	public void print(Object obj) {
		w.write("" + obj);
	}

	@Override
	public void print(String s) {
		w.write("" + s);
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
		w.write(x + "\n");
	}

	@Override
	public void println(char x) {
		w.write(x + "\n");
	}

	@Override
	public void println(char[] x) {
		w.write(String.valueOf(x) + "\n");
	}

	@Override
	public void println(double x) {
		w.write(x + "\n");
	}

	@Override
	public void println(float x) {
		w.write(x + "\n");
	}

	@Override
	public void println(int x) {
		w.write(x + "\n");
	}

	@Override
	public void println(long x) {
		w.write(x + "\n");
	}

	@Override
	public void println(Object x) {
		w.write(x + "\n");
	}

	@Override
	public void println(String x) {
		w.write(x + "\n");
	}

	private static interface W {
		public void write(String s);
	}

	private static class DW implements W {
		public void write(String s) {
			getLogger().info(Toolkit.getCurTime() + " " + s);
		}
	}

	private static class EW implements W {
		public void write(String s) {
			getLogger().warning(Toolkit.getCurTime() + " " + s);
		}
	}

	public static Logger getLogger() {
		try {
			if (!new File("./logs").exists())
				new File("./logs").mkdir();
			if (!new File("./logs/system").exists())
				new File("./logs/system").mkdir();
			if (logger == null) {
				logger = Logger.getLogger("sysout");
				Handler[] handler = logger.getHandlers();
				if (handler != null) {
					for (int i = 0; i < handler.length; i++)
						logger.removeHandler(handler[i]);
				}
				FileHandler fh1 = new FileHandler(
						"./logs/system/system_out.log", 5120000, 10, true);
				fh1.setFormatter(new LogXmlFormater());
				logger.addHandler(fh1);
				Handler h[] = logger.getParent().getHandlers();
				for (int i = 0; i < h.length; i++) {
					logger.getParent().removeHandler(h[i]);
				}
			}
			return logger;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
