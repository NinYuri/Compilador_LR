package compilador;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IDE extends javax.swing.JFrame 
{
    Linea numeroLinea;
    Directorio direc;
    List<String[]> tablaSim = new ArrayList<>();
    String error = "";
    
    boolean [][] reglaAsig = {
        {true, false, false, false},
        {true, true, false, false},
        {false, false, true, false},
        {false, false, false, true}
    };
    
    public IDE() 
    {
        initComponents();
        setResizable(false);
        setExtendedState(MAXIMIZED_BOTH);
        getContentPane().setBackground(new Color(36,38,48));
        
        Iniciar();
    }
    
    public void Lexico()
    {
        //Sintactico obs = new Sintactico();
        boolean ban = true;
        Analisis c = new Analisis();
        File archivo = new File("Compilacion.yum");
        PrintWriter escribir;
        try {
            escribir = new PrintWriter(archivo);
            escribir.print(jTPCode.getText());
            escribir.close();
        }
        catch(FileNotFoundException e) {
            Logger.getLogger(IDE.class.getName()).log(Level.SEVERE, null, e);
        }
        try {
            Reader lector  = new BufferedReader(new FileReader("Compilacion.yum"));
            Lexer lexer = new Lexer(lector);
            String resLexico = "";            
            Tokens token = lexer.yylex();
            //obs.lexemaOriginal = "" + lexer.lexeme;
            
            while(ban) {
                if(token == null) { //|| !obs.error.equals("")) {
                    //if (obs.error.equals("")) {
                    //    obs.BuscarElemento("$", c.linea + 1);
                    //}
                    //jTPError.setText(obs.error);
                    resLexico += "$";
                    jTPLexico.setText(resLexico);
                    ban = false;
                    //jTPSintactico.setText(obs.resultado);
                    return;
                }
                switch(token) {
                    case Error:
                        jTPError.setText("Error léxico en la línea " + (c.linea + 1) + ": El lexema " + lexer.lexeme + " es irreconocible. \n");
                        //obs.error+="Error léxico en la línea " + (c.linea + 1) + ": el lexema " + lexer.lexeme + " es irreconocible. \n";
                        //obs.BuscarElemento("$", c.linea + 1);
                        //jTPSintactico.setText(obs.resultado);
                        //jTPError.setText(obs.error);
                        
                        //if (lexer.lexeme.equals("" + '"')) {

                            //ban = false;
                            //obs.BuscarElemento("$", c.linea + 1);
                            resLexico += "$";
                            jTPLexico.setText(resLexico);
                            //jTPSintactico.setText(obs.resultado);
                            //jTPError.setText(obs.error);
                        //}                        
                        return;
                    case id, idI, idF, idC, idS, idClass, idMet, num, litcar, litcad:
                        if(token == Tokens.idI || token == Tokens.idF || token == Tokens.idC || token == Tokens.idS || token == Tokens.idMet) {
                            Tabla(String.valueOf(token), String.valueOf(lexer.lexeme), c.linea);
                            token = Tokens.id;
                        } else if(token == Tokens.id) {
                            if(!buscarID(lexer.lexeme)) {
                                //corregir linea
                                error += "Error semántico en la línea " + (c.linea + 1) + ": La variable " + lexer.lexeme + " no se encuentra definida.";
                                break;
                            }
                        }
                        resLexico += token + "\n";
                        //obs.BuscarElemento("" + token, c.linea + 1);
                        break;
                    default:
                        resLexico += lexer.lexeme + "\n";
                        //obs.BuscarElemento("" + lexer.lexeme, c.linea + 1);
                        break;
                }
                if(!error.equals("")) {
                    resLexico += "$";
                    jTPLexico.setText(resLexico);
                    jTPError.setText(error);
                    return;
                }
                token = lexer.yylex();
            }
        }
        catch(FileNotFoundException ex) {
            Logger.getLogger(IDE.class.getName()).log(Level.SEVERE, null, ex);            
        }
        catch(IOException ex) {
            Logger.getLogger(IDE.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void Tabla(String token, String lexema, int linea)
    {
        if(!tablaSim.isEmpty()) {
            if(!Buscar(lexema, linea))
                Tipo(token, lexema);
        } else
            Tipo(token, lexema);
    }
    
    private boolean Buscar(String lexema, int linea)
    {
        for(String[] vars: tablaSim)
            if(vars[0].equals(lexema)) {
                if(vars[1].equals("m"))
                    error += "Error semántico en la línea " + (linea + 1) + ": El método " + lexema + " ya se encuentra declarado.";
                else
                    error += "Error semántico en la línea " + (linea + 1) + ": La variable " + lexema + " ya se encuentra definida como " + tipoStr(vars[1]) + ".";
                return true;                                   
            }
        return false;
    }
    
    public boolean buscarID(String lexema)
    {
        for(String[] vars: tablaSim)
            if(vars[0].equals(lexema)) {
                return true;
            }
        return false;
    }
    
    private void Tipo(String token, String lexema)
    {
        switch(token) {
            case "idI":
                tablaSim.add(new String[]{lexema, "0"});
                break;
            case "idF":
                tablaSim.add(new String[]{lexema, "1"});
                break;
            case "idC":
                tablaSim.add(new String[]{lexema, "2"});
                break;
            case "idS":
                tablaSim.add(new String[]{lexema, "3"});
                break;
            case "idMet":
                tablaSim.add(new String[]{lexema, "m"});
                break;
        }
    }
    
    private String tipoStr(String tipo)
    {
        switch(tipo) {
            case "0":
                return "int";
            case "1":
                return "float";
            case "2":
                return "char";
            case "3":
                return "String";
        }
        return "";
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblNew = new javax.swing.JLabel();
        lblOpen = new javax.swing.JLabel();
        lblSave = new javax.swing.JLabel();
        lblSaveAs = new javax.swing.JLabel();
        lblRun = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTPCode = new javax.swing.JTextPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTPLexico = new javax.swing.JTextPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTPSintactico = new javax.swing.JTextPane();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTPCodInt = new javax.swing.JTextPane();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTPError = new javax.swing.JTextPane();
        menuBar = new javax.swing.JMenuBar();
        menuArchivo = new javax.swing.JMenu();
        mnNew = new javax.swing.JMenuItem();
        mnOpen = new javax.swing.JMenuItem();
        mnSave = new javax.swing.JMenuItem();
        mnSaveAs = new javax.swing.JMenuItem();
        menuCompilar = new javax.swing.JMenu();
        mnRun = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lblNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/newFile.png"))); // NOI18N
        lblNew.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        lblNew.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        lblNew.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblNewMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblNewMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblNewMouseExited(evt);
            }
        });

        lblOpen.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/openFile.png"))); // NOI18N
        lblOpen.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        lblOpen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblOpen.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblOpenMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblOpenMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblOpenMouseExited(evt);
            }
        });

        lblSave.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/save.png"))); // NOI18N
        lblSave.setToolTipText("");
        lblSave.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        lblSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        lblSave.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblSaveMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblSaveMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblSaveMouseExited(evt);
            }
        });

        lblSaveAs.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSaveAs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/saveAs.png"))); // NOI18N
        lblSaveAs.setToolTipText("");
        lblSaveAs.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        lblSaveAs.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        lblSaveAs.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblSaveAsMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblSaveAsMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblSaveAsMouseExited(evt);
            }
        });

        lblRun.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblRun.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/run.png"))); // NOI18N
        lblRun.setToolTipText("");
        lblRun.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        lblRun.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        lblRun.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblRunMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblRunMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblRunMouseExited(evt);
            }
        });

        jTPCode.setBackground(new java.awt.Color(36, 38, 48));
        jTPCode.setFont(new java.awt.Font("Bahnschrift", 0, 18)); // NOI18N
        jTPCode.setForeground(new java.awt.Color(255, 255, 255));
        jScrollPane1.setViewportView(jTPCode);

        jTPLexico.setEditable(false);
        jTPLexico.setBackground(new java.awt.Color(36, 38, 48));
        jTPLexico.setFont(new java.awt.Font("Bahnschrift", 0, 18)); // NOI18N
        jTPLexico.setForeground(new java.awt.Color(255, 255, 255));
        jScrollPane2.setViewportView(jTPLexico);

        jScrollPane3.setBackground(new java.awt.Color(36, 38, 48));
        jScrollPane3.setForeground(new java.awt.Color(255, 255, 255));

        jTPSintactico.setEditable(false);
        jTPSintactico.setBackground(new java.awt.Color(36, 38, 48));
        jTPSintactico.setFont(new java.awt.Font("Bahnschrift", 0, 18)); // NOI18N
        jTPSintactico.setForeground(new java.awt.Color(255, 255, 255));
        jScrollPane3.setViewportView(jTPSintactico);

        jTPCodInt.setEditable(false);
        jTPCodInt.setBackground(new java.awt.Color(36, 38, 48));
        jTPCodInt.setFont(new java.awt.Font("Bahnschrift", 0, 18)); // NOI18N
        jTPCodInt.setForeground(new java.awt.Color(255, 255, 255));
        jScrollPane4.setViewportView(jTPCodInt);

        jLabel1.setFont(new java.awt.Font("Bahnschrift", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("LÉXICO");

        jLabel2.setFont(new java.awt.Font("Bahnschrift", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("SINTÁCTICO");

        jLabel3.setFont(new java.awt.Font("Bahnschrift", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("CÓDIGO INTERMEDIO");

        jLabel4.setFont(new java.awt.Font("Bahnschrift", 1, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("ERROR");

        jTPError.setEditable(false);
        jTPError.setBackground(new java.awt.Color(36, 38, 48));
        jTPError.setFont(new java.awt.Font("Bahnschrift", 0, 18)); // NOI18N
        jTPError.setForeground(new java.awt.Color(255, 255, 255));
        jScrollPane5.setViewportView(jTPError);

        menuBar.setForeground(new java.awt.Color(36, 38, 48));
        menuBar.setFont(new java.awt.Font("Bahnschrift", 0, 14)); // NOI18N

        menuArchivo.setForeground(new java.awt.Color(36, 38, 48));
        menuArchivo.setText("Archivo");
        menuArchivo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuArchivo.setFont(new java.awt.Font("Bahnschrift", 0, 18)); // NOI18N
        menuArchivo.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        mnNew.setFont(new java.awt.Font("Bahnschrift", 0, 16)); // NOI18N
        mnNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/newFileOp.png"))); // NOI18N
        mnNew.setText("Nuevo archivo");
        mnNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnNewActionPerformed(evt);
            }
        });
        menuArchivo.add(mnNew);

        mnOpen.setFont(new java.awt.Font("Bahnschrift", 0, 16)); // NOI18N
        mnOpen.setText("Abrir");
        mnOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnOpenActionPerformed(evt);
            }
        });
        menuArchivo.add(mnOpen);

        mnSave.setFont(new java.awt.Font("Bahnschrift", 0, 16)); // NOI18N
        mnSave.setText("Guardar");
        mnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnSaveActionPerformed(evt);
            }
        });
        menuArchivo.add(mnSave);

        mnSaveAs.setFont(new java.awt.Font("Bahnschrift", 0, 16)); // NOI18N
        mnSaveAs.setText("Guardar como...");
        mnSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnSaveAsActionPerformed(evt);
            }
        });
        menuArchivo.add(mnSaveAs);

        menuBar.add(menuArchivo);

        menuCompilar.setForeground(new java.awt.Color(36, 38, 48));
        menuCompilar.setText("Compilar");
        menuCompilar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        menuCompilar.setFont(new java.awt.Font("Bahnschrift", 0, 18)); // NOI18N

        mnRun.setFont(new java.awt.Font("Bahnschrift", 0, 16)); // NOI18N
        mnRun.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/runOp.png"))); // NOI18N
        mnRun.setText("Compilar proyecto");
        mnRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnRunActionPerformed(evt);
            }
        });
        menuCompilar.add(mnRun);

        menuBar.add(menuCompilar);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblNew, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblOpen, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSave, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSaveAs, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblRun, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 834, Short.MAX_VALUE)
                            .addComponent(jScrollPane5))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 579, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(13, 13, 13)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 580, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(13, 13, 13)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(13, 13, 13)
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 580, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(406, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblRun, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSaveAs, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSave, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblOpen, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNew, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                    .addComponent(jScrollPane5))
                .addContainerGap(60, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lblNewMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblNewMouseEntered
        lblNew.setOpaque(true);
        lblNew.setBackground(new Color(96,102,133));
    }//GEN-LAST:event_lblNewMouseEntered

    private void lblNewMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblNewMouseExited
        lblNew.setOpaque(false);
        lblNew.setBackground(new Color(36,38,48));
    }//GEN-LAST:event_lblNewMouseExited

    private void lblOpenMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblOpenMouseEntered
        lblOpen.setOpaque(true);
        lblOpen.setBackground(new Color(96,102,133));
    }//GEN-LAST:event_lblOpenMouseEntered

    private void lblOpenMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblOpenMouseExited
        lblOpen.setOpaque(false);
        lblOpen.setBackground(new Color(36,38,48));
    }//GEN-LAST:event_lblOpenMouseExited

    private void lblSaveMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSaveMouseEntered
       lblSave.setOpaque(true);
       lblSave.setBackground(new Color(96,102,133));
    }//GEN-LAST:event_lblSaveMouseEntered

    private void lblSaveMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSaveMouseExited
        lblSave.setOpaque(false);
        lblSave.setBackground(new Color(36,38,48));
    }//GEN-LAST:event_lblSaveMouseExited

    private void lblSaveAsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSaveAsMouseEntered
        lblSaveAs.setOpaque(true);
        lblSaveAs.setBackground(new Color(96,102,133));
    }//GEN-LAST:event_lblSaveAsMouseEntered

    private void lblSaveAsMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSaveAsMouseExited
        lblSaveAs.setOpaque(false);
        lblSaveAs.setBackground(new Color(36,38,48));
    }//GEN-LAST:event_lblSaveAsMouseExited

    private void lblRunMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblRunMouseEntered
        lblRun.setOpaque(true);
        lblRun.setBackground(new Color(96,102,133));
    }//GEN-LAST:event_lblRunMouseEntered

    private void lblRunMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblRunMouseExited
        lblRun.setOpaque(false);
        lblRun.setBackground(new Color(36,38,48));
    }//GEN-LAST:event_lblRunMouseExited

    private void lblNewMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblNewMouseClicked
        direc.Nuevo(this);
        clearAllComp();
        clearVar();
    }//GEN-LAST:event_lblNewMouseClicked

    private void lblOpenMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblOpenMouseClicked
        direc.Abrir(this);
        clearAllComp();
        clearVar();
    }//GEN-LAST:event_lblOpenMouseClicked

    private void lblSaveMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSaveMouseClicked
        direc.Guardar(this);
        clearAllComp();
        clearVar();
    }//GEN-LAST:event_lblSaveMouseClicked

    private void lblSaveAsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSaveAsMouseClicked
        direc.guardarC(this);
        clearAllComp();
        clearVar();
    }//GEN-LAST:event_lblSaveAsMouseClicked

    private void mnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnNewActionPerformed
        direc.Nuevo(this);
        clearAllComp();
        clearVar();
    }//GEN-LAST:event_mnNewActionPerformed

    private void mnOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnOpenActionPerformed
        direc.Abrir(this);
        clearAllComp();
        clearVar();
    }//GEN-LAST:event_mnOpenActionPerformed

    private void mnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnSaveActionPerformed
        direc.Guardar(this);
        clearAllComp();
        clearVar();
    }//GEN-LAST:event_mnSaveActionPerformed

    private void mnSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnSaveAsActionPerformed
        direc.guardarC(this);
        clearAllComp();
        clearVar();
    }//GEN-LAST:event_mnSaveAsActionPerformed

    private void lblRunMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblRunMouseClicked
        clearAllComp();
        clearVar();
        Lexico();
    }//GEN-LAST:event_lblRunMouseClicked

    private void mnRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnRunActionPerformed
        clearAllComp();
        clearVar();
        Lexico();
    }//GEN-LAST:event_mnRunActionPerformed

    private void Iniciar()
    {
        direc = new Directorio();
        setTitle("#YURIDE");
        String[] options = new String[] {"Guardar y continuar", "Descartar"};
        
        numeroLinea = new Linea(jTPCode);
        jScrollPane1.setRowHeaderView(numeroLinea);
    }
    
    public void clearAllComp()
    {
        jTPLexico.setText("");
        jTPSintactico.setText("");
        jTPCodInt.setText("");
        jTPError.setText("");
    }
    
    private void clearVar()
    {
        tablaSim = new ArrayList<>();
        error = "";
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(IDE.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(IDE.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(IDE.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(IDE.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new IDE().setVisible(true);
            }
        });
    }        

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTextPane jTPCodInt;
    public javax.swing.JTextPane jTPCode;
    private javax.swing.JTextPane jTPError;
    private javax.swing.JTextPane jTPLexico;
    private javax.swing.JTextPane jTPSintactico;
    private javax.swing.JLabel lblNew;
    private javax.swing.JLabel lblOpen;
    private javax.swing.JLabel lblRun;
    private javax.swing.JLabel lblSave;
    private javax.swing.JLabel lblSaveAs;
    private javax.swing.JMenu menuArchivo;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu menuCompilar;
    private javax.swing.JMenuItem mnNew;
    private javax.swing.JMenuItem mnOpen;
    private javax.swing.JMenuItem mnRun;
    private javax.swing.JMenuItem mnSave;
    private javax.swing.JMenuItem mnSaveAs;
    // End of variables declaration//GEN-END:variables
}
