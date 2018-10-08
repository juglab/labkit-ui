package net.imglib2.labkit.menu;

public class MenuKey<T> {

	private final Class< T > inputParameterClass;

	public MenuKey(Class< T > inputParameterClass) {
		this.inputParameterClass = inputParameterClass;
	}

	public Class< T > inputParameterClass() {
		return inputParameterClass;
	}
}
