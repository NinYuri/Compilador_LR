package compilador;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Directorio 
{
    private FileNameExtensionFilter filter = new FileNameExtensionFilter("yum files", "yum");
    JFileChooser selecFile = new JFileChooser();
    File file;
    String[] options = new String[]{"Guardar y continuar", "Descartar"};
    
    public String getTextFile(File file) 
    {
        String text = "";
        try {
            // Crea un objeto BufferedReader para leer el archivo. Se usa InputStreamReader para especificar la codificación UTF-8.            
            BufferedReader entrada = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
            // Lee cada carácter del archivo uno por uno.
            while(true) {
                int b = entrada.read();
                // Si el valor de retorno es -1, se ha llegado al final del archivo y se sale del bucle.                
                if(b != -1) // Si el valor de retorno no es -1, se concatena el carácter a la cadena de texto.
                    text += (char)b;
                else
                    break;
            }            
        } catch(FileNotFoundException ex) {
            System.out.println("El archivo no pudo ser encontrado..." + ex.getMessage());
            return null;
        } catch(IOException ex) {
            System.out.println("Error al leer el archivo..." + ex.getMessage());
            return null;
        }
        // Se devuelve la cadena de texto que contiene el contenido del archivo.
        return text;
    }
    
    public boolean saveFile(File archivo, String text)
    {
        try {
            // Crea un objeto FileOutputStream para escribir en el archivo especificado.
            FileOutputStream output = new FileOutputStream(archivo);
            // Convierte la cadena de texto en un arreglo de bytes utilizando el método getBytes() de la clase String.
            byte[] bytesText = text.getBytes();
            // Escribe los bytes en el archivo utilizando el método write() del objeto FileOutputStream.
            output.write(bytesText);
        } catch(FileNotFoundException ex) {
            System.out.println("Error de fileNotFoundException..." + ex.getMessage());
            return false;
        } catch(IOException ex) {
            System.out.println("Error al escribir en el archivo..." + ex.getMessage());
            return false;
        }
        // Si el archivo se guarda correctamente, se devuelve true.
        return true;
    }
    
    public boolean guardarEditNuevo(File fileG, JFileChooser selecFileG, IDE compF)
    {
        int x;
        // Verifica si el título del archivo actual es igual a "[#YURIDE]*".
        // Si es así, entonces no se ha guardado el archivo, de lo contrario se pregunta al usuario si desea guardar los cambios.
        if(compF.getTitle().equals("[#YURIDE]*"))
            x = 0;
        else
            x = JOptionPane.showOptionDialog(compF, "El archivo actual está siendo editado, ¿desea guardar los cambios?",
                                            "¿Descartar edición?", -1, 3, null, options, options[0]);
        if(x == 0) {
            // Si el usuario elige guardar los cambios y ha seleccionado un archivo en el objeto JFileChooser,
            // entonces guarda el archivo en el objeto File especificado utilizando el método saveFile.
            if(selecFileG.getSelectedFile() != null) {
                boolean save = saveFile(fileG, compF.jTPCode.getText());
                if(save)
                    compF.setTitle(fileG.getName());                    
            }
            // Si no hay ningún archivo seleccionado en el objeto JFileChooser y el título del archivo actual es igual a "[#YURIDE]*",
            // entonces se pregunta al usuario si desea guardar el archivo.
            else 
                if(compF.getTitle().equals("[#YURIDE]*")) {
                    int y = JOptionPane.showOptionDialog(compF, "¿Desea guardar el archivo actual?",
                                                    "¿Descartar edición de archivo nuevo?", -1, 3, null, options, options[0]);
                    if(y == 0) {
                        // Si el usuario elige guardar el archivo y ha seleccionado un archivo en el objeto JFileChooser,
                        // entonces lo guarda en el objeto File especificado utilizando el método guardarArch.
                        if(selecFileG.showDialog(compF, "Guardar") == JFileChooser.APPROVE_OPTION){
                            fileG = selecFileG.getSelectedFile();
                            String fileGname = fileG.getName();                        
                
                            if(fileGname.endsWith(".yum")) {
                                if(!fileGname.split("[.]")[0].replace(" ","").equals("")) {
                                    if(!fileG.exists())
                                        guardarArch(fileG, compF);  
                                    else {
                                        int z = JOptionPane.showConfirmDialog(compF, "Ya hay un archivo con este nombre, ¿desea "
                                                                         +"sobreescribirlo?", "Sobreescribir archivo", 2);
                                        if(z == 0)
                                            guardarArch(fileG, compF);                                        
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(compF, "Escriba un nombre válido para el archivo",
                                                              "Nombre inválido", 2);
                                    return false;
                                }
                            } else {
                                JOptionPane.showMessageDialog(compF, "El archivo debe tener la extensión '.yum'",
                                                          "Extensión inválida", 2); 
                                return false;
                            }
                        }                    
                    } else
                        return true;
                }
                // Si el usuario elige guardar los cambios y no ha seleccionado un archivo en el objeto JFileChooser,
                // entonces se pregunta al usuario si desea sobrescribir el archivo existente.
                else {
                    int z = JOptionPane.showConfirmDialog(compF, "Ya hay un archivo con este nombre, ¿desea "
                                                                         +"sobreescribirlo?", "Sobreescribir archivo", 2);
                    if(z == 0)
                        guardarArch(fileG, compF); 
                }               
        }
        return true;
    }    
    
    public boolean guardarEditAbrir(File fileG, JFileChooser selecFileG, IDE compF) 
    {
        int x;
        if(compF.getTitle().equals("[#YURIDE]*"))
            x = 0;
        else
            x = JOptionPane.showOptionDialog(compF, "El archivo actual está siendo editado, ¿desea guardar los cambios?",
                                            "¿Descartar edición?", -1, 3, null, options, options[0]);
        if(x == 0) {
            if(selecFileG.getSelectedFile() != null) {
                boolean save = saveFile(fileG, compF.jTPCode.getText());
                if(save)
                    compF.setTitle(fileG.getName());                    
            } else 
                if(compF.getTitle().equals("[#YURIDE]*")) {
                    int y = JOptionPane.showOptionDialog(compF, "¿Desea guardar el archivo actual?",
                                                    "¿Descartar edición de archivo nuevo?", -1, 3, null, options, options[0]);               
                    if(y == 0) {
                        if(selecFileG.showDialog(compF, "Guardar") == JFileChooser.APPROVE_OPTION) {
                            fileG = selecFileG.getSelectedFile();
                            String fileGname = fileG.getName();
                
                            if(fileGname.endsWith(".yum")){
                                if(!fileGname.split("[.]")[0].replace(" ","").equals("")) {
                                    if(!fileG.exists())
                                        guardarArch(fileG, compF);  
                                    else {
                                        int z = JOptionPane.showConfirmDialog(compF, "Ya hay un archivo con este nombre, ¿desea "
                                                                         +"sobreescribirlo?", "Sobreescribir archivo", 2);
                                        if(z == 0)
                                            guardarArch(fileG, compF);  
                                        else {                                    
                                        }
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(compF, "Escriba un nombre válido para el archivo",
                                                              "Nombre inválido", 2);
                                    return false;
                                }
                            } else {
                                JOptionPane.showMessageDialog(compF, "El archivo debe de tener la extensión '.yam'",
                                                          "Extensión inválida", 2); 
                                return false;
                            }
                        }                    
                    } else {
                        compF.jTPCode.setText("");
                        compF.setTitle("[#YURIDE]");
                    }
                } else {
                    int z = JOptionPane.showConfirmDialog(compF, "Ya hay un archivo con este nombre, ¿desea "
                                                           + "sobreescribirlo?", "Sobreescribir archivo", 2);
                    if(z == 0)
                        guardarArch(fileG, compF); 
                }               
        } else {
            compF.jTPCode.setText("");
            compF.setTitle("[#YURIDE]");
        }
        return true;
    }   
    
    public void guardarArch(File file, IDE compF)
    {
        boolean save = saveFile(file, compF.jTPCode.getText());
        // Esta línea llama al método saveFile para guardar el contenido del archivo en compF.jTPCodigo.getText(). 
        // El resultado se guarda en una variable booleana llamada save.
        if(save)
            compF.setTitle(file.getName());
        else
            JOptionPane.showMessageDialog(compF, "No se pudo guardar el archivo",
                                "Error desconocido", 2); 
    }
        
    public void Nuevo(IDE compF)
    { 
        file = selecFile.getSelectedFile();
        
        if(compF.getTitle().contains("*")) {
            // Esta línea verifica si el título de la ventana del editor de texto contiene un asterisco "*", lo que indica que el archivo actual se ha editado pero no se ha guardado.
            if(guardarEditNuevo(file, selecFile, compF)) {
                // Si el archivo actual se ha editado y no se ha guardado, se llama al método "guardarEditNuevo" que intenta guardarlo y 
                // devuelve un valor booleano que indica si la operación fue exitosa o no.
                compF.setTitle("[#YURIDE]");
                compF.jTPCode.setText("");
                // Esta línea crea un nuevo objeto "JFileChooser" para permitir al usuario seleccionar un nuevo archivo.
                selecFile = new JFileChooser();
                // Esta línea establece la variable "file" en "null" para indicar que no hay ningún archivo abierto actualmente en la aplicación.
                file = null;
            }
        } else {
            compF.setTitle("[#YURIDE]");
            compF.jTPCode.setText("");
            selecFile = new JFileChooser();
            file = null;
        }
    }    
    
    public boolean Abrir(IDE compF)
    {       
        if(compF.getTitle().contains("*")) {
            if(guardarEditAbrir(file, selecFile, compF)) {
                selecFile = new JFileChooser();
                file = null;
            }
        }
        
        JFileChooser tSelecFile = new JFileChooser();       
        tSelecFile.setFileFilter(filter);
        
        File tFile;
        if(tSelecFile.showDialog(compF, "Abrir") == JFileChooser.APPROVE_OPTION) {
            tFile = tSelecFile.getSelectedFile();
            String filename = tFile.getName();
             
            if(filename.endsWith(".yum")) {
                if(!filename.split("[.]")[0].replace(" ","").equals("")) {
                    if(!tFile.exists())
                        JOptionPane.showMessageDialog(compF, "El archivo que se desea abrir no existe en el directorio especificado",
                                                     "Archivo no encontrado", 2);      
                    else {
                        String t = getTextFile(tFile);                          
                        if(t != null) {
                            compF.jTPCode.setText(t);
                            compF.setTitle(tFile.getName());
                            compF.clearAllComp();
                            selecFile = tSelecFile;
                            file = tFile;
                        } else {
                            JOptionPane.showMessageDialog(compF, "Error al leer el archivo",
                                                         "Error desconocido", 2);
                            return false;
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(compF, "Escriba un nombre válido para el archivo",
                                                  "Nombre inválido", 2); 
                    return false;
                }
            } else {
                JOptionPane.showMessageDialog(compF, "El archivo debe tener la extensión '.yum'",
                                             "Extensión inválida", 2);  
                return false;
            }
        } else
            return false;
        return true;
    }
    
    public boolean Importar(IDE compF)
    {       
        if(compF.getTitle().contains("*")) {
            if(guardarEditAbrir(file, selecFile, compF)) {
                selecFile = new JFileChooser();
                file = null;
            }
        }
        JFileChooser tSelecFile = new JFileChooser();
        File tFile;
        if(tSelecFile.showDialog(compF, "Abrir") == JFileChooser.APPROVE_OPTION) {
            tFile = tSelecFile.getSelectedFile();
            String filename = tFile.getName();
            if(!filename.split("[.]")[0].replace(" ","").equals("")) {
                if(!tFile.exists())
                    JOptionPane.showMessageDialog(compF, "El archivo que se desea abrir no existe en el directorio especificado",
                                    "Archivo no encontrado", 2);      
                else {
                    String t = getTextFile(tFile);                           
                    if(t != null) {
                        compF.jTPCode.setText(t);
                        compF.setTitle(tFile.getName());
                        compF.clearAllComp();
                        selecFile = tSelecFile;
                        file = tFile;
                    }
                    else {
                        JOptionPane.showMessageDialog(compF, "Error al leer el archivo",
                                "Error desconocido", 2);
                        return false;
                    }
                }
            } else {
                JOptionPane.showMessageDialog(compF, "Escriba un nombre válido para el archivo",
                                        "Nombre inválido", 2); 
                return false;
            }
        } else
            return false;
        return true;
    }
    
    public boolean Guardar(IDE compF)
    {
        if(file != null)
            guardarArch(file, compF);
        else {
            JFileChooser tSelecFile = new JFileChooser();
            tSelecFile.setFileFilter(filter);
            File tFile;
            
            /*Si la variable "file" es diferente de "null", se llama al método "guardarArch" con "file" y "compF" como parámetros. 
            Esto significa que el archivo ya ha sido guardado previamente y se actualizará con los cambios realizados. Si la variable 
            "file" es igual a "null", significa que el archivo aún no ha sido guardado, por lo que se crea un nuevo objeto "JFileChooser"
            y se muestra el cuadro de diálogo "Guardar". Si el usuario selecciona un archivo y hace clic en "Guardar", se crea un objeto 
            "File" y se almacena en la variable "tFile".*/
            
            if(tSelecFile.showDialog(compF, "Guardar") == JFileChooser.APPROVE_OPTION) {
                tFile = tSelecFile.getSelectedFile();
                String filename = tFile.getName();
                
                if(filename.endsWith(".yum")) {
                    if(!filename.split("[.]")[0].replace(" ","").equals("")) {
                        if(!tFile.exists()) {
                            guardarArch(tFile, compF);  
                            file = tFile;
                            selecFile = tSelecFile;
                        } else {
                            int x = JOptionPane.showConfirmDialog(compF, "Ya hay un archivo con este nombre, ¿desea "
                                                                +"sobreescribirlo?", "Sobreescribir archivo", 2);
                            if(x == 0) {
                                guardarArch(tFile, compF); 
                                file = tFile;
                                selecFile = tSelecFile;
                            } else {
                                selecFile = new JFileChooser();
                                file = null;
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(compF, "Escriba un nombre válido para el archivo",
                                                  "Nombre inválido", 2);
                        selecFile = new JFileChooser();
                        file = null;
                        return false;
                    }
                } else {
                    JOptionPane.showMessageDialog(compF, "El archivo debe tener la extensión '.yum'",
                                                  "Extensión inválida", 2); 
                    selecFile = new JFileChooser();
                    file = null;
                    return false;
                }
            } else 
                return false;
        }
        return true;
    }
    
    public void guardarC(IDE compF)
    {
        JFileChooser tSelecFile = new JFileChooser();
        // Se define un objeto JFileChooser para mostrar el cuadro de diálogo de selección de archivo.
        tSelecFile.setFileFilter(filter);
        if(tSelecFile.showDialog(compF, "Guardar como") == JFileChooser.APPROVE_OPTION) {
            // Se muestra el cuadro de diálogo de selección de archivo y se verifica que el usuario haya seleccionado la opción "Guardar como":
            File tFile;
            tFile = tSelecFile.getSelectedFile();
            String filename = tFile.getName();
                
            if(filename.endsWith(".yum")) {
                if(!filename.split("[.]")[0].replace(" ","").equals("")) {
                    guardarArch(tFile, compF);  
                    file = tFile;
                    selecFile = tSelecFile;
                } else
                    JOptionPane.showMessageDialog(compF, "Escriba un nombre válido para el archivo",
                                                 "Nombre inválido", 2); 
            } else
                JOptionPane.showMessageDialog(compF, "El archivo debe tener la extensión '.yum'",
                                             "Extensión inválida", 2);  
        }
    }
}
