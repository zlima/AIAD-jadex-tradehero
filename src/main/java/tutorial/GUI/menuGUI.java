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
    public JPanel passive;
    public JPanel lista;
    public JButton criarPassiveBT;
    public JButton criarRandomBT;
    public JSpinner saldoSpinnPassive;
    public JSpinner seguidoresSpinnPassive;
    public JSpinner seguirSpinnPassive;
    public JSpinner saldoSpinnRand;
    public JSpinner seguirSpinnRand;
    SpinnerNumberModel model ;

    private void createUIComponents() {
        // TODO: place custom component creation code here
        model = new SpinnerNumberModel(0.0,-999999999999999.0 ,999999999999999.0,0.1);
        saldoSpinnPassive = new JSpinner(model);
        saldoSpinnPassive.setValue(999999.0);

        saldoSpinnTip = new JSpinner(model);
        saldoSpinnTip.setValue(999999.0);

        saldoSpinnRand = new JSpinner(model);
        saldoSpinnRand.setValue(999999.0);
    }


}
