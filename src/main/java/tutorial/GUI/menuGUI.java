package tutorial.GUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Cenas on 12/8/2016.
 */
public class menuGUI {
    public JList agentesListGUI;
    public JPanel panel;
    public JSpinner seguirSpinnTip;
    public JSpinner saldoSpinnTip;
    public JSpinner comprarSpinnTip;
    public JSpinner venderSpinnTip;
    public JSpinner minrateSpinnTip;
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
    public JSpinner minrateSpinnPassive;
    public JSpinner saldoSpinnRand;
    public JSpinner seguirSpinnRand;
    SpinnerNumberModel model ;
    SpinnerNumberModel model2 ;

    private void createUIComponents() {
        // TODO: place custom component creation code here
        model = new SpinnerNumberModel(0.0,-999999999999999.0 ,999999999999999.0,0.1);
        model2 = new SpinnerNumberModel(0.00,-1000.00 ,10000.00 ,0.01);
        saldoSpinnPassive = new JSpinner(model);
        saldoSpinnPassive.setValue(999999.0);
        minrateSpinnPassive = new JSpinner(model2);
        minrateSpinnPassive.setValue(1.00);

        saldoSpinnTip = new JSpinner(model);
        saldoSpinnTip.setValue(999999.0);
        minrateSpinnTip = new JSpinner(model2);
        minrateSpinnTip.setValue(1.00);

        saldoSpinnRand = new JSpinner(model);
        saldoSpinnRand.setValue(999999.0);
    }


}
