package se.emabe390.efh;

import javax.swing.*;
import java.awt.*;

import java.util.function.Consumer;

class FittingRowPanel extends JPanel {

    private JTextField numberField = new JTextField(5);
    private JTextArea textArea = new JTextArea(3, 20);

    public FittingRowPanel(int index, Consumer<FittingRowPanel> onRemove) {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JButton removeButton = new JButton("-");
        removeButton.addActionListener(e -> onRemove.accept(this));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(removeButton);
        top.add(new JLabel("Fitting " + index + " #:"));
        top.add(numberField);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(textArea), BorderLayout.CENTER);
    }

    public int getNumber() {
        try {
            return Integer.parseInt(numberField.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getText() {
        return textArea.getText();
    }
}