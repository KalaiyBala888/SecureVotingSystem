import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class VotingSystem extends JFrame {

    private Connection conn;
    private int generatedOTP = 0;
    private int currentVoterId = 0;

    public VotingSystem() {
        connectDB();
        if (conn == null) {
            JOptionPane.showMessageDialog(null,
                "Cannot connect to database. Start MySQL and check credentials.",
                "DB Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        setTitle("Voting System");
        setSize(720, 520);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        showHomePage();
    }

    // -----------------------
    // Connect to DB
    // -----------------------
    private void connectDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/voting_system?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                "root", "" // change if your MySQL root has a password
            );
            System.out.println("Database Connected!");
        } catch (Exception e) {
            e.printStackTrace();
            conn = null;
            JOptionPane.showMessageDialog(null, "DB Error: " + e.getMessage());
        }
    }

    // -----------------------
    // Home page
    // -----------------------
    private void showHomePage() {
        getContentPane().removeAll();
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(245, 245, 250));
        setContentPane(root);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);
        g.gridx = 0;
        g.gridy = 0;

        JLabel title = new JLabel("Secure Voting System");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(new Color(22, 38, 84));
        root.add(title, g);

        g.gridy++;
        JButton voterBtn = new JButton("Voter");
        voterBtn.setPreferredSize(new Dimension(240, 44));
        styleBigButton(voterBtn, new Color(46, 204, 113)); // green
        root.add(voterBtn, g);

        g.gridy++;
        JButton adminBtn = new JButton("Admin");
        adminBtn.setPreferredSize(new Dimension(240, 44));
        styleBigButton(adminBtn, new Color(52, 152, 219)); // blue
        root.add(adminBtn, g);

        g.gridy++;
        JButton exitBtn = new JButton("Exit");
        exitBtn.setPreferredSize(new Dimension(240, 44));
        styleBigButton(exitBtn, new Color(231, 76, 60)); // red
        root.add(exitBtn, g);

        voterBtn.addActionListener(e -> showVoterRegistration());
        adminBtn.addActionListener(e -> showAdminLogin());
        exitBtn.addActionListener(e -> System.exit(0));

        revalidate();
        repaint();
    }

    private void styleBigButton(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.white);
        b.setFocusPainted(false);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 14f));
        b.setOpaque(true);
        b.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    }

    // -----------------------
    // Voter Registration (Name, Aadhar, Mobile)
    // -----------------------
    private void showVoterRegistration() {
        getContentPane().removeAll();
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 250, 240)); // warm tint
        setContentPane(panel);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.gridx = 0;
        g.gridy = 0;
        g.anchor = GridBagConstraints.WEST;

        JLabel lName = new JLabel("Name:");
        lName.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(lName, g);

        g.gridx = 1;
        JTextField nameField = new JTextField(20);
        panel.add(nameField, g);

        g.gridx = 0; g.gridy++;
        JLabel lAadhar = new JLabel("Aadhar (12 digits):");
        panel.add(lAadhar, g);
        g.gridx = 1;
        JTextField aadharField = new JTextField(20);
        panel.add(aadharField, g);

        g.gridx = 0; g.gridy++;
        JLabel lMobile = new JLabel("Mobile (10 digits):");
        panel.add(lMobile, g);
        g.gridx = 1;
        JTextField mobileField = new JTextField(20);
        panel.add(mobileField, g);

        g.gridx = 0; g.gridy++;
        JButton registerBtn = new JButton("Generate Voter ID & OTP");
        styleBigButton(registerBtn, new Color(155, 89, 182)); // purple
        panel.add(registerBtn, g);
        g.gridx = 1;
        JButton backBtn = new JButton("Back");
        styleBigButton(backBtn, new Color(149, 165, 166)); // gray
        panel.add(backBtn, g);

        registerBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String aadhar = aadharField.getText().trim();
            String mobile = mobileField.getText().trim();

            if(name.isEmpty() || aadhar.isEmpty() || mobile.isEmpty()) {
                JOptionPane.showMessageDialog(this,"All fields are required.");
                return;
            }
            if(aadhar.length() != 12 || !aadhar.matches("\\d+")) {
                JOptionPane.showMessageDialog(this,"Aadhar must be 12 digits.");
                return;
            }
            if(mobile.length() != 10 || !mobile.matches("\\d+")) {
                JOptionPane.showMessageDialog(this,"Mobile must be 10 digits.");
                return;
            }

            // Insert voter
            String sql = "INSERT INTO voters (name, aadhar, mobile) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setString(2, aadhar);
                ps.setString(3, mobile);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if(rs.next()) {
                        currentVoterId = rs.getInt(1);
                        generatedOTP = new Random().nextInt(900000) + 100000; // 6-digit OTP
                        JOptionPane.showMessageDialog(this,
                                "Your Voter ID: " + currentVoterId + "\nYour OTP: " + generatedOTP);
                        showVoterOTP();
                    }
                }
            } catch (SQLException ex) {
                if(ex.getErrorCode() == 1062) {
                    JOptionPane.showMessageDialog(this,"Voter already registered with this Aadhar.");
                } else {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,"Registration Error: "+ex.getMessage());
                }
            }
        });

        backBtn.addActionListener(e -> showHomePage());

        revalidate();
        repaint();
    }

    // -----------------------
    // OTP Verification
    // -----------------------
    private void showVoterOTP() {
        getContentPane().removeAll();
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 255, 255)); // cool tint
        setContentPane(panel);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.gridx=0; g.gridy=0;

        addToPanel(panel, new JLabel("Enter Voter ID:"), g);
        g.gridx=1;
        JTextField idField = new JTextField(10);
        idField.setText(String.valueOf(currentVoterId));
        panel.add(idField, g);

        g.gridx=0; g.gridy++;
        panel.add(new JLabel("Enter OTP:"), g);
        g.gridx=1;
        JTextField otpField = new JTextField(10);
        panel.add(otpField, g);

        g.gridx=0; g.gridy++;
        JButton verifyBtn = new JButton("Verify & Vote");
        styleBigButton(verifyBtn, new Color(241, 196, 15)); // yellow
        panel.add(verifyBtn, g);
        g.gridx=1;
        JButton backBtn = new JButton("Back");
        styleBigButton(backBtn, new Color(149, 165, 166));
        panel.add(backBtn, g);

        verifyBtn.addActionListener(e -> {
            try {
                int vid = Integer.parseInt(idField.getText().trim());
                int otp = Integer.parseInt(otpField.getText().trim());
                if(vid != currentVoterId || otp != generatedOTP) {
                    JOptionPane.showMessageDialog(this,"Invalid Voter ID or OTP.");
                    return;
                }
                showVotePage(currentVoterId);
            } catch(NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this,"Invalid numeric input for Voter ID or OTP.");
            }
        });

        backBtn.addActionListener(e -> showHomePage());

        revalidate();
        repaint();
    }

    private void addToPanel(JPanel p, Component c, GridBagConstraints g) {
        p.add(c, g);
    }

    // -----------------------
    // Voting Page
    // -----------------------
    private void showVotePage(int voterId) {
        getContentPane().removeAll();
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 245, 240));
        setContentPane(panel);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.gridx = 0; g.gridy = 0;

        panel.add(new JLabel("Select Candidate:"), g);
        g.gridx=1;
        JComboBox<String> candidateBox = new JComboBox<>();
        panel.add(candidateBox, g);

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT name FROM candidates ORDER BY candidate_id")) {
            while(rs.next()) candidateBox.addItem(rs.getString(1));
        } catch(SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,"Cannot load candidates: "+ex.getMessage());
        }

        g.gridx=0; g.gridy++;
        JButton voteBtn = new JButton("Submit Vote");
        styleBigButton(voteBtn, new Color(34, 167, 240));
        panel.add(voteBtn,g);
        g.gridx=1;
        JButton cancelBtn = new JButton("Cancel");
        styleBigButton(cancelBtn, new Color(189, 195, 199));
        panel.add(cancelBtn,g);

        voteBtn.addActionListener(e -> {
            String candidateName = (String) candidateBox.getSelectedItem();
            String getCidSql = "SELECT candidate_id FROM candidates WHERE name = ? LIMIT 1";
            String insertVoteSql = "INSERT INTO votes (voter_id, candidate_id) VALUES (?, ?)";
            String markVotedSql = "UPDATE voters SET has_voted=1 WHERE voter_id=?";

            try {
                conn.setAutoCommit(false);
                int cid;
                try (PreparedStatement ps = conn.prepareStatement(getCidSql)) {
                    ps.setString(1, candidateName);
                    try (ResultSet rs = ps.executeQuery()) {
                        if(!rs.next()) { conn.rollback(); conn.setAutoCommit(true); return; }
                        cid = rs.getInt(1);
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(insertVoteSql)) {
                    ps.setInt(1, voterId);
                    ps.setInt(2, cid);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(markVotedSql)) {
                    ps.setInt(1, voterId);
                    ps.executeUpdate();
                }

                conn.commit(); conn.setAutoCommit(true);
                JOptionPane.showMessageDialog(this,"Vote Submitted. Thank you!");
                showHomePage();

            } catch(SQLException ex) {
                ex.printStackTrace();
                try { conn.rollback(); conn.setAutoCommit(true); } catch(SQLException r){r.printStackTrace();}
                JOptionPane.showMessageDialog(this,"Error submitting vote: "+ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> showHomePage());
        revalidate();
        repaint();
    }

    // -----------------------
    // Admin Login
    // -----------------------
    private void showAdminLogin() {
        getContentPane().removeAll();
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(250, 245, 255));
        setContentPane(panel);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.gridx=0; g.gridy=0;

        addToPanel(panel, new JLabel("Admin Username:"), g);
        g.gridx=1;
        JTextField userField = new JTextField(18);
        panel.add(userField, g);

        g.gridx=0; g.gridy++;
        addToPanel(panel, new JLabel("Password:"), g);
        g.gridx=1;
        JPasswordField passField = new JPasswordField(18);
        panel.add(passField, g);

        g.gridx=0; g.gridy++;
        JButton loginBtn = new JButton("Login");
        styleBigButton(loginBtn, new Color(52, 152, 219));
        panel.add(loginBtn, g);
        g.gridx=1;
        JButton backBtn = new JButton("Back");
        styleBigButton(backBtn, new Color(149, 165, 166));
        panel.add(backBtn, g);

        loginBtn.addActionListener(e -> {
            String user = userField.getText().trim();
            String pwd = new String(passField.getPassword());
            if(user.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(this,"Enter username and password.");
                return;
            }

            String sql = "SELECT admin_id FROM admins WHERE username=? AND password_hash=SHA2(?,256)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1,user);
                ps.setString(2,pwd);
                try (ResultSet rs = ps.executeQuery()) {
                    if(rs.next()) showAdminDashboard();
                    else JOptionPane.showMessageDialog(this,"Invalid admin credentials.");
                }
            } catch(SQLException ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this,"Admin login error: "+ex.getMessage()); }
        });

        backBtn.addActionListener(e -> showHomePage());
        revalidate();
        repaint();
    }

    // -----------------------
    // Admin Dashboard (with chart + percentages)
    // -----------------------
    private void showAdminDashboard() {
        getContentPane().removeAll();
        JPanel root = new JPanel(new BorderLayout(8,8));
        root.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        root.setBackground(new Color(245, 248, 255));
        setContentPane(root);

        JPanel top = new JPanel();
        top.setBackground(new Color(245, 248, 255));
        JButton refresh = new JButton("Refresh");
        styleBigButton(refresh, new Color(46, 204, 113));
        JButton logout = new JButton("Logout");
        styleBigButton(logout, new Color(231, 76, 60));
        top.add(refresh); top.add(logout);
        root.add(top, BorderLayout.NORTH);

        // Chart + percentage panel
        ChartPanel chartPanel = new ChartPanel();
        chartPanel.setPreferredSize(new Dimension(460, 360));
        root.add(chartPanel, BorderLayout.CENTER);

        JTextArea pctArea = new JTextArea();
        pctArea.setEditable(false);
        pctArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        pctArea.setBorder(BorderFactory.createTitledBorder("Winning Percentages"));
        pctArea.setBackground(new Color(255,255,255));
        pctArea.setPreferredSize(new Dimension(220, 360));
        root.add(new JScrollPane(pctArea), BorderLayout.EAST);

        refresh.addActionListener(e -> {
            loadResults(chartPanel, pctArea);
        });
        logout.addActionListener(e -> showHomePage());

        loadResults(chartPanel, pctArea);

        revalidate();
        repaint();
    }

    private void loadResults(ChartPanel chartPanel, JTextArea pctArea) {
        String sql = "SELECT c.candidate_id, c.name, COUNT(v.vote_id) AS votes " +
                     "FROM candidates c LEFT JOIN votes v ON c.candidate_id = v.candidate_id " +
                     "GROUP BY c.candidate_id ORDER BY c.candidate_id";
        Map<String, Integer> counts = new LinkedHashMap<>();
        int totalVotes = 0;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString("name");
                int v = rs.getInt("votes");
                counts.put(name, v);
                totalVotes += v;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,"Error loading results: "+ex.getMessage());
            return;
        }

        chartPanel.setData(counts);
        chartPanel.repaint();

        // build percentage text
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Total Votes: %d\n\n", totalVotes));
        if(totalVotes == 0) {
            for (Map.Entry<String,Integer> e : counts.entrySet()) {
                sb.append(String.format("%-18s : %3d votes  (0.00%%)\n", e.getKey(), e.getValue()));
            }
        } else {
            // determine leader
            int max = -1;
            String leader = null;
            for (Map.Entry<String,Integer> e : counts.entrySet()) {
                if (e.getValue() > max) { max = e.getValue(); leader = e.getKey(); }
            }
            for (Map.Entry<String,Integer> e : counts.entrySet()) {
                double pct = totalVotes == 0 ? 0.0 : (e.getValue() * 100.0 / totalVotes);
                if (e.getKey().equals(leader)) {
                    sb.append(String.format("%-18s : %3d votes  (%.2f%%)  <-- Leading\n", e.getKey(), e.getValue(), pct));
                } else {
                    sb.append(String.format("%-18s : %3d votes  (%.2f%%)\n", e.getKey(), e.getValue(), pct));
                }
            }
        }
        pctArea.setText(sb.toString());
    }

    // -----------------------
    // Simple chart panel (draws bar chart from data)
    // -----------------------
    static class ChartPanel extends JPanel {
        private Map<String,Integer> data = new LinkedHashMap<>();

        public void setData(Map<String,Integer> data) {
            this.data = new LinkedHashMap<>(data);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                // background
                g.setColor(new Color(250, 250, 255));
                g.fillRect(0,0,w,h);

                if (data == null || data.isEmpty()) {
                    g.setColor(Color.DARK_GRAY);
                    g.setFont(new Font("SansSerif", Font.BOLD, 16));
                    String msg = "No candidates or votes to display";
                    FontMetrics fm = g.getFontMetrics();
                    g.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
                    return;
                }

                // compute max
                int max = 1;
                int total = 0;
                for (int v : data.values()) { if (v > max) max = v; total += v; }

                int margin = 40;
                int chartW = w - margin * 2;
                int chartH = h - margin * 2;

                int barCount = data.size();
                int gap = 12;
                int barWidth = Math.max(20, (chartW - gap * (barCount - 1)) / Math.max(1, barCount));

                // Draw axes
                g.setColor(Color.gray);
                g.drawLine(margin, margin + chartH, margin + chartW, margin + chartH); // x-axis
                g.drawLine(margin, margin, margin, margin + chartH); // y-axis

                // draw bars
                int i = 0;
                int x = margin;
                Color[] palette = new Color[] {
                    new Color(52,152,219), new Color(46,204,113), new Color(231,76,60),
                    new Color(155,89,182), new Color(241,196,15), new Color(26,188,156)
                };

                for (Map.Entry<String,Integer> e : data.entrySet()) {
                    int val = e.getValue();
                    double ratio = (double) val / (double) max;
                    int barH = (int) Math.round(ratio * (chartH - 40)); // leave top space
                    int bx = x + i * (barWidth + gap);
                    int by = margin + chartH - barH;

                    Color c = palette[i % palette.length];
                    g.setColor(c);
                    g.fillRoundRect(bx, by, barWidth, barH, 8, 8);
                    g.setColor(c.darker());
                    g.drawRoundRect(bx, by, barWidth, barH, 8, 8);

                    // value label on top
                    g.setColor(Color.darkGray);
                    String vstr = String.valueOf(val);
                    FontMetrics fm = g.getFontMetrics();
                    int txtW = fm.stringWidth(vstr);
                    g.drawString(vstr, bx + (barWidth - txtW) / 2, by - 6);

                    // candidate label rotated or wrapped
                    String name = e.getKey();
                    int labelY = margin + chartH + 16;
                    // center label under bar
                    int labelX = bx + (barWidth / 2);
                    drawCenteredString(g, name, labelX, labelY);

                    i++;
                }

                // draw simple y ticks (0, max/2, max)
                g.setColor(Color.DARK_GRAY);
                g.setFont(new Font("SansSerif", Font.PLAIN, 11));
                String t0 = "0";
                String t1 = String.valueOf(Math.max(1, max/2));
                String t2 = String.valueOf(max);
                g.drawString(t0, 6, margin + chartH);
                g.drawString(t1, 6, margin + chartH - (chartH-40)/2);
                g.drawString(t2, 6, margin + 12);

            } finally {
                g.dispose();
            }
        }

        private void drawCenteredString(Graphics2D g, String text, int centerX, int y) {
            FontMetrics fm = g.getFontMetrics();
            int tw = fm.stringWidth(text);
            g.drawString(text, centerX - tw/2, y);
        }
    }

    // -----------------------
    // Main
    // -----------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VotingSystem app = new VotingSystem();
            app.setVisible(true);
        });
    }
}
