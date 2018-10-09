
package net.imglib2.labkit;

import net.imglib2.labkit.menu.MenuFactory;
import net.imglib2.labkit.menu.MenuKey;
import net.imglib2.labkit.utils.ProgressConsumer;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import javax.swing.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DefaultExtensible implements Extensible {

	private final Context context;
	private final JFrame dialogBoxOwner;
	private final MenuFactory menus = new MenuFactory();

	public DefaultExtensible(Context context, JFrame dialogBoxOwner)
	{
		this.context = context;
		this.dialogBoxOwner = dialogBoxOwner;
	}

	@Override
	public Context context() {
		return context;
	}

	@Override
	public < T > void addMenuItem(MenuKey< T > key, String title, float priority,
			Consumer< T > action, Icon icon, String keyStroke)
	{
		menus.addMenuItem(key, title, priority, action, icon, keyStroke);
	}

	@Override
	public JFrame dialogParent() {
		return dialogBoxOwner;
	}

	@Override
	public ProgressConsumer progressConsumer() {
		return context.getService(StatusService.class)::showProgress;
	}

	public <T> JPopupMenu createPopupMenu( MenuKey< T > key, Supplier< T > item ) {
		return menus.createPopupMenu( key, item );
	}

	public <T> JMenu createMenu( MenuKey< T > key, Supplier< T > item ) {
		return menus.createMenu( key, item );
	}

	public List< AbstractNamedAction > getShortCuts() {
		return menus.shortCutActions();
	}
}
