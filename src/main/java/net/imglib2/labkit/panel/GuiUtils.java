
package net.imglib2.labkit.panel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

// TODO use Tims CardPanel https://raw.githubusercontent.com/knime-ip/knip-bdv/4489ea811ce5155038ec919c708ed8b84a6b0297/org.knime.knip.bdv.panel/src/org/knime/knip/bdv/uicomponents/CardPanel.java
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
			button.setIcon(new ImageIcon(GuiUtils.class.getResource(icon)));
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

	public static JComponent createCheckboxGroupedPanel(Action action,
		JComponent panel)
	{
		JPanel dark = new JPanel();
		dark.setLayout(new BorderLayout());
		JCheckBox checkbox = createCheckbox(action);
		checkbox.setBackground(new Color(200, 200, 200));
		dark.setBackground(new Color(200, 200, 200));
		dark.add(checkbox, BorderLayout.PAGE_START);
		dark.add(panel, BorderLayout.CENTER);
		return dark;
	}

	private static JCheckBox createCheckbox(Action image) {
		JCheckBox checkbox = new JCheckBox(image);
		checkbox.setIcon(new ImageIcon(GuiUtils.class.getResource(
			"/images/invisible.png")));
		checkbox.setSelectedIcon(new ImageIcon(GuiUtils.class.getResource(
			"/images/visible.png")));
		checkbox.setPressedIcon(new ImageIcon(GuiUtils.class.getResource(
			"/images/visible-hover.png")));
		checkbox.setRolloverIcon(new ImageIcon(GuiUtils.class.getResource(
			"/images/invisible-hover.png")));
		checkbox.setRolloverSelectedIcon(new ImageIcon(GuiUtils.class.getResource(
			"/images/visible-hover.png")));
		checkbox.setFocusable(false);
		return checkbox;
	}
}
