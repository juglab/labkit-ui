
package net.imglib2.labkit.panel;

import net.imglib2.Dimensions;
import net.imglib2.util.Intervals;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.util.RunnableAction;

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
		checkbox.setIcon(loadIcon("invisible.png"));
		checkbox.setSelectedIcon(loadIcon("visible.png"));
		checkbox.setPressedIcon(loadIcon("visible-hover.png"));
		checkbox.setRolloverIcon(loadIcon("invisible-hover.png"));
		checkbox.setRolloverSelectedIcon(loadIcon("visible-hover.png"));
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
		result.putValue( Action.SMALL_ICON, icon );
		result.putValue( Action.LARGE_ICON_KEY, icon );
		return result;
	}

	public static ImageIcon loadIcon(String iconPath) {
		return new ImageIcon(GuiUtils.class.getResource("/images/" + iconPath));
	}
}
