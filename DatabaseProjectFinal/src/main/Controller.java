package main;

import panels.*;
import panels.queries.GuildFromRegion;
import panels.queries.HunterFromGuild;
import panels.queries.NoGuildsFromRegion;
import panels.queries.NoHuntersFromGuild;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class Controller
{
    private ResultSet rs;
    private final DatabaseConnector dbcon = new DatabaseConnector();
    private final ApplicationWindow frame;
    private MainMenu mainMenu = new MainMenu();
    private MainMenu insertMenu = new MainMenu();
    private MainMenu updateDeleteMenu = new MainMenu();
    private MainMenu queryMenu = new MainMenu();
    private MainMenu manageUsersMenu = new MainMenu();
    private boolean adminMode = false;

    public static JLabel createLabel(String text)
    {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Monospaced", Font.BOLD, 18));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        return label;
    }

    public static JLabel createTitle(String text)
    {
        JLabel title = new JLabel(text);
        title.setHorizontalAlignment(SwingConstants.HORIZONTAL);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        title.setFont(new Font("Monospaced", Font.BOLD, 30));
        return title;
    }

    public static JButton createButton(String text)
    {
        JButton button = new JButton(text);
        button.setFont(new Font("Monospaced", Font.BOLD, 18));
        return button;
    }

    public Controller(LoginPage loginPage, ApplicationWindow frame)
    {
        this.frame = frame;
        CustomButton hunterInsertButton = new CustomButton("Hunter", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                InsertData insertData = new InsertData("Hunter", Arrays.asList("Hunter ID", "Name", "Class", "Rank", "Date Joined", "No. Quests Cleared", "Main Weapon", "Guild ID", "Special Abilities"),
                        Arrays.asList("5in", "30vn", "15vn", "1in", "0dn", "0in", "20v", "5f", "25+"));
                /*
                    i = integer/numeric
                    v = varchar
                    d = date
                    + = multivalued varchar
                    0 = no limit
                    number = size constraint
                    n = not null
                    c = custom
                 */
                try
                {
                    insertData.setForeignKeys(dbcon, Arrays.asList("guildid"), Arrays.asList("guild"));
                } catch(SQLException ex)
                {
                    JLabel label = new JLabel("SQL Error - " + ex);
                    label.setFont(new Font("Arial", Font.BOLD, 18));
                    JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                }
                insertData.addAddListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        boolean valid = insertData.isValidData();
                        try
                        {
                            if(valid && !insertData.isDuplicate(dbcon, "hunterid", "hunter"))
                            {
                                int result = insertData.insertToDatabase(dbcon, 8,
                                        "hunter (hunterid, name, class, rank, datejoined, questscleared, mainweapon, guildid) VALUES (");
                                if(result > 0)
                                {
                                    int nextResult = 0;
                                    for(String value : insertData.getMultivaluedContainerText(8))
                                    {
                                        String prepSql = "INSERT INTO SPECIAL_ABILITIES (hunterid, specialability) VALUES (" +
                                                insertData.getContainerText(0) + ", '" + value + "')";
                                        System.out.println(prepSql);
                                        nextResult = dbcon.executePrepared(prepSql);
                                    }
                                    if(nextResult > 0)
                                    {
                                        JLabel label = new JLabel("Data added successfully");
                                        label.setFont(new Font("Arial", Font.BOLD, 18));
                                        JOptionPane.showMessageDialog(frame, label, "Insert Successful", JOptionPane.INFORMATION_MESSAGE);
                                        insertData.clearInputBoxes();
                                    }
                                }
                            }
                        } catch(SQLException ex)
                        {
                            JLabel label = new JLabel("SQL Error - " + ex);
                            label.setFont(new Font("Arial", Font.BOLD, 18));
                            JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });
                insertData.addCancelListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        frame.changeContent(insertMenu);
                    }
                });
                frame.changeContent(insertData);
            }
        });
        CustomButton guildInsertButton = new CustomButton("Guild", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                InsertData insertData = new InsertData("Guild", Arrays.asList("Guild ID", "Name", "Faction Element", "Guild Leader ID", "Region Code"),
                        Arrays.asList("5in", "30vn", "15v", "5f", "5fn"));
                try
                {
                    insertData.setForeignKeys(dbcon, Arrays.asList("hunterid", "regioncode"), Arrays.asList("hunter", "region"));
                } catch(SQLException ex)
                {
                    JLabel label = new JLabel("SQL Error - " + ex);
                    label.setFont(new Font("Arial", Font.BOLD, 18));
                    JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                }
                insertData.addAddListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        boolean valid = insertData.isValidData();
                        try
                        {
                            if(valid && !insertData.isDuplicate(dbcon, "guildid", "guild"))
                            {
                                int result = insertData.insertToDatabase(dbcon, 5,
                                        "guild (guildid, name, factionelement, guildleaderid, regioncode) VALUES (");
                                if(result > 0)
                                {
                                    JLabel label = new JLabel("Data added successfully");
                                    label.setFont(new Font("Arial", Font.BOLD, 18));
                                    JOptionPane.showMessageDialog(frame, label, "Insert Successful", JOptionPane.INFORMATION_MESSAGE);
                                    insertData.clearInputBoxes();
                                }
                            }
                        } catch(SQLException ex)
                        {
                            JLabel label = new JLabel("SQL Error - " + ex);
                            label.setFont(new Font("Arial", Font.BOLD, 18));
                            JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });
                insertData.addCancelListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        frame.changeContent(insertMenu);
                    }
                });
                frame.changeContent(insertData);
            }
        });
        CustomButton dungeonInsertButton = new CustomButton("Dungeon", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                InsertData insertData = new InsertData("Dungeon", Arrays.asList("Dungeon Code", "Difficulty Level", "Min. Monster Count", "Max. Monster Count", "Refresh Cooldown Timer", "Owner ID", "Boss Code", "Region Code", "Monster Codes"),
                        Arrays.asList("5in", "1in", "0in", "0in", "0i", "5f", "5fn", "5fn", "5+"));
                try
                {
                    insertData.setForeignKeys(dbcon, Arrays.asList("guildid", "monstercode", "regioncode"), Arrays.asList("guild", "monster", "region"));
                } catch(SQLException ex)
                {
                    JLabel label = new JLabel("SQL Error - " + ex);
                    label.setFont(new Font("Arial", Font.BOLD, 18));
                    JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                }
                insertData.addAddListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        boolean valid = insertData.isValidData();
                        boolean minMaxValid = checkMinMaxConstraint(insertData.getContainerText(2), insertData.getContainerText(3));
                        try
                        {
                            if(valid && minMaxValid && !insertData.isDuplicate(dbcon, "dungeoncode", "dungeon"))
                            {
                                int result = insertData.insertToDatabase(dbcon, 8,
                                        "dungeon (dungeoncode, difficultylevel, minmonstercount, maxmonstercount, refreshcooldowntimer, ownerid, bosscode, regioncode) VALUES (");
                                if(result > 0)
                                {
                                    int nextResult = 0;
                                    for(String value : insertData.getMultivaluedContainerText(8))
                                    {
                                        ResultSet rs = dbcon.executeStatement("SELECT MONSTERCODE FROM MONSTER WHERE MONSTERCODE = " + value);
                                        if(rs.next())
                                        {
                                            String prepSql = "INSERT INTO CONTAINS (DUNGEONCODE, MONSTERCODE) VALUES (" +
                                                    insertData.getContainerText(0) + ", " + value + ")";
                                            System.out.println(prepSql);
                                            nextResult = dbcon.executePrepared(prepSql);
                                        } else
                                        {
                                            JLabel label = new JLabel("Error: One of the monster codes does not exist");
                                            label.setFont(new Font("Arial", Font.BOLD, 18));
                                            JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                                        }
                                    }
                                    if(nextResult > 0)
                                    {
                                        JLabel label = new JLabel("Data added successfully");
                                        label.setFont(new Font("Arial", Font.BOLD, 18));
                                        JOptionPane.showMessageDialog(frame, label, "Insert Successful", JOptionPane.INFORMATION_MESSAGE);
                                        insertData.clearInputBoxes();
                                    }
                                }
                            }
                        } catch(SQLException ex)
                        {
                            JLabel label = new JLabel("SQL Error - " + ex);
                            label.setFont(new Font("Arial", Font.BOLD, 18));
                            JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });
                insertData.addCancelListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        frame.changeContent(insertMenu);
                    }
                });
                frame.changeContent(insertData);
            }
        });
        CustomButton monsterInsertButton = new CustomButton("Monster", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                InsertData insertData = new InsertData("Monster", Arrays.asList("Monster Code", "Name Type", "Difficulty Level", "Health Points", "Weapon", "Is Boss (T or F)", "Base Damage", "Monster Core (Gem) Code", "Skill Moves"),
                        Arrays.asList("5in", "30vn", "1in", "0in", "20v", "1vn", "0in", "1fn", "25+"));
                try
                {
                    insertData.setForeignKeys(dbcon, Arrays.asList("gemcode"), Arrays.asList("gem"));
                } catch(SQLException ex)
                {
                    JLabel label = new JLabel("SQL Error - " + ex);
                    label.setFont(new Font("Arial", Font.BOLD, 18));
                    JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                }
                insertData.addAddListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        boolean valid = insertData.isValidData();
                        boolean boolValid = checkBoolConstraint(insertData.getContainerText(5), 0);
                        try
                        {
                            if(valid && boolValid && !insertData.isDuplicate(dbcon, "monstercode", "monster"))
                            {
                                int result = insertData.insertToDatabase(dbcon, 8,
                                        "monster (monstercode, nametype, difficultylevel, healthpoints, weapon, isboss, basedamage, monstercorecode) VALUES (");
                                if(result > 0)
                                {
                                    int nextResult = 0;
                                    for(String value : insertData.getMultivaluedContainerText(8))
                                    {
                                        String prepSql = "INSERT INTO SKILL_MOVES (MONSTERCODE, SKILLMOVE) VALUES (" +
                                                insertData.getContainerText(0) + ", '" + value + "')";
                                        System.out.println(prepSql);
                                        nextResult = dbcon.executePrepared(prepSql);
                                    }
                                    if(nextResult > 0)
                                    {
                                        JLabel label = new JLabel("Data added successfully");
                                        label.setFont(new Font("Arial", Font.BOLD, 18));
                                        JOptionPane.showMessageDialog(frame, label, "Insert Successful", JOptionPane.INFORMATION_MESSAGE);
                                        insertData.clearInputBoxes();
                                    }
                                }
                            }
                        } catch(SQLException ex)
                        {
                            JLabel label = new JLabel("SQL Error - " + ex);
                            label.setFont(new Font("Arial", Font.BOLD, 18));
                            JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });
                insertData.addCancelListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        frame.changeContent(insertMenu);
                    }
                });
                frame.changeContent(insertData);
            }
        });
        CustomButton gemInsertButton = new CustomButton("Gem", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                InsertData insertData = new InsertData("Gem", Arrays.asList("Gem Code", "Value", "Color"),
                        Arrays.asList("1in", "0in", "20vn"));
                insertData.addAddListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        boolean valid = insertData.isValidData();
                        try
                        {
                            if(valid && !insertData.isDuplicate(dbcon, "gemcode", "gem"))
                            {
                                int result = insertData.insertToDatabase(dbcon, 3,
                                        "gem (gemcode, value, color) VALUES (");
                                if(result > 0)
                                {
                                    JLabel label = new JLabel("Data added successfully");
                                    label.setFont(new Font("Arial", Font.BOLD, 18));
                                    JOptionPane.showMessageDialog(frame, label, "Insert Successful", JOptionPane.INFORMATION_MESSAGE);
                                    insertData.clearInputBoxes();
                                }
                            }
                        } catch(SQLException ex)
                        {
                            JLabel label = new JLabel("SQL Error - " + ex);
                            label.setFont(new Font("Arial", Font.BOLD, 18));
                            JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });
                insertData.addCancelListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        frame.changeContent(insertMenu);
                    }
                });
                frame.changeContent(insertData);
            }
        });
        CustomButton regionInsertButton = new CustomButton("Region", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                InsertData insertData = new InsertData("Region", Arrays.asList("Region Code", "Region Name"),
                        Arrays.asList("5in", "30vn"));
                insertData.addAddListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        boolean valid = insertData.isValidData();
                        try
                        {
                            if(valid && !insertData.isDuplicate(dbcon, "regioncode", "region"))
                            {
                                int result = insertData.insertToDatabase(dbcon, 2,
                                        "region (regioncode, regionname) VALUES (");
                                if(result > 0)
                                {
                                    JLabel label = new JLabel("Data added successfully");
                                    label.setFont(new Font("Arial", Font.BOLD, 18));
                                    JOptionPane.showMessageDialog(frame, label, "Insert Successful", JOptionPane.INFORMATION_MESSAGE);
                                    insertData.clearInputBoxes();
                                }
                            }
                        } catch(SQLException ex)
                        {
                            JLabel label = new JLabel("SQL Error - " + ex);
                            label.setFont(new Font("Arial", Font.BOLD, 18));
                            JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });
                insertData.addCancelListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        frame.changeContent(insertMenu);
                    }
                });
                frame.changeContent(insertData);
            }
        });
        CustomButton habInsertButton = new CustomButton("Hunter Association Branch", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                InsertData insertData = new InsertData("Hunter Association Branch", Arrays.asList("Branch ID", "Region Code"),
                        Arrays.asList("5in", "5fn"));
                try
                {
                    insertData.setForeignKeys(dbcon, Arrays.asList("regioncode"), Arrays.asList("region"));
                } catch(SQLException ex)
                {
                    JLabel label = new JLabel("SQL Error - " + ex);
                    label.setFont(new Font("Arial", Font.BOLD, 18));
                    JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                }
                insertData.addAddListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        boolean valid = insertData.isValidData();
                        try
                        {
                            if(valid && !insertData.isDuplicate(dbcon, "branchid", "hunter_association_branch"))
                            {
                                int result = insertData.insertToDatabase(dbcon, 2,
                                        "hunter_association_branch (branchid, regioncode) VALUES (");
                                if(result > 0)
                                {
                                    JLabel label = new JLabel("Data added successfully");
                                    label.setFont(new Font("Arial", Font.BOLD, 18));
                                    JOptionPane.showMessageDialog(frame, label, "Insert Successful", JOptionPane.INFORMATION_MESSAGE);
                                    insertData.clearInputBoxes();
                                }
                            }
                        } catch(SQLException ex)
                        {
                            JLabel label = new JLabel("SQL Error - " + ex);
                            label.setFont(new Font("Arial", Font.BOLD, 18));
                            JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });
                insertData.addCancelListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        frame.changeContent(insertMenu);
                    }
                });
                frame.changeContent(insertData);
            }
        });
        CustomButton haeInsertButton = new CustomButton("Hunter Association Employee", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                InsertData insertData = new InsertData("Hunter Association Employee", Arrays.asList("Employee ID", "Name", "Salary", "Hire Date", "Branch ID", "Employee Type", "Rank", "Commission", "Client ID", "Authorization Level (1 or 2)"),
                        Arrays.asList("5in", "30vn", "0in", "0dn", "5fn", "0c", "1in", "0in", "5fn", "1i"));
                try
                {
                    insertData.setForeignKeys(dbcon, Arrays.asList("branchid", "hunterid"), Arrays.asList("hunter_association_branch", "hunter"));
                } catch(SQLException ex)
                {
                    JLabel label = new JLabel("SQL Error - " + ex);
                    label.setFont(new Font("Arial", Font.BOLD, 18));
                    JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                }
                insertData.addAddListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        boolean valid = insertData.isValidData();
                        boolean boolValid = checkBoolConstraint(insertData.getContainerText(9), 1);
                        try
                        {
                            if(valid && boolValid && !insertData.isDuplicate(dbcon, "employeeid", "hunter_association_employee"))
                            {
                                int result = insertData.insertToDatabase(dbcon, 6,
                                        "hunter_association_employee (employeeid, name, salary, hiredate, branchid, emptype) VALUES (");
                                if(result > 0)
                                {
                                    int nextResult;
                                    String prepSql;
                                    if(insertData.getCustomComboBoxIndex() == 0)
                                    {
                                        prepSql = "INSERT INTO receptionist (employeeid, authorizationlevel) VALUES ( +" +
                                                insertData.getContainerText(0) + ", " + insertData.getContainerText(9) + ")";
                                        nextResult = dbcon.executePrepared(prepSql);
                                    } else
                                    {
                                        prepSql = "INSERT INTO guide_helper (employeeid, rank, commission, clientid) VALUES ( +" +
                                                insertData.getContainerText(0) + ", " +
                                                insertData.getContainerText(6) + ", " +
                                                insertData.getContainerText(7) + ", " +
                                                insertData.getContainerText(8) + ")";
                                        nextResult = dbcon.executePrepared(prepSql);
                                    }
                                    if(nextResult > 0)
                                    {
                                        JLabel label = new JLabel("Data added successfully");
                                        label.setFont(new Font("Arial", Font.BOLD, 18));
                                        JOptionPane.showMessageDialog(frame, label, "Insert Successful", JOptionPane.INFORMATION_MESSAGE);
                                        insertData.clearInputBoxes();
                                    }
                                }
                            }
                        } catch(SQLException ex)
                        {
                            JLabel label = new JLabel("SQL Error - " + ex);
                            label.setFont(new Font("Arial", Font.BOLD, 18));
                            JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });
                insertData.addCancelListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        frame.changeContent(insertMenu);
                    }
                });
                frame.changeContent(insertData);
            }
        });
        CustomButton hunterUDButton = new CustomButton("Hunter", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                UpdateDeleteData updateDeleteData = new UpdateDeleteData("Hunter", Arrays.asList("Hunter ID", "Name", "Class", "Rank", "Date Joined", "No. Quests Cleared", "Main Weapon", "Guild ID", "Special Abilities"),
                        Arrays.asList("5in", "30vn", "15vn", "1in", "0dn", "0in", "20v", "5f", "25+"));
                try
                {
                    updateDeleteData.getNewData(dbcon, Arrays.asList("guildid"), Arrays.asList("guild"),
                            "hunterid", "hunter",
                            Arrays.asList("hunterid", "name", "class", "rank", "datejoined", "questscleared", "mainweapon", "guildid"),
                            Arrays.asList("specialability", "special_abilities", "hunterid"));
                } catch(SQLException ex)
                {
                    JLabel label = new JLabel("SQL Error - " + ex);
                    label.setFont(new Font("Arial", Font.BOLD, 18));
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                }
                updateDeleteData.addUpdateListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to update the table?");
                        if(confirmation == JOptionPane.YES_OPTION)
                        {
                            boolean valid = updateDeleteData.isValidData();
                            try
                            {
                                if(valid)
                                {
                                    int result = updateDeleteData.updateToDatabase(dbcon, "hunter", Arrays.asList("hunterid", "name", "class", "rank", "datejoined", "questscleared", "mainweapon", "guildid"));
                                    if(result > 0)
                                    {
                                        int deleteMultivaluedResult = dbcon.executePrepared("DELETE SPECIAL_ABILITIES WHERE HUNTERID = " + updateDeleteData.getStoredPk());
                                        int addMultivaluedResult = 0;
                                        if(deleteMultivaluedResult > 0)
                                        {
                                            for(String value : updateDeleteData.getMultivaluedContainerText(8))
                                            {
                                                String prepSql = "INSERT INTO SPECIAL_ABILITIES (hunterid, specialability) VALUES (" +
                                                        updateDeleteData.getStoredPk()+ ", '" + value + "')";
                                                System.out.println(prepSql);
                                                addMultivaluedResult = dbcon.executePrepared(prepSql);
                                            }
                                            if(addMultivaluedResult > 0)
                                            {
                                                JLabel label = new JLabel("Data updated successfully");
                                                label.setFont(new Font("Arial", Font.BOLD, 18));
                                                JOptionPane.showMessageDialog(frame, label, "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                                                updateDeleteData.getNewData(dbcon, Arrays.asList("guildid"), Arrays.asList("guild"),
                                                        "hunterid", "hunter",
                                                        Arrays.asList("hunterid", "name", "class", "rank", "datejoined", "questscleared", "mainweapon", "guildid"),
                                                        Arrays.asList("specialability", "special_abilities", "hunterid"));
                                            }
                                        }
                                    }
                                }
                            } catch(SQLException ex)
                            {
                                JLabel label = new JLabel("SQL Error - " + ex);
                                label.setFont(new Font("Arial", Font.BOLD, 18));
                                JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                });
                updateDeleteData.addDeleteListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete from table?");
                        if(confirmation == JOptionPane.YES_OPTION)
                        {
                            try
                            {
                                int newResult = dbcon.executePrepared("DELETE SPECIAL_ABILITIES WHERE HUNTERID = " + updateDeleteData.getStoredPk());
                                if(newResult > 0)
                                {
                                    int result = updateDeleteData.deleteFromDatabase(dbcon, "hunterid", "hunter");
                                    if(result > 0)
                                    {
                                        JLabel label = new JLabel("Data deleted successfully");
                                        label.setFont(new Font("Arial", Font.BOLD, 18));
                                        JOptionPane.showMessageDialog(frame, label, "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                                        updateDeleteData.getNewData(dbcon, Arrays.asList("guildid"), Arrays.asList("hunter"), "hunterid", "hunter", Arrays.asList("hunterid", "name", "class", "rank", "datejoined", "questscleared", "mainweapon", "guildid"),
                                                Arrays.asList("specialability", "special_abilities", "hunterid"));
                                    }
                                }
                            } catch(SQLException ex)
                            {
                                JLabel label = new JLabel("SQL Error - " + ex);
                                label.setFont(new Font("Arial", Font.BOLD, 18));
                                JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                });
                updateDeleteData.addCancelListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        frame.changeContent(updateDeleteMenu);
                    }
                });
                frame.changeContent(updateDeleteData);
            }
        });

        CustomButton guildUDButton = new CustomButton("Guild", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                UpdateDeleteData updateDeleteData = new UpdateDeleteData("Guild", Arrays.asList("Guild ID", "Name", "Faction Element", "Guild Leader ID", "Region Code"),
                        Arrays.asList("5in", "30vn", "15v", "5fn", "5fn"));
                try
                {
                    updateDeleteData.getNewData(dbcon, Arrays.asList("hunterid", "regioncode"), Arrays.asList("hunter", "region"), "guildid", "guild", Arrays.asList("GUILDID", "NAME", "FACTIONELEMENT", "GUILDLEADERID", "REGIONCODE"), null);
                } catch(SQLException ex)
                {
                    JLabel label = new JLabel("SQL Error - " + ex);
                    label.setFont(new Font("Arial", Font.BOLD, 18));
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                }
                updateDeleteData.addUpdateListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to update the table?");
                        if(confirmation == JOptionPane.YES_OPTION)
                        {
                            boolean valid = updateDeleteData.isValidData();
                            try
                            {
                                if(valid)
                                {
                                    int result = updateDeleteData.updateToDatabase(dbcon, "guild", Arrays.asList("GUILDID", "NAME", "FACTIONELEMENT", "GUILDLEADERID", "REGIONCODE"));
                                    if(result > 0)
                                    {
                                        JLabel label = new JLabel("Data updated successfully");
                                        label.setFont(new Font("Arial", Font.BOLD, 18));
                                        JOptionPane.showMessageDialog(frame, label, "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                                        updateDeleteData.getNewData(dbcon, Arrays.asList("hunterid", "regioncode"), Arrays.asList("hunter", "region"), "guildid", "guild", Arrays.asList("GUILDID", "NAME", "FACTIONELEMENT", "GUILDLEADERID", "REGIONCODE"), null);
                                    }
                                }
                            } catch(SQLException ex)
                            {
                                JLabel label = new JLabel("SQL Error - " + ex);
                                label.setFont(new Font("Arial", Font.BOLD, 18));
                                JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                });
                updateDeleteData.addDeleteListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete from table?");
                        if(confirmation == JOptionPane.YES_OPTION)
                        {
                            try
                            {
                                int result = updateDeleteData.deleteFromDatabase(dbcon, "guildid", "guild");
                                if(result > 0)
                                {
                                    JLabel label = new JLabel("Data deleted successfully");
                                    label.setFont(new Font("Arial", Font.BOLD, 18));
                                    JOptionPane.showMessageDialog(frame, label, "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                                    updateDeleteData.getNewData(dbcon, Arrays.asList("hunterid", "regioncode"), Arrays.asList("hunter", "region"), "guildid", "guild", Arrays.asList("GUILDID", "NAME", "FACTIONELEMENT", "GUILDLEADERID", "REGIONCODE"), null);
                                }
                            } catch(SQLException ex)
                            {
                                JLabel label = new JLabel("SQL Error - " + ex);
                                label.setFont(new Font("Arial", Font.BOLD, 18));
                                JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                });
                updateDeleteData.addCancelListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        frame.changeContent(updateDeleteMenu);
                    }
                });
                frame.changeContent(updateDeleteData);
            }
        });
        CustomButton dungeonUDButton = new CustomButton("Dungeon", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                UpdateDeleteData updateDeleteData = new UpdateDeleteData("Dungeon", Arrays.asList("Dungeon Code", "Difficulty Level", "Min. Monster Count", "Max. Monster Count", "Refresh Cooldown Timer", "Owner ID", "Boss Code", "Region Code", "Monster Codes"),
                        Arrays.asList("5in", "1in", "0in", "0in", "0i", "5f", "5fn", "5fn", "5+"));
                try
                {
                    updateDeleteData.getNewData(dbcon, Arrays.asList("guildid", "monstercode", "regioncode"), Arrays.asList("guild", "monster", "region"), "dungeoncode", "dungeon", Arrays.asList("DUNGEONCODE", "DIFFICULTYLEVEL", "MINMONSTERCOUNT", "MAXMONSTERCOUNT", "REFRESHCOOLDOWNTIMER", "OWNERID", "BOSSCODE", "REGIONCODE"),
                            Arrays.asList("monstercode", "contains", "dungeoncode"));
                } catch(SQLException ex)
                {
                    JLabel label = new JLabel("SQL Error - " + ex);
                    label.setFont(new Font("Arial", Font.BOLD, 18));
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                }
                updateDeleteData.addUpdateListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to update the table?");
                        if(confirmation == JOptionPane.YES_OPTION)
                        {
                            boolean valid = updateDeleteData.isValidData();
                            try
                            {
                                if(valid)
                                {
                                    int result = updateDeleteData.updateToDatabase(dbcon, "dungeon", Arrays.asList("DUNGEONCODE", "DIFFICULTYLEVEL", "MINMONSTERCOUNT", "MAXMONSTERCOUNT", "REFRESHCOOLDOWNTIMER", "OWNERID", "BOSSCODE", "REGIONCODE"));
                                    int deleteMultivaluedResult = dbcon.executePrepared("DELETE CONTAINS WHERE DUNGEONCODE = " + updateDeleteData.getStoredPk());
                                    int addMultivaluedResult = 0;
                                    if(deleteMultivaluedResult > 0)
                                    {
                                        for(String value : updateDeleteData.getMultivaluedContainerText(8))
                                        {
                                            String prepSql = "INSERT INTO CONTAINS (DUNGEONCODE, MONSTERCODE) VALUES (" +
                                                    updateDeleteData.getStoredPk()+ ", " + value + ")";
                                            System.out.println(prepSql);
                                            addMultivaluedResult = dbcon.executePrepared(prepSql);
                                        }
                                        if(addMultivaluedResult > 0)
                                        {
                                            JLabel label = new JLabel("Data updated successfully");
                                            label.setFont(new Font("Arial", Font.BOLD, 18));
                                            JOptionPane.showMessageDialog(frame, label, "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                                            updateDeleteData.getNewData(dbcon, Arrays.asList("guildid", "monstercode", "regioncode"), Arrays.asList("guild", "monster", "region"), "dungeoncode", "dungeon", Arrays.asList("DUNGEONCODE", "DIFFICULTYLEVEL", "MINMONSTERCOUNT", "MAXMONSTERCOUNT", "REFRESHCOOLDOWNTIMER", "OWNERID", "BOSSCODE", "REGIONCODE"),
                                                    Arrays.asList("monstercode", "contains", "dungeoncode"));
                                        }
                                    }
                                }
                            } catch(SQLException ex)
                            {
                                JLabel label = new JLabel("SQL Error - " + ex);
                                label.setFont(new Font("Arial", Font.BOLD, 18));
                                JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                });
                updateDeleteData.addDeleteListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete from table?");
                        if(confirmation == JOptionPane.YES_OPTION)
                        {
                            try
                            {
                                int newResult = dbcon.executePrepared("DELETE CONTAINS WHERE DUNGEONCODE = " + updateDeleteData.getStoredPk());
                                if(newResult > 0)
                                {
                                    int result = updateDeleteData.deleteFromDatabase(dbcon, "dungeoncode", "dungeon");
                                    if(result > 0)
                                    {
                                        JLabel label = new JLabel("Data deleted successfully");
                                        label.setFont(new Font("Arial", Font.BOLD, 18));
                                        JOptionPane.showMessageDialog(frame, label, "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                                        updateDeleteData.getNewData(dbcon, Arrays.asList("guildid", "monstercode", "regioncode"), Arrays.asList("guild", "monster", "region"), "dungeoncode", "dungeon", Arrays.asList("DUNGEONCODE", "DIFFICULTYLEVEL", "MINMONSTERCOUNT", "MAXMONSTERCOUNT", "REFRESHCOOLDOWNTIMER", "OWNERID", "BOSSCODE", "REGIONCODE"),
                                                Arrays.asList("monstercode", "contains", "dungeoncode"));
                                    }
                                }
                            } catch(SQLException ex)
                            {
                                JLabel label = new JLabel("SQL Error - " + ex);
                                label.setFont(new Font("Arial", Font.BOLD, 18));
                                JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                });
                updateDeleteData.addCancelListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        frame.changeContent(updateDeleteMenu);
                    }
                });
                frame.changeContent(updateDeleteData);
            }
        });
        CustomButton monsterUDButton = new CustomButton("Monster", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                UpdateDeleteData updateDeleteData = new UpdateDeleteData("Monster", Arrays.asList("Monster Code", "Name Type", "Difficulty Level", "Health Points", "Weapon", "Is Boss (T or F)", "Base Damage", "Monster Core (Gem) Code", "Skill Moves"),
                        Arrays.asList("5in", "30vn", "1in", "0in", "20v", "1vn", "0in", "1fn", "25+"));
                try
                {
                    updateDeleteData.getNewData(dbcon, Arrays.asList("gemcode"), Arrays.asList("gem"), "monstercode", "monster", Arrays.asList("monstercode", "nametype", "difficultylevel", "HEALTHPOINTS", "WEAPON", "ISBOSS", "BASEDAMAGE", "MONSTERCORECODE"),
                            Arrays.asList("skillmove", "skill_moves", "monstercode"));
                } catch(SQLException ex)
                {
                    JLabel label = new JLabel("SQL Error - " + ex);
                    label.setFont(new Font("Arial", Font.BOLD, 18));
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                }
                updateDeleteData.addUpdateListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to update the table?");
                        if(confirmation == JOptionPane.YES_OPTION)
                        {
                            boolean valid = updateDeleteData.isValidData();
                            try
                            {
                                if(valid)
                                {
                                    int result = updateDeleteData.updateToDatabase(dbcon, "monster", Arrays.asList("monstercode", "nametype", "difficultylevel", "HEALTHPOINTS", "WEAPON", "ISBOSS", "BASEDAMAGE", "MONSTERCORECODE"));
                                    if(result > 0)
                                    {
                                        int deleteMultivaluedResult = dbcon.executePrepared("DELETE SKILL_MOVES WHERE MONSTERCODE = " + updateDeleteData.getStoredPk());
                                        int addMultivaluedResult = 0;
                                        if(deleteMultivaluedResult > 0)
                                        {
                                            for(String value : updateDeleteData.getMultivaluedContainerText(8))
                                            {
                                                String prepSql = "INSERT INTO SKILL_MOVES (MONSTERCODE, skillmove) VALUES (" +
                                                        updateDeleteData.getStoredPk()+ ", '" + value + "')";
                                                System.out.println(prepSql);
                                                addMultivaluedResult = dbcon.executePrepared(prepSql);
                                            }
                                            if(addMultivaluedResult > 0)
                                            {
                                                JLabel label = new JLabel("Data updated successfully");
                                                label.setFont(new Font("Arial", Font.BOLD, 18));
                                                JOptionPane.showMessageDialog(frame, label, "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                                                updateDeleteData.getNewData(dbcon, Arrays.asList("gemcode"), Arrays.asList("gem"), "monstercode", "monster", Arrays.asList("monstercode", "nametype", "difficultylevel", "HEALTHPOINTS", "WEAPON", "ISBOSS", "BASEDAMAGE", "MONSTERCORECODE"),
                                                        Arrays.asList("skillmove", "skill_moves", "monstercode"));
                                            }
                                        }
                                    }
                                }
                            } catch(SQLException ex)
                            {
                                JLabel label = new JLabel("SQL Error - " + ex);
                                label.setFont(new Font("Arial", Font.BOLD, 18));
                                JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                });
                updateDeleteData.addDeleteListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete from table?");
                        if(confirmation == JOptionPane.YES_OPTION)
                        {
                            try
                            {
                                int newResult = dbcon.executePrepared("DELETE SKILL_MOVES WHERE MONSTERCODE = " + updateDeleteData.getStoredPk());
                                if(newResult > 0)
                                {
                                    int result = updateDeleteData.deleteFromDatabase(dbcon, "monstercode", "monster");
                                    if(result > 0)
                                    {
                                        JLabel label = new JLabel("Data deleted successfully");
                                        label.setFont(new Font("Arial", Font.BOLD, 18));
                                        JOptionPane.showMessageDialog(frame, label, "Delete Successful", JOptionPane.INFORMATION_MESSAGE);
                                        updateDeleteData.getNewData(dbcon, Arrays.asList("gemcode"), Arrays.asList("gem"), "monstercode", "monster", Arrays.asList("monstercode", "nametype", "difficultylevel", "HEALTHPOINTS", "WEAPON", "ISBOSS", "BASEDAMAGE", "MONSTERCORECODE"),
                                                Arrays.asList("skillmove", "skill_moves", "monstercode"));
                                    }
                                }
                            } catch(SQLException ex)
                            {
                                JLabel label = new JLabel("SQL Error - " + ex);
                                label.setFont(new Font("Arial", Font.BOLD, 18));
                                JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                });
                updateDeleteData.addCancelListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        frame.changeContent(updateDeleteMenu);
                    }
                });
                frame.changeContent(updateDeleteData);
            }
        });
        CustomButton gemUDButton = new CustomButton("Gem", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                UpdateDeleteData updateDeleteData = new UpdateDeleteData("Gem", Arrays.asList("Gem Code", "Value", "Color"),
                        Arrays.asList("1in", "0in", "20vn"));
                try
                {
                    updateDeleteData.getNewData(dbcon, null, null, "gemcode", "gem", Arrays.asList("gemcode", "value", "color"), null);
                } catch(SQLException ex)
                {
                    JLabel label = new JLabel("SQL Error - " + ex);
                    label.setFont(new Font("Arial", Font.BOLD, 18));
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                }
                updateDeleteData.addUpdateListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to update the table?");
                        if(confirmation == JOptionPane.YES_OPTION)
                        {
                            boolean valid = updateDeleteData.isValidData();
                            try
                            {
                                if(valid)
                                {
                                    int result = updateDeleteData.updateToDatabase(dbcon, "gem", Arrays.asList("gemcode", "value", "color"));
                                    if(result > 0)
                                    {
                                        JLabel label = new JLabel("Data updated successfully");
                                        label.setFont(new Font("Arial", Font.BOLD, 18));
                                        JOptionPane.showMessageDialog(frame, label, "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                                        updateDeleteData.getNewData(dbcon, null, null, "gemcode", "gem", Arrays.asList("gemcode", "value", "color"), null);
                                    }
                                }
                            } catch(SQLException ex)
                            {
                                JLabel label = new JLabel("SQL Error - " + ex);
                                label.setFont(new Font("Arial", Font.BOLD, 18));
                                JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                });
                updateDeleteData.addDeleteListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete from table?");
                        if(confirmation == JOptionPane.YES_OPTION)
                        {
                            try
                            {
                                int result = updateDeleteData.deleteFromDatabase(dbcon, "gemcode", "gem");
                                if(result > 0)
                                {
                                    JLabel label = new JLabel("Data deleted successfully");
                                    label.setFont(new Font("Arial", Font.BOLD, 18));
                                    JOptionPane.showMessageDialog(frame, label, "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                                    updateDeleteData.getNewData(dbcon, null, null, "gemcode", "gem", Arrays.asList("gemcode", "value", "color"), null);
                                }
                            } catch(SQLException ex)
                            {
                                JLabel label = new JLabel("SQL Error - " + ex);
                                label.setFont(new Font("Arial", Font.BOLD, 18));
                                JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                });
                updateDeleteData.addCancelListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        frame.changeContent(updateDeleteMenu);
                    }
                });
                frame.changeContent(updateDeleteData);
            }
        });
        CustomButton regionUDButton = new CustomButton("Region", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                UpdateDeleteData updateDeleteData = new UpdateDeleteData("Region", Arrays.asList("Region Code", "Region Name"),
                        Arrays.asList("5in", "30vn"));
                try
                {
                    updateDeleteData.getNewData(dbcon, null, null, "regioncode", "region", Arrays.asList("regioncode", "regionname"), null);
                } catch(SQLException ex)
                {
                    JLabel label = new JLabel("SQL Error - " + ex);
                    label.setFont(new Font("Arial", Font.BOLD, 18));
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                }
                updateDeleteData.addUpdateListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to update the table?");
                        if(confirmation == JOptionPane.YES_OPTION)
                        {
                            boolean valid = updateDeleteData.isValidData();
                            try
                            {
                                if(valid)
                                {
                                    int result = updateDeleteData.updateToDatabase(dbcon, "region", Arrays.asList("regioncode", "regionname"));
                                    if(result > 0)
                                    {
                                        JLabel label = new JLabel("Data updated successfully");
                                        label.setFont(new Font("Arial", Font.BOLD, 18));
                                        JOptionPane.showMessageDialog(frame, label, "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                                        updateDeleteData.getNewData(dbcon, null, null, "regioncode", "region", Arrays.asList("regioncode", "regionname"), null);
                                    }
                                }
                            } catch(SQLException ex)
                            {
                                JLabel label = new JLabel("SQL Error - " + ex);
                                label.setFont(new Font("Arial", Font.BOLD, 18));
                                JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                });
                updateDeleteData.addDeleteListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete from table?");
                        if(confirmation == JOptionPane.YES_OPTION)
                        {
                            try
                            {
                                int result = updateDeleteData.deleteFromDatabase(dbcon, "regioncode", "region");
                                if(result > 0)
                                {
                                    JLabel label = new JLabel("Data deleted successfully");
                                    label.setFont(new Font("Arial", Font.BOLD, 18));
                                    JOptionPane.showMessageDialog(frame, label, "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                                    updateDeleteData.getNewData(dbcon, null, null, "regioncode", "region", Arrays.asList("regioncode", "regionname"), null);
                                }
                            } catch(SQLException ex)
                            {
                                JLabel label = new JLabel("SQL Error - " + ex);
                                label.setFont(new Font("Arial", Font.BOLD, 18));
                                JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                });
                updateDeleteData.addCancelListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        frame.changeContent(updateDeleteMenu);
                    }
                });
                frame.changeContent(updateDeleteData);
            }
        });
        CustomButton habUDButton = new CustomButton("Hunter Association Branch", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                UpdateDeleteData updateDeleteData = new UpdateDeleteData("Hunter Association Branch", Arrays.asList("Branch ID", "Region Code"),
                        Arrays.asList("5in", "5fn"));
                try
                {
                    updateDeleteData.getNewData(dbcon, Arrays.asList("regioncode"), Arrays.asList("region"), "branchid", "hunter_association_branch", Arrays.asList("BRANCHID","REGIONCODE"), null);
                } catch(SQLException ex)
                {
                    JLabel label = new JLabel("SQL Error - " + ex);
                    label.setFont(new Font("Arial", Font.BOLD, 18));
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                }
                updateDeleteData.addUpdateListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to update the table?");
                        if(confirmation == JOptionPane.YES_OPTION)
                        {
                            boolean valid = updateDeleteData.isValidData();
                            try
                            {
                                if(valid)
                                {
                                    int result = updateDeleteData.updateToDatabase(dbcon, "hunter_association_branch", Arrays.asList("BRANCHID", "REGIONCODE"));
                                    if(result > 0)
                                    {
                                        JLabel label = new JLabel("Data updated successfully");
                                        label.setFont(new Font("Arial", Font.BOLD, 18));
                                        JOptionPane.showMessageDialog(frame, label, "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                                        updateDeleteData.getNewData(dbcon, Arrays.asList("regioncode"), Arrays.asList("region"), "branchid", "hunter_association_branch", Arrays.asList("BRANCHID","REGIONCODE"), null);
                                    }
                                }
                            } catch(SQLException ex)
                            {
                                JLabel label = new JLabel("SQL Error - " + ex);
                                label.setFont(new Font("Arial", Font.BOLD, 18));
                                JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                });
                updateDeleteData.addDeleteListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete from table?");
                        if(confirmation == JOptionPane.YES_OPTION)
                        {
                            try
                            {
                                int result = updateDeleteData.deleteFromDatabase(dbcon, "branchid", "hunter_association_branch");
                                if(result > 0)
                                {
                                    JLabel label = new JLabel("Data deleted successfully");
                                    label.setFont(new Font("Arial", Font.BOLD, 18));
                                    JOptionPane.showMessageDialog(frame, label, "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                                    updateDeleteData.getNewData(dbcon, Arrays.asList("regioncode"), Arrays.asList("region"), "branchid", "hunter_association_branch", Arrays.asList("BRANCHID","REGIONCODE"), null);
                                }
                            } catch(SQLException ex)
                            {
                                JLabel label = new JLabel("SQL Error - " + ex);
                                label.setFont(new Font("Arial", Font.BOLD, 18));
                                JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                });
                updateDeleteData.addCancelListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        frame.changeContent(updateDeleteMenu);
                    }
                });
                frame.changeContent(updateDeleteData);
            }
        });
        CustomButton haeUDButton = new CustomButton("Hunter Association Employee", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                UpdateDeleteData updateDeleteData = new UpdateDeleteData("Hunter Association Employee", Arrays.asList("Employee ID", "Name", "Salary", "Hire Date", "Branch ID", "Employee Type", "Rank", "Commission", "Client ID", "Authorization Level (1 or 2)"),
                        Arrays.asList("5in", "30vn", "0in", "0dn", "5fn", "0c", "1in", "0in", "5fn", "1i"));
                try
                {
                    updateDeleteData.getNewData(dbcon, Arrays.asList("branchid", "hunterid"), Arrays.asList("hunter_association_branch", "hunter"), "employeeid", "HUNTER_ASSOCIATION_EMPLOYEE", Arrays.asList("EMPLOYEEID", "NAME", "SALARY", "HIREDATE", "BRANCHID", "EMPTYPE"), null);

                } catch(SQLException ex)
                {
                    JLabel label = new JLabel("SQL Error - " + ex);
                    label.setFont(new Font("Arial", Font.BOLD, 18));
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                }
                updateDeleteData.addUpdateListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to update the table?");
                        if(confirmation == JOptionPane.YES_OPTION)
                        {
                            boolean valid = updateDeleteData.isValidData();
                            try
                            {
                                if(valid)
                                {
                                    int result = updateDeleteData.updateToDatabase(dbcon, "hunter_association_employee", Arrays.asList("EMPLOYEEID", "NAME", "SALARY", "HIREDATE", "BRANCHID", "EMPTYPE"));
                                    if(result > 0)
                                    {
                                        JLabel label = new JLabel("Data updated successfully");
                                        label.setFont(new Font("Arial", Font.BOLD, 18));
                                        JOptionPane.showMessageDialog(frame, label, "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                                        updateDeleteData.getNewData(dbcon, Arrays.asList("branchid", "hunterid"), Arrays.asList("branch", "hunter"), "employeeid", "hunter_association_employee", Arrays.asList("EMPLOYEEID", "NAME", "SALARY", "HIREDATE", "BRANCHID", "EMPTYPE"), null);
                                    }
                                }
                            } catch(SQLException ex)
                            {
                                JLabel label = new JLabel("SQL Error - " + ex);
                                label.setFont(new Font("Arial", Font.BOLD, 18));
                                JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                });
                updateDeleteData.addDeleteListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete from table?");
                        if(confirmation == JOptionPane.YES_OPTION)
                        {
                            try
                            {
                                int result = updateDeleteData.deleteFromDatabase(dbcon, "employeeid", "hunter_association_employee");
                                if(result > 0)
                                {
                                    JLabel label = new JLabel("Data deleted successfully");
                                    label.setFont(new Font("Arial", Font.BOLD, 18));
                                    JOptionPane.showMessageDialog(frame, label, "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                                    updateDeleteData.getNewData(dbcon, Arrays.asList("branchid", "hunterid"), Arrays.asList("branch", "hunter"), "employeeid", "hunter_association_employee", Arrays.asList("EMPLOYEEID", "NAME", "SALARY", "HIREDATE", "BRANCHID", "EMPTYPE"), null);
                                }
                            } catch(SQLException ex)
                            {
                                JLabel label = new JLabel("SQL Error - " + ex);
                                label.setFont(new Font("Arial", Font.BOLD, 18));
                                JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                });
                updateDeleteData.addCancelListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        frame.changeContent(updateDeleteMenu);
                    }
                });
                frame.changeContent(updateDeleteData);
            }
        });

        CustomButton hbgQuery = new CustomButton("Search Hunters By Guild", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                frame.changeContent(createQueryPanel(new HunterFromGuild()));
            }
        });
        CustomButton gbrQuery = new CustomButton("Search Guilds By Region", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                frame.changeContent(createQueryPanel(new GuildFromRegion()));
            }
        });
        CustomButton nhbgQuery = new CustomButton("Search No. Of Hunters By Guild", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                frame.changeContent(createQueryPanel(new NoHuntersFromGuild()));
            }
        });
        CustomButton ngbrQuery = new CustomButton("Search No. Of Guilds By Region", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                frame.changeContent(createQueryPanel(new NoGuildsFromRegion()));
            }
        });

        CustomButton insertDataButton = new CustomButton("Insert Data", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                frame.changeContent(insertMenu);
            }
        });
        CustomButton updateDeleteButton = new CustomButton("Update/Delete Data", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                frame.changeContent(updateDeleteMenu);
            }
        });
        CustomButton addUserButton = new CustomButton("Add New User", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                InsertData insertData = new InsertData("User", Arrays.asList("Username", "Password", "Name", "Is Admin (0 for admin, 1 for normal)"),
                        Arrays.asList("25vn", "25vn", "25vn", "1in"));
                insertData.addAddListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        boolean valid = insertData.isValidData();
                        boolean checkBoolConstraint = checkBoolConstraint(insertData.getContainerText(3), 2);
                        try
                        {
                            if(valid && checkBoolConstraint && !insertData.isDuplicateString(dbcon, "username", "loginusers"))
                            {
                                String prepSql = "INSERT INTO loginusers (username, password, name, type) VALUES ('" +
                                        insertData.getContainerText(0).toLowerCase() + "', '" +
                                        insertData.getContainerText(1) + "', '" +
                                        insertData.getContainerText(2).toLowerCase() + "', " +
                                        insertData.getContainerText(3) + ")";
                                System.out.println(prepSql);
                                int result = dbcon.executePrepared(prepSql);
                                if(result > 0)
                                {
                                    JLabel label = new JLabel("Data added successfully");
                                    label.setFont(new Font("Arial", Font.BOLD, 18));
                                    JOptionPane.showMessageDialog(frame, label, "Insert Successful", JOptionPane.INFORMATION_MESSAGE);
                                    insertData.clearInputBoxes();
                                }
                            }
                        } catch(SQLException ex)
                        {
                            JLabel label = new JLabel("SQL Error - " + ex);
                            label.setFont(new Font("Arial", Font.BOLD, 18));
                            JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });
                insertData.addCancelListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        frame.changeContent(manageUsersMenu);
                    }
                });
                frame.changeContent(insertData);
            }
        });
        CustomButton updateDeleteUserButton = new CustomButton("Update/Delete User", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                UpdateDeleteData updateDeleteData = new UpdateDeleteData("User", Arrays.asList("Username", "Password", "Name", "Admin (0 for admin, 1 for normal)"),
                        Arrays.asList("25vn", "25vn", "25vn", "1in"));
                try
                {
                    updateDeleteData.getNewData(dbcon, null, null, "username", "loginusers", Arrays.asList("username", "password", "name", "type"), null);
                } catch(SQLException ex)
                {
                    JLabel label = new JLabel("SQL Error - " + ex);
                    label.setFont(new Font("Arial", Font.BOLD, 18));
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                }
                updateDeleteData.addUpdateListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to update the table?");
                        if(confirmation == JOptionPane.YES_OPTION)
                        {
                            boolean valid = updateDeleteData.isValidData();
                            boolean checkBoolConstraint = checkBoolConstraint(updateDeleteData.getContainerText(3), 2);
                            try
                            {
                                if(valid && checkBoolConstraint)
                                {
                                    String prepSql = "UPDATE loginusers SET username = '" +
                                            updateDeleteData.getContainerText(0) + "', password = '" +
                                            updateDeleteData.getContainerText(1) + "', name = '" +
                                            updateDeleteData.getContainerText(2) + "', type = " +
                                            updateDeleteData.getContainerText(3) + " WHERE username = '" + updateDeleteData.getStoredPk() + "'";
                                    System.out.println(prepSql);
                                    int result = dbcon.executePrepared(prepSql);
                                    if(result > 0)
                                    {
                                        JLabel label = new JLabel("Data updated successfully");
                                        label.setFont(new Font("Arial", Font.BOLD, 18));
                                        JOptionPane.showMessageDialog(frame, label, "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                                        updateDeleteData.getNewData(dbcon, null, null, "username", "loginusers", Arrays.asList("username", "password", "name", "type"), null);

                                    }
                                }
                            } catch(SQLException ex)
                            {
                                JLabel label = new JLabel("SQL Error - " + ex);
                                label.setFont(new Font("Arial", Font.BOLD, 18));
                                JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                });
                updateDeleteData.addDeleteListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete from table?");
                        if(confirmation == JOptionPane.YES_OPTION)
                        {
                            try
                            {
                                String prepSql = "DELETE loginusers WHERE username = '" + updateDeleteData.getStoredPk() + "'";
                                System.out.println(prepSql);
                                int result = dbcon.executePrepared(prepSql);
                                if(result > 0)
                                {
                                    JLabel label = new JLabel("Data deleted successfully");
                                    label.setFont(new Font("Arial", Font.BOLD, 18));
                                    JOptionPane.showMessageDialog(frame, label, "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                                    updateDeleteData.getNewData(dbcon, null, null, "username", "loginusers", Arrays.asList("username", "password", "name", "type"), null);
                                }
                            } catch(SQLException ex)
                            {
                                JLabel label = new JLabel("SQL Error - " + ex);
                                label.setFont(new Font("Arial", Font.BOLD, 18));
                                JOptionPane.showMessageDialog(frame, label, "SQL Error", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                });
                updateDeleteData.addCancelListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        frame.changeContent(manageUsersMenu);
                    }
                });
                frame.changeContent(updateDeleteData);
            }
        });
        CustomButton manageUserButton = new CustomButton("Manage Users", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                frame.changeContent(manageUsersMenu);
            }
        });
        CustomButton queryButton = new CustomButton("Query Data", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                frame.changeContent(queryMenu);
            }
        });
        CustomButton logoutButton = new CustomButton("Log Out", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                frame.changeContent(loginPage);
                loginPage.reset();
            }
        });
        CustomButton backInsertButton = new CustomButton("Back", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                frame.changeContent(mainMenu);
            }
        });
        CustomButton backUDButton = new CustomButton("Back", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                frame.changeContent(mainMenu);
            }
        });
        CustomButton backQueryButton = new CustomButton("Back", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                frame.changeContent(mainMenu);
            }
        });
        CustomButton backManageButton = new CustomButton("Back", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                frame.changeContent(mainMenu);
            }
        });
        insertMenu = new MainMenu("Select Table to Insert Data", 4, 2, backInsertButton, hunterInsertButton, guildInsertButton, dungeonInsertButton, monsterInsertButton, gemInsertButton, regionInsertButton, habInsertButton, haeInsertButton);
        updateDeleteMenu = new MainMenu("Select Table to Update/Delete Data", 4, 2, backUDButton, hunterUDButton, guildUDButton, dungeonUDButton, monsterUDButton, gemUDButton, regionUDButton, habUDButton, haeUDButton);
        queryMenu = new MainMenu("Select Query", 2, 2, backQueryButton, hbgQuery, gbrQuery, ngbrQuery, nhbgQuery);
        manageUsersMenu = new MainMenu("Manage Users", 1, 2, backManageButton, addUserButton, updateDeleteUserButton);
        loginPage.addLoginListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    String username = loginPage.getUsername();
                    String password = loginPage.getPassword();
                    boolean validLogin = false;
                    //-------------------------------
                    String sqlStr = "SELECT * FROM loginusers "
                            + "WHERE username = '" + username
                            + "' AND password = '" + password + "'";
                    rs = dbcon.executeStatement(sqlStr);
                    rs.beforeFirst();
                    if(rs.next())
                    {
                        validLogin = (rs.getString("username").equals(username) && rs.getString("password").equals(password));
                        if(validLogin)
                        {
                            if(rs.getInt("type") == 0)
                                adminMode = true;
                            else
                                adminMode = false;
                            if(adminMode)
                                mainMenu = new MainMenu("Hunter Association Database", 2, 2, logoutButton, insertDataButton, updateDeleteButton, manageUserButton, queryButton);
                            else
                                mainMenu = new MainMenu("Hunter Association Database", 1, 3, logoutButton, insertDataButton, updateDeleteButton, queryButton);

                            frame.changeContent(mainMenu);
                        }
                    }
                    if(!validLogin)
                    {
                        showError("Incorrect Username/Password");
                        loginPage.reset();
                    }

                } catch(SQLException ex)
                {
                    showError("SQL Error - " + ex.getMessage());
                }
            }
        });
    }

    private boolean checkBoolConstraint(String containerText, int mode)
    {
        if(mode == 0)
        {
            if(containerText.toUpperCase().equals("T") || containerText.toUpperCase().equals("F"))
                return true;
            else
            {
                JOptionPane.showMessageDialog(frame, "IsBoss invalid, must be True (T) or False (F)", "Cannot add to database", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else if(mode == 1)
        {
            if(containerText.equals("1") || containerText.equals("2") || containerText.toUpperCase().equals("NOT_SHOWING"))
                return true;
            else
            {
                JOptionPane.showMessageDialog(frame, "Authorization Level invalid, must be 1 or 2", "Cannot add to database", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else
        {
            if(containerText.equals("0") || containerText.equals("1"))
                return true;
            else
            {
                JOptionPane.showMessageDialog(frame, "Admin Level invalid, must be 0 or 1", "Cannot add to database", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
    }

    private boolean checkMinMaxConstraint(String minStr, String maxStr)
    {
        int min = Integer.parseInt(minStr);
        int max = Integer.parseInt(maxStr);
        if(max > min)
            return true;
        else
        {
            JOptionPane.showMessageDialog(frame, "Max. Monster Count must be greater than Min. Monster Count", "Cannot add to database", JOptionPane.ERROR_MESSAGE);
            return false;
        }

    }

    private JPanel createQueryPanel(JPanel panel)
    {
        JPanel completePanel = new JPanel(new BorderLayout());
        completePanel.add(panel, BorderLayout.CENTER);
        completePanel.add(new CustomButton("Back", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                frame.changeContent(queryMenu);
            }
        }), BorderLayout.SOUTH);
        return completePanel;
    }

    private void showError(String message)
    {
        JLabel label = createLabel(message);
        JOptionPane.showMessageDialog(frame, label, "ERROR", JOptionPane.ERROR_MESSAGE);
    }
}
