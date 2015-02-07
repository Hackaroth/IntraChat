/*
* IntraChat 
* 
* Form Main
* 
* This file is part of the IntraChat project
* This is the main form of the application
* 
* Copyright (C) 2012  Stefano BARILETTI <hackaroth@gmail.com>

* This program is free software: you can redistribute it and/or modify it under the 
* terms of the GNU General Public License as published by the Free Software 
* Foundation, either version 3 of the License, or (at your option) any later version.

* This program is distributed in the hope that it will be useful, but WITHOUT ANY 
* WARRANTY; without even the implied warranty of MERCHANTABILITY or 
* FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
* for more details.

* You should have received a copy of the GNU General Public License along with
* this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package intrachat;

import com.google.gson.Gson;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.Thread.State;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import sun.security.x509.DeltaCRLIndicatorExtension;

public class FormMain extends javax.swing.JFrame {

    private UserList Users;
    private RunnableAutoscan rAutoScan;
    private FormAutoScan formAutoScan;
    private DefaultListModel model;
    public FormMain() throws HeadlessException {
    }
    
    public FormMain(UserList users) {
        initComponents();
        setWindowsPosition();
        
        Users = users;
        lvUserList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        model = new DefaultListModel();
        
        for (User u : Users.getUserList()) {
            u.setOnUserChangedHandler(OnUserChanged);
            
            model.addElement(u);
        }
        formAutoScan = new FormAutoScan(this, true);
        lvUserList.setModel(model);
        lvUserList.setCellRenderer(new UserListCellRender());

        rAutoScan = new RunnableAutoscan(formAutoScan);
        rAutoScan.setOnAutoScanCompleteHandler(OnAutoScanComplete);
        rAutoScan.setOnAutoScanErrorHandler(OnAutoScanError);
        
        lblMyName.setText(UserList.getMySelf().getName());
        cmbState.setSelectedIndex(UserList.getMySelf().getState().ordinal());
        
        if (!users.getMySelf().getIcon().isEmpty()) {
            String[] _userIcon = users.getMySelf().getIcon().split(";");

            if (_userIcon != null && _userIcon.length == 2) {
                String path = "resources/images/users/" + _userIcon[0] + "/" + _userIcon[1];
                java.net.URL imgURL = getClass().getResource(path);

                if (imgURL != null) {
                    ImageIcon icon = new ImageIcon(imgURL);
                    lblUserIcon.setIcon(icon);
                }            
            }
        }
        
        lvUserList.setComponentPopupMenu(puMenu);
                    
        String icon = "resources/images/IntraChat.png";
        java.net.URL imgURL = getClass().getResource(icon);
        if (imgURL != null) {
            Image iconImage = Toolkit.getDefaultToolkit().getImage(imgURL);
            this.setIconImage(iconImage);
        }
    }
    
    OnAutoScanErrorHandler OnAutoScanError = new OnAutoScanErrorHandler() {

        @Override
        public void OnAutoScanError(String errorMessage) {
            formAutoScan.dispose();
            JOptionPane.showMessageDialog(null, errorMessage, "IntraChat", JOptionPane.OK_OPTION);
        }
    };
            
    OnAutoScanCompleteHandler OnAutoScanComplete = new OnAutoScanCompleteHandler() {

        @Override
        public void OnAutoScanComplete(ArrayList<User> users) {
            formAutoScan.dispose();
            for (User u : users) {
                
                if (Users.AddUser(u)) {
                    u.setOnUserChangedHandler(OnUserChanged);
                    model.addElement(u);
                }
            }
        }
    };    
    
    OnUserChangedHandler OnUserChanged = new OnUserChangedHandler() {

        @Override
        public void OnUserChanged(User user) {
            lvUserList.repaint();
        }
    };
    
    OnUserChangedHandler OnUserAdded = new OnUserChangedHandler() {

        @Override
        public void OnUserChanged(User user) {
            user.setOnUserChangedHandler(OnUserChanged);
            model.addElement(user);
        }
    };

    private void AddUser() {
        User u = new User(-2,"New User", "", ""); 

        FormUser _fu = new FormUser(this, true, u, Users);
        
        _fu.setOnUserChangedHandler(OnUserAdded);
        _fu.setVisible(true);
    }
    
    private void EditUser() {
        int[] _indexs = lvUserList.getSelectedIndices();
        
        if (_indexs.length == 1) {
            User u = Users.getUserAt(_indexs[0]);  
            
            FormUser _fu = new FormUser(this, true, u, Users);
            _fu.setVisible(true);
        }
    }
    
    private void DeleteUser() {
            if (lvUserList.getSelectedIndices().length > 0 && JOptionPane.showConfirmDialog(this, "Delete all selected users?","IntraChat",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            try {
                Object[] _selectedUser = lvUserList.getSelectedValues();

                for (Object obj : _selectedUser) {
                        User u = (User)obj;
                        
                        Users.DeleteUser(u);
                        model.removeElement(u);   
                }
            }
            catch(Exception ex) 
            {
                
            }
        }
    }
    private void SendMessage() {
        int[] _indexs = lvUserList.getSelectedIndices();
        final ArrayList<User> _rcpt = new ArrayList<User>();

        for (int _idx : _indexs) {
            if (_idx >= 0) {
                User u = Users.getUserAt(_idx);

                if (u.getState() != UserState.Offline && !u.getIpAddress().isEmpty())
                    _rcpt.add(u);

            }
        }

        if (_rcpt.isEmpty())
            return;

        FormSendMessage _sm = new FormSendMessage(null, true);
        _sm.setOnMessageReceivedHandler(new OnMessageReceivedHandler() {

            @Override
            public void onMessageReceived(String message) {
                try {
                    User myself = UserList.getMySelf();
                    if (myself == null) return;

                    Message msg = new Message(UserList.getMySelf() , _rcpt, message);

                    MessageSender.Send(msg);

                } catch (UnknownHostException ex) {
                        Logger.getLogger(FormShowMessage.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                        Logger.getLogger(FormShowMessage.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        _sm.setVisible(true);         
    }

    private void setWindowsPosition() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        // Determine the new location of the window
        int w = this.getSize().width;
        int h = this.getSize().height;
        int x = (dim.width-w)/2;
        int y = (dim.height-h)/2;

        // Move the window
        this.setLocation(x, y);        
    } 
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        puMenu = new javax.swing.JPopupMenu();
        mipEditUser = new javax.swing.JMenuItem();
        mipDeleteUser = new javax.swing.JMenuItem();
        mipSendMessage = new javax.swing.JMenuItem();
        spScrollPanel = new javax.swing.JScrollPane();
        lvUserList = new javax.swing.JList();
        pnlInfo = new javax.swing.JPanel();
        lblMyName = new javax.swing.JLabel();
        cmbState = new javax.swing.JComboBox();
        btnEditUser = new javax.swing.JButton();
        btnSendMessage = new javax.swing.JButton();
        btnAddUser = new javax.swing.JButton();
        lblUserIcon = new javax.swing.JLabel();
        mbMenu = new javax.swing.JMenuBar();
        mFile = new javax.swing.JMenu();
        miExit = new javax.swing.JMenuItem();
        mEdit = new javax.swing.JMenu();
        miHistory = new javax.swing.JMenuItem();
        miSettings = new javax.swing.JMenuItem();
        mSettings = new javax.swing.JMenu();
        miAddUser = new javax.swing.JMenuItem();
        miEditUser = new javax.swing.JMenuItem();
        miDeleteUser = new javax.swing.JMenuItem();
        miSendMessage = new javax.swing.JMenuItem();
        miAutoscan = new javax.swing.JMenuItem();
        mAbout = new javax.swing.JMenu();
        miAbout = new javax.swing.JMenuItem();

        mipEditUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/intrachat/resources/images/edit_user_16.png"))); // NOI18N
        mipEditUser.setText("Edit user");
        mipEditUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mipEditUserActionPerformed(evt);
            }
        });
        puMenu.add(mipEditUser);

        mipDeleteUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/intrachat/resources/images/remove_user_16.png"))); // NOI18N
        mipDeleteUser.setText("Delete user");
        mipDeleteUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mipDeleteUserActionPerformed(evt);
            }
        });
        puMenu.add(mipDeleteUser);

        mipSendMessage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/intrachat/resources/images/send_16.png"))); // NOI18N
        mipSendMessage.setText("Send message");
        mipSendMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mipSendMessageActionPerformed(evt);
            }
        });
        puMenu.add(mipSendMessage);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("IntraChat");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
            public void windowIconified(java.awt.event.WindowEvent evt) {
                formWindowIconified(evt);
            }
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        lvUserList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lvUserListMouseClicked(evt);
            }
        });
        spScrollPanel.setViewportView(lvUserList);

        lblMyName.setFont(new java.awt.Font("Ubuntu", 0, 26)); // NOI18N
        lblMyName.setText("lblMyName");

        cmbState.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Online", "Busy", "Away", "Offline" }));
        cmbState.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbStateItemStateChanged(evt);
            }
        });

        btnEditUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/intrachat/resources/images/edit_user.png"))); // NOI18N

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, btnEditUser, org.jdesktop.beansbinding.ELProperty.create("Edit selected user"), btnEditUser, org.jdesktop.beansbinding.BeanProperty.create("toolTipText"));
        bindingGroup.addBinding(binding);

        btnEditUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditUserActionPerformed(evt);
            }
        });

        btnSendMessage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/intrachat/resources/images/send.png"))); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, btnSendMessage, org.jdesktop.beansbinding.ELProperty.create("Send message to all selected users"), btnSendMessage, org.jdesktop.beansbinding.BeanProperty.create("toolTipText"));
        bindingGroup.addBinding(binding);

        btnSendMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendMessageActionPerformed(evt);
            }
        });

        btnAddUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/intrachat/resources/images/add_user.png"))); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, btnAddUser, org.jdesktop.beansbinding.ELProperty.create("Add new user"), btnAddUser, org.jdesktop.beansbinding.BeanProperty.create("toolTipText"));
        bindingGroup.addBinding(binding);

        btnAddUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddUserActionPerformed(evt);
            }
        });

        lblUserIcon.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.darkGray, java.awt.Color.blue));
        lblUserIcon.setPreferredSize(new java.awt.Dimension(64, 64));

        javax.swing.GroupLayout pnlInfoLayout = new javax.swing.GroupLayout(pnlInfo);
        pnlInfo.setLayout(pnlInfoLayout);
        pnlInfoLayout.setHorizontalGroup(
            pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblUserIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlInfoLayout.createSequentialGroup()
                        .addComponent(cmbState, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAddUser, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEditUser, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSendMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblMyName, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlInfoLayout.setVerticalGroup(
            pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlInfoLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlInfoLayout.createSequentialGroup()
                        .addComponent(lblUserIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(pnlInfoLayout.createSequentialGroup()
                        .addComponent(lblMyName)
                        .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlInfoLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cmbState, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(25, 25, 25))
                            .addGroup(pnlInfoLayout.createSequentialGroup()
                                .addGap(7, 7, 7)
                                .addGroup(pnlInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnEditUser, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnSendMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnAddUser, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18))))))
        );

        mFile.setText("File");

        miExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/intrachat/resources/images/exit.png"))); // NOI18N
        miExit.setText("Exit");
        miExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miExitActionPerformed(evt);
            }
        });
        mFile.add(miExit);

        mbMenu.add(mFile);

        mEdit.setText("Edit");

        miHistory.setIcon(new javax.swing.ImageIcon(getClass().getResource("/intrachat/resources/images/Log.png"))); // NOI18N
        miHistory.setText("Message History");
        miHistory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miHistoryActionPerformed(evt);
            }
        });
        mEdit.add(miHistory);

        miSettings.setIcon(new javax.swing.ImageIcon(getClass().getResource("/intrachat/resources/images/Settings.png"))); // NOI18N
        miSettings.setText("Settings");
        miSettings.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                miSettingsMouseClicked(evt);
            }
        });
        miSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miSettingsActionPerformed(evt);
            }
        });
        mEdit.add(miSettings);

        mbMenu.add(mEdit);

        mSettings.setText("Users");

        miAddUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/intrachat/resources/images/add_user_16.png"))); // NOI18N
        miAddUser.setText("Add user");
        miAddUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miAddUserActionPerformed(evt);
            }
        });
        mSettings.add(miAddUser);

        miEditUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/intrachat/resources/images/edit_user_16.png"))); // NOI18N
        miEditUser.setText("Edit user");
        miEditUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miEditUserActionPerformed(evt);
            }
        });
        mSettings.add(miEditUser);

        miDeleteUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/intrachat/resources/images/remove_user_16.png"))); // NOI18N
        miDeleteUser.setText("Delete user");
        miDeleteUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miDeleteUserActionPerformed(evt);
            }
        });
        mSettings.add(miDeleteUser);

        miSendMessage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/intrachat/resources/images/send_16.png"))); // NOI18N
        miSendMessage.setText("Send message");
        miSendMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miSendMessageActionPerformed(evt);
            }
        });
        mSettings.add(miSendMessage);

        miAutoscan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/intrachat/resources/images/Radar.png"))); // NOI18N
        miAutoscan.setText("Autoscan");
        miAutoscan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miAutoscanActionPerformed(evt);
            }
        });
        mSettings.add(miAutoscan);

        mbMenu.add(mSettings);

        mAbout.setText("?");

        miAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/intrachat/resources/images/info.png"))); // NOI18N
        miAbout.setText("About");
        miAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miAboutActionPerformed(evt);
            }
        });
        mAbout.add(miAbout);

        mbMenu.add(mAbout);

        setJMenuBar(mbMenu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spScrollPanel)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(pnlInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(spScrollPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lvUserListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lvUserListMouseClicked
        if (evt.getClickCount() == 2) {
            SendMessage();          
        }
            
    }//GEN-LAST:event_lvUserListMouseClicked

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
    }//GEN-LAST:event_formWindowActivated

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened

    }//GEN-LAST:event_formWindowOpened

    private void cmbStateItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbStateItemStateChanged
        String stateName = (String) evt.getItem();
        
        UserList.getMySelf().setState(Settings.getUserStateByName(stateName.toUpperCase()));
    }//GEN-LAST:event_cmbStateItemStateChanged

    private void miSettingsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_miSettingsMouseClicked

    }//GEN-LAST:event_miSettingsMouseClicked

    private void miSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miSettingsActionPerformed
        FormSettings _settings = new FormSettings(this, true);
        
        _settings.setOnSettingsChangedHandler(new OnSettingsChangedHandler() {

            @Override
            public void OnSettingsChanged() {
                UserList.getMySelf().setName(Settings.getMyName());
                UserList.getMySelf().setIcon(Settings.getUserIcon());
                lblMyName.setText(UserList.getMySelf().getName());
                
                if (!UserList.getMySelf().getIcon().isEmpty()) {
                    String[] _userIcon = UserList.getMySelf().getIcon().split(";");

                    if (_userIcon != null && _userIcon.length == 2) {
                        String path = "resources/images/users/" + _userIcon[0] + "/" + _userIcon[1];
                        java.net.URL imgURL = getClass().getResource(path);

                        if (imgURL != null) {
                            ImageIcon icon = new ImageIcon(imgURL);
                            lblUserIcon.setIcon(icon);
                        }            
                    }
                }                
            }
        });
        
        _settings.setVisible(true);
    }//GEN-LAST:event_miSettingsActionPerformed

    private void miAutoscanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miAutoscanActionPerformed
        formAutoScan.Stopped(false);
        new Thread(rAutoScan).start();
        formAutoScan.setVisible(true);
    }//GEN-LAST:event_miAutoscanActionPerformed

    private void btnSendMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendMessageActionPerformed
        SendMessage();
    }//GEN-LAST:event_btnSendMessageActionPerformed

    private void miSendMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miSendMessageActionPerformed
        SendMessage();
    }//GEN-LAST:event_miSendMessageActionPerformed

    private void miDeleteUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miDeleteUserActionPerformed
        DeleteUser();        
    }//GEN-LAST:event_miDeleteUserActionPerformed

    private void miExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_miExitActionPerformed

    private void miEditUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miEditUserActionPerformed
        EditUser();
    }//GEN-LAST:event_miEditUserActionPerformed

    private void btnEditUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditUserActionPerformed
        EditUser();
    }//GEN-LAST:event_btnEditUserActionPerformed

    private void btnAddUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddUserActionPerformed
        AddUser();
    }//GEN-LAST:event_btnAddUserActionPerformed

    private void miAddUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miAddUserActionPerformed
        AddUser();
    }//GEN-LAST:event_miAddUserActionPerformed

    private void miHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miHistoryActionPerformed
        FormMessageHistory _history = new FormMessageHistory(this, true);
        _history.setVisible(true);
    }//GEN-LAST:event_miHistoryActionPerformed

    private void miAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miAboutActionPerformed
        new FormAbout(this, true).setVisible(true);
    }//GEN-LAST:event_miAboutActionPerformed

    private void mipEditUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mipEditUserActionPerformed
        EditUser();
    }//GEN-LAST:event_mipEditUserActionPerformed

    private void mipDeleteUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mipDeleteUserActionPerformed
       DeleteUser();
    }//GEN-LAST:event_mipDeleteUserActionPerformed

    private void formWindowIconified(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowIconified
    }//GEN-LAST:event_formWindowIconified

    private void mipSendMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mipSendMessageActionPerformed
        SendMessage();
    }//GEN-LAST:event_mipSendMessageActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FormMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FormMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FormMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FormMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new FormMain(null).setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddUser;
    private javax.swing.JButton btnEditUser;
    private javax.swing.JButton btnSendMessage;
    private javax.swing.JComboBox cmbState;
    private javax.swing.JLabel lblMyName;
    private javax.swing.JLabel lblUserIcon;
    private javax.swing.JList lvUserList;
    private javax.swing.JMenu mAbout;
    private javax.swing.JMenu mEdit;
    private javax.swing.JMenu mFile;
    private javax.swing.JMenu mSettings;
    private javax.swing.JMenuBar mbMenu;
    private javax.swing.JMenuItem miAbout;
    private javax.swing.JMenuItem miAddUser;
    private javax.swing.JMenuItem miAutoscan;
    private javax.swing.JMenuItem miDeleteUser;
    private javax.swing.JMenuItem miEditUser;
    private javax.swing.JMenuItem miExit;
    private javax.swing.JMenuItem miHistory;
    private javax.swing.JMenuItem miSendMessage;
    private javax.swing.JMenuItem miSettings;
    private javax.swing.JMenuItem mipDeleteUser;
    private javax.swing.JMenuItem mipEditUser;
    private javax.swing.JMenuItem mipSendMessage;
    private javax.swing.JPanel pnlInfo;
    private javax.swing.JPopupMenu puMenu;
    private javax.swing.JScrollPane spScrollPanel;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
