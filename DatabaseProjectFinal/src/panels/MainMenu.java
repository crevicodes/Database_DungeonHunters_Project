package panels;

import main.Controller;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JPanel
{
    private final JButton loginButton = new JButton("Login");
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    public MainMenu(String title, int rows, int columns, JButton back, JButton... buttons)
    {
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = Controller.createTitle(title);
        JPanel buttonPanel = new JPanel(new GridLayout(rows, columns, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        setLayout(new BorderLayout());
        add(titleLabel, BorderLayout.NORTH);
        for(JButton button : buttons)
        {
            buttonPanel.add(button);
        }
        buttonPanel.setMaximumSize(buttonPanel.getPreferredSize());
        add(buttonPanel, BorderLayout.CENTER);
        if(back != null)
        {
            JPanel backPanel = new JPanel(new GridLayout(1, 1));
            back.setMaximumSize(back.getPreferredSize());
            backPanel.add(back);
            backPanel.setMaximumSize(buttonPanel.getPreferredSize());
            add(backPanel, BorderLayout.SOUTH);
        }
    }

    public MainMenu() {}
}
