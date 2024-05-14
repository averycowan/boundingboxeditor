package com.averycowan.boundingboxeditor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.Collectors;

public class DrawingFrame extends JFrame {
    public static final String WORK_DIR = System.getProperty("user.home") + File.separator + "Pictures" + File.separator + "BoundingBoxEditor" + File.separator;
    public static final String IMAGE_DIR = WORK_DIR + "images" + File.separator;
    public static final String LABEL_DIR = WORK_DIR + "labels" + File.separator;

    public static final String LABEL_NAMES_FILE = WORK_DIR + "label_names.txt";


    public static final HashMap<String, String> LABEL_NAMES = new HashMap<>();

    private static void registerLabel(String label) {
        String name = label
                .replace(' ', '_')
                .replace('/', '_')
                .replace('-', '_');
        LABEL_NAMES.put(name, label);
    }


    static {
        try {
            Scanner label_names_file = new Scanner(new File(LABEL_NAMES_FILE));
            while (label_names_file.hasNextLine()) {
                registerLabel(label_names_file.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.err.println("Could not find label names file in " + LABEL_NAMES_FILE + "\nExiting...");
            System.exit(1);
        }
    }

    private Image image = null;

    private String image_title = "";
    private String image_label = "";

    float scale = 1.0f;
    private final ArrayList<BBox> bboxs = new ArrayList<>();

    private boolean dragging = false;
    private int mouse_x = 0;
    private int mouse_y = 0;

    private final JDragPanel foreground;

    private void reset_mouse() {
        dragging = false;
        mouse_x = 0;
        mouse_y = 0;
    }

    private static class KeyListener implements java.awt.event.KeyListener {

        DrawingFrame frame;

        public KeyListener(DrawingFrame frame) {
            super();
            this.frame = frame;
        }

        @Override
        public void keyTyped(KeyEvent e) {
            System.out.println("Key typed: " + e.getKeyChar() + (int) e.getKeyChar());
            switch ((int) e.getKeyChar()) {
                case 15 -> {
                    System.out.println("Open");
                    frame.open();
                    e.consume();
                }
                case 19 -> { // Ctrl-S
                    System.out.println("Save");
                    frame.save();
                    e.consume();
                }
                case 26 -> {
                    System.out.println("Undo");
                    frame.undo();
                    e.consume();
                }
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    }

    public static class DrawListener implements java.awt.event.MouseListener {

        private final DrawingFrame frame;

        public DrawListener(DrawingFrame frame) {
            super();
            this.frame = frame;
        }

        private void reset() {
            frame.reset_mouse();
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            frame.mouse_x = e.getX();
            frame.mouse_y = e.getY();
            System.out.println("Mouse pressed: " + frame.mouse_x + ", " + frame.mouse_y);
            frame.dragging = true;
            e.consume();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            int xmin = Math.min(frame.mouse_x, e.getX());
            int ymin = Math.min(frame.mouse_y, e.getY());
            int xmax = Math.max(frame.mouse_x, e.getX());
            int ymax = Math.max(frame.mouse_y, e.getY());
            System.out.println("Mouse released: " + e.getX() + ", " + e.getY());
            frame.add_bbox(xmin, ymin, xmax, ymax);
            e.consume();
            reset();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }

    public class JImagePanel extends JPanel {
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (image != null) {
                g.drawImage(image, 0, 0, null);
            }
        }
    }

    public class JDragPanel extends JPanel {
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (bboxs != null) {
                for (BBox bbox : bboxs) {
                    int x = (int) (bbox.xmin * scale);
                    int y = (int) (bbox.ymin * scale);
                    int width = (int) ((bbox.xmax - bbox.xmin) * scale);
                    int height = (int) ((bbox.ymax - bbox.ymin) * scale);
                    g.drawRect(x, y, width, height);
                }
            }
            if (dragging) {
                Point mouse_coords = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(mouse_coords, this);
                int x_min = Math.min(mouse_x, mouse_coords.x);
                int y_min = Math.min(mouse_y, mouse_coords.y);
                int x_max = Math.max(mouse_x, mouse_coords.x);
                int y_max = Math.max(mouse_y, mouse_coords.y);
                System.out.println("Dragging from " + mouse_x + ", " + mouse_y + " to " + mouse_coords.x + ", " + mouse_coords.y);
                g.drawRect(x_min, y_min, x_max - x_min, y_max - y_min);
            }
        }
    }

    public DrawingFrame() {
        super();
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 255));
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = (int) (screensize.height * 0.9);
        int width = (int) (screensize.width * 0.9);
        screensize = new Dimension(width, height);
        setSize(screensize);
        JLayeredPane panel = new JLayeredPane();
        panel.setSize(screensize);
        JImagePanel background = new JImagePanel();
        background.setSize(screensize);
        background.setBackground(new Color(0, 0, 0, 127));
        background.setLocation(0, 0);
        panel.setLayer(background, JLayeredPane.DEFAULT_LAYER);
        panel.add(background, JLayeredPane.DEFAULT_LAYER);
        foreground = new JDragPanel();
        foreground.setSize(screensize);
        foreground.setOpaque(false);
        foreground.setLocation(0, 0);
        panel.setLayer(foreground, JLayeredPane.DRAG_LAYER);
        panel.add(foreground, JLayeredPane.DRAG_LAYER);
        panel.validate();
        KeyListener key_listener = new KeyListener(this);
        addKeyListener(key_listener);
        DrawListener draw_listener = new DrawListener(this);
        addMouseListener(draw_listener);
        add(panel);
        setVisible(true);
        new Timer(1000 / 60, e -> foreground.repaint()).start();

    }

    private void save() {
        String path = LABEL_DIR + image_title + "+" + image_label + ".csv";
        System.out.println("Saving to " + path);
        String data = bboxs.stream().map(BBox::toString).collect(Collectors.joining("\n"));
        System.out.println(data);
        try {
            Files.writeString(Path.of(path), data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void open() {
        System.out.println("Opening");
        try {
            String contents = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
            System.out.println("Clipboard contents: " + contents);
            if (contents.contains("/"))
                throw new RuntimeException("Invalid image name, uses '/' " + contents);
            if (!contents.contains("+"))
                throw new RuntimeException("Invalid image name, no '+' " + contents);
            String[] name_parts = contents.split("\\+");
            if (name_parts.length > 3)
                throw new RuntimeException("Invalid image name, too many '+' " + contents);
            image_title = name_parts[0];
            image_label = name_parts[1];

            open_image(image_title + ".jpg");
        } catch (UnsupportedFlavorException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void undo() {
        System.out.println("Undoing");
        if (bboxs.size() > 0) {
            bboxs.remove(bboxs.size() - 1);
            repaint();
        }
    }

    public void open_image(String name) {
        System.out.println("Opening image: " + name);
        String path = IMAGE_DIR + name;
        try {
            this.bboxs.clear();
            this.image = null;
            BufferedImage image = ImageIO.read(new File(path));
            int height = image.getHeight(null);
            int width = image.getWidth(null);
            scale = Math.min((float) getHeight() / height, (float) getWidth() / width);
            System.out.println("Scale: " + scale);
            this.image = image.getScaledInstance((int) (width * scale), (int) (height * scale), Image.SCALE_SMOOTH);
            repaint();
        } catch (IOException e) {
            System.out.println("Error loading image: " + path);
            System.out.println(e.toString());
            throw new RuntimeException(e);
        }
    }

    public void add_bbox(int xmin, int ymin, int xmax, int ymax) {
        BBox bbox = new BBox((int) (xmin / scale), (int) (ymin / scale), (int) (xmax / scale), (int) (ymax / scale), LABEL_NAMES.get(image_label));
        System.out.println("Adding bbox: " + bbox);
        bboxs.add(bbox);
        repaint();
    }
}
