
package net.imglib2.labkit;

import net.imglib2.labkit.menu.MenuFactory;
import net.imglib2.labkit.menu.MenuKey;
import net.imglib2.labkit.utils.ProgressConsumer;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DefaultExtensible implements Extensible {

	private final Context context;
	private final JFrame dialogBoxOwner;
	private final BasicLabelingComponent labelingComponent;
	private final MenuFactory menus = new MenuFactory();

	public DefaultExtensible(Context context, JFrame dialogBoxOwner,
		BasicLabelingComponent labelingComponent)
	{
		this.context = context;
		this.dialogBoxOwner = dialogBoxOwner;
		this.labelingComponent = labelingComponent;
	}

	@Override
	public Context context() {
		return context;
	}

	@Override
	public void addAction(String title, String command, Runnable action,
		String keyStroke)
	{
		RunnableAction a = new RunnableAction(title, action);
		a.putValue(Action.ACTION_COMMAND_KEY, command);
		a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyStroke));
		labelingComponent.addAction(a);
	}

	@Override
	public < T > void addMenuItem(MenuKey< T > key, String title,
			Consumer< T > action, Icon icon)
	{
		menus.addMenuItem(key, title, action, icon);
	}

	@Override
	public JFrame dialogParent() {
		return dialogBoxOwner;
	}

	@Override
	public ProgressConsumer progressConsumer() {
		return context.getService(StatusService.class)::showProgress;
	}

	public <T> JPopupMenu createMenu( MenuKey< T > key, Supplier< T > item ) {
		return menus.createMenu( key, item );
	}
}
