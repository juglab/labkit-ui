
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
}
