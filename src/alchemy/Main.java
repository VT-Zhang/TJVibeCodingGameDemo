package alchemy;

import javax.swing.*;
import java.awt.Dimension;
import java.nio.file.Path;

/** Separate entry point for City × Sport Alchemy. Requires only JDK 17+. */
public final class Main {
    private Main() {}
    public static void main(String[] args){
        SwingUtilities.invokeLater(()->{
            JFrame frame=new JFrame("City × Sport Alchemy");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new AlchemyPanel(new AlchemyModel(Path.of(".city-sport-alchemy"))));
            frame.pack();frame.setMinimumSize(new Dimension(1050,700));frame.setLocationRelativeTo(null);frame.setVisible(true);
        });
    }
}
