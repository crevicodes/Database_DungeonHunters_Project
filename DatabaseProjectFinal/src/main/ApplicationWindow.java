package main;

import panels.LoginPage;

import javax.swing.*;
import java.awt.*;

public class ApplicationWindow extends JFrame
{
    public static void main(String[] args)
    {
        new ApplicationWindow(new LoginPage());
    }

    public ApplicationWindow(LoginPage loginPage)
    {
        new Controller(loginPage, this);
        setTitle("Hunter Association Database");
        changeContent(loginPage);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        pack();
        setVisible(true);

    }

    public void changeContent(Container c)
    {
        setContentPane(c);
        pack();
    }
}
