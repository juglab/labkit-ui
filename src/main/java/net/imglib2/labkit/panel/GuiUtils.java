
package net.imglib2.labkit.panel;

import net.imglib2.Dimensions;
import net.imglib2.util.Intervals;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.DragBehaviour;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;

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

	private static JCheckBox createCheckbox(Action image) {
		return styleCheckboxUsingEye(new JCheckBox(image));
	}

	public static JCheckBox styleCheckboxUsingEye(JCheckBox checkbox) {
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

	static MouseAdapter toMouseListener(DragBehaviour behavior) {
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

	public static JComponent createDimensionsInfo(Dimensions interval) {
		Color background = UIManager.getColor("List.background");
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("insets 8, gap 0", "10[grow]", ""));
		panel.setBackground(background);
		JLabel label = new JLabel("Dimensions: " + Arrays.toString(Intervals
			.dimensionsAsLongArray(interval)));
		label.setBackground(background);
		label.setOpaque(true);
		panel.add(label, "grow, span");
		return panel;
	}
}
