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
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IDE extends javax.swing.JFrame 
{
    Sintactico sint = new Sintactico();
    Linea numeroLinea;
    Directorio direc;
    List<String[]> tablaSim = new ArrayList<>();
    List<String[]> tablaVal = new ArrayList<>();
    Stack<String> expPosfija = new Stack<>();
    Stack<String> tempMain = new Stack<>();
    Stack<String> valMain = new Stack<>();
    Stack<String> tempMet = new Stack<>();
    Stack<String> auxiliar = new Stack<>();
    Stack<String> variables = new Stack<>();
    Stack<String> pilaOpers = new Stack<>();
    Stack<String> temp = new Stack<>();
    Stack<String> aux = new Stack<>();
    String error = "";
    int llaves = 0;            
    
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
            Stack<String> pilaSint = new Stack();
            StringBuilder texto = new StringBuilder();
            StringBuilder codInt = new StringBuilder();
            boolean banMet = false;
            boolean banMain = false;
            boolean valor = false;
            Tokens token = lexer.yylex();
            
            while(ban) {
                if(token == null) {
                    resLexico += "$";
                    sint.Sintactico("$");
                    jTPLexico.setText(resLexico);
                    
                    pilaSint = sint.Pila();                 
                    for(String reng : pilaSint)
                        texto.append(reng).append("\n");
                    
                    if(llaves != 0) {
                        jTPSintactico.setText(texto.toString());
                        jTPError.setText("Error sintáctico en la línea " + (c.linea + 1) + ": Existen llaves { sin cierre.");
                        return;
                    }                                                                               
                    texto.append("$I0CLASS'");
                    jTPSintactico.setText(texto.toString());
                    
                    codInter();
                    System.out.println("DESPUES DE METODOS");
                    for(String var : expPosfija)
                        codInt.append(var).append("\n");
                    jTPCodInt.setText(codInt.toString());
                    
                    ban = false;
                    return;
                }
                switch(token) {
                    case Error:
                        jTPError.setText("Error léxico en la línea " + (c.linea + 1) + ": El lexema " + lexer.lexeme + " es irreconocible. \n");                        
                        resLexico += "$";
                        jTPLexico.setText(resLexico);
                        pilaSint = sint.Pila();                 
                        for(String reng : pilaSint)
                            texto.append(reng).append("\n");
                        jTPSintactico.setText(texto.toString());
                        codInter();
                        for(String var : expPosfija)
                            codInt.append(var).append("\n");
                        jTPCodInt.setText(codInt.toString());
                        return;
                    case id, idI, idF, idC, idS, idClass, idMet, num, litcar, litcad:
                        if(token == Tokens.idClass)
                            token = Tokens.id;
                        else
                            if(token == Tokens.idI || token == Tokens.idF || token == Tokens.idC || token == Tokens.idS) {
                                Tabla(String.valueOf(token), String.valueOf(lexer.lexeme), c.linea);
                                sint.Tabla(tablaSim);
                                sint.ID(lexer.lexeme);
                                variables.push(String.valueOf(lexer.lexeme));
                                valor = true;
                                token = Tokens.id;
                            } else if(token == Tokens.idMet) {
                                Tabla(String.valueOf(token), String.valueOf(lexer.lexeme), c.linea);
                                sint.Tabla(tablaSim);
                                sint.ID(lexer.lexeme);
                                tempMet.push("Etq" + String.valueOf(lexer.lexeme) + ":");
                                banMet = true;
                                token = Tokens.id;                               
                            }                            
                            else if(token == Tokens.id) {
                                if(!buscarID(lexer.lexeme)) {
                                    error += "Error semántico en la línea " + (c.linea + 2) + ": La variable o método " + lexer.lexeme + " no se encuentra definido.";
                                    break;
                                }
                            }                     
                        
                        if(token == Tokens.id || token == Tokens.num || token == Tokens.litcad || token == Tokens.litcar) {
                            sint.Variable(String.valueOf(token), String.valueOf(lexer.lexeme));                            
                            
                            if(token != Tokens.id) {  
                                if(!banMet || !banMain)
                                    if(valor) {
                                        tablaVal.add(new String[]{variables.peek(), String.valueOf(lexer.lexeme)});
                                        valor = false;
                                    }
                            }                               
                        }

                        resLexico += token + "\n";                        
                        sint.Sintactico(String.valueOf(token));
                        if(banMet)
                            tempMet.push(String.valueOf(token));
                        else
                            if(banMain) {
                                tempMain.push(String.valueOf(token));
                                valMain.push(String.valueOf(lexer.lexeme));
                            }
                        break;
                    case open_key:
                        llaves++;
                        resLexico += lexer.lexeme + "\n";
                        sint.Sintactico(String.valueOf(lexer.lexeme));
                        if(banMet)
                            tempMet.push(String.valueOf(lexer.lexeme));
                        else
                            if(banMain) {
                                tempMain.push(String.valueOf(lexer.lexeme));
                                valMain.push(String.valueOf(lexer.lexeme));
                            }
                        break;
                    case close_key:
                        if(llaves != 0) {
                            llaves--;
                            resLexico += lexer.lexeme + "\n";
                            sint.Sintactico(String.valueOf(lexer.lexeme));
                        }                          
                        else
                            error += "Error sintáctico en la línea " + (c.linea + 1) + ": Existe una llave } innecesaria.";                                       
                        if(banMet)
                            tempMet.push(String.valueOf(lexer.lexeme));
                        else
                            if(banMain) {
                                tempMain.push(String.valueOf(lexer.lexeme));
                                valMain.push(String.valueOf(lexer.lexeme));
                            }
                        break;  
                    case mainType:
                        resLexico += lexer.lexeme + "\n";
                        sint.Sintactico(String.valueOf(lexer.lexeme));
                        Tabla(String.valueOf(token), String.valueOf(lexer.lexeme), c.linea);
                        tempMain.push("Etq" + String.valueOf(lexer.lexeme) + ":");
                        banMain = true;
                        banMet = false;
                        break;
                    default:
                        resLexico += lexer.lexeme + "\n";
                        sint.Sintactico(String.valueOf(lexer.lexeme));
                        if(banMet)
                            tempMet.push(String.valueOf(lexer.lexeme));
                        else
                            if(banMain) {
                                tempMain.push(String.valueOf(lexer.lexeme));
                                valMain.push(String.valueOf(lexer.lexeme));
                            }
                        break;
                }
                if(!error.equals("")) {
                    resLexico += "$";
                    jTPLexico.setText(resLexico);
                    pilaSint = sint.Pila();                 
                    for(String reng : pilaSint)
                        texto.append(reng).append("\n");
                    jTPSintactico.setText(texto.toString());
                    jTPError.setText(error);
                    codInter();
                    for(String var : expPosfija)
                        codInt.append(var).append("\n");
                    jTPCodInt.setText(codInt.toString());
                    return;
                }
                if(sint.errSint) {
                    resLexico += "$";
                    jTPLexico.setText(resLexico);
                    pilaSint = sint.Pila();                 
                    for(String reng : pilaSint)
                        texto.append(reng).append("\n");
                    jTPSintactico.setText(texto.toString());
                    jTPError.setText("Error sintáctico en la línea " + (c.linea + 1) + ": Se recibició un " + lexer.lexeme + " cuando se esperaba un " + sint.Esperado() + ".");
                    codInter();
                    for(String var : expPosfija)
                        codInt.append(var).append("\n");
                    jTPCodInt.setText(codInt.toString());
                    return;
                }
                if(!sint.error.equals("")) {
                    resLexico += "$";
                    jTPLexico.setText(resLexico);
                    pilaSint = sint.Pila();                 
                    for(String reng : pilaSint)
                        texto.append(reng).append("\n");
                    jTPSintactico.setText(texto.toString());
                    jTPError.setText("Error semántico en la línea " + (c.linea + 1) + ": " + sint.error);
                    codInter();
                    for(String var : expPosfija)
                        codInt.append(var).append("\n");
                    jTPCodInt.setText(codInt.toString());
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
            case "mainType":
                tablaSim.add(new String[] {lexema, "m"});
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
    
    private void codInter()
    {
        String mensaje;
        boolean banMain = true;
        
        for(String[] varGlob: tablaSim) {
            String lexema = varGlob[0];
            String tipo = varGlob[1];
            
            String valor = buscarVal(lexema);
        
            switch(tipoStr(tipo)) {
                case "int":
                    mensaje = "int " + lexema;
                    if(!valor.equals(""))
                        mensaje += valor;
                    else
                        mensaje += ";";
                    expPosfija.push(mensaje);
                    break;
                case "float":
                    mensaje = "float " + lexema;
                    if(!valor.equals(""))
                        mensaje += valor;
                    else
                        mensaje += ";";
                    expPosfija.push(mensaje);
                    break;
                case "char":
                    mensaje = "char " + lexema;
                    if(!valor.equals(""))
                        mensaje += valor;
                    else
                        mensaje += ";";
                    expPosfija.push(mensaje);
                    break;
                case "String":
                    mensaje = "String " + lexema;
                    if(!valor.equals(""))
                        mensaje += valor;
                    else
                        mensaje += ";";
                    expPosfija.push(mensaje);                    
                    break;                    
                case "":
                    if(banMain) {
                        pilaInterm("Main");
                        expPosfija.push("exit(0);");
                    }
                    /*if(!tempMet.isEmpty()) {
                        pilaMet();
                        for(String car : tempMet)
                            expPosfija.push(car);                                            
                        expPosfija.push("goto EtiqReturn" + lexema);
                    }*/
                    return;
                default:
                    break;
            }
        }
    }
    
    private String buscarVal(String lexema)
    {
        for(String[] val: tablaVal)
            if(val[0].equals(lexema))
                if(!val[1].equals(""))
                    return " = " + val[1] + ";";
        return "";
    }
    
    private void pilaInterm(String contex)
    {
        String token, valor, mensaje;
        boolean ini = false;
        int conIf = 0, conWhile = 0, varIf = 0, varWhile = 0;
        Stack<String> pilaContexto = new Stack<>();
        Stack<String> temporal = new Stack<>();
        auxiliar.clear();
        
        if(contex.equals("Metodo")) {
            while(!tempMet.isEmpty())
                auxiliar.push(tempMet.pop());            
        } else {
            while(!tempMain.isEmpty())
                auxiliar.push(tempMain.pop());
            while(!valMain.isEmpty())
                temporal.push(valMain.pop());
        }
 
        expPosfija.push("\n" + auxiliar.pop());
        
        while(!auxiliar.isEmpty() && !temporal.isEmpty()) {
            while(!ini) {
                token = auxiliar.pop();
                valor = temporal.pop();
                if(token.equals("{")) {
                    ini = true;
                    break;
                }       
            }
            token = auxiliar.pop();
            valor = temporal.pop();
            
            switch(token) {
                case "int", "float", "char":
                    mensaje = token + " ";
                    do {
                        if(temporal.peek().equals(","))
                            mensaje = mensaje.trim() + temporal.pop() + " ";
                        else
                            mensaje += temporal.pop() + " ";
                        auxiliar.pop();
                    } while(!temporal.peek().equals(";"));
                    
                    mensaje = mensaje.trim() + temporal.pop();
                    auxiliar.pop();                    
                    expPosfija.push(mensaje);
                    break;
                case "String":
                    mensaje = "char ";                    
                    do {
                        if(temporal.peek().equals(",")) {
                            mensaje = mensaje.trim();
                            for(int i = 0; i < 2; i++) {
                                mensaje += temporal.pop() + " ";
                                auxiliar.pop();
                            }
                            mensaje = mensaje.trim() + "[100] ";
                        } else {
                            mensaje += temporal.pop();
                            auxiliar.pop();
                            mensaje += "[100] ";
                        }
                                                
                        while(!temporal.peek().equals(",") && !temporal.peek().equals(";")) {
                            mensaje += temporal.pop() + " ";
                            auxiliar.pop();
                        }       
                    } while(temporal.peek().equals(","));
                    
                    mensaje = mensaje.trim() + temporal.pop();
                    auxiliar.pop();                    
                    expPosfija.push(mensaje);
                    break;
                case "print":
                    boolean ban = false;
                    mensaje = "printf";                                      
                    do {
                        token = auxiliar.pop();
                        mensaje += temporal.pop() + " ";  
                        
                        if(token.equals("litcad"))
                            ban = true;
                    } while(!temporal.peek().equals(")"));
                    
                    if(!ban)
                        mensaje += ", \"\"" + temporal.pop();
                    else
                        mensaje += temporal.pop();
                    auxiliar.pop();
                    
                    mensaje += temporal.pop();
                    auxiliar.pop();
                    expPosfija.push(mensaje);
                    break;
                    
                    
                /*case "print":
                    mensaje = token + " ";
                    do {
                        mensaje += auxiliar.pop() + " ";
                    } while(!auxiliar.peek().equals(";"));
                    
                    mensaje = mensaje.trim() + auxiliar.pop();
                    tempMet.push(mensaje);
                    break;*/
                case "id":
                    mensaje = token + " ";
                    token = auxiliar.pop();
                    
                    if(token.equals("=")) {
                        mensaje += token + " ";
                        
                        token = auxiliar.pop();
                        if(token.equals("read")) {
                            mensaje += token;
                            do {
                                mensaje += auxiliar.pop();
                            } while(!auxiliar.peek().equals(";"));
                            mensaje += auxiliar.pop();
                        } else {
                            Posfija(token);
                            do {
                                Posfija(auxiliar.pop());
                            } while(!auxiliar.peek().equals(";"));
                            Posfija(auxiliar.pop());
                            codOper();
                            mensaje += " v1;";
                        }
                    } else
                        mensaje = mensaje.trim() + token + auxiliar.pop() + auxiliar.pop();                                                                                                                    
                        
                    tempMet.push(mensaje);
                    break;
                case "if":
                    mensaje = token + " ";
                    do {
                        mensaje += auxiliar.pop() + " ";
                    } while(!auxiliar.peek().equals("{")); 
                    
                    String[] partes = mensaje.split(" ");
                    tempMet.push("vc" + (++varIf) + " = " + partes[2] + ";");
                    tempMet.push("vc" + (++varIf) + " = " + partes[4] + ";");
                    tempMet.push("vc" + (--varIf) + " = " + "vc" + (varIf) + " " + partes[3] + " vc" + (++varIf) + ";");
                    tempMet.push("if(!vc" + (--varIf) + ")");
                    tempMet.push("goto Else" + ++conIf + ";");     
                    pilaContexto.push("If" + conIf);
                    break;
                case "while":
                    mensaje = token + " ";
                    do {
                        mensaje += auxiliar.pop() + " ";
                    } while(!auxiliar.peek().equals("{"));
                    
                    String[] partesw = mensaje.split(" ");
                    tempMet.push("While" + ++conWhile + ":");
                    tempMet.push("vw" + (++varWhile) + " = " + partesw[2] + ";");
                    tempMet.push("vw" + (++varWhile) + " = " + partesw[4] + ";");
                    tempMet.push("vw" + (--varWhile) + " = " + "vw" + varWhile + " " + partesw[3] + " vw" + (++varWhile) + ";");
                    tempMet.push("if(!vw" + (--varWhile) + ")");
                    tempMet.push("goto Fin_While" + conWhile);
                    pilaContexto.push("While" + conWhile);
                    break;
                case "{":
                    break;
                case "}":
                if(!pilaContexto.isEmpty()) {
                    String contexto = pilaContexto.pop();
                    if(contexto.startsWith("If")) {
                        tempMet.push("goto End_If" + contexto.substring(2) + ";"); 
                        tempMet.push("Else" + contexto.substring(2) + ":");
                        tempMet.push("goto End_If" + contexto.substring(2) + ";");
                        tempMet.push("End_If" + contexto.substring(2) + ":");                        
                    } else if (contexto.startsWith("While")) {
                        tempMet.push("goto While" + contexto.substring(5) + ";");
                        tempMet.push("Fin_While" + contexto.substring(5) + ":");
                    }
                }
                break;
            }
        }
    }
    
    private void pilaMain()
    {
        String token, mensaje, temp;
        boolean ini = false;
        int conIf = 0, conWhile = 0, varIf = 0, varWhile = 0;
        Stack<String> pilaContexto = new Stack<>();
        
        while(!tempMain.isEmpty())
            auxiliar.push(tempMain.pop());
        
        tempMain.push("\n" + auxiliar.pop());
        while(!auxiliar.isEmpty()) {
            while(!ini) {
                token = auxiliar.pop();
                if(token.equals("{")) {
                    ini = true;
                    break;
                }       
            }
            token = auxiliar.pop();
            
            switch(token) {
                case "int", "float", "char", "String":
                    mensaje = token + " ";
                    do {
                        mensaje += auxiliar.pop() + " ";
                    } while(!auxiliar.peek().equals(";"));                    
                    
                    mensaje = mensaje.trim() + auxiliar.pop();
                    tempMain.push(mensaje);
                    break;
                case "print":
                    mensaje = token + " ";
                    do {
                        mensaje += auxiliar.pop() + " ";
                    } while(!auxiliar.peek().equals(";"));
                    
                    mensaje = mensaje.trim() + auxiliar.pop();
                    tempMain.push(mensaje);
                    break;
                case "id":
                    mensaje = token + " ";
                    token = auxiliar.pop();
                    
                    if(token.equals("=")) {
                        mensaje += token + " ";
                        
                        token = auxiliar.pop();
                        if(token.equals("read")) {
                            mensaje += token;
                            do {
                                mensaje += auxiliar.pop();
                            } while(!auxiliar.peek().equals(";"));
                            mensaje += auxiliar.pop();
                        } else {
                            Posfija(token);
                            do {
                                Posfija(auxiliar.pop());
                            } while(!auxiliar.peek().equals(";"));
                            Posfija(auxiliar.pop());
                            codOper();
                            mensaje += " v1;";
                        }
                    } else {
                        temp = mensaje.trim();
                        mensaje = "goto Etiq" + buscarM() + ";\n";
                        mensaje += "EtiqReturn" + buscarM();                          
                    }
                        
                    tempMain.push(mensaje);
                    break;
                case "if":
                    mensaje = token + " ";
                    do {
                        mensaje += auxiliar.pop() + " ";
                    } while(!auxiliar.peek().equals("{")); 
                    
                    String[] partes = mensaje.split(" ");
                    tempMain.push("vc" + (++varIf) + " = " + partes[2] + ";");
                    tempMain.push("vc" + (++varIf) + " = " + partes[4] + ";");
                    tempMain.push("vc" + (--varIf) + " = " + "vc" + (varIf) + " " + partes[3] + " vc" + (++varIf) + ";");
                    tempMain.push("if(!vc" + (--varIf) + ")");
                    tempMain.push("goto Else" + ++conIf + ";");     
                    pilaContexto.push("If" + conIf);
                    break;
                case "while":
                    mensaje = token + " ";
                    do {
                        mensaje += auxiliar.pop() + " ";
                    } while(!auxiliar.peek().equals("{"));
                    
                    String[] partesw = mensaje.split(" ");
                    tempMain.push("While" + ++conWhile + ":");
                    tempMain.push("vw" + (++varWhile) + " = " + partesw[2] + ";");
                    tempMain.push("vw" + (++varWhile) + " = " + partesw[4] + ";");
                    tempMain.push("vw" + (--varWhile) + " = " + "vw" + varWhile + " " + partesw[3] + " vw" + (++varWhile) + ";");
                    tempMain.push("if(!vw" + (--varWhile) + ")");
                    tempMain.push("goto Fin_While" + conWhile);
                    pilaContexto.push("While" + conWhile);
                    break;
                case "{":
                    if(auxiliar.isEmpty())
                        return;
                    else
                        break;
                case "}":
                if(!pilaContexto.isEmpty()) {
                    String contexto = pilaContexto.pop();
                    if(contexto.startsWith("If")) {
                        tempMain.push("goto End_If" + contexto.substring(2) + ";"); 
                        tempMain.push("Else" + contexto.substring(2) + ":");
                        tempMain.push("goto End_If" + contexto.substring(2) + ";");
                        tempMain.push("End_If" + contexto.substring(2) + ":");                        
                    } else if (contexto.startsWith("While")) {
                        tempMain.push("goto While" + contexto.substring(5) + ";");
                        tempMain.push("Fin_While" + contexto.substring(5) + ":");
                    }
                }
                break;
            }
        }
    }
    
    private void Posfija(String lexema)
    {
        int prioCima, prioToken;
        if(lexema.equals("id") || lexema.equals("num"))
            temp.push(lexema);
        else {
            switch(lexema) {
                case "(":
                    pilaOpers.push(lexema);
                    break;
                case ")":
                    while(!pilaOpers.peek().equals("("))
                        temp.push(pilaOpers.pop());
                    pilaOpers.pop();
                    break;
                case ";":
                    while(!pilaOpers.isEmpty())
                        temp.push(pilaOpers.pop());
                    break;
                default:  
                    if(!pilaOpers.isEmpty()) {
                        prioCima = Prioridad(pilaOpers.peek());
                        prioToken = Prioridad(lexema);
                                                
                        if(prioCima == 0)
                            pilaOpers.push(lexema);
                        else {
                            while(!pilaOpers.isEmpty() && prioCima >= prioToken) {
                                temp.push(pilaOpers.pop());
                                prioCima = Prioridad(pilaOpers.peek());
                            }                            
                            pilaOpers.push(lexema);
                        }
                    }
                    else
                        if(!lexema.equals("="))
                            pilaOpers.push(lexema);
            }
        }
    }
    
    private int Prioridad(String lexema)
    {
        switch(lexema) {
            case "+", "-":
                return 1;
            case "*", "/":
                return 2;
        }
        return 0;
    }
    
    private void codOper()
    {
        int cont = 1;
        while(!temp.isEmpty())
            aux.push(temp.pop());
        
        while(!aux.isEmpty()) {            
            String var = aux.pop();
            
            if(var.equals("id") || var.equals("num"))
                tempMet.push("v" + cont++ + " = " + var + ";");
            else
                switch(var) {
                    case "+":
                        cont -= 2;
                        if(cont == 0)
                            tempMet.push("v" + ++cont + " = +v" + cont + ";");
                        else
                            tempMet.push("v" + cont + " = v" + cont + " + v" + ++cont + ";");
                        break;
                    case "-":
                        cont -= 2;
                        if(cont == 0)
                            tempMet.push("v" + ++cont + " = -v" + cont + ";"); 
                        else
                            tempMet.push("v" + cont + " = v" + cont + " - v" + ++cont + ";");
                        break;
                    case "/":
                        cont -= 2;
                        tempMet.push("v" + cont + " = v" + cont + " / v" + ++cont + ";");
                        break;
                    case "*":
                        cont -= 2;
                        tempMet.push("v" + cont + " = v" + cont + " * v" + ++cont + ";");                       
                }            
        }
        cont--;
        if(cont == 0)
            tempMet.push("v" + ++cont + " = v" + cont + " ;");
        else
            tempMet.push("v" + cont + " = v" + cont + " ;");
    }
    
    private String buscarM() 
    {
        for(String[] fila : tablaSim) {
            if(fila[1].equals("m"))
                return fila[0];            
        }
        return null;
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
        System.out.println("PILA Metodos");
        for(String car : tempMet)
            System.out.println(car);
        System.out.println();
        
        System.out.println("PILA Main");
        for(String car : tempMain)
            System.out.println(car);
        System.out.println();
        
        System.out.println("PILA VALORES Main");
        for(String car : valMain)
            System.out.println(car);
        System.out.println();
        
        System.out.println("TABLA DE SIMBOLOS");
        for(String[] fila : tablaSim) {
            for(String dato : fila)
                System.out.print(dato + "\t");            
            System.out.println();
        }
        System.out.println();
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
        tablaVal = new ArrayList<>();
        expPosfija.clear();
        tempMet.clear();
        tempMain.clear();
        valMain.clear();
        auxiliar.clear();
        variables.clear();
        pilaOpers.clear();
        aux.clear();
        temp.clear();
        error = "";
        llaves = 0;
        sint.Reinicio();
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
