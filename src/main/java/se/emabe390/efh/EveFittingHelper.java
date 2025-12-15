package se.emabe390.efh;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EveFittingHelper extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    private final JTextArea assetText = new JTextArea(5, 30);
    private final JTextArea multibuyOutput = new JTextArea();

    private final JPanel fittingListPanel = new JPanel();
    private final List<FittingRowPanel> fittingRows = new ArrayList<>();
    private int bCount = 0;

    public EveFittingHelper() {
        setTitle("EveFittingHelper v1.0");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        cards.add(createABView(), "AB");
        cards.add(createCView(), "C");

        add(cards);
    }

    private JPanel createABView() {
        JPanel root = new JPanel(new BorderLayout(10, 10));

        // A panel
        JPanel aPanel = new JPanel(new BorderLayout());
        aPanel.setBorder(BorderFactory.createTitledBorder("Assets"));
        aPanel.add(new JScrollPane(assetText));

        // B panel
        fittingListPanel.setLayout(new BoxLayout(fittingListPanel, BoxLayout.Y_AXIS));
        JScrollPane bScroll = new JScrollPane(fittingListPanel);

        JButton addB = new JButton("+ Add Fitting");
        addB.addActionListener(e -> addBRow());

        JPanel bContainer = new JPanel(new BorderLayout());
        bContainer.setBorder(BorderFactory.createTitledBorder("Fits"));
        bContainer.add(bScroll, BorderLayout.CENTER);
        bContainer.add(addB, BorderLayout.SOUTH);

        JButton showC = new JButton("Show Required Items");
        showC.addActionListener(e -> switchToC());

        JPanel left = new JPanel(new BorderLayout());
        left.add(aPanel, BorderLayout.CENTER);
        left.add(showC, BorderLayout.SOUTH);

        root.add(left, BorderLayout.WEST);
        root.add(bContainer, BorderLayout.CENTER);

        return root;
    }

    private JPanel createCView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // ----- Output Area -----
        multibuyOutput.setEditable(false);
        panel.add(new JScrollPane(multibuyOutput), BorderLayout.CENTER);

        // ----- Bottom Bar -----
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new EmptyBorder(5, 10, 10, 10));
        bottomPanel.setBackground(Color.WHITE);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightButtons.setBackground(Color.WHITE);

        JButton toggleButton = new JButton("Back to Assets & Fits");
        JButton resetButton = new JButton("Reset");

        toggleButton.addActionListener(e -> cardLayout.show(cards, "AB"));

        resetButton.addActionListener(e -> resetAll());

        rightButtons.add(resetButton);
        rightButtons.add(toggleButton);

        bottomPanel.add(rightButtons, BorderLayout.EAST);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void resetAll() {
        // Clear A
        assetText.setText("");

        // Remove all B rows
        fittingRows.clear();
        fittingListPanel.removeAll();
        fittingListPanel.revalidate();
        fittingListPanel.repaint();

        // Reset counter
        bCount = 0;

        // Clear C
        multibuyOutput.setText("");

        // Go back to AB view
        cardLayout.show(cards, "AB");
    }


    private void addBRow() {
        bCount++;

        FittingRowPanel row = new FittingRowPanel(bCount, r -> {
            fittingRows.remove(r);
            fittingListPanel.remove(r);
            fittingListPanel.revalidate();
            fittingListPanel.repaint();
        });

        fittingRows.add(row);
        fittingListPanel.add(row);
        fittingListPanel.revalidate();
    }

    private void switchToC() {

        Map<String, Integer> have = new HashMap<>();

        for (String line : assetText.getText().split("\n")) {
            String[] split = line.split("\t");
            if (split.length < 2) {
                System.out.println("Skipping line: '" + line + "' too few characters");
                continue;
            }
            try {
                int num;
                String numString = split[1].replaceAll("[^\\d.]", "");
                if (numString.isBlank()) {
                    num = 1;
                } else {
                    num = Integer.parseInt(numString);
                }
                String name = split[0];
                have.put(name, have.getOrDefault(name, 0) + num);
            } catch (NumberFormatException ignore) {
                System.out.println("Invalid number: " + split[1] + " with name " + split[0]);
            }
        }

        Pattern emptyXSlot = Pattern.compile("^\\[Empty (.*) slot]$");
        Pattern multipleItems = Pattern.compile("^(.*) x(\\d*)$");
        Pattern shipMatcher = Pattern.compile("^\\[([^,]*).*?]$");

        Map<String, Integer> want = new HashMap<>();
        for (FittingRowPanel fittingPanel : fittingRows) {
            int multiplier = fittingPanel.getNumber();
            for (String row : fittingPanel.getText().split("\n")) {
                if (row.isBlank()) continue;
                Matcher matcher = emptyXSlot.matcher(row);
                if (matcher.matches()) continue;
                String name;
                int count;
                matcher = shipMatcher.matcher(row);
                if (matcher.matches()) {
                    // It's a ship. figure out the name.
                    name = matcher.group(1);
                    count = 1;
                } else {
                    matcher = multipleItems.matcher(row);
                    if (matcher.matches()) {
                        // It's on the format CNV x30
                        name = matcher.group(1).trim();
                        try {
                            count = Integer.parseInt(matcher.group(2));
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid number on row '" + row + "'.");
                            continue;
                        }

                    } else {
                        name = row.trim();
                        count = 1;
                    }
                }
                want.put(name, want.getOrDefault(name, 0) + count * multiplier);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String name : want.keySet().stream().sorted().toList()) {
           int count = want.get(name);
            int needToBuy = have.getOrDefault(name, 0) - count;
            if (needToBuy < 0) {
                sb.append(name).append("\t").append(-needToBuy).append("\n");
            }
        }

        multibuyOutput.setText(sb.toString());
        cardLayout.show(cards, "C");
    }

    static void main() {
        SwingUtilities.invokeLater(() -> new EveFittingHelper().setVisible(true));
    }
}
