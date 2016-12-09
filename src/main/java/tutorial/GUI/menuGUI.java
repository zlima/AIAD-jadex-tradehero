package tutorial.GUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Cenas on 12/8/2016.
 */
public class menuGUI {
    private JList agentesListGUI;
    public JPanel panel;
    public JSpinner seguirSpinnTip;
    public JSpinner saldoSpinnTip;
    public JSpinner comprarSpinnTip;
    public JSpinner venderSpinnTip;
    public JButton criarButton;
    public JPanel tipo1;
    public JPanel random;
    public JPanel mason;
    public JPanel lista;
    public JButton criarMansoBT;
    public JButton criarRandomBT;
    public JSpinner saldoSpinnManso;
    public JSpinner seguidoresSpinnManso;
    public JSpinner seguirSpinnManso;
    public JSpinner saldoSpinnRand;
    public JSpinner seguirSpinnRand;
    SpinnerNumberModel model ;

    private void createUIComponents() {
        // TODO: place custom component creation code here
        model = new SpinnerNumberModel(0.0,-999999999999999.0 ,999999999999999.0,0.1);
        saldoSpinnManso = new JSpinner(model);
        saldoSpinnManso.setValue(999999.0);

        saldoSpinnTip = new JSpinner(model);
        saldoSpinnTip.setValue(999999.0);

        saldoSpinnRand = new JSpinner(model);
        saldoSpinnRand.setValue(999999.0);
    }


}
