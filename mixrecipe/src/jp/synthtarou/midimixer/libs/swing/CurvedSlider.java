package jp.synthtarou.midimixer.libs.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.midimixer.libs.swing.themes.ThemeManager;

/**
 *
 * @thanks (https://stackoverflow.com/questions/25546399/how-can-i-make-a-jslider-in-a-curve-shape)
 */
public class CurvedSlider extends JPanel implements MouseListener, MouseMotionListener
{
    private double _minAngleRad = 0.0;
    private double _maxAngleRad = 0.0;
    private MXRangedValue _value = MXRangedValue.ZERO7;

    private Color _highlight;
    private Color _selectionColor;
    
    public void setEditable(boolean e) {
        super.setEnabled(e);
        updateUI();
    }  
    
    public void updateUI() {
        super.updateUI();

        _highlight = new JTextField().getSelectionColor();
        _selectionColor = MXUtil.mixtureColor(Color.red, 10, Color.yellow, 90);
    }

    int _circleR;
    
    /**
     *
     * @param circleR
     */
    public CurvedSlider(int circleR)
    {
        _circleR = circleR;
        addMouseListener(this);
        addMouseMotionListener(this);
        setMinimumSize(new Dimension(circleR, circleR));
        setMaximumSize(new Dimension(circleR, circleR));
        setPreferredSize(new Dimension(circleR, circleR));
        setSize(new Dimension(circleR, circleR));

        double minAngle = 220 / 180.0 * Math.PI;
        double maxAngle = -40 / 180.0 * Math.PI;

        setAngles(minAngle, maxAngle);
    }

    public void setAngles(double minAngleRad, double maxAngleRad)
    {
        _minAngleRad = minAngleRad;
        _maxAngleRad = maxAngleRad;
        repaint();
    }

    public void setValue(MXRangedValue value)
    {
        if (_value != value) {
            _value = value;
            repaint();
            vokeListenerList();
        }
    }

    public int getValue()
    {
        return _value._value;
    }

    @Override
    protected void paintComponent(Graphics gr)
    {
        super.paintComponent(gr);
        
        Color foreground = getForeground();
        Color background = getBackground();

        if (isEnabled()== false) {
            foreground = MXUtil.mixtureColor(foreground, 70, Color.white, 30);
        }

        Graphics2D g = (Graphics2D)gr;
        g.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING, 
            RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(background);
        g.fillRect(0,0,getWidth(),getHeight());
        
        int value = _value._value;
        int rangeFrom = _value._min;
        int rangeTo = _value._max;

        double alpha = ((double)value - rangeFrom) / (rangeTo - rangeFrom);
        if (_value._max < _value._min) { 
            alpha = ((double)rangeTo - value) / (rangeTo - rangeFrom);
        }
        double angleRad = _minAngleRad + alpha * (_maxAngleRad - _minAngleRad);

        double radius = Math.min(getWidth(), getHeight()) / 3.0;

        final double thickness = 15;
        double xC = getWidth() / 2.0;
        double yC = getHeight() / 2.0;
        double x0 = xC + Math.cos(angleRad) * (radius - thickness);
        double y0 = yC - Math.sin(angleRad) * (radius - thickness);
        double x1 = xC + Math.cos(angleRad) * radius;
        double y1 = yC - Math.sin(angleRad) * radius;

        Arc2D.Double a0 = new Arc2D.Double(
            xC-radius -2, yC-radius -2, 
            radius+radius +4, radius+radius +4, 
            Math.toDegrees(_minAngleRad + 0.15), 
            Math.toDegrees(_maxAngleRad-_minAngleRad - 0.3), 
            Arc2D.PIE);
        
        g.setColor(foreground);
        g.fill(a0);

        Arc2D.Double a1 = new Arc2D.Double(
            xC-radius, yC-radius, 
            radius+radius, radius+radius, 
            Math.toDegrees(angleRad), 
            Math.toDegrees(_maxAngleRad-angleRad), 
            Arc2D.PIE);

        g.setColor(_highlight);
        g.fill(a1);

        Arc2D.Double a2 = new Arc2D.Double(
            xC-radius, yC-radius, 
            radius+radius, radius+radius,   
            Math.toDegrees(_minAngleRad), 
            Math.toDegrees(angleRad-_minAngleRad), 
            Arc2D.PIE);

        g.setColor(_selectionColor);
        g.fill(a2);

        Arc2D.Double a1inner = new Arc2D.Double(
            xC-radius/2-3, yC-radius/2-3,
            (radius+radius)/2+6, (radius+radius)/2+6,
            Math.toDegrees(_minAngleRad), 
            Math.toDegrees(_maxAngleRad-_minAngleRad), 
            Arc2D.PIE);

        g.setColor(foreground);
        g.fill(a1inner);

        Arc2D.Double b1inner = new Arc2D.Double(
            xC-radius/2, yC-radius/2,
            (radius+radius)/2, (radius+radius)/2, 
            Math.toDegrees(_minAngleRad + 1), 
            Math.toDegrees(_maxAngleRad-_minAngleRad - 2), 
            Arc2D.PIE);

        g.setColor(background);
        g.fill(b1inner);

        String str = String.valueOf(_value._value);
        if (ThemeManager.getInstance().isColorfulMetalTheme()) {        
            g.setColor(MXUtil.mixtureColor(Color.red, 70, Color.yellow, 30));
        }
        else  {
            g.setColor(MXUtil.mixtureColor(Color.blue, 70, Color.pink, 30));
        }
        g.setFont(staticFont);
        int x = g.getFontMetrics().stringWidth(str);
        int y = g.getFontMetrics().getHeight();
        g.drawString(str,(getWidth() -x )/2, getHeight() - 4);
    }
    
    static Font staticFont  = new Font("Serif" , Font.BOLD , 12);

    static boolean mouseModeCircle = true;

    public static void setMouseCircleIsCircle(boolean isCircle) {
        mouseModeCircle = isCircle;
    }
    
    public static boolean isMouseCircleIsCircle() {
        return mouseModeCircle;
    }

    double startValue = 0;
    Point startPoint = null;

    private void updateAngle(Point p)
    {
        double xC = startPoint.x;
        double yC = startPoint.y;

        if (startPoint == null) {
            return;
        }

        if (mouseModeCircle) {
            xC = getWidth() / 2.0;
            yC = getHeight() / 2.0;
        }

        double dx = p.getX() - xC;
        double dy = p.getY() - yC;
        int rangeFrom = _value._min;
        int rangeTo = _value._max;

        if (mouseModeCircle) {
            double angleRad = Math.atan2(-dy, dx);
            if (angleRad < -Math.PI / 2)
            {
                angleRad = 2 * Math.PI + angleRad;
            }
            angleRad = Math.max(_maxAngleRad, Math.min(_minAngleRad, angleRad));
            double alpha = (angleRad - _minAngleRad) / (_maxAngleRad - _minAngleRad);
            double value = rangeFrom + alpha * (rangeTo - rangeFrom);
            setValue(_value.changeValue((int)value));
        }else {
            // Kind of X-Y Pad
            double distance = dx - dy;
            double range = rangeTo - rangeFrom;
            double value2 = startValue + (distance * range / 250);

            if (value2 < rangeFrom) value2 = rangeFrom;
            if (value2 > rangeTo) value2 = rangeTo;

            setValue(_value.changeValue((int)value2));
        }
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        if (isEnabled()) {
            if(SwingUtilities.isRightMouseButton(e)){
            }else{ 
                updateAngle(e.getPoint());
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
    }

    public Runnable _editor = null;

    @Override
    public void mousePressed(MouseEvent e)
    {
        if (isEnabled()) {
            if(SwingUtilities.isRightMouseButton(e)){
            }else{
                startPoint = e.getPoint();
                startValue = _value._value;
                updateAngle(e.getPoint());
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        startPoint = null;
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
    }
    
    ArrayList<ChangeListener> listenerList = new ArrayList<ChangeListener>();

    public synchronized void addChangeListener(ChangeListener listener) {
        listenerList.add(listener);
    }
    
    public synchronized void vokeListenerList() {
        ChangeEvent evt = new ChangeEvent(this);
        for (int i = 0; i < listenerList.size(); ++ i) {
            listenerList.get(i).stateChanged(evt);
        }
    }
}
