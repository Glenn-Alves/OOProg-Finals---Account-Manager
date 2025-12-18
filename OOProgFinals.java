package com.demo.ooprogfinals;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.List;
    
// ---------------- ENUMS ----------------
enum Country {
    PHILIPPINES,
    UNITED_STATES,
    CANADA,
    UNITED_KINGDOM,
    AUSTRALIA,
    JAPAN,
    SOUTH_KOREA,
    CHINA,
    INDIA,
    SINGAPORE,
    MALAYSIA,
    INDONESIA,
    VIETNAM,
    THAILAND,
    GERMANY,
    FRANCE,
    ITALY,
    SPAIN,
    OTHER
}


enum Months {
    JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE,
    JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER
}

// ---------------- BASE / CHILD CLASSES ----------------
class Person {
    protected String name;
    protected int age;
    protected String birthdate; // e.g., JANUARY 15 2005
    protected String email;

    public Person(String name, int age, String birthdate, String email) {
        this.name = name;
        this.age = age;
        this.birthdate = birthdate;
        this.email = email;
    }
}

class Account extends Person {
    private Country country;

    public Account(String name, int age, String birthdate, String email, Country country) {
        super(name, age, birthdate, email);
        this.country = country;
    }

    public Country getCountry() {
        return country;
    }

    public void setLocation(Country c) {
        this.country = c;
    }

    public void setName(String n) { this.name = n; }
    public void setAge(int a) { this.age = a; }
    public void setBirthdate(String b) { this.birthdate = b; }
    public void setEmail(String e) { this.email = e; }

    @Override
    public String toString() {
        return "Name: " + name +
                "\nAge: " + age +
                "\nBirthYear: " + birthdate +
                "\nEmail: " + email +
                "\nCountry: " + country;
    }
}

// ---------------- MAIN APP ----------------
public class OOProgFinals {

    private static final String FILE_PATH = "accounts.txt";
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private final List<Account> accounts = new ArrayList<>();

    // Form fields
    private JTextField nameField;
    private JTextField ageField;
    private JComboBox<Months> monthBox;
    private JComboBox<Integer> dayBox;
    private JComboBox<Integer> yearBox;
    private JTextField emailField;
    private JComboBox<Country> locBox;

    // State for editing
    private int editingIndex = -1;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OOProgFinals().createAndShowGUI());
    }

    private void createAndShowGUI() {
        loadAccountsFromFile();

        frame = new JFrame("Parreno's Account Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout(10, 10));

        // Left: Controls (Add / Form / Actions)
        JPanel left = new JPanel();
        left.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        left.add(createFormPanel());
        left.add(Box.createVerticalStrut(10)); // space
        left.add(createButtonsPanel());

        // Right: Table view
        JPanel right = new JPanel(new BorderLayout(8,8));
        right.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        right.add(new JLabel("Registered Accounts:"), BorderLayout.NORTH);
        right.add(createTablePanel(), BorderLayout.CENTER);

        // Top: Search bar
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.add(new JLabel("Search (name / email / location):"));
        JTextField searchField = new JTextField(30);
        JButton searchBtn = new JButton("Search");
        JButton refreshBtn = new JButton("Refresh");
        top.add(searchField);
        top.add(searchBtn);
        top.add(refreshBtn);

        searchBtn.addActionListener(e -> doSearch(searchField.getText().trim()));
        refreshBtn.addActionListener(e -> refreshTable());

        frame.add(top, BorderLayout.NORTH);
        frame.add(left, BorderLayout.WEST);
        frame.add(right, BorderLayout.CENTER);

        refreshTable();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createFormPanel() {
        JPanel p = new JPanel(new GridLayout(10, 2, 6, 6));

        nameField = new JTextField();
        ageField = new JTextField();
        ageField.setEditable(false);

        monthBox = new JComboBox<>(Months.values());
        dayBox = new JComboBox<>();
        for (int i = 1; i <= 31; i++) dayBox.addItem(i);

        yearBox = new JComboBox<>();
        for (int y = 1960; y <= LocalDate.now().getYear(); y++) yearBox.addItem(y);

        emailField = new JTextField();
        locBox = new JComboBox<>(Country.values());

        // Add listeners to calculate age automatically
        ActionListener ageCalc = e -> calculateAndSetAge();
        monthBox.addActionListener(ageCalc);
        dayBox.addActionListener(ageCalc);
        yearBox.addActionListener(ageCalc);

        p.add(new JLabel("Full Name:")); p.add(nameField);
        p.add(new JLabel("Age (auto):")); p.add(ageField);
        p.add(new JLabel("Birth Month:")); p.add(monthBox);
        p.add(new JLabel("Birth Day:")); p.add(dayBox);
        p.add(new JLabel("Birth Year:")); p.add(yearBox);
        p.add(new JLabel("Email:")); p.add(emailField);
        p.add(new JLabel("Country:")); p.add(locBox);

        return p;
    }

    private JPanel createButtonsPanel() {
        JPanel p = new JPanel(new GridLayout(6,1,6,6));

        JButton addBtn = new JButton("Add Account");
        JButton editBtn = new JButton("Edit Selected");
        JButton saveEditBtn = new JButton("Save Edit");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton clearBtn = new JButton("Clear Form");
        JButton exportBtn = new JButton("Export to TXT");

        p.add(addBtn);
        p.add(editBtn);
        p.add(saveEditBtn);
        p.add(deleteBtn);
        p.add(clearBtn);
        p.add(exportBtn);

        addBtn.addActionListener(e -> addAccount());
        editBtn.addActionListener(e -> loadSelectedToForm());
        saveEditBtn.addActionListener(e -> saveEdit());
        deleteBtn.addActionListener(e -> deleteSelected());
        clearBtn.addActionListener(e -> clearForm());
        exportBtn.addActionListener(e -> {
            try {
                writeAllToFile();
                JOptionPane.showMessageDialog(frame, "All accounts saved to " + FILE_PATH);
            } catch (HeadlessException | IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error saving file: " + ex.getMessage());
            }
        });

        return p;
    }

    private JScrollPane createTablePanel() {
        String[] cols = {"No.", "Name", "Age", "Birthdate", "Email", "Country"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(false); // NO sorting

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(520, 400));
        return sp;
    }

    // ---------------- Core functions ----------------

    private void calculateAndSetAge() {
        try {
            int day = (Integer) dayBox.getSelectedItem();
            int year = (Integer) yearBox.getSelectedItem();
            Months m = (Months) monthBox.getSelectedItem();
            int monthNumber = m.ordinal() + 1;
            LocalDate birth = LocalDate.of(year, monthNumber, day);
            int age = Period.between(birth, LocalDate.now()).getYears();
            ageField.setText(String.valueOf(age));
        } catch (Exception ignored) { }
    }

    private void addAccount() {
        try {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { JOptionPane.showMessageDialog(frame, "Name is required."); return; }

// Prevent numbers or symbols
            if (!name.matches("[A-Za-z ]+")) {
                JOptionPane.showMessageDialog(frame, "Name must not contain numbers or special characters.");
                return;
            }
       
            int age = Integer.parseInt(ageField.getText().trim());
            Months m = (Months) monthBox.getSelectedItem();
            int day = (Integer) dayBox.getSelectedItem();
            int year = (Integer) yearBox.getSelectedItem();
            String birthdate = m + " " + day + " " + year;
            String email = emailField.getText().trim();
            Country loc = (Country) locBox.getSelectedItem();

            Account acc = new Account(name, age, birthdate, email, loc);
            accounts.add(acc);
            writeAllToFile(); // overwrite file to keep consistent
            refreshTable();
            clearForm();
            JOptionPane.showMessageDialog(frame, "Account added.");
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(frame, "Invalid age. Please check birthdate selections.");
        } catch (HeadlessException | IOException ex) {
            JOptionPane.showMessageDialog(frame, "Error adding account: " + ex.getMessage());
        }
    }

    private void loadSelectedToForm() {
        int sel = table.getSelectedRow();
        if (sel == -1) { JOptionPane.showMessageDialog(frame, "Select a row first."); return; }
        int modelIndex = table.convertRowIndexToModel(sel);
        editingIndex = modelIndex;
        Account a = accounts.get(modelIndex);

        nameField.setText(a.name);
        ageField.setText(String.valueOf(a.age));
        // parse birthdate in form "MONTH day year"
        try {
            String[] parts = a.birthdate.split(" ");
            Months m = Months.valueOf(parts[0]);
            int day = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);
            monthBox.setSelectedItem(m);
            dayBox.setSelectedItem(day);
            yearBox.setSelectedItem(year);
        } catch (NumberFormatException ignored) {}
        emailField.setText(a.email);
        locBox.setSelectedItem(a.getCountry());
    }

    private void saveEdit() {
        if (editingIndex == -1) { JOptionPane.showMessageDialog(frame, "No account loaded for editing."); return; }
        
        try {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { JOptionPane.showMessageDialog(frame, "Name is required."); return; }
            if (!name.matches("[A-Za-z ]+")) {
            JOptionPane.showMessageDialog(frame, "Name must not contain numbers or special characters.");
            return;
           }
            
            int age = Integer.parseInt(ageField.getText().trim());
            Months m = (Months) monthBox.getSelectedItem();
            int day = (Integer) dayBox.getSelectedItem();
            int year = (Integer) yearBox.getSelectedItem();
            String birthdate = m + " " + day + " " + year;
            String email = emailField.getText().trim();
            Country loc = (Country) locBox.getSelectedItem();

            Account a = accounts.get(editingIndex);
            a.setName(name);
            a.setAge(age);
            a.setBirthdate(birthdate);
            a.setEmail(email);
            a.setLocation(loc);

            writeAllToFile();
            refreshTable();
            clearForm();
            editingIndex = -1;
            JOptionPane.showMessageDialog(frame, "Account updated.");
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(frame, "Invalid age. Please check birthdate selections.");
        } catch (HeadlessException | IOException ex) {
            JOptionPane.showMessageDialog(frame, "Error saving edit: " + ex.getMessage());
        }
    }

    private void deleteSelected() {
        int sel = table.getSelectedRow();
        if (sel == -1) { JOptionPane.showMessageDialog(frame, "Select a row first."); return; }
        int modelIndex = table.convertRowIndexToModel(sel);
        int confirm = JOptionPane.showConfirmDialog(frame, "Delete selected account?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        accounts.remove(modelIndex);
        try {
            writeAllToFile();
            refreshTable();
            JOptionPane.showMessageDialog(frame, "Account deleted.");
        } catch (HeadlessException | IOException ex) {
            JOptionPane.showMessageDialog(frame, "Error deleting: " + ex.getMessage());
        }
    }

    private void clearForm() {
        nameField.setText("");
        ageField.setText("");
        emailField.setText("");
        monthBox.setSelectedIndex(0);
        dayBox.setSelectedIndex(0);
        yearBox.setSelectedItem(LocalDate.now().getYear());
        locBox.setSelectedIndex(0);
        editingIndex = -1;
    }

    private void doSearch(String q) {
        if (q.isEmpty()) { refreshTable(); return; }
        String ql = q.toLowerCase();
        List<Account> filtered = new ArrayList<>();
        for (Account a : accounts) {
            if (a.name.toLowerCase().contains(ql) ||
                a.email.toLowerCase().contains(ql) ||
                a.getCountry().name().toLowerCase().contains(ql)) {
                filtered.add(a);
            }
        }
        populateTable(filtered);
    }

    private void refreshTable() {
        populateTable(accounts);
    }

    private void populateTable(List<Account> list) {
        tableModel.setRowCount(0);
        int i = 1;
        for (Account a : list) {
            tableModel.addRow(new Object[]{
                    i++,
                    a.name,
                    a.age,
                    a.birthdate,
                    a.email,
                    a.getCountry().name()
            });
        }
    }

    // ---------------- File I/O ----------------

    private void loadAccountsFromFile() {
        accounts.clear();
        File f = new File(FILE_PATH);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            String name = null, birth = null, email = null, loc = null;
            int age = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("Name:")) {
                    name = line.substring(5).trim();
                } else if (line.startsWith("Age:")) {
                    String s = line.substring(4).trim();
                    try { age = Integer.parseInt(s); } catch (NumberFormatException ignored) { age = 0; }
                } else if (line.startsWith("BirthYear:")) {
                    birth = line.substring(10).trim();
                } else if (line.startsWith("Email:")) {
                    email = line.substring(6).trim();
                } else if (line.startsWith("Country:")) {
                    loc = line.substring(9).trim();
                } else if (line.startsWith("----------------")) {
                    // end of entry
                    if (name != null) {
                        Country country = Country.OTHER;
                        try { country = Country.valueOf(loc); } catch (Exception ignored) {}
                        accounts.add(new Account(name, age, birth, email, country));
                    }
                    name = null; birth = null; email = null; loc = null; age = 0;
                }
            }
            // handle file without trailing separator
            if (name != null) {
                Country country = Country.OTHER;
                try { country = Country.valueOf(loc); } catch (Exception ignored) {}
                accounts.add(new Account(name, age, birth, email, country));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading file: " + e.getMessage());
        }
    }

    private void writeAllToFile() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            int count = 1;
            for (Account a : accounts) {
                bw.write("Account " + count + ":\n");
                bw.write(a.toString() + "\n");
                bw.write("---------------------------\n");
                count++;
            }
        }
    }
}
