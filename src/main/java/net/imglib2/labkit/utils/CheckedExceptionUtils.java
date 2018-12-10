
package net.imglib2.labkit.utils;

public class CheckedExceptionUtils {

	public static void run(RunnableWithException r) {
		try {
			r.run();
		}
		catch (Exception e) {
			if (e instanceof RuntimeException) throw (RuntimeException) e;
			throw new RuntimeException(e);
		}
	}

	public static <R> R run(SupplierWithException<R> r) {
		try {
			return r.get();
		}
		catch (Exception e) {
			if (e instanceof RuntimeException) throw (RuntimeException) e;
			throw new RuntimeException(e);
		}
	}

	public interface RunnableWithException {

		void run() throws Exception;
	}

	public interface SupplierWithException<R> {

		R get() throws Exception;
	}
}
