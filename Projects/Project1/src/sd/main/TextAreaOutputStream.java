package sd.main;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.io.OutputStream;
import java.io.IOException;

public class TextAreaOutputStream extends OutputStream {
    private JTextArea textArea;
    
    public TextAreaOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }
    
    @Override
    public void write(int b) throws IOException {
        SwingUtilities.invokeLater(() -> {
            textArea.append(String.valueOf((char) b));
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }
}