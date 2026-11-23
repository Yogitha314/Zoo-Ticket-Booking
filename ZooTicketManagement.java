// 3.4. PACKAGE
package zooTicket;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;

// 3.4. INTERFACE
interface Bookable {
    void book(int count) throws TicketsUnavailableException;
    void cancel(int count);
}

// 3.5. CUSTOM EXCEPTION
class TicketsUnavailableException extends Exception {
    public TicketsUnavailableException(String message) {
        super(message);
    }
}

// 3.1. ENCAPSULATION & ABSTRACTION
abstract class Ticket implements Bookable {
    private String ticketName;
    private int totalQuantity;
    private int available;
    private double price;

    public Ticket(String ticketName, int totalQuantity, double price) {
        this.ticketName = ticketName;
        this.totalQuantity = totalQuantity;
        this.available = totalQuantity;
        this.price = price;
    }

    public String getTicketName() {
        return ticketName;
    }

    public int getAvailable() {
        return available;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public double getPrice() {
        return price;
    }

    // Synchronized for Thread Safety (3.6)
    @Override
    public synchronized void book(int count) throws TicketsUnavailableException {
        if (count <= 0) {
            throw new IllegalArgumentException("Ticket quantity must be positive.");
        }
        if (count > available) {
            throw new TicketsUnavailableException("Not enough tickets available. Only " + available + " left.");
        }
        available -= count;
    }

    @Override
    public synchronized void cancel(int count) {
        if (count <= 0 || count > (totalQuantity - available)) {
            throw new IllegalArgumentException("Invalid cancellation quantity.");
        }
        available += count;
    }

    // Abstract method for Runtime Polymorphism (3.3)
    public abstract String displayDetails();
}

// 3.2. INHERITANCE & 3.3. RUNTIME POLYMORPHISM (METHOD OVERRIDING)
class AdultTicket extends Ticket {
    public AdultTicket(int quantity, double price) {
        super("Adult Ticket", quantity, price);
    }

    @Override
    public String displayDetails() {
        return String.format("%-22s | Available: %-3d | Price: Rs.%.2f",
                getTicketName(), getAvailable(), getPrice());
    }
}

class ChildTicket extends Ticket {
    public ChildTicket(int quantity, double price) {
        super("Child Ticket", quantity, price);
    }

    @Override
    public String displayDetails() {
        return String.format("%-22s | Available: %-3d | Price: Rs.%.2f",
                getTicketName(), getAvailable(), getPrice());
    }
}

class SeniorTicket extends Ticket {
    public SeniorTicket(int quantity, double price) {
        super("Senior Citizen Ticket", quantity, price);
    }

    @Override
    public String displayDetails() {
        return String.format("%-22s | Available: %-3d | Price: Rs.%.2f",
                getTicketName(), getAvailable(), getPrice());
    }
}

// MAIN CLASS WITH 3.10. EVENT HANDLING (Swing + ActionListener)
public class ZooTicketManagement extends JFrame implements ActionListener {

    // 3.9. LOGGING
    private static final Logger logger = Logger.getLogger(ZooTicketManagement.class.getName());
    private static final String RECEIPT_FILE = "Zoo_Receipt.txt";

    // 3.8. COLLECTIONS (ArrayList & HashMap)
    private ArrayList<Ticket> ticketList = new ArrayList<>();
    private HashMap<String, Integer> bookedMap = new HashMap<>();

    // GUI Components
    private JTextField visitorField, qtyField;
    private JComboBox<String> ticketDropdown;
    private JTextArea outputArea;
    private JLabel statusLabel;
    private JButton viewBtn, bookBtn, cancelBtn, billBtn, readFileBtn, exitBtn;

    // 3.3. COMPILE-TIME POLYMORPHISM (METHOD OVERLOADING)
    public static String getWelcomeMessage() {
        return "===== ZOO TICKET BOOKING MANAGEMENT SYSTEM =====";
    }

    public static String getWelcomeMessage(String visitorName) {
        return "Welcome, " + visitorName + "! Ready to book your zoo tickets.";
    }

    public ZooTicketManagement() {
        setupLogger();
        initializeData();
        setupGUI();
        logger.info("Zoo Ticket Booking System GUI launched.");
    }

    // 3.9. LOGGING SETUP (Outputs to both Console and zoo_booking.log)
    private void setupLogger() {
        try {
            FileHandler fileHandler = new FileHandler("zoo_booking.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            System.out.println("Logger file error: " + e.getMessage());
        }
    }

    // Initialize Collections Data
    private void initializeData() {
        ticketList.add(new AdultTicket(100, 500));
        ticketList.add(new ChildTicket(80, 250));
        ticketList.add(new SeniorTicket(40, 300));

        for (Ticket t : ticketList) {
            bookedMap.put(t.getTicketName(), 0);
        }
    }

    // Build GUI Window
    private void setupGUI() {
        setTitle("Zoo Ticket Booking Management System");
        setSize(720, 540);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top Input Panel
        JPanel topPanel = new JPanel(new GridLayout(3, 2, 10, 8));
        topPanel.setBorder(BorderFactory.createTitledBorder("Visitor & Ticket Details"));

        topPanel.add(new JLabel("Visitor Name:"));
        visitorField = new JTextField("Mithra");
        topPanel.add(visitorField);

        topPanel.add(new JLabel("Select Ticket Type:"));
        ticketDropdown = new JComboBox<>();
        for (Ticket t : ticketList) {
            ticketDropdown.addItem(t.getTicketName());
        }
        topPanel.add(ticketDropdown);

        topPanel.add(new JLabel("Quantity:"));
        qtyField = new JTextField("1");
        topPanel.add(qtyField);

        add(topPanel, BorderLayout.NORTH);

        // Center Display Area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        outputArea.setText(getWelcomeMessage() + "\n" + getWelcomeMessage(getFormattedVisitorName()) + "\n\nClick 'View Tickets' to see availability.");
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        // Bottom Buttons + Thread Status Panel
        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 8, 8));

        viewBtn = new JButton("1. View Tickets");
        bookBtn = new JButton("2. Book Tickets");
        cancelBtn = new JButton("3. Cancel Tickets");
        billBtn = new JButton("4. Bill & Save File");
        readFileBtn = new JButton("5. Read Saved Bill");
        exitBtn = new JButton("6. Exit");

        // Register Event Listeners (3.10)
        viewBtn.addActionListener(this);
        bookBtn.addActionListener(this);
        cancelBtn.addActionListener(this);
        billBtn.addActionListener(this);
        readFileBtn.addActionListener(this);
        exitBtn.addActionListener(this);

        buttonPanel.add(viewBtn);
        buttonPanel.add(bookBtn);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(billBtn);
        buttonPanel.add(readFileBtn);
        buttonPanel.add(exitBtn);

        statusLabel = new JLabel(" Status: Ready");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());

        southPanel.add(buttonPanel, BorderLayout.CENTER);
        southPanel.add(statusLabel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    // String Manipulation Helper (3.7)
    private String getFormattedVisitorName() {
        String name = visitorField.getText().trim();
        return name.isEmpty() ? "GUEST" : name.toUpperCase();
    }

    // 3.10. EVENT HANDLING METHOD
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == viewBtn) {
            showAvailableTickets();
        } else if (e.getSource() == bookBtn) {
            handleBooking();
        } else if (e.getSource() == cancelBtn) {
            handleCancellation();
        } else if (e.getSource() == billBtn) {
            generateAndSaveBill();
        } else if (e.getSource() == readFileBtn) {
            readSavedReceipt();
        } else if (e.getSource() == exitBtn) {
            logger.info("User exited Zoo Ticket Management System.");
            JOptionPane.showMessageDialog(this, "Thank you for visiting the zoo!");
            System.exit(0);
        }
    }

    private void showAvailableTickets() {
        StringBuilder sb = new StringBuilder();
        sb.append(getWelcomeMessage()).append("\n");
        sb.append(getWelcomeMessage(getFormattedVisitorName())).append("\n\n");
        sb.append("===== AVAILABLE TICKETS =====\n");

        // Runtime Polymorphism using ArrayList
        for (Ticket t : ticketList) {
            sb.append(t.displayDetails()).append("\n");
        }
        outputArea.setText(sb.toString());
        statusLabel.setText(" Status: Displayed available tickets.");
        logger.info("Viewed available tickets.");
    }

    private void handleBooking() {
        try {
            int qty = Integer.parseInt(qtyField.getText().trim());
            int index = ticketDropdown.getSelectedIndex();
            Ticket selectedTicket = ticketList.get(index);
            String visitor = getFormattedVisitorName();

            // 3.6. THREADING: Start background thread to process booking
            BookingThread thread = new BookingThread(selectedTicket, qty, visitor);
            thread.start();

        } catch (NumberFormatException ex) {
            logger.warning("Invalid number entered for booking: " + qtyField.getText());
            JOptionPane.showMessageDialog(this, "Please enter a valid whole number for quantity.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCancellation() {
        try {
            int qty = Integer.parseInt(qtyField.getText().trim());
            int index = ticketDropdown.getSelectedIndex();
            Ticket selectedTicket = ticketList.get(index);
            String ticketName = selectedTicket.getTicketName();
            int currentlyBooked = bookedMap.get(ticketName);

            // 3.5. EXCEPTION HANDLING
            if (qty <= 0 || qty > currentlyBooked) {
                throw new IllegalArgumentException(
                        "Cannot cancel " + qty + " ticket(s). You have only booked " + currentlyBooked + ".");
            }

            selectedTicket.cancel(qty);
            bookedMap.put(ticketName, currentlyBooked - qty);

            String msg = "===== CANCELLATION SUCCESSFUL =====\n"
                    + "Visitor: " + getFormattedVisitorName() + "\n"
                    + "Ticket Type: " + ticketName + "\n"
                    + "Tickets Cancelled: " + qty + "\n"
                    + "Remaining Booked: " + bookedMap.get(ticketName) + "\n";

            outputArea.setText(msg);
            statusLabel.setText(" Status: Cancellation completed.");
            logger.info("Cancelled " + qty + " x " + ticketName);

        } catch (NumberFormatException ex) {
            logger.warning("Invalid number entered for cancellation.");
            JOptionPane.showMessageDialog(this, "Enter a valid whole number.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            logger.warning("Cancellation failed: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Cancellation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 3.7. FILE HANDLING WITH STRING (Write Bill to File)
    private void generateAndSaveBill() {
        String visitor = getFormattedVisitorName();
        StringBuilder bill = new StringBuilder();
        double total = 0;

        bill.append("========= ZOO TICKET BILL =========\n");
        bill.append("Visitor Name : ").append(visitor).append("\n");
        bill.append("-----------------------------------\n");

        for (Ticket t : ticketList) {
            int count = bookedMap.get(t.getTicketName());
            double itemCost = count * t.getPrice();
            total += itemCost;
            bill.append(String.format("%-22s : %d x Rs.%.2f = Rs.%.2f\n",
                    t.getTicketName(), count, t.getPrice(), itemCost));
        }

        double discount = (total >= 1000) ? total * 0.10 : 0;
        double finalCost = total - discount;

        bill.append("-----------------------------------\n");
        bill.append(String.format("Total Cost : Rs.%.2f\n", total));
        bill.append(String.format("Discount   : Rs.%.2f\n", discount));
        bill.append(String.format("Final Cost : Rs.%.2f\n", finalCost));

        if (total < 1000) {
            bill.append("Note: 10% discount applies to bills of Rs.1000 or more.\n");
        }

        bill.append("\n========= BOOKING SUMMARY =========\n");
        for (Ticket t : ticketList) {
            bill.append(String.format("%-22s | Booked: %-3d | Available: %d\n",
                    t.getTicketName(), bookedMap.get(t.getTicketName()), t.getAvailable()));
        }

        String billText = bill.toString();
        outputArea.setText(billText);

        // Save String to File
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RECEIPT_FILE))) {
            writer.write(billText);
            statusLabel.setText(" Status: Bill saved to " + RECEIPT_FILE);
            logger.info("Bill generated and saved to " + RECEIPT_FILE + " (Final Cost: Rs." + finalCost + ")");
            JOptionPane.showMessageDialog(this, "Bill saved successfully to " + RECEIPT_FILE);
        } catch (IOException ex) {
            logger.severe("File write error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 3.7. FILE HANDLING WITH STRING (Read Bill from File)
    private void readSavedReceipt() {
        StringBuilder content = new StringBuilder();
        int lineCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(RECEIPT_FILE))) {
            String line;
            content.append("--- READING FROM FILE: ").append(RECEIPT_FILE).append(" ---\n\n");
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
                lineCount++;
            }
            content.append("\n[Total lines read: ").append(lineCount)
                   .append(" | Total characters: ").append(content.length()).append("]");

            outputArea.setText(content.toString());
            statusLabel.setText(" Status: Loaded saved bill from " + RECEIPT_FILE);
            logger.info("Read saved receipt from " + RECEIPT_FILE);
        } catch (IOException ex) {
            logger.warning("Could not read receipt file: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "No saved receipt found. Click 'Bill & Save File' first.",
                    "File Not Found", JOptionPane.WARNING_MESSAGE);
        }
    }

    // 3.6. THREADING (Inner Thread Class)
    class BookingThread extends Thread {
        private Ticket ticket;
        private int count;
        private String visitor;

        public BookingThread(Ticket ticket, int count, String visitor) {
            super("ZooBookingWorkerThread");
            this.ticket = ticket;
            this.count = count;
            this.visitor = visitor;
        }

        @Override
        public void run() {
            SwingUtilities.invokeLater(() -> statusLabel.setText(" Status: Processing booking in " + getName() + "..."));
            try {
                Thread.sleep(600); // Simulates background booking processing
                ticket.book(count);

                synchronized (bookedMap) {
                    int prev = bookedMap.get(ticket.getTicketName());
                    bookedMap.put(ticket.getTicketName(), prev + count);
                }

                double cost = count * ticket.getPrice();
                String result = "===== BOOKING SUCCESSFUL =====\n"
                        + "Processed By : " + getName() + "\n"
                        + "Visitor      : " + visitor + "\n"
                        + "Ticket Type  : " + ticket.getTicketName() + "\n"
                        + "Quantity     : " + count + "\n"
                        + String.format("Booking Cost : Rs.%.2f\n", cost)
                        + "Available Now: " + ticket.getAvailable() + "\n";

                logger.info("Booked " + count + " x " + ticket.getTicketName() + " for " + visitor);

                SwingUtilities.invokeLater(() -> {
                    outputArea.setText(result);
                    statusLabel.setText(" Status: Booking completed by " + getName());
                });

            } catch (TicketsUnavailableException | IllegalArgumentException ex) {
                logger.warning("Booking error: " + ex.getMessage());
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText(" Status: Booking failed.");
                    JOptionPane.showMessageDialog(ZooTicketManagement.this, "Error: " + ex.getMessage(),
                            "Booking Error", JOptionPane.ERROR_MESSAGE);
                });
            } catch (InterruptedException ex) {
                logger.severe("Booking thread interrupted: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(getWelcomeMessage());
        SwingUtilities.invokeLater(() -> new ZooTicketManagement());
    }
}