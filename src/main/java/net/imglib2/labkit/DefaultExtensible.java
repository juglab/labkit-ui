
package net.imglib2.labkit;

import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.utils.ProgressConsumer;
import net.imglib2.util.ConstantUtils;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DefaultExtensible implements Extensible {

	private final Context context;
	private final JFrame dialogBoxOwner;
	private final BasicLabelingComponent labelingComponent;
	private final List<Entry<Label>> labelMenu = new ArrayList<>();
	private final List<Entry<SegmentationItem>> segmenterMenu = new ArrayList<>();

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
	public void addLabelMenuItem(String title, Consumer<Label> action, Icon icon) {
		labelMenu.add(new Entry<>(title, action, icon));
	}

	@Override
	public void addSegmenterMenuItem(String title, Consumer<SegmentationItem> action, Icon icon) {
		segmenterMenu.add(new Entry<>(title, action, icon));
	}

	@Override
	public JFrame dialogParent() {
		return dialogBoxOwner;
	}

	@Override
	public ProgressConsumer progressConsumer() {
		return context.getService(StatusService.class)::showProgress;
	}

	public JPopupMenu createSegmenterMenu(Supplier<SegmentationItem> item) {
		return createPopupMenu(item, this.segmenterMenu);
	}

	public JPopupMenu createLabelMenu(Supplier<Label> item) {
		return createPopupMenu(item, this.labelMenu);
	}

	private <T> JPopupMenu createPopupMenu(Supplier<T> item, List<Entry<T>> segmenterMenu) {
		JPopupMenu menu = new JPopupMenu();
		for (Entry<T> entry : segmenterMenu) {
			RunnableAction action = new RunnableAction(entry.title,
					() -> entry.action.accept(item.get()));
			action.putValue(Action.SMALL_ICON, entry.icon);
			action.putValue(Action.LARGE_ICON_KEY, entry.icon);
			menu.add(action);
		}
		return menu;
	}

	private static class Entry<T> {

		private final String title;
		private final Consumer<T> action;
		private final Icon icon;

		private Entry(String title, Consumer<T> action, Icon icon) {
			this.title = title;
			this.action = action;
			this.icon = icon;
		}
	}
}
