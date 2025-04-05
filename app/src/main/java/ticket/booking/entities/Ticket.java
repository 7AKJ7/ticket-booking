package ticket.booking.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Ticket {

    private String ticketId;
    private String userId;
    private String source;
    private String destination;
    private String dateOfTravel;
    private Train train;

    // Getter for row
    private int row;   // Added
    // Getter for seat
    private int seat;  // Added



    public Ticket(String ticketId, String userId, String source, String destination, String dateOfTravel, Train train) {
        this.ticketId = ticketId;
        this.userId = userId;
        this.source = source;
        this.destination = destination;
        this.dateOfTravel = dateOfTravel;
        this.train = train;
        this.row = row;
        this.seat = seat;
    }
    public Ticket(){}

    public String getTicketInfo(){
        return String.format(
                "Ticket ID: %s belongs to User %s from %s to %s on %s | Train No: %s",
                ticketId, userId, source, destination, dateOfTravel, train != null ? train.getTrainNo() : "Unknown"
        );
    }


    public String getTrainId() {
        return train != null ? train.getTrainId() : null;
    }

}
