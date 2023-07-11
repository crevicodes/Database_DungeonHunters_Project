package panels;

import main.Controller;
import main.DatabaseConnector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.HashMap;

public class InsertData extends JPanel
{
    private final ArrayList<Container> containers = new ArrayList<>();
    private final ArrayList<Container> labelContainers = new ArrayList<>();
    private final ArrayList<HashMap<String, Object>> constraintsList = new ArrayList<>();
    private final List<String> attributeNames;
    private final JButton addButton = Controller.createButton("Add");
    private final JButton cancelButton = Controller.createButton("Cancel");
    private JComboBox<String> customComboBox = new JComboBox<>();

    public InsertData(String tableName, List<String> attributeNames, List<String> dataTypes)
    {
        this.attributeNames = attributeNames;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout());
        JLabel title = Controller.createTitle("Add New " + tableName);
        add(title, BorderLayout.NORTH);
        JPanel contents = new JPanel(new GridLayout(attributeNames.size(), 2, 5, 5));
        for(int i = 0; i < attributeNames.size(); i++)
        {
            contents.add(addLabel(dataTypes.get(i), i));
            extractAttribute(dataTypes.get(i));
            contents.add(containers.get(i));
        }
        if(customComboBox.getItemCount() != 0)
            customComboBox.setSelectedIndex(0);
        contents.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(contents);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(cancelButton, BorderLayout.WEST);
        bottomPanel.add(addButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private Container addLabel(String attributeStr, int i)
    {
        if(attributeStr.indexOf('+') != -1 || attributeStr.indexOf('d') != -1)
        {
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(Controller.createLabel(attributeNames.get(i)), BorderLayout.NORTH);
            if(attributeStr.indexOf('+') != -1)
                panel.add(new JLabel("Multivalued (comma separated)"), BorderLayout.SOUTH);
            else
                panel.add(new JLabel("(dd-MMM-yyyy)"), BorderLayout.SOUTH);
            labelContainers.add(panel);
            return panel;
        } else
        {
            JLabel label = Controller.createLabel(attributeNames.get(i));
            labelContainers.add(label);
            return label;
        }
    }

    private void extractAttribute(String attributeStr)
    {
        HashMap<String, Object> constraints = new HashMap<>();
        if(attributeStr.indexOf('f') != -1)
        {
            containers.add(new JComboBox<>());
            constraints.put("type", "integerF");
        } else if(attributeStr.indexOf('c') != -1)
        {
            customComboBox = new JComboBox<>(new String[]{"Receptionist", "Guide Helper"});
            customComboBox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if(customComboBox.getSelectedIndex() == 0)
                    {
                        for(int i = 6; i <= 8; i++)
                        {
                            labelContainers.get(i).setVisible(false);
                            containers.get(i).setVisible(false);
                        }
                        labelContainers.get(9).setVisible(true);
                        containers.get(9).setVisible(true);
                    } else
                    {
                        for(int i = 6; i <= 8; i++)
                        {
                            labelContainers.get(i).setVisible(true);
                            containers.get(i).setVisible(true);
                        }
                        labelContainers.get(9).setVisible(false);
                        containers.get(9).setVisible(false);
                    }
                }
            });
            containers.add(customComboBox);
            constraints.put("type", "custom");
        } else
        {
            JTextField field = new JTextField();
            field.setFont(new Font("Arial", Font.PLAIN, 18));
            containers.add(field);
        }
        if(attributeStr.indexOf('v') != -1)
            constraints.put("type", "varchar");
        else if(attributeStr.indexOf('+') != -1)
            constraints.put("type", "varchar+");
        else if(attributeStr.indexOf('i') != -1)
            constraints.put("type", "integer");
        else if(attributeStr.indexOf('d') != -1)
            constraints.put("type", "date");
        if(attributeStr.indexOf('n') != -1 || attributeStr.indexOf('+') != -1)
            constraints.put("isNull", false);
        else
            constraints.put("isNull", true);
        constraints.put("size", getConstraintSize(attributeStr));
        constraintsList.add(constraints);
    }

    private int getConstraintSize(String attributeStr)
    {
        StringBuilder numString = new StringBuilder();
        for(char c : attributeStr.toCharArray())
        {
            if(Character.isDigit(c))
                numString.append(c);
        }
        return Integer.parseInt(numString.toString());
    }

    private boolean isInteger(String s)
    {
        try
        {
            Integer.parseInt(s);
            return true;
        } catch(NumberFormatException ex)
        {
            return false;
        }
    }

    public boolean isValidData()
    {
        StringBuilder error = new StringBuilder();
        for(int i = 0; i < attributeNames.size(); i++)
        {
            HashMap<String, Object> constraints = constraintsList.get(i);
            boolean isNull = (boolean) constraints.get("isNull");
            boolean emptyWhenNotNull = false;
            Container container = containers.get(i);
            if(container.isShowing())
            {
                if(container.getClass() == JTextField.class && !isNull && ((JTextField) container).getText().trim().isEmpty())
                {
                    error.append(attributeNames.get(i) + " invalid, cannot be empty\n");
                    emptyWhenNotNull = true;
                }
            }
            String type = constraints.get("type").toString();
            int size = (int) constraints.get("size");
            if(container.isShowing())
            {
                if(type.equals("varchar"))
                {
                    if(((JTextField) container).getText().trim().length() > size)
                        error.append(attributeNames.get(i) + " invalid, must be lesser than or equal to " + size + " characters\n");
                } else if(type.equals("integer"))
                {
                    String input = ((JTextField) container).getText().trim();
                    if(!emptyWhenNotNull && !isInteger(input))
                        error.append(attributeNames.get(i) + " invalid, must be an integer\n");
                    if(!emptyWhenNotNull && size != 0)
                        if(input.length() != size)
                            error.append(attributeNames.get(i) + " invalid, must be " + size + " digits\n");
                } else if(type.equals("varchar+"))
                {
                    String[] split = ((JTextField) container).getText().trim().split(",");
                    int j = 0;
                    for(String s : split)
                    {
                        if(s.length() > size)
                            error.append(attributeNames.get(i) + " invalid at position " + (j + 1) + ", must be lesser than or equal to " + size + " characters\n");
                        j++;
                    }
                } else if(type.equals("date"))
                {
                    if(!emptyWhenNotNull)
                    {
                        try
                        {
                            Date date = new SimpleDateFormat("dd-MMM-yyyy").parse(((JTextField) container).getText().trim());
                        } catch(ParseException e)
                        {
                            error.append(attributeNames.get(i) + " invalid, must be in (dd-MMM-yyyy) format\n");
                        }
                    }
                }
            }
        }
        String errorString = error.toString();
        if(errorString.isEmpty())
        {
            return true;
        } else
        {
            JOptionPane.showMessageDialog(null, errorString, "Cannot add to database", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public void setForeignKeys(DatabaseConnector dbCon, List<String> foreignKey, List<String> tableName) throws SQLException
    {
        int j = 0;
        for(int i = 0; i < attributeNames.size(); i++)
        {
            HashMap<String, Object> constraints = constraintsList.get(i);
            if(containers.get(i).getClass() == JComboBox.class && !constraints.get("type").equals("custom"))
            {
                ResultSet rs = dbCon.executeStatement("SELECT " + foreignKey.get(j) + " FROM " + tableName.get(j) + " ORDER BY " + foreignKey.get(j) + " ASC");
                while (rs.next())
                    ((JComboBox) (containers.get(i))).addItem(rs.getString(foreignKey.get(j)));
                if(constraints.get("isNull").equals(true))
                    ((JComboBox) (containers.get(i))).addItem("None");
                rs.close();
                j++;
            }
        }
    }

    public boolean isDuplicate(DatabaseConnector dbCon, String primaryKey, String table) throws SQLException
    {
        boolean isDuplicate;
        String sql = "SELECT " + primaryKey + " FROM " + table + " WHERE " + primaryKey + " = " + Integer.parseInt(((JTextField) containers.get(0)).getText().trim());
        ResultSet rs = dbCon.executeStatement(sql);
        isDuplicate = rs.isBeforeFirst();
        if(isDuplicate)
            JOptionPane.showMessageDialog(null, "Primary Key invalid, duplicate found within database", "Cannot add to database", JOptionPane.ERROR_MESSAGE);
        return isDuplicate;
    }

    public boolean isDuplicateString(DatabaseConnector dbCon, String primaryKey, String table) throws SQLException
    {
        boolean isDuplicate;
        String sql = "SELECT " + primaryKey + " FROM " + table + " WHERE " + primaryKey + " = '" + (((JTextField) containers.get(0)).getText().trim() + "'");
        ResultSet rs = dbCon.executeStatement(sql);
        isDuplicate = rs.isBeforeFirst();
        if(isDuplicate)
            JOptionPane.showMessageDialog(null, "Primary Key invalid, duplicate found within database", "Cannot add to database", JOptionPane.ERROR_MESSAGE);
        return isDuplicate;
    }

    public int getCustomComboBoxIndex()
    {
        return customComboBox.getSelectedIndex();
    }

    public String getContainerText(int i)
    {
        if(containers.get(i).isShowing())
        {
            if(containers.get(i).getClass() == JTextField.class)
                return ((JTextField) containers.get(i)).getText().trim().toUpperCase();
            else
                return ((JComboBox) containers.get(i)).getSelectedItem().toString();
        } else
        {
            return "NOT_SHOWING";
        }
    }

    public String[] getMultivaluedContainerText(int i)
    {
        return ((JTextField) containers.get(i)).getText().trim().split(",");
    }

    public int insertToDatabase(DatabaseConnector dbcon, int size, String sql) throws SQLException
    {
        StringBuilder prepSql = new StringBuilder();
        prepSql.append("INSERT INTO " + sql);
        for(int i = 0; i < size; i++)
        {
            HashMap<String, Object> constraints = constraintsList.get(i);
            String type = constraints.get("type").toString();
            if(type.equals("varchar") || type.equals("date"))
                prepSql.append("'");
            if(containers.get(i).getClass() == JTextField.class)
                prepSql.append(((JTextField) containers.get(i)).getText().trim());
            else if(containers.get(i).getClass() == JComboBox.class)
                if(type.equals("custom"))
                    prepSql.append(((JComboBox) containers.get(i)).getSelectedIndex() + 1);
                else if(constraints.get("isNull").equals(true) && ((JComboBox) containers.get(i)).getSelectedItem().equals("None"))
                    prepSql.append("NULL");
                else
                    prepSql.append(((JComboBox) containers.get(i)).getSelectedItem().toString().trim());
            if(type.equals("varchar") || type.equals("date"))
                prepSql.append("'");
            if(i != size - 1)
                prepSql.append(", ");
            else
                prepSql.append(")");
        }
        String prepSqlStr = prepSql.toString();
        System.out.println(prepSqlStr);
        return dbcon.executePrepared(prepSqlStr);
    }

    public void clearInputBoxes()
    {
        for(int i = 0; i < attributeNames.size(); i++)
        {
            if(containers.get(i).getClass() == JTextField.class)
            {
                ((JTextField) containers.get(i)).setText("");
            } else if(containers.get(i).getClass() == JComboBox.class)
            {
                ((JComboBox) containers.get(i)).setSelectedItem(0);
            }
        }
    }

    public void addAddListener(ActionListener l)
    {
        addButton.addActionListener(l);
    }

    public void addCancelListener(ActionListener l)
    {
        cancelButton.addActionListener(l);
    }
}
