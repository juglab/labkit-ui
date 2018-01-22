package net.imglib2.labkit.panel;

import net.imglib2.labkit.utils.Notifier;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ComponentList<K, C extends JComponent> {

	private final Color SELECTED_BACKGROUND = UIManager.getColor("List.selectionBackground");
	private final Color BACKGROUND = UIManager.getColor("List.background");

	private JPanel background = new JPanel();
	private K selected;
	private Map<C, K> panels = new HashMap<>();
	private Notifier<Runnable> listeners = new Notifier<>();

	public ComponentList() {
		background.setLayout(new MigLayout("insets 4pt, gap 4pt", "[grow]"));
		background.setBackground(BACKGROUND);
	}

	public JComponent getCompnent() {
		return new JScrollPane(background);
	}

	public void add(K key, C component) {
		component.setBackground(BACKGROUND);
		component.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setSelected(key);
			}

			@Override
			public void mousePressed(MouseEvent e) {

			}

			@Override
			public void mouseReleased(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {

			}
		});
		panels.put(component, key);
		background.add(component, "grow, wrap");
		background.revalidate();
		background.repaint();
	}

	public void setSelected(K key) {
		if(this.selected == key)
			return;
		this.selected = key;
		panels.forEach((component, k) -> component.setBackground( k == selected ? SELECTED_BACKGROUND : BACKGROUND));
		listeners.forEach(l -> l.run());
	}

	public K getSelected() {
		return selected;
	}

	public void clear() {
		panels.clear();
		background.removeAll();
		background.revalidate();
		background.repaint();
	}

	public Notifier<Runnable> listeners() {
		return listeners;
	}

	// -- demo --

	public static void main(String... args) {
		JFrame frame = new JFrame();
		frame.setSize(300, 600);
		frame.setLayout(new MigLayout("", "[grow]","[grow][]"));
		ComponentList<String, JPanel> panelList = new ComponentList<>();
		frame.add(panelList.getCompnent(), "grow, wrap");
		Random random = new Random();
		frame.add(new JButton(new RunnableAction("add", () -> panelList.add(Integer.toString(random.nextInt()), panelList.newExamplePanel()))), "split");
		frame.add(new JButton(new RunnableAction("clear", () -> panelList.clear())));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private static JPanel newExamplePanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("", "[][][grow]"));
		panel.add(new JCheckBox());
		JButton button = new JButton();
		button.setBackground(Color.RED);
		panel.add(button);
		panel.add(new JLabel("Hello"));
		return panel;
	}
}
