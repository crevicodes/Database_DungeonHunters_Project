package panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import main.Controller;
import main.Controller.*;

public class LoginPage extends JPanel
{
    private final JButton loginButton = Controller.createButton("Login");
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    public LoginPage()
    {
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout());
        JLabel title = Controller.createTitle("Hunter Association Database");
        add(title, BorderLayout.NORTH);
        JPanel detailPanel = new JPanel(new GridLayout(2, 2));
        detailPanel.add(Controller.createLabel("Username"));
        detailPanel.add(usernameField);
        detailPanel.add(Controller.createLabel("Password"));
        detailPanel.add(passwordField);
        detailPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(detailPanel, BorderLayout.CENTER);
        add(loginButton, BorderLayout.SOUTH);
        passwordField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if(e.getKeyCode() == KeyEvent.VK_ENTER)
                    loginButton.getActionListeners()[0].actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

            }
        });
    }

    public void addLoginListener(ActionListener l)
    {
        loginButton.addActionListener(l);
    }

    public String getUsername()
    {
        return usernameField.getText().trim();
    }

    public String getPassword()
    {
        return passwordField.getText();
    }

    public void reset()
    {
        usernameField.setText("");
        passwordField.setText("");
    }
}
