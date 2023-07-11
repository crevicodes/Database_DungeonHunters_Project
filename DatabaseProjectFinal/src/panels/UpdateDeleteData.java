package panels;

import main.Controller;
import main.DatabaseConnector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class UpdateDeleteData extends JPanel
{
    private final ArrayList<Container> containers = new ArrayList<>();
    private final ArrayList<HashMap<String, Object>> constraintsList = new ArrayList<>();
    private final ArrayList<Container> labelContainers = new ArrayList<>();
    private List<String> attributeNames = new ArrayList<>();
    private final JButton updateButton = Controller.createButton("Update");
    private final JButton deleteButton = Controller.createButton("Delete");
    private final JButton prevButton = Controller.createButton("<< Previous");
    private final JButton nextButton = Controller.createButton("Next >>");
    private final JButton cancelButton = Controller.createButton("Cancel");
    private String storedPk = "";
    private ResultSet rs;
    private JComboBox<String> customComboBox = new JComboBox<>();


    public UpdateDeleteData(String tableName, List<String> attributeNames, List<String> dataTypes)
    {
        this.attributeNames = attributeNames;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout());
        JLabel title = Controller.createTitle("Update/Delete " + tableName);
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
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(cancelButton);
        bottomPanel.add(prevButton);
        bottomPanel.add(updateButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(nextButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public String getStoredPk()
    {
        return storedPk;
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
            if(containers.isEmpty())
                field.setEditable(false);
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

    public void getNewData(DatabaseConnector dbCon, List<String> foreignKeys, List<String> table, String primaryKey, String pkTable, List<String> fields, List<String> multivalued) throws SQLException
    {
        String sql;
        int j = 0;
        if(foreignKeys != null && table != null)
        {
            for(int i = 0; i < attributeNames.size(); i++)
            {
                if(containers.get(i).getClass() == JComboBox.class && !constraintsList.get(i).get("type").equals("custom"))
                {
                    sql = "SELECT " + foreignKeys.get(j) + " FROM " + table.get(j) + " ORDER BY " + foreignKeys.get(j) + " ASC";
                    rs = dbCon.executeStatement(sql);
                    ((JComboBox) (containers.get(i))).removeAllItems();
                    while(rs.next())
                        ((JComboBox) (containers.get(i))).addItem(rs.getString(foreignKeys.get(j)));
                    if(constraintsList.get(i).get("isNull").equals(true))
                        ((JComboBox) (containers.get(i))).addItem("None");
                    j++;
                }
            }
        }
        StringBuilder prepSql = new StringBuilder();
        prepSql.append("SELECT ");
        for(int i = 0; i < fields.size(); i++)
        {
            HashMap<String, Object> constraints = constraintsList.get(i);
            prepSql.append(fields.get(i));
            if(i != fields.size() - 1)
                prepSql.append(", ");
        }
        prepSql.append(" FROM " + pkTable + " ORDER BY " + primaryKey + " ASC");
        System.out.println(prepSql.toString());
        rs = dbCon.executeStatement(prepSql.toString());
        rs.beforeFirst();
        rs.first();
        populateFields(dbCon, fields, multivalued);
        nextButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    if(!rs.isLast())
                    {
                        rs.next();
                        populateFields(dbCon, fields, multivalued);
                    }
                } catch(SQLException ex)
                {
                }
            }
        });
        prevButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    if(!rs.isFirst())
                    {
                        rs.previous();
                        populateFields(dbCon, fields, multivalued);
                    }
                } catch(SQLException ex)
                {

                }
            }
        });
    }

    private String retrieveMultivaluedAttribute(DatabaseConnector dbcon, String attribute, String table, String primaryKey) throws SQLException
    {
        ResultSet rs = dbcon.executeStatement("SELECT " + attribute + " FROM " + table + " WHERE " + primaryKey + " = " + storedPk);
        StringBuilder values = new StringBuilder();
        while(rs.next())
        {
            values.append(rs.getString(attribute));
            if(!rs.isLast())
                values.append(",");
        }
        for(int i = 0; i < attributeNames.size(); i++)
        {
            if(constraintsList.get(i).get("type").equals("varchar+"))
            {
                return values.toString();
            }
        }
        return "";
    }

    private void populateFields(DatabaseConnector dbcon, List<String> fields, List<String> multivalued) throws SQLException
    {
        for(int i = 0; i < fields.size(); i++)
        {
            HashMap<String, Object> constraints = constraintsList.get(i);
            if(i == 0)
                storedPk = rs.getString(fields.get(0));
            if(containers.get(i).getClass() == JTextField.class)
            {
                if(constraints.get("type").equals("date"))
                {
                    Date date = rs.getDate(fields.get(i));
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
                    ((JTextField) containers.get(i)).setText(formatter.format(date));
                } else if(!constraints.get("type").equals("varchar+"))
                {
                    ((JTextField) containers.get(i)).setText(rs.getString(fields.get(i)));
                }
                else
                    ((JTextField) containers.get(i)).setText(retrieveMultivaluedAttribute(dbcon, multivalued.get(0), multivalued.get(1), multivalued.get(2)));
            } else if(containers.get(i).getClass() == JComboBox.class)
            {
                String s = rs.getString(fields.get(i));
                if(rs.wasNull())
                    ((JComboBox) containers.get(i)).setSelectedItem("None");
                else
                    ((JComboBox) containers.get(i)).setSelectedItem(rs.getString(fields.get(i)));

            }
        }
        prevButton.setEnabled(!rs.isFirst());
        nextButton.setEnabled(!rs.isLast());
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

    public int updateToDatabase(DatabaseConnector dbcon, String table, List<String> fields) throws SQLException
    {
        StringBuilder prepSql = new StringBuilder();
        prepSql.append("UPDATE " + table + " SET ");
        for(int i = 0; i < fields.size(); i++)
        {
            prepSql.append(fields.get(i) + " = ");
            HashMap<String, Object> constraints = constraintsList.get(i);
            String type = constraints.get("type").toString();
            if(type.equals("varchar") || type.equals("date"))
                prepSql.append("'");
            if(containers.get(i).getClass() == JTextField.class)
                prepSql.append(((JTextField) containers.get(i)).getText().trim());
            else if(containers.get(i).getClass() == JComboBox.class)
                if(type.equals("custom"))
                    prepSql.append(((JComboBox) containers.get(i)).getSelectedIndex() + 1);
                else if(((JComboBox) containers.get(i)).getSelectedItem().equals("None"))
                    prepSql.append("NULL");
                else
                    prepSql.append(((JComboBox) containers.get(i)).getSelectedItem().toString().trim());

            if(type.equals("varchar") || type.equals("date"))
                prepSql.append("'");
            if(i != fields.size() - 1)
                prepSql.append(", ");
        }
        prepSql.append(" WHERE " + fields.get(0) + " = " + storedPk);
        String prepSqlStr = prepSql.toString();
        System.out.println(prepSqlStr);
        return dbcon.executePrepared(prepSqlStr);
    }

    public int deleteFromDatabase(DatabaseConnector dbcon, String primaryKey, String tableName) throws SQLException
    {
        if(customComboBox.getItemCount() != 0)
        {
            if(customComboBox.getSelectedIndex() == 0)
            {
                String sql = "DELETE RECEPTIONIST WHERE EMPLOYEEID = " + storedPk;
                dbcon.executePrepared(sql);
            } else
            {
                String sql = "DELETE GUIDE_HELPER WHERE EMPLOYEEID = " + storedPk;
                dbcon.executePrepared(sql);
            }
        }
        String sql = "DELETE " + tableName + " WHERE " + primaryKey + " = " + storedPk;
        return dbcon.executePrepared(sql);
    }

    public void addUpdateListener(ActionListener l)
    {
        updateButton.addActionListener(l);
    }

    public void addDeleteListener(ActionListener l)
    {
        deleteButton.addActionListener(l);
    }

    public void addCancelListener(ActionListener l)
    {
        cancelButton.addActionListener(l);
    }
}
