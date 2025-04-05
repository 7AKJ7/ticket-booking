package ticket.booking;

import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.services.UserBookingService;
import ticket.booking.util.userServiceUtil;

import java.io.IOException;
import java.util.*;

public class App {
    public static void main(String[] args) {
        System.out.println("Running Train Booking System");
        Scanner scanner = new Scanner(System.in);
        int option = 0;
        boolean isLoggedIn = false;
        UserBookingService userBookingService = null;
        User currentUser = null;
        Train trainSelectedForBooking = null;

        while (option != 7) {
            System.out.println("\nChoose option:");
            System.out.println("1. Sign up");
            System.out.println("2. Login");
            System.out.println("3. Fetch Bookings");
            System.out.println("4. Search Trains");
            System.out.println("5. Book a Seat");
            System.out.println("6. Cancel my Booking");
            System.out.println("7. Exit the App");

            try {
                option = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Please enter a number from 1 to 7.");
                scanner.next(); // clear invalid input
                continue;
            }

            switch (option) {
                case 1:
                    System.out.println("Enter the username to signup:");
                    String nameToSignUp = scanner.next();
                    System.out.println("Enter the password to signup:");
                    String passwordToSignUp = scanner.next();
                    String hashedPassword = userServiceUtil.hashPassword(passwordToSignUp);

                    User userToSignup = new User(
                            nameToSignUp,
                            passwordToSignUp,
                            hashedPassword,
                            new ArrayList<>(),
                            UUID.randomUUID().toString()
                    );

                    try {
                        userBookingService = new UserBookingService();
                        boolean signedUp = userBookingService.signUp(userToSignup);
                        if (signedUp) {
                            System.out.println("Signup successful.");
                        } else {
                            System.out.println("Signup failed.");
                        }
                    } catch (IOException ex) {
                        System.out.println("Error during signup: " + ex.getMessage());
                    }
                    break;

                case 2:
                    System.out.println("Enter the username to login:");
                    String nameToLogin = scanner.next();
                    System.out.println("Enter the password to login:");
                    String passwordToLogin = scanner.next();

                    try {
                        User tempUser = new User(nameToLogin, passwordToLogin,
                                null, new ArrayList<>(), null);
                        userBookingService = new UserBookingService(tempUser);
                        boolean loggedIn = userBookingService.loginUser();
                        if (loggedIn) {
                            isLoggedIn = true;
                            currentUser = tempUser;
                            System.out.println("Login successful. Welcome, " + nameToLogin + "!");
                        } else {
                            System.out.println("Login failed. Invalid credentials.");
                        }
                    } catch (IOException ex) {
                        System.out.println("Login failed: " + ex.getMessage());
                    }
                    break;

                case 3:
                    if (!isLoggedIn) {
                        System.out.println("Please login first.");
                        break;
                    }
                    System.out.println("Fetching your bookings...");
                    userBookingService.fetchBookings();
                    break;

                case 4:
                    if (!isLoggedIn) {
                        System.out.println("Please login first.");
                        break;
                    }
                    System.out.println("Type your source station:");
                    String source = scanner.next();
                    System.out.println("Type your destination station:");
                    String dest = scanner.next();
                    List<Train> trains = userBookingService.getTrains(source, dest);
                    if (trains.isEmpty()) {
                        System.out.println("No trains found.");
                        break;
                    }

                    int index = 1;
                    for (Train t : trains) {
                        System.out.println(index + ". " + t.getTrainInfo());
                        index++;
                    }
                    System.out.println("Select a train by typing 1,2,3...");
                    int selected = scanner.nextInt();
                    if (selected < 1 || selected > trains.size()) {
                        System.out.println("Invalid selection.");
                        break;
                    }
                    trainSelectedForBooking = trains.get(selected - 1);
                    System.out.println("Train selected.");
                    break;

                case 5:
                    if (!isLoggedIn || trainSelectedForBooking == null) {
                        System.out.println("Please login and select a train first.");
                        break;
                    }

                    System.out.println("Available seats:");
                    List<List<Integer>> seats = userBookingService.fetchSeats(trainSelectedForBooking);
                    int r = 0;
                    for (List<Integer> row : seats) {
                        System.out.print("Row " + r + ": ");
                        for (Integer val : row) {
                            System.out.print(val + " ");
                        }
                        System.out.println();
                        r++;
                    }

                    System.out.println("Select the seat by typing the row and column:");
                    System.out.print("Enter the row: ");
                    int row = scanner.nextInt();
                    System.out.print("Enter the column: ");
                    int col = scanner.nextInt();

                    boolean booked = userBookingService.bookTrainSeat(trainSelectedForBooking, row, col);
                    if (booked) {
                        System.out.println("Booked! Enjoy your journey.");
                    } else {
                        System.out.println("Can't book this seat. It might already be taken.");
                    }
                    break;

                case 6:
                    if (!isLoggedIn) {
                        System.out.println("Please login first.");
                        break;
                    }
                    System.out.println("Enter ticket ID to cancel:");
                    String ticketId = scanner.next();
                    boolean cancelled = userBookingService.cancelBooking(ticketId);
                    if (cancelled) {
                        System.out.println("Ticket cancelled.");
                    } else {
                        System.out.println("Cancellation failed.");
                    }
                    break;

                case 7:
                    System.out.println("Exiting the app. Goodbye!");
                    break;

                default:
                    System.out.println("Invalid option. Please choose between 1 and 7.");
                    break;
            }
        }
    }
}
