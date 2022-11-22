package it.unibo.oop.reactivegui02;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Second example of reactive GUI.
 */
public final class ConcurrentGUI extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private final JLabel display = new JLabel();
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    private final JButton stop = new JButton("stop");

    public ConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(up);
        panel.add(down);
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);

        final Agent agent = new Agent();
        up.addActionListener((e) -> agent.setIncrementing());
        down.addActionListener((e) -> agent.setDecrementing());
        stop.addActionListener((e) -> agent.stopCounting());
        
        new Thread(agent).start();
    }

    private class Agent implements Runnable {
        private volatile boolean isIncrementing = true; // if false then it's decrementing
        private volatile boolean stop;
        private int counter = 0;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    if (this.isIncrementing) {
                        this.counter++;
                    }
                    else {
                        this.counter--;
                    }
                    
                    // in modo che l'EDT di Swing non debba accedere direttamente a this.counter
                    final var nextText = Integer.toString(this.counter);
                    // invokeLater in modo da non bloccare la GUI in attesa di aggiornare il testo
                    SwingUtilities.invokeLater(() -> ConcurrentGUI.this.display.setText(nextText));

                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void setIncrementing() {
            this.isIncrementing = true;
        }

        public void setDecrementing() {
            this.isIncrementing = false;
        }

        public void stopCounting() {
            this.stop = true;
            ConcurrentGUI.this.up.setEnabled(false);
            ConcurrentGUI.this.down.setEnabled(false);
            ConcurrentGUI.this.stop.setEnabled(false);
        }
    }
}
