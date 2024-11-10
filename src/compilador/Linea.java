package compilador;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 *
 * @author juanp
 */
public class Linea extends JPanel
        implements CaretListener, DocumentListener, PropertyChangeListener {

    public final static float LEFT = 0.0f;
    public final static float CENTER = 0.5f;
    public final static float RIGHT = 1.0f;

    private final static Border OUTER = new MatteBorder(0, 0, 0, 2, new Color(36,38,48));

    private final static int HEIGHT = Integer.MAX_VALUE - 1000000;

    //  Text component this TextTextLineNumber component is in sync with
    private JTextComponent component;

    //  Properties that can be changed
    private boolean updateFont;
    private int borderGap;
    private Color currentLineForeground;
    private float digitAlignment;
    private int minimumDisplayDigits;

    //  Keep history information to reduce the number of times the component
    //  needs to be repainted
    private int lastDigits;
    private int lastHeight;
    private int lastLine;

    private HashMap<String, FontMetrics> fonts;

    /**
     * Create a line number component for a text component. This minimum display
     * width will be based on 3 digits.
     *
     * @param component the related text component
     */
    public Linea(JTextComponent component) {
        this(component, 3);
    }

    /**
     * Create a line number component for a text component.
     *
     * @param component the related text component
     * @param minimumDisplayDigits the number of digits used to calculate the
     * minimum width of the component
     */
    public Linea(JTextComponent component, int minimumDisplayDigits) {
        this.component = component;

        setFont(component.getFont());

        setBorderGap(5);
        setCurrentLineForeground(Color.RED);
        setDigitAlignment(RIGHT);
        setMinimumDisplayDigits(minimumDisplayDigits);

        component.getDocument().addDocumentListener(this);
        component.addCaretListener(this);
        component.addPropertyChangeListener("font", this);
    }
    
    /*Este código de Java define el constructor para la clase Linea. Esta clase parece ser una
    extensión de JTextComponent que agrega la funcionalidad de mostrar el número de línea en la que se encuentra el cursor en un color diferente.
Los parámetros del constructor son component y minimumDisplayDigits. component es un objeto de 
    tipo JTextComponent que representa el componente de texto para el que se mostrarán las líneas numeradas
    . minimumDisplayDigits es un entero que representa el número mínimo de dígitos que se utilizarán para mostrar el número de línea.
Dentro del constructor, la variable component se inicializa con el valor del parámetro component.
    Luego se establece la fuente del componente a través del método setFont().
A continuación, se establecen varias propiedades del objeto Linea utilizando los métodos correspondientes:
setBorderGap() establece la separación entre el borde del componente y el número de línea.
setCurrentLineForeground() establece el color del número de línea de la línea actual en rojo.
setDigitAlignment() establece la alineación del número de línea a la derecha.
setMinimumDisplayDigits() establece el número mínimo de dígitos que se utilizarán para mostrar el número de línea.
Finalmente, se agregan varios listeners (escuchadores) al componente component para que la clase Linea pueda
    realizar las acciones necesarias cuando se produzcan ciertos eventos, como cuando se cambia el contenido
    del documento, se mueve el cursor o se cambia la fuente del texto. En particular, se agregan los siguientes listeners:
addDocumentListener() agrega un DocumentListener para detectar los cambios en el contenido del documento.
addCaretListener() agrega un CaretListener para detectar los cambios en la posición del cursor.
addPropertyChangeListener() agrega un PropertyChangeListener para detectar los cambios en la propiedad de fuente del texto.
*/

    /**
     * Gets the update font property
     *
     * @return the update font property
     */
    public boolean getUpdateFont() {
        return updateFont;
    }

    /**
     * Set the update font property. Indicates whether this Font should be
     * updated automatically when the Font of the related text component is
     * changed.
     *
     * @param updateFont when true update the Font and repaint the line numbers,
     * otherwise just repaint the line numbers.
     */
    public void setUpdateFont(boolean updateFont) {
        this.updateFont = updateFont;
    }

    /**
     * Gets the border gap
     *
     * @return the border gap in pixels
     */
    public int getBorderGap() {
        return borderGap;
    }

    /**
     * The border gap is used in calculating the left and right insets of the
     * border. Default value is 5.
     *
     * @param borderGap the gap in pixels
     */
    public void setBorderGap(int borderGap) {
        this.borderGap = borderGap;
        Border inner = new EmptyBorder(0, borderGap, 0, borderGap);
        setBorder(new CompoundBorder(OUTER, inner));
        lastDigits = 0;
        setPreferredWidth();
    }
/*
Este código de Java define el método setBorderGap en una clase que parece ser una extensión de un componente de texto en Swing.
Este método establece la separación entre el borde del componente y el número de línea que se muestra en la parte izquierda del componente.
El parámetro borderGap es un entero que representa la cantidad de píxeles de separación que se desea tener entre el borde del componente y el número de línea.
Dentro del método, la variable borderGap de la clase se establece en el valor del parámetro borderGap. A continuación, se crea
un nuevo borde interior (inner) utilizando la clase EmptyBorder, que crea un borde vacío con una separación determinada en
cada uno de sus lados. En este caso, se establece la separación en cero para la parte superior e inferior del componente y en borderGap píxeles para la parte izquierda y derecha.
Luego se crea un nuevo borde compuesto (CompoundBorder) utilizando el borde exterior definido en otra parte del código (OUTER)
y el borde interior recién creado (inner). Este nuevo borde compuesto se establece como el borde del componente utilizando el método setBorder.
Se establece lastDigits a cero, que probablemente se utiliza en otro método relacionado con el número de dígitos que se muestran en el número de línea.
Por último, se llama al método setPreferredWidth() que probablemente se utiliza para establecer el ancho preferido del componente en función de su contenido.
*/
    
    
    /**
     * Gets the current line rendering Color
     *
     * @return the Color used to render the current line number
     */
    public Color getCurrentLineForeground() {
        return currentLineForeground == null ? getForeground() : currentLineForeground;
    }
    
    /*Este código de Java define el método getCurrentLineForeground() en una clase que parece ser una extensión de un componente de texto en Swing.
    Este método devuelve el color que se utiliza para resaltar el número de línea correspondiente a la línea actual en el componente de texto.
La variable currentLineForeground es un objeto de tipo Color que representa el color que se utiliza para resaltar el número de línea 
    correspondiente a la línea actual. Si currentLineForeground no es null, el método devuelve currentLineForeground. En caso contrario,
    devuelve el color del texto predeterminado (getForeground()).
La expresión return currentLineForeground == null ? getForeground() : currentLineForeground; utiliza el operador ternario (? :) 
    para determinar qué valor se debe devolver. Si currentLineForeground es null, se devuelve getForeground(). De lo contrario, 
    se devuelve currentLineForeground. Esto se utiliza para proporcionar un valor predeterminado en caso de que no se haya establecido
    un color de resaltado para la línea actual*/

    /**
     * The Color used to render the current line digits. Default is Coolor.RED.
     *
     * @param currentLineForeground the Color used to render the current line
     */
    public void setCurrentLineForeground(Color currentLineForeground) {
        this.currentLineForeground = currentLineForeground;
    }
/*
    Este código de Java define el método setCurrentLineForeground() en una clase que parece ser una extensión de un componente
    de texto en Swing. Este método establece el color que se utiliza para resaltar el número de 
    línea correspondiente a la línea actual en el componente de texto.
El parámetro currentLineForeground es un objeto de tipo Color que representa el color que se desea utilizar para resaltar el número
    de línea correspondiente a la línea actual.
Dentro del método, la variable de instancia currentLineForeground se establece en el valor del parámetro currentLineForeground.
    Esto significa que, al llamar a este método y pasar un objeto Color como parámetro, se cambiará el color que se utiliza para
    resaltar el número de línea correspondiente a la línea actual en el componente de texto.
En resumen, este método proporciona una forma de establecer dinámicamente el color de resaltado para el número de línea correspondiente
    a la línea actual en un componente de texto en Swing.

    */
    
    
    /**
     * Gets the digit alignment
     *
     * @return the alignment of the painted digits
     */
    public float getDigitAlignment() {
        return digitAlignment;
    }

    /**
     * Specify the horizontal alignment of the digits within the component.
     * Common values would be:
     * <ul>
     * <li>TextLineNumber.LEFT
     * <li>TextLineNumber.CENTER
     * <li>TextLineNumber.RIGHT (default)
     * </ul>
     *
     * @param currentLineForeground the Color used to render the current line
     */
    public void setDigitAlignment(float digitAlignment) {
        this.digitAlignment
                = digitAlignment > 1.0f ? 1.0f : digitAlignment < 0.0f ? -1.0f : digitAlignment;
    }
    
    /*Este código de Java define el método setDigitAlignment en una clase que parece ser una extensión de un componente de texto en Swing.
    Este método establece la alineación horizontal del número de línea que se muestra en el componente.
El parámetro digitAlignment es un número en punto flotante que representa la alineación horizontal que se desea establecer para el
    número de línea. Si el valor es mayor que 1.0, se establece en 1.0, si el valor es menor que 0.0, se establece en -1.0, de lo contrario,
    se establece en el valor proporcionado.
    */

    /**
     * Gets the minimum display digits
     *
     * @return the minimum display digits
     */
    public int getMinimumDisplayDigits() {
        return minimumDisplayDigits;
    }

    /**
     * Specify the mimimum number of digits used to calculate the preferred
     * width of the component. Default is 3.
     *
     * @param minimumDisplayDigits the number digits used in the preferred width
     * calculation
     */
    public void setMinimumDisplayDigits(int minimumDisplayDigits) {
        this.minimumDisplayDigits = minimumDisplayDigits;
        setPreferredWidth();
    }
    /*Este código de Java define el método setMinimumDisplayDigits en una clase que parece ser una extensión de un
    componente de texto en Swing. Este método establece el número mínimo de dígitos que se muestran para el número de línea en el componente de texto.
El parámetro minimumDisplayDigits es un número entero que representa el número mínimo de dígitos que se muestran
    para el número de línea. Si este valor es mayor que el número de dígitos necesarios para representar el número
    de línea más alto en el componente, se mostrarán ceros a la izquierda para rellenar el espacio.*/

    /**
     * Calculate the width needed to display the maximum line number
     */
    private void setPreferredWidth() {
        Element root = component.getDocument().getDefaultRootElement();
        int lines = root.getElementCount();
        int digits = Math.max(String.valueOf(lines).length(), minimumDisplayDigits);

        //  Update sizes when number of digits in the line number changes
        if (lastDigits != digits) {
            lastDigits = digits;
            FontMetrics fontMetrics = getFontMetrics(getFont());
            int width = fontMetrics.charWidth('0') * digits;
            Insets insets = getInsets();
            int preferredWidth = insets.left + insets.right + width;

            Dimension d = getPreferredSize();
            d.setSize(preferredWidth, HEIGHT);
            setPreferredSize(d);
            setSize(d);
        }
    }
    
    /*
    Este código de Java define el método setPreferredWidth en una clase que parece ser una extensión de un componente de texto en Swing.
    Este método se utiliza para establecer el ancho preferido del componente de texto en función del número mínimo de dígitos y otros parámetros
    configurados previamente.
Dentro del método, primero se obtiene el elemento raíz del documento del componente de texto. A continuación, se calcula el número de líneas
    en el documento y se determina el número de dígitos necesarios para representar el número de línea más alto. El número de dígitos se establece
    en digits, que es el máximo entre el número de dígitos necesarios para representar el número de línea más alto y el número mínimo de dígitos
    configurado previamente.
Luego, se comprueba si el número de dígitos ha cambiado desde la última vez que se actualizó el ancho preferido. Si es así, se actualiza el ancho
    preferido del componente de texto en función del número de dígitos. Para hacerlo, se utiliza la métrica de fuente para calcular el ancho de un
    solo dígito y se multiplica por el número de dígitos. Luego, se suman los márgenes del componente y el ancho calculado para obtener el ancho preferido.
Finalmente, se establece la dimensión preferida del componente de texto en función del ancho preferido y de la altura preestablecida, se establece
    la dimensión del componente en la dimensión preferida y se actualiza la UI del componente.
En resumen, este método se utiliza para garantizar que el ancho del componente de texto sea suficiente para mostrar el número de línea y otros 
    elementos necesarios, y para actualizar el ancho del componente cuando sea necesario.

    */

    /**
     * Draw the line numbers
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //	Determine the width of the space available to draw the line number
        FontMetrics fontMetrics = component.getFontMetrics(component.getFont());
        Insets insets = getInsets();
        int availableWidth = getSize().width - insets.left - insets.right;

        //  Determine the rows to draw within the clipped bounds.
        Rectangle clip = g.getClipBounds();
        int rowStartOffset = component.viewToModel(new Point(0, clip.y));
        int endOffset = component.viewToModel(new Point(0, clip.y + clip.height));

        while (rowStartOffset <= endOffset) {
            try {
                if (isCurrentLine(rowStartOffset)) {
                    g.setColor(getCurrentLineForeground());
                } else {
                    g.setColor(getForeground());
                }

                //  Get the line number as a string and then determine the
                //  "X" and "Y" offsets for drawing the string.
                String lineNumber = getTextLineNumber(rowStartOffset);
                int stringWidth = fontMetrics.stringWidth(lineNumber);
                int x = getOffsetX(availableWidth, stringWidth) + insets.left;
                int y = getOffsetY(rowStartOffset, fontMetrics);
                g.drawString(lineNumber, x, y);

                //  Move to the next row
                rowStartOffset = Utilities.getRowEnd(component, rowStartOffset) + 1;
            } catch (Exception e) {
                break;
            }
        }
    }
    /*El método "paintComponent" es responsable de dibujar los números de línea en el componente. Primero, se llama al método 
    "paintComponent" de la superclase para que se puedan pintar los componentes base.
Luego, se calcula el ancho disponible para dibujar los números de línea restando los márgenes del componente. 
    A continuación, se determinan las filas que deben dibujarse dentro de los límites recortados.
Para cada fila, se comprueba si es la fila actual. Si es así, se establece el color de la fuente en el 
    color de línea actual, de lo contrario se establece en el color de fuente predeterminado.
Luego, se obtiene el número de línea como una cadena y se calculan los desplazamientos "X" e "Y" para dibujar la cadena.
Finalmente, se llama al método "drawString" para dibujar el número de línea y se mueve a la siguiente fila 
    hasta que se hayan dibujado todas las filas dentro de los límites recortados. Si se produce una excepción, se sale del bucle.*/

    /*
	 *  We need to know if the caret is currently positioned on the line we
	 *  are about to paint so the line number can be highlighted.
     */
    private boolean isCurrentLine(int rowStartOffset) {
        int caretPosition = component.getCaretPosition();
        Element root = component.getDocument().getDefaultRootElement();

        if (root.getElementIndex(rowStartOffset) == root.getElementIndex(caretPosition)) {
            return true;
        } else {
            return false;
        }
    }

    /*Este método comprueba si la línea actual es la línea en la que se encuentra el cursor de la posición actual en el componente de texto.
Primero, se obtiene la posición del cursor con component.getCaretPosition(). Luego, se obtiene el elemento raíz del documento del componente de texto
    con component.getDocument().getDefaultRootElement(). A continuación, se utiliza root.getElementIndex(rowStartOffset) y root.getElementIndex(caretPosition)
    para obtener los índices de los elementos que contienen la posición inicial de la fila y la posición actual del cursor, respectivamente. Si estos índices 
    son iguales, entonces el cursor se encuentra en la misma línea que la posición de inicio de la fila y se devuelve true. De lo contrario, se devuelve false.*/
    
    /*
	 *	Get the line number to be drawn. The empty string will be returned
	 *  when a line of text has wrapped.
     */
    protected String getTextLineNumber(int rowStartOffset) {
        Element root = component.getDocument().getDefaultRootElement();
        int index = root.getElementIndex(rowStartOffset);
        Element line = root.getElement(index);

        if (line.getStartOffset() == rowStartOffset) {
            return String.valueOf(index + 1);
        } else {
            return "";
        }
    }

   /*Este método se utiliza para obtener el número de línea correspondiente a la posición inicial de una fila dada en el
    componente de texto. Primero, se obtiene el elemento raíz del documento del componente de texto y se busca el índice 
    del elemento que contiene el desplazamiento inicial de la fila dada. Luego, se obtiene el elemento de esa fila y se verifica
    si su desplazamiento inicial coincide con el desplazamiento inicial de la fila dada. Si es así, devuelve el número de línea
    como una cadena (el índice de elemento más uno, ya que las líneas se numeran desde 1). Si no es así, devuelve una cadena vacía.
    Este método se utiliza en el método paintComponent para dibujar los números de línea correspondientes en la columna izquierda del componente de texto.
*/ 
    
    /*
	 *  Determine the X offset to properly align the line number when drawn
    El método getOffsetX calcula el desplazamiento horizontal necesario para centrar el número de línea en el área disponible para dibujar. Toma dos argumentos: el ancho disponible para dibujar y el ancho del número de línea. El método multiplica la diferencia entre 
    estos dos valores por el valor digitAlignment, que se establece mediante el método setDigitAlignment.
     */
    private int getOffsetX(int availableWidth, int stringWidth) {
        return (int) ((availableWidth - stringWidth) * digitAlignment);
    }

    /*
	 *  Determine the Y offset for the current row
     */
    private int getOffsetY(int rowStartOffset, FontMetrics fontMetrics)
            throws BadLocationException {
        //  Get the bounding rectangle of the row

        Rectangle r = component.modelToView(rowStartOffset);
        int lineHeight = fontMetrics.getHeight();
        int y = r.y + r.height;
        int descent = 0;

        //  The text needs to be positioned above the bottom of the bounding
        //  rectangle based on the descent of the font(s) contained on the row.
        if (r.height == lineHeight) // default font is being used
        {
            descent = fontMetrics.getDescent();
        } else // We need to check all the attributes for font changes
        {
            if (fonts == null) {
                fonts = new HashMap<String, FontMetrics>();
            }

            Element root = component.getDocument().getDefaultRootElement();
            int index = root.getElementIndex(rowStartOffset);
            Element line = root.getElement(index);

            for (int i = 0; i < line.getElementCount(); i++) {
                Element child = line.getElement(i);
                AttributeSet as = child.getAttributes();
                String fontFamily = (String) as.getAttribute(StyleConstants.FontFamily);
                Integer fontSize = (Integer) as.getAttribute(StyleConstants.FontSize);
                String key = fontFamily + fontSize;

                FontMetrics fm = fonts.get(key);

                if (fm == null) {
                    Font font = new Font(fontFamily, Font.PLAIN, fontSize);
                    fm = component.getFontMetrics(font);
                    fonts.put(key, fm);
                }

                descent = Math.max(descent, fm.getDescent());
            }
        }

        return y - descent;
    }
/*
    Este método se utiliza para obtener el desplazamiento vertical para dibujar el número de línea en relación con la posición de la línea de 
    texto correspondiente en la interfaz de usuario. El método acepta dos parámetros: el primer parámetro, rowStartOffset, es el desplazamiento
    del modelo donde comienza la línea actual y el segundo parámetro, fontMetrics, es un objeto FontMetrics que contiene información de métricas
    de fuente como la altura de línea.
Primero, se obtiene un rectángulo que representa la posición y el tamaño de la línea actual llamando al método modelToView() del componente 
    en la posición rowStartOffset. La altura de línea se obtiene del objeto FontMetrics y se almacena en la variable lineHeight. La variable
    y se inicializa como la suma de la posición y y la altura height del rectángulo, lo que da la posición vertical en la que se dibujará el número de línea.
Si la altura del rectángulo es igual a la altura de línea, se usa la fuente predeterminada del componente y se llama al método getDescent()
    en el objeto FontMetrics para obtener la cantidad de espacio debajo de la línea de base. Si no es así, se usa una fuente diferente, lo que
    significa que el texto de la línea de texto puede tener diferentes tamaños de fuente y estilos en diferentes partes de la línea. En este caso,
    se crea un mapa HashMap llamado fonts para almacenar las métricas de fuente para cada fuente utilizada. Luego, para cada elemento en la línea actual,
    se obtienen los atributos de estilo de fuente, como la familia de fuente y el tamaño de fuente. Se usa una combinación de la familia y el tamaño de fuente 
    como clave en el mapa fonts para buscar el objeto FontMetrics correspondiente. Si no se encuentra el objeto FontMetrics, se crea uno nuevo y se almacena en el mapa fonts.
Se determina el descenso máximo de todas las métricas de fuente de los elementos de línea y se almacena en la variable descent. Finalmente, el desplazamiento
    vertical para dibujar el número de línea se calcula restando el descenso del valor de y. Este valor se devuelve como resultado del método.
    */
    
    
//
//  Implement CaretListener interface
//
    @Override
    public void caretUpdate(CaretEvent e) {
        //  Get the line the caret is positioned on

        int caretPosition = component.getCaretPosition();
        Element root = component.getDocument().getDefaultRootElement();
        int currentLine = root.getElementIndex(caretPosition);

        //  Need to repaint so the correct line number can be highlighted
        if (lastLine != currentLine) {
            repaint();
            lastLine = currentLine;
        }
    }

//
//  Implement DocumentListener interface
//
    @Override
    public void changedUpdate(DocumentEvent e) {
        documentChanged();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        documentChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        documentChanged();
    }

    /*
	 *  A document change may affect the number of displayed lines of text.
	 *  Therefore the lines numbers will also change.
     */
    private void documentChanged() {
        //  View of the component has not been updated at the time
        //  the DocumentEvent is fired

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    int endPos = component.getDocument().getLength();
                    Rectangle rect = component.modelToView(endPos);

                    if (rect != null && rect.y != lastHeight) {
                        setPreferredWidth();
                        repaint();
                        lastHeight = rect.y;
                    }
                } catch (BadLocationException ex) {
                    /* nothing to do */ }
            }
        });
    }

//
//  Implement PropertyChangeListener interface
//
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getNewValue() instanceof Font) {
            if (updateFont) {
                Font newFont = (Font) evt.getNewValue();
                setFont(newFont);
                lastDigits = 0;
                setPreferredWidth();
            } else {
                repaint();
            }
        }
    }
}
