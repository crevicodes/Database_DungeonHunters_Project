package panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class CustomButton extends JButton
{
    public CustomButton(String text, ActionListener listener)
    {
        super(text);
        setFont(new Font("Monospaced", Font.BOLD, 18));
        addActionListener(listener);
    }
}
