/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paintbrush;

/**
 *
 * @author Mohamed Hosny
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Stack;
import static java.util.concurrent.ThreadLocalRandom.current;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;


public class PaintBrush extends JFrame implements MouseListener, MouseMotionListener {


     private JPanel drawingPanel;
    private JButton clearButton, undoButton, redoButton, lineButton;
    private JComboBox<String> shapeComboBox, styleComboBox;
   
  private JButton normalButton, filledButton, dottedButton;
    private JButton[] colorButtons;
    private JSlider thicknessSlider; // Slider for pencil thickness

    private ArrayList<Shape> shapes = new ArrayList<>();
    private Stack<ArrayList<Shape>> undoStack = new Stack<>();
    private Stack<ArrayList<Shape>> redoStack = new Stack<>();
    private Shape currentShape;
    private Color currentColor = Color.BLACK;
    private int startX, startY, endX, endY;
    private boolean isPencilMode = false; // Flag for pencil mode
    private float pencilThickness = 1.0f; // Default pencil thickness
    
    public PaintBrush() {
        setTitle("Paint Brush");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        drawingPanel = new DrawingPanel();
        drawingPanel.addMouseListener(this);
        drawingPanel.addMouseMotionListener(this);
        
         

        String[] shapeOptions = {"Line", "Rectangle", "Oval", "Triangle", "Pencil"};
        shapeComboBox = new JComboBox<>(shapeOptions);
        shapeComboBox.addActionListener(e -> {
            String selectedShape = (String) shapeComboBox.getSelectedItem();
            isPencilMode = "Pencil".equals(selectedShape);
        });

        String[] styleOptions = {"Normal", "Filled", "Dotted"};
        styleComboBox = new JComboBox<>(styleOptions);

        colorButtons = new JButton[6];
        Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.BLACK, Color.ORANGE};
        for (int i = 0; i < colorButtons.length; i++) {
            colorButtons[i] = new JButton();
            colorButtons[i].setBackground(colors[i]);
            colorButtons[i].addActionListener(e -> {
                currentColor = ((JButton) e.getSource()).getBackground();
            });
        }

        // thickness slider
        thicknessSlider = new JSlider(1, 20, 1); // Thickness range from 1 to 20
        thicknessSlider.addChangeListener(e -> pencilThickness = thicknessSlider.getValue());

        // clear button
        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clear());

        // undo button
        undoButton = new JButton("Undo");
        undoButton.addActionListener(e -> undo());

        // redo button
        redoButton = new JButton("Redo");
        redoButton.addActionListener(e -> redo());

        JPanel controlPanel = new JPanel();
        controlPanel.add(shapeComboBox);
        controlPanel.add(styleComboBox);
        controlPanel.add(clearButton);
        controlPanel.add(undoButton);
        controlPanel.add(redoButton);
        controlPanel.add(new JLabel("Colors:"));
        for (JButton button : colorButtons) {
            controlPanel.add(button);
        }
        controlPanel.add(new JLabel("Thickness:"));
        controlPanel.add(thicknessSlider);

        add(controlPanel, BorderLayout.NORTH);
        add(drawingPanel, BorderLayout.CENTER);

        pack();
        setVisible(true);
    }
      
  @Override
    public void mouseReleased(MouseEvent e) {
        if (isPencilMode) {
            endX = e.getX();
            endY = e.getY();
            shapes.add(new Line(currentColor, startX, startY, endX, endY, pencilThickness, "Normal")); // Add the last line segment
            drawingPanel.repaint();
        }
    }


     @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
    
     @Override
    public void mousePressed(MouseEvent e) {
        startX = e.getX();
        startY = e.getY();
        endX = startX;
        endY = startY;

        if (!isPencilMode) {
            String selectedShape = (String) shapeComboBox.getSelectedItem();
            String selectedStyle = (String) styleComboBox.getSelectedItem();
            switch (selectedShape) {
                case "Line":
                    currentShape = new Line(currentColor, startX, startY, endX, endY, pencilThickness, selectedStyle);
                    break;
                case "Rectangle":
                    currentShape = new Rectangle(currentColor, startX, startY, endX, endY, pencilThickness, selectedStyle);

                    break;
                case "Oval":
                    currentShape = new Oval(currentColor, startX, startY, endX, endY, pencilThickness, selectedStyle);
                    break;
                case "Triangle":
                    currentShape = new Triangle(currentColor, startX, startY, endX, endY, pencilThickness, selectedStyle);
                    break;
            }
            shapes.add(currentShape);
            saveToUndoStack();
        }
    }


   @Override
    public void mouseDragged(MouseEvent e) {
        endX = e.getX();
        endY = e.getY();

        if (isPencilMode) {
            shapes.add(new Line(currentColor, startX, startY, endX, endY, pencilThickness, "Normal")); // Draw line segments for pencil
            startX = endX;
            startY = endY;
            drawingPanel.repaint();
        } else {
            currentShape.setEndCoordinates(endX, endY);
            drawingPanel.repaint();
        }
    }

    private void saveToUndoStack() {
        undoStack.push(new ArrayList<>(shapes));
        redoStack.clear();
    }
     private void undo() {
        if (!shapes.isEmpty()) {
            ArrayList<Shape> lastShapes = new ArrayList<>(shapes);
            undoStack.push(lastShapes);
            shapes.remove(shapes.size() - 1);
            drawingPanel.repaint();
        }
    }

    private void redo() {
        if (!undoStack.isEmpty()) {
            ArrayList<Shape> lastShapes = undoStack.pop();
            shapes.add(lastShapes.get(lastShapes.size() - 1));
            drawingPanel.repaint();
        }
    }

    private void clear() {
        shapes.clear();
        undoStack.clear();
        redoStack.clear();
        drawingPanel.repaint();
    }
   private class DrawingPanel extends JPanel {
        DrawingPanel() {
            setBackground(Color.WHITE); // Set background color
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (Shape shape : shapes) {
                shape.draw(g);
            }
        }
    }
// Shape abstract class
  private abstract class Shape {
        Color color;
        float thickness;
        String style;

        Shape(Color color, float thickness, String style) {
            this.color = color;
            this.thickness = thickness;
            this.style = style;
        }

        abstract void draw(Graphics g);
        abstract void setEndCoordinates(int x, int y);
    }

// Line class
  private class Line extends Shape {
        int x1, y1, x2, y2;

        Line(Color color, int x1, int y1, int x2, int y2, float thickness, String style) {
            super(color, thickness, style);
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        @Override
        void draw(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if ("Dotted".equals(style)) {
                g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{5.0f}, 0.0f));
            }
            g2d.drawLine(x1, y1, x2, y2);
            if ("Filled".equals(style)) {
                // Filling logic for lines can be added if needed
            }
        }

        @Override
        void setEndCoordinates(int x, int y) {
            this.x2 = x;
            this.y2 = y;
        }
    }

// Rectangle class
  private class Rectangle extends Shape {
        int x, y, width, height;

        Rectangle(Color color, int x, int y, int width, int height, float thickness, String style) {
            super(color, thickness, style);
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Override
        void draw(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(thickness));
            if ("Dotted".equals(style)) {
                g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{5.0f}, 0.0f));
            }
            g2d.drawRect(x, y, width, height);
            if ("Filled".equals(style)) {
                g2d.fillRect(x, y, width, height);
            }
        }

        @Override
        void setEndCoordinates(int x, int y) {
            this.width = x - this.x;
            this.height = y - this.y;
        }
    }

// Oval class
  private class Oval extends Shape {
        int x, y, width, height;

        Oval(Color color, int x, int y, int width, int height, float thickness, String style) {
            super(color, thickness, style);
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Override
        void draw(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(thickness));
            if ("Dotted".equals(style)) {
                g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{5.0f}, 0.0f));
            }
            g2d.drawOval(x, y, width, height);
            if ("Filled".equals(style)) {
                g2d.fillOval(x, y, width, height);
            }
        }

        @Override
        void setEndCoordinates(int x, int y) {
            this.width = x - this.x;
            this.height = y - this.y;
        }
    }

    private class Triangle extends Shape {
        int x1, y1, x2, y2, x3, y3;

        Triangle(Color color, int x1, int y1, int x2, int y2, float thickness, String style) {
            super(color, thickness, style);
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.x3 = (x1 + x2) / 2; // Simple triangle logic
            this.y3 = y1 - Math.abs(x2 - x1); // Height based on width
        }

        @Override
        void draw(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(thickness));
            if ("Dotted".equals(style)) {
                g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{5.0f}, 0.0f));
            }
            Path2D triangle = new Path2D.Double();
            triangle.moveTo(x1, y1);
            triangle.lineTo(x2, y2);
            triangle.lineTo(x3, y3);
            triangle.closePath();
            g2d.draw(triangle);
            if ("Filled".equals(style)) {
                g2d.fill(triangle);
            }
        }

        @Override
        void setEndCoordinates(int x, int y) {
            this.x2 = x;
            this.y2 = y;
            this.x3 = (x1 + x2) / 2; // Update the third point based on new coordinates
            this.y3 = y1 - Math.abs(x2 - x1); // Maintain the height
        }
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
         SwingUtilities.invokeLater(PaintBrush::new);
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
            java.util.logging.Logger.getLogger(PaintBrush.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PaintBrush.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PaintBrush.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PaintBrush.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PaintBrush().setVisible(true);
            }
        });
    }

    
   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}