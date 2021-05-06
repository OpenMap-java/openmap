package com.bbn.openmap.layer.classification;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.util.PropUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
* An OpenMap Layer to display typical header/footer/declass blocks
* 
* <pre><code>
############################
# Example Properties for a classification layer
classification.class=com.bbn.openmap.layer.classification.ClassificationLayer
classification.prettyName=Classification
# Classification, header and footer
classification.classification=UNCLASSIFIED
# Declass block, bottom left
classification.declassBlock=Classified by: Someone\nDerived from: Multiple Sources\nDeclassify on: 2020-02-15\nYour Organisation\nStreet, City, Zip
# If provided, this will override the automatic color for the header/footer
classification.classColorOverride=008C54
############################
</code></pre>
* 
* @author Gary Briggs 
*/
public class ClassificationLayer extends OMGraphicHandlerLayer {
    
    public final static String[] CLASSIFICATIONS = new String[] {
        "UNCLASSIFIED",
        "UNCLASSIFIED//FOR OFFICIAL USE ONLY",
        "CONFIDENTIAL",
        "SECRET",
        "SECRET//NOFORN",
        "TOP SECRET",
        "UNMARKED"
    };
    
    String classification = "UNCLASSIFIED";
    String declassBlockText = "Classified by: Someone\n"
                + "Derived from: Multiple Sources\n"
                + "Declassify on: 2020-02-15\n"
                + "Your Organisation\n"
                + "Street, City, Zip";
    Color classColorOverride = null;
    String classColorOverrideString = null;

    public final static String classificationProperty = "classification";
    public final static String declassBlockProperty = "declassBlock";
    public final static String classColorOverrideProperty = "classColorOverride";

    @Override
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        String realPrefix = PropUtils.getScopedPropertyPrefix(this);
        
        System.out.println();
        classification = props.getProperty(realPrefix + classificationProperty, classification);
        declassBlockText = props.getProperty(realPrefix + declassBlockProperty, declassBlockText);
        classColorOverrideString = props.getProperty(realPrefix + classColorOverrideProperty, null);
        if(null != classColorOverrideString) {
            classColorOverride = PropUtils.parseColor(classColorOverrideString);
        }
                    
    }
    
    @Override
    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);

        props.put(prefix + "class", this.getClass().getName());
        props.put(prefix + classificationProperty, classification);
        props.put(prefix + declassBlockProperty, declassBlockText);
        if(null != classColorOverride) {
            props.put(prefix + classColorOverrideProperty, classColorOverrideString);
        }
        return props;
    }
    
    @Override
    public Properties getPropertyInfo(Properties list) {
        list = super.getPropertyInfo(list);
        list.put(classificationProperty, "Classification to mark top and bottom");
        list.put(declassBlockProperty, "Declassify Block");
        list.put(classColorOverrideProperty, "Force classification to be this color");
        return list;
    }

    @Override
    public Component getGUI() {
        JPanel pan = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel classlabel = new JLabel("You may edit this combo box");
        
        final JComboBox<String> combo = new JComboBox<String>(CLASSIFICATIONS);
        combo.setEditable(true);
        combo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                classification = (String) combo.getSelectedItem();
                doPrepare();
            }
        });
        
        final JTextArea declass = new JTextArea(declassBlockText);
        declass.setMinimumSize(new Dimension(300, 90));        
        declass.setPreferredSize(new Dimension(300, 90));    
        
        DocumentListener dl = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                declassBlockText = declass.getText();
                classification = (String) combo.getSelectedItem();
                doPrepare();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                declassBlockText = declass.getText();
                classification = (String) combo.getSelectedItem();
                doPrepare();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                declassBlockText = declass.getText();
                classification = (String) combo.getSelectedItem();
                doPrepare();
            }
        };

        declass.getDocument().addDocumentListener(dl);
        Component editorComponent = combo.getEditor().getEditorComponent();
        if(editorComponent instanceof JTextField) {
            ((JTextField) editorComponent).getDocument().addDocumentListener(dl);
        }
        
        pan.add(classlabel, gbc);
        pan.add(combo, gbc);
        pan.add(declass, gbc);
        return pan;
    }
    
    public static Color getClassificationColorFor(String c) {
        if(null == c || c.contains("UNMARKED")) {
            return Color.WHITE;
        }
        if(c.contains("SCI")) {
            return Color.YELLOW;
        }
        if(c.startsWith("U")) {
            return Color.GREEN;
        }
        if(c.startsWith("C")) {
            return Color.BLUE;
        }
        if(c.startsWith("S")) {
            return Color.RED;
        }
        if(c.startsWith("T")) {
            return Color.ORANGE;
        }
        return Color.BLACK;
    }
    
    @Override
    public synchronized OMGraphicList prepare() {
        OMGraphicList classList = new OMGraphicList();
        
        final Color bgColor = Color.WHITE;
        
        // First render is called before display is shown, so this returns zero
        int width = this.getWidth();
        if(0 == width) {
            width = 640;
        }
        int height = this.getHeight();
        if(0 == height) {
            height = 480;
        }
        
        if(null != classification && 0 < classification.length() && !classification.toUpperCase().contains("UNMARKED")) {
            
            final String classTrimmed = classification.trim();
            Color classCol = null!=classColorOverride?classColorOverride:getClassificationColorFor(classification);

            final int xoffset = width/2;
            final int yoffset = 20;
            final String classDisplayText = "  " + classTrimmed + "  ";

            OMText topMark = new OMText(xoffset, yoffset, classDisplayText, OMText.JUSTIFY_CENTER);
            topMark.setFillPaint(bgColor);
            Font font = topMark.getFont().deriveFont(Font.BOLD).deriveFont(18.0f);
            topMark.setLinePaint(classCol);
            topMark.setFont(font);
            classList.add(topMark);

            OMText bottomMark = new OMText(xoffset, height - 10, classDisplayText, OMText.JUSTIFY_CENTER);
            bottomMark.setFillPaint(bgColor);
            bottomMark.setLinePaint(classCol);
            bottomMark.setFont(font);
            classList.add(bottomMark);
            
        }
        
        if(null != declassBlockText && declassBlockText.length()>0) {
            OMText declassBlock = new OMText(5, height - 70, declassBlockText, OMText.JUSTIFY_LEFT);
            Font font = declassBlock.getFont();
            
            declassBlock.setFillPaint(bgColor);
            classList.add(declassBlock);
        }
        
        setList(classList);
        
        return super.prepare();
    }

}
