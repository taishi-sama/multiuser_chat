import com.google.gson.GsonBuilder;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static java.lang.System.exit;

public class ClientMain {
    public static void main(String[] args) throws IOException {
        Terminal term = new DefaultTerminalFactory().createTerminal();
        Screen screen = new TerminalScreen(term);
        WindowBasedTextGUI gui = new MultiWindowTextGUI(screen);
        var w = new ChatWindow(gui);

        gui.addWindow(w);
        screen.startScreen();
        try {
            while (w.getTextGUI() != null) {
                w.getTextGUI().getGUIThread().processEventsAndUpdate();
                w.UpdateTick();
            }
        }
        finally {
            w.close();
            term.close();
            System.exit(0);
        }


    }
}
