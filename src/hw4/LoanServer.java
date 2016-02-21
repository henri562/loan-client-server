package hw4;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author Mengchuan Lin
 */
public class LoanServer extends JFrame {
    private JTextArea jta;
    private JPanel jpl;

    private Loan loan;
    private static int clientNo;

    /** Default constructor */
    public LoanServer() {
        initComponents();
        jta.append("Loan server started at " + new Date() + '\n');
    }

    private void initComponents() {
        //add components to panel
        jpl = new JPanel();
        jpl.setLayout(new BorderLayout());
        jta = new JTextArea();
        jta.setEditable(false);
        jpl.add(new JScrollPane(jta), BorderLayout.CENTER);

        //add components to frame
        setLayout(new BorderLayout());
        add(jpl, BorderLayout.CENTER);

        setTitle("Loan Server");
        Dimension d = new Dimension(500, 300);
        setSize(d);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(200, 300);
        setVisible(true);
    }

    private class LoanServerThread extends Thread {
        private final Socket socket;

        public LoanServerThread(Socket socket) {
            super("LoanServerThread");
            this.socket = socket;
            printClientInfo();
        }

        @Override
        public void run() {
            loan = new Loan();

            try (DataInputStream in =
                 new DataInputStream(socket.getInputStream());
                 DataOutputStream out =
                 new DataOutputStream(socket.getOutputStream());
                ){
                //read input from client
                loan.setLoanAmount(in.readDouble());
                loan.setNumberOfYears(in.readInt());
                loan.setAnnualInterestRate(in.readDouble());

                DecimalFormat df = new DecimalFormat("0.00");
                jta.append("Loan amount: $"
                                  + df.format(loan.getLoanAmount()) + '\n');
                jta.append("Number of years: "
                                  + loan.getNumberOfYears() + '\n');
                jta.append("Annual interest rate: "
                                  + df.format(loan.getAnnualInterestRate())
                                  + "%\n");

                //compute payments
                double monthlyPayment = loan.getMonthlyPayment();
                double totalPayment = loan.getTotalPayment();

                //send results back to client
                out.writeDouble(monthlyPayment);
                out.writeDouble(totalPayment);

                jta.append("Monthly payment: $"
                                  + df.format(monthlyPayment) + '\n');
                jta.append("Total payment: $"
                                  + df.format(totalPayment) + "\n\n");

                socket.close();
            }
            catch (IOException ioe) {
                System.err.println(ioe);
            }
        }

        private void printClientInfo() {
            jta.append("Starting thread for Client " + ++clientNo
                              + " at " + new Date() + '\n');
            jta.append("Client " + clientNo + "'s host name is "
                              + socket.getInetAddress().getHostName()
                              + '\n');
            jta.append("Client " + clientNo + "'s IP address is "
                              + socket.getRemoteSocketAddress().toString()
                              + '\n');
        }
    }

    public static void main(String[] args) throws IOException {

        //instantiate server object
        LoanServer s = new LoanServer();
        int portNum = 9999;
        boolean listening = true;

        //create server socket and listen for client connections indefinitely
        try (ServerSocket serverSocket = new ServerSocket(portNum)) {
            while (listening) {
                s.new LoanServerThread(serverSocket.accept()).start();
            }
        }
        catch (IOException ioe) {
            System.err.println("Could not listen on port " + portNum);
            System.exit(-1);
        }
    }
}
