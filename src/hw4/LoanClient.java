package hw4;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DecimalFormat;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author Mengchuan Lin
 */
public class LoanClient extends JFrame {
    private JTextArea jta;
    private JButton jbtSubmit;
    private JTextField jtfInterest, jtfYear, jtfLoan;
    private JPanel jpl1, jpl2, jpl3;

    private DataInputStream fromServer;
    private DataOutputStream toServer;
    private double loanAmount, annualInterestRate, monthlyPayment, totalPayment;
    private int numberOfYears;

    public LoanClient() {
        initComponents();
        //register listener
        jbtSubmit.addActionListener((ActionEvent e) -> {
            if (!jtfLoan.getText().isEmpty() && !jtfInterest.getText().isEmpty()
                && !jtfYear.getText().isEmpty()) {
                //check for correct input format
                try {
                    loanAmount = Double.parseDouble(jtfLoan.getText().trim());
                    annualInterestRate =
                              Double.parseDouble(jtfInterest.getText().trim());
                    numberOfYears = Integer.parseInt(jtfYear.getText().trim());
                    exchangeData();
                }
                catch (NumberFormatException nfe){
                    System.err.println(nfe);
                    JOptionPane.showMessageDialog(null, "Enter numbers only.",
                                                  "Wrong input format",
                                                  JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void initComponents() {
        //populate label panel
        jpl1 = new JPanel();
        jpl1.setLayout(new GridLayout(3, 1));
        jpl1.add(new JLabel("Loan amount"));
        jpl1.add(new JLabel("Number of years"));
        jpl1.add(new JLabel("Annual interest rate"));

        //populate button panel
        jpl2 = new JPanel();
        jpl2.setLayout(new GridLayout(3, 1));
        jtfLoan = new JTextField();
        jtfLoan.setHorizontalAlignment(JTextField.RIGHT);
        jpl2.add(jtfLoan);
        jtfYear = new JTextField();
        jtfYear.setHorizontalAlignment(JTextField.RIGHT);
        jpl2.add(jtfYear);
        jtfInterest = new JTextField();
        jtfInterest.setHorizontalAlignment(JTextField.RIGHT);
        jpl2.add(jtfInterest);

        //put label and button panel on jpl3
        jpl3 = new JPanel();
        jpl3.setLayout(new BorderLayout());
        jpl3.add(jpl1, BorderLayout.WEST);
        jpl3.add(jpl2, BorderLayout.CENTER);
        jbtSubmit = new JButton("Submit");
        jpl3.add(jbtSubmit, BorderLayout.EAST);

        //align panels on frame
        setLayout(new BorderLayout());
        add(jpl3, BorderLayout.NORTH);
        jta = new JTextArea();
        jta.setEditable(false);
        add(new JScrollPane(jta), BorderLayout.CENTER);

        setTitle("Loan Client");
        Dimension d = new Dimension(500, 300);
        setSize(d);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(700, 300);
        setVisible(true);
    }

    private void exchangeData() {
        int portNum = 9999;
        try (Socket clientSocket =  new Socket("localhost", portNum);) {
            DecimalFormat df = new DecimalFormat("0.00");
            jta.append("Loan amount: $" + df.format(loanAmount) + '\n');
            jta.append("Number of years: " + numberOfYears + '\n');
            jta.append("Annual interest rate: " + df.format(annualInterestRate)
                       + "%\n");

            //transmit data to server
            toServer = new DataOutputStream(clientSocket.getOutputStream());
            toServer.writeDouble(loanAmount);
            toServer.writeInt(numberOfYears);
            toServer.writeDouble(annualInterestRate);
            toServer.flush();

            //receive results from server
            fromServer = new DataInputStream(clientSocket.getInputStream());
            monthlyPayment = fromServer.readDouble();
            totalPayment = fromServer.readDouble();

            jta.append("Monthly payment: $" + df.format(monthlyPayment) + '\n');
            jta.append("Total payment: $" + df.format(totalPayment) + "\n\n");
        }
        catch (IOException ioe) {
            System.err.println(ioe);
        }
    }

    public static void main(String[] args) {
        new LoanClient();
    }
}
