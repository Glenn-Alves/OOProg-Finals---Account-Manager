import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.List;

import models.Account;
import enums.Country;
import enums.Months;

public class Main {

    private static final String FILE_PATH = "accounts.txt";

    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private final List<Account> accounts = new ArrayList<>();

    private JTextField nameField;
    private JTextField ageField;
    private JComboBox<Months> monthBox;
    private JComboBox<Integer> dayBox;
    private JComboBox<Integer> yearBox;
    private JTextField emailField;
    private JComboBox<Country> locBox;

    private int editingIndex = -1;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().createAndShowGUI());
    }

    private boolean isValidEmail(String email) {
    // Basic email validation regex pattern
    String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
    return email.matches(emailRegex);
}

   private void createAndShowGUI() {
    loadAccountsFromFile();
    
    frame = new JFrame("Parreno's Account Manager");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setResizable(false);
    frame.setSize(1280, 720);
    frame.setLayout(new BorderLayout(10, 10));
    
    ImageIcon image = new ImageIcon("LOGO.png");
    frame.setIconImage(image.getImage());

    frame.getContentPane().setBackground(new Color(0xc6c6c6));

    // Left: Controls
    JPanel left = new JPanel();
    left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
    left.setBackground(new Color(0xc6c6c6));
    left.setOpaque(true);

    left.add(createFormPanel());
    left.add(Box.createVerticalStrut(10));
    left.add(createButtonsPanel());

    // Right: Table view
    JPanel right = new JPanel(new BorderLayout(8,8));
    JLabel title = new JLabel("Registered Accounts:");
    title.setFont(new Font("Roboto", Font.BOLD, 16));
    title.setForeground(Color.BLACK);
    right.add(title, BorderLayout.NORTH);
    right.setBackground(new Color(0xc6c6c6));
    right.setOpaque(true);
    right.add(createTablePanel(), BorderLayout.CENTER);

    // Top: Search bar
    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    top.setBackground(new Color(0xc6c6c6));
    
    JLabel searchLabel = new JLabel("Search:"); 
    searchLabel.setForeground(Color.BLACK);
    top.add(searchLabel);
    
    JTextField searchField = new JTextField("Name or Email", 30);
    searchField.setBackground(Color.WHITE);
    searchField.setForeground(Color.BLACK);
    searchField.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    searchField.addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            if (searchField.getText().equals("Name or Email")) {
                searchField.setText("");
                searchField.setForeground(Color.black);
            }
        }
    });

    JButton searchBtn = new JButton("Search");
    JButton refreshBtn = new JButton("Refresh");

    // APPLY HOVER EFFECT TO TOP BUTTONS
    JButton[] topButtons = {searchBtn, refreshBtn};
    for (JButton btn : topButtons) {
        Color idle = Color.WHITE;
        Color hover = new Color(0xdfdfdf);
        Color pressed = new Color(0xaaaaaa);

        btn.setBackground(idle);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(MouseEvent e) { btn.setBackground(idle); }
            public void mousePressed(MouseEvent e) { btn.setBackground(pressed); }
            public void mouseReleased(MouseEvent e) { 
                btn.setBackground(btn.getBounds().contains(e.getPoint()) ? hover : idle); 
            }
        });
    }

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
    p.setBackground(new Color(0xdfdfdf));

    p.setBorder(
    BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(0xb9b9b9), 1),
        BorderFactory.createEmptyBorder(10, 10, 10, 12)
    )
);


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

    // Auto age calculation
    ActionListener ageCalc = e -> calculateAndSetAge();
    monthBox.addActionListener(ageCalc);
    dayBox.addActionListener(ageCalc);
    yearBox.addActionListener(ageCalc);

    // Add components
    p.add(new JLabel("Full Name:")); p.add(nameField);
    p.add(new JLabel("Birth Month:")); p.add(monthBox);
    p.add(new JLabel("Birth Day:")); p.add(dayBox);
    p.add(new JLabel("Birth Year:")); p.add(yearBox);
    p.add(new JLabel("Email:")); p.add(emailField);
    p.add(new JLabel("Country:")); p.add(locBox);

    // ===== #4 GOES HERE =====
    Color bg = Color.white;
    Color fg = Color.BLACK;

    nameField.setBackground(bg);
    nameField.setForeground(fg);

    emailField.setBackground(bg);
    emailField.setForeground(fg);

    monthBox.setBackground(bg);
    monthBox.setForeground(fg);

    dayBox.setBackground(bg);
    dayBox.setForeground(fg);

    yearBox.setBackground(bg);
    yearBox.setForeground(fg);

    locBox.setBackground(bg);
    locBox.setForeground(fg);

    // Label colors
    for (Component c : p.getComponents()) {
        if (c instanceof JLabel) {
            c.setForeground(Color.BLACK);
        }
    }



    return p;
}


    private JPanel createButtonsPanel() {
        JPanel p = new JPanel(new GridLayout(6,6,6,3));
        
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
        p.setBackground(Color.lightGray);

        for (Component c : p.getComponents()) {
            if (c instanceof JButton) {
        JButton btn = (JButton) c;
        
        // Base Colors
        Color idleColor = new Color(0xdfdfdf); // Your light gray
        Color hoverColor = new Color(0xaaaaaa); // Darker gray for hover
        
        // Initial Style
        btn.setBackground(idleColor);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false); // Keeps it flat
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);

        // Hover Effect Logic
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(idleColor);
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                btn.setBackground(new Color(0x888888));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (btn.getBounds().contains(e.getPoint())) {
                    btn.setBackground(hoverColor);
                } else {
                    btn.setBackground(idleColor);
                }
            }
        });
    }
        }

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

    // 1️⃣ Table model
    tableModel = new DefaultTableModel(cols, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    // 2️⃣ Create table
    table = new JTable(tableModel);

    // 3️⃣ Style table
    table.setBackground(new Color(0xdfdfdf));
    table.setForeground(Color.black);
    table.setGridColor(new Color(0xdfdfdf));
    table.setSelectionBackground(Color.lightGray);
    table.setSelectionForeground(Color.black);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setAutoCreateRowSorter(false);

    // 4️⃣ Header styling
    JTableHeader header = table.getTableHeader();
    header.setBackground(new Color(0xfdfdfd));
    header.setForeground(Color.black);

    // 5️⃣ ScrollPane
    JScrollPane sp = new JScrollPane(table);
    sp.setPreferredSize(new Dimension(520, 400));
    sp.getViewport().setBackground(new Color(0xdfdfdf));
    sp.setBackground(Color.black);


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
        if (name.isEmpty()) { 
            JOptionPane.showMessageDialog(frame, "Name is required."); 
            return; 
        }

        // Prevent numbers or symbols in name
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
        
        // === EMAIL VALIDATION START ===
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Email is required.");
            return;
        }
        
        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid email address.\n" +
                "Example: user@example.com");
            return;
        }
        // === EMAIL VALIDATION END ===

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

                nameField.setText(a.getName());
        ageField.setText(String.valueOf(a.getAge()));
        emailField.setText(a.getEmail());

String[] parts = a.getBirthdate().split(" ");

        // parse birthdate in form "MONTH day year"
        try {
            Months m = Months.valueOf(parts[0]);
            int day = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);
            monthBox.setSelectedItem(m);
            dayBox.setSelectedItem(day);
            yearBox.setSelectedItem(year);
        } catch (NumberFormatException ignored) {}
        locBox.setSelectedItem(a.getCountry());
    }

   private void saveEdit() {
    if (editingIndex == -1) { 
        JOptionPane.showMessageDialog(frame, "No account loaded for editing."); 
        return; 
    }
    
    try {
        String name = nameField.getText().trim();
        if (name.isEmpty()) { 
            JOptionPane.showMessageDialog(frame, "Name is required."); 
            return; 
        }
        
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
        
        // === EMAIL VALIDATION START ===
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Email is required.");
            return;
        }
        
        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid email address.\n" +
                "Example: user@example.com");
            return;
        }
        // === EMAIL VALIDATION END ===

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
    if (a.getName().toLowerCase().contains(ql) ||
        a.getEmail().toLowerCase().contains(ql) ||
        a.getCountry().name().toLowerCase().contains(ql)) {
        filtered.add(a);
    }
}
populateTable(filtered);

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
    a.getName(),
    a.getAge(),
    a.getBirthdate(),
    a.getEmail(),
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
