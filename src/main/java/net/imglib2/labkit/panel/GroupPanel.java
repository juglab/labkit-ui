package net.imglib2.labkit.panel;

import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by random on 09.05.18.
 */
public class GroupPanel {

    protected JButton createActionIconButton(String name, final Action action, String icon) {
        JButton button = new JButton( action );
        button.setText(name);
        if(icon != ""){
            button.setIcon(new ImageIcon(getClass().getResource( icon )));
            button.setIconTextGap(5);
            button.setMargin(new Insets(button.getMargin().top, 3, button.getMargin().bottom, button.getMargin().right));
        }
        return button;
    }

    protected ImageIcon createIcon( final Color color ) {
        final BufferedImage image =
                new BufferedImage( 20, 10, BufferedImage.TYPE_INT_RGB );
        final Graphics g = image.getGraphics();
        g.setColor( color );
        g.fillRect( 0, 0, image.getWidth(), image.getHeight() );
        g.dispose();
        return new ImageIcon( image );
    }
}
