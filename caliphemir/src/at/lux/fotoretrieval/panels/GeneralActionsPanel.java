/*
 * GeneralActionsPanel.java
 *
 * Created on 15. Februar 2006, 22:39
 */

package at.lux.fotoretrieval.panels;

/**
 *
 * @author  mlux
 */
public class GeneralActionsPanel extends javax.swing.JPanel {
    
    /** Creates new form GeneralActionsPanel */
    public GeneralActionsPanel() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        createIndexButton = new javax.swing.JButton();
        showHelpButton = new javax.swing.JButton();
        visitHomepageButton = new javax.swing.JButton();
        showAboutButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new java.awt.GridLayout(0, 2));

        createIndexButton.setBackground(new java.awt.Color(255, 255, 255));
        createIndexButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/actions/index.png")));
        createIndexButton.setText("Create Index");
        createIndexButton.setActionCommand("wizardIndex");
        createIndexButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        createIndexButton.setBorderPainted(false);
        createIndexButton.setFocusPainted(false);
        createIndexButton.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        createIndexButton.setPreferredSize(new java.awt.Dimension(130, 50));
        jPanel1.add(createIndexButton);

        showHelpButton.setBackground(new java.awt.Color(255, 255, 255));
        showHelpButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/actions/help.png")));
        showHelpButton.setText("Show Online Help");
        showHelpButton.setActionCommand("showHelpOnline");
        showHelpButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        showHelpButton.setBorderPainted(false);
        showHelpButton.setFocusPainted(false);
        showHelpButton.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        showHelpButton.setPreferredSize(new java.awt.Dimension(130, 50));
        jPanel1.add(showHelpButton);

        visitHomepageButton.setBackground(new java.awt.Color(255, 255, 255));
        visitHomepageButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/actions/internet.png")));
        visitHomepageButton.setText("Visit Homepage");
        visitHomepageButton.setActionCommand("visitHomepage");
        visitHomepageButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        visitHomepageButton.setBorderPainted(false);
        visitHomepageButton.setFocusPainted(false);
        visitHomepageButton.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        visitHomepageButton.setPreferredSize(new java.awt.Dimension(130, 50));
        jPanel1.add(visitHomepageButton);

        showAboutButton.setBackground(new java.awt.Color(255, 255, 255));
        showAboutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/actions/about.png")));
        showAboutButton.setText("About Emir");
        showAboutButton.setActionCommand("about");
        showAboutButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        showAboutButton.setBorderPainted(false);
        showAboutButton.setFocusPainted(false);
        showAboutButton.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        showAboutButton.setPreferredSize(new java.awt.Dimension(130, 50));
        jPanel1.add(showAboutButton);

        jPanel2.add(jPanel1);

        add(jPanel2, java.awt.BorderLayout.CENTER);

    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton createIndexButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    public javax.swing.JButton showAboutButton;
    public javax.swing.JButton showHelpButton;
    public javax.swing.JButton visitHomepageButton;
    // End of variables declaration//GEN-END:variables
    
}
