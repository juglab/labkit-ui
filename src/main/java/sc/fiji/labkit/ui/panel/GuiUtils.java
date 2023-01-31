/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.ui.panel;

import sc.fiji.labkit.ui.models.Holder;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * Swing GUI related helper functions.
 */
public class GuiUtils {

	private GuiUtils() {
		// prevent from instantiation
	}

	public static JButton createActionIconButton(String name, final Action action,
		String icon)
	{
		JButton button = new JButton(action);
		button.setText(name);
		if (icon != "") {
			button.setIcon(loadIcon(icon));
			button.setIconTextGap(5);
			button.setMargin(new Insets(button.getMargin().top, 3, button
				.getMargin().bottom, button.getMargin().right));
		}
		return button;
	}

	public static ImageIcon createIcon(final Color color) {
		final BufferedImage image = new BufferedImage(20, 10,
			BufferedImage.TYPE_INT_RGB);
		final Graphics g = image.getGraphics();
		g.setColor(color);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		g.dispose();
		return new ImageIcon(image);
	}

	public static JPanel createCheckboxGroupedPanel(Holder<Boolean> visibility,
		String text, JComponent panel)
	{
		JPanel dark = new JPanel();
		dark.setLayout(new BorderLayout());
		JCheckBox checkbox = createCheckbox(visibility, text);
		JPanel title = new JPanel();
		title.setBackground(new Color(200, 200, 200));
		title.setLayout(new MigLayout("insets 4pt, gap 8pt, fillx", "10[][]10"));
		title.add(new JLabel(checkbox.getText()), "push");
		checkbox.setText("");
		checkbox.setOpaque(false);
		title.add(checkbox);
		dark.setBackground(new Color(200, 200, 200));
		dark.add(title, BorderLayout.PAGE_START);
		dark.add(panel, BorderLayout.CENTER);
		return dark;
	}

	private static JCheckBox createCheckbox(Holder<Boolean> visibility,
		String text)
	{
		final JCheckBox checkbox = new LinkedCheckBox(text, visibility);
		return styleCheckboxUsingEye(checkbox);
	}

	private static class LinkedCheckBox extends JCheckBox {

		private final Holder<Boolean> model;
		private final Runnable onModelChanged = this::onModelChanged;

		private LinkedCheckBox(String text, Holder<Boolean> model) {
			super(text);
			this.model = model;
			this.model.notifier().addWeakListener(onModelChanged);
			setSelected(model.get());
			addItemListener(this::onUserAction);
		}

		private void onUserAction(ItemEvent itemEvent) {
			model.set(isSelected());
		}

		private void onModelChanged() {
			setSelected(model.get());
		}
	}

	public static JCheckBox styleCheckboxUsingEye(JCheckBox checkbox) {
		checkbox.setIcon(loadIcon("invisible.png"));
		checkbox.setSelectedIcon(loadIcon("visible.png"));
		checkbox.setPressedIcon(loadIcon("visible-hover.png"));
		checkbox.setRolloverIcon(loadIcon("invisible-hover.png"));
		checkbox.setRolloverSelectedIcon(loadIcon("visible-hover.png"));
		checkbox.setFocusable(false);
		return checkbox;
	}

	public static MouseAdapter toMouseListener(DragBehaviour behavior) {
		return new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				behavior.init(e.getX(), e.getY());
			}

			@Override
			public void mouseExited(MouseEvent e) {
				behavior.end(e.getX(), e.getY());
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				behavior.drag(e.getX(), e.getY());
			}
		};
	}

	public static JButton createIconButton(Action action) {
		JButton result = new JButton(action);
		result.setText("");
		result.setBorder(BorderFactory.createEmptyBorder());
		result.setContentAreaFilled(false);
		result.setOpaque(false);
		return result;
	}

	public static RunnableAction createAction(String title, Runnable action,
		String iconPath)
	{
		RunnableAction result = new RunnableAction(title, action);
		final ImageIcon icon = loadIcon(iconPath);
		result.putValue(Action.SMALL_ICON, icon);
		result.putValue(Action.LARGE_ICON_KEY, icon);
		return result;
	}

	public static ImageIcon loadIcon(String iconPath) {
		return new ImageIcon(GuiUtils.class.getResource("/images/" + iconPath));
	}
}
