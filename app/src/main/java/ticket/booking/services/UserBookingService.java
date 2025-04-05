package ticket.booking.services;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.util.userServiceUtil;
import java.io.File;
import java.io.IOException;

import java.util.*;
import ticket.booking.entities.Ticket;

public class UserBookingService {

    private ObjectMapper objectMapper = new ObjectMapper();
    private List<User> userList;
    private User user;

    private final String USER_FILE_PATH = "app/src/main/java/ticket/booking/localDb/users.json";

    public UserBookingService(User user) throws IOException {
        this.user = user;
        loadUserListFromFile();
    }

    public UserBookingService() throws IOException {
        loadUserListFromFile();
    }

    private void loadUserListFromFile() throws IOException {
        userList = objectMapper.readValue(new File(USER_FILE_PATH), new TypeReference<List<User>>() {});
    }

    private void saveUserListToFile() throws IOException {
        File usersFile = new File(USER_FILE_PATH);
        objectMapper.writeValue(usersFile, userList);
    }

    private void updateUserInFile() throws IOException {
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getName().equals(user.getName())) {
                userList.set(i, user); // Replace with updated user
                break;
            }
        }
        saveUserListToFile();
    }

    public Boolean loginUser() {
        Optional<User> foundUser = userList.stream().filter(user1 ->
                user1.getName().equals(user.getName()) &&
                        userServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword())
        ).findFirst();

        foundUser.ifPresent(value -> this.user = value); // Update current user object

        return foundUser.isPresent();
    }

    public Boolean signUp(User user1) {
        try {
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;
        } catch (IOException ex) {
            return Boolean.FALSE;
        }
    }

    public void fetchBookings() {
        if (user != null) {
            user.printTickets();
        } else {
            System.out.println("Please login first.");
        }
    }

    public Boolean cancelBooking(String ticketId) {
        if (ticketId == null || ticketId.isEmpty()) {
            System.out.println("Ticket ID cannot be null or empty.");
            return Boolean.FALSE;
        }

        Optional<Ticket> toCancel = user.getTicketsBooked().stream()
                .filter(ticket -> ticket.getTicketId().equals(ticketId))
                .findFirst();

        if (toCancel.isPresent()) {
            Ticket ticket = toCancel.get();
            // Update seat to available in train
            try {
                TrainService trainService = new TrainService();
                Train train = trainService.getTrainById(ticket.getTrainId());
                if (train != null) {
                    List<List<Integer>> seats = train.getSeats();
                    seats.get(ticket.getRow()).set(ticket.getSeat(), 0); // Mark as available
                    train.setSeats(seats);
                    trainService.addTrain(train); // Save train
                }
            } catch (IOException e) {
                System.out.println("Error updating train seat map.");
            }

            // Remove ticket from user and update file
            user.getTicketsBooked().remove(ticket);
            try {
                updateUserInFile();
            } catch (IOException e) {
                System.out.println("Error updating user file.");
            }

            System.out.println("Ticket with ID " + ticketId + " has been canceled.");
            return Boolean.TRUE;
        } else {
            System.out.println("No ticket found with ID " + ticketId);
            return Boolean.FALSE;
        }
    }

    public List<Train> getTrains(String source, String destination) {
        try {
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        } catch (IOException ex) {
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train) {
        return train.getSeats();
    }

    public Boolean bookTrainSeat(Train train, int row, int seat) {
        try {
            TrainService trainService = new TrainService();
            List<List<Integer>> seats = train.getSeats();

            if (row >= 0 && row < seats.size() && seat >= 0 && seat < seats.get(row).size()) {
                if (seats.get(row).get(seat) == 0) {
                    seats.get(row).set(seat, 1);
                    train.setSeats(seats);
                    trainService.addTrain(train);

                    // Create full ticket object
                    String ticketId = UUID.randomUUID().toString().substring(0, 8);
                    // Before this



                    String userId = user.getName();
                    String source = train.getStations().get(0);
                    String destination = train.getStations().get(train.getStations().size() - 1);
                    String dateOfTravel = train.getStationTimes().getOrDefault(destination, "Unknown");

// Now create full ticket
                    Ticket ticket = new Ticket(ticketId, userId, source, destination, dateOfTravel, train);

                    user.getTicketsBooked().add(ticket);
                    updateUserInFile();

                    System.out.println("Booked! Enjoy your journey. Ticket ID: " + ticketId);
                    return true;
                } else {
                    System.out.println("Seat is already booked.");
                    return false;
                }
            } else {
                System.out.println("Invalid seat selection.");
                return false;
            }
        } catch (IOException ex) {
            System.out.println("Error booking seat.");
            return Boolean.FALSE;
        }
    }

}
