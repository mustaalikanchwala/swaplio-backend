package com.swaplio.swaplio_backend.service;

// service/MeetingService.java
import com.swaplio.swaplio_backend.dto.meeting.CreateMeetingRequest;
import com.swaplio.swaplio_backend.model.Listing;
import com.swaplio.swaplio_backend.model.Meeting;
import com.swaplio.swaplio_backend.model.User;
import com.swaplio.swaplio_backend.repository.ListingRepository;
import com.swaplio.swaplio_backend.repository.MeetingRepository;
import com.swaplio.swaplio_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public Meeting scheduleMeeting(CreateMeetingRequest request, String buyerEmail) {
        User buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Listing listing = listingRepository.findById(request.listingId())
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        Meeting meeting = Meeting.builder()
                .listing(listing)
                .buyer(buyer)
                .seller(listing.getSeller())
                .meetingDate(request.meetingDate())
                .meetingTime(request.meetingTime())
                .location(request.location())
                .notes(request.notes())
                .build();
        return meetingRepository.save(meeting);
    }

    public List<Meeting> getMyMeetingsAsBuyer(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return meetingRepository.findByBuyerId(user.getId());
    }

    public List<Meeting> getMyMeetingsAsSeller(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return meetingRepository.findBySellerId(user.getId());
    }
}