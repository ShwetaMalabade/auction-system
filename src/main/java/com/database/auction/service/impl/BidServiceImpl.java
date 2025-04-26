package com.database.auction.service.impl;

import com.database.auction.dto.BidDto;
import com.database.auction.entity.AuctionItems;
import com.database.auction.entity.Bid;
import com.database.auction.entity.Users;
import com.database.auction.exception.AuctionItemNotFoundException;
import com.database.auction.exception.UserNotFound;
import com.database.auction.mapper.BidMapper;
import com.database.auction.repository.AuctionItemsRepository;
import com.database.auction.repository.BidRepository;
import com.database.auction.repository.UsersRepository;
import com.database.auction.service.BidService;
import com.database.auction.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BidServiceImpl implements BidService {

    private final BidRepository bidRepo;
    private final AuctionItemsRepository itemsRepo;
    private final UsersRepository usersRepo;
    private final BidMapper mapper;
    private final NotificationService notificationService;


    @Autowired
    public BidServiceImpl(BidRepository bidRepo,
                          AuctionItemsRepository itemsRepo,
                          UsersRepository usersRepo,
                          BidMapper mapper,
                          NotificationService notificationService) {
        this.bidRepo   = bidRepo;
        this.itemsRepo = itemsRepo;
        this.usersRepo = usersRepo;
        this.mapper    = mapper;
        this.notificationService = notificationService;
    }

//    @Override
//    public BidDto placeBid(BidDto dto) {
//        log.info("Placing a bid for "+dto.getBuyerId());
//        // 1️⃣ load by business key, not PK
//        AuctionItems item = itemsRepo
//                .findByAuctionIdNative(dto.getAuctionId())
//                .orElseThrow(() -> new AuctionItemNotFoundException(
//                        "Auction not found: " + dto.getAuctionId()));
//
//        Users buyer = usersRepo.findById(dto.getBuyerId())
//                .orElseThrow(() -> new UserNotFound(
//                        "Buyer not found: " + dto.getBuyerId()));
//
//        if (dto.getBidTime() == null) {
//            dto.setBidTime(new Date());
//        }
//
//        Bid bid = mapper.toEntity(dto, item, buyer);
//        Bid saved = bidRepo.save(bid);
//
//        updateCurrentBid(item);
//
//        return mapper.toDto(saved);
//    }

    @Override
    public BidDto placeBid(BidDto dto) {
        log.info("Placing bid by buyer {} on auction {}", dto.getBuyerId(), dto.getAuctionId());

        // 1) Load auction and buyer (existing code)
        AuctionItems item = itemsRepo
                .findByAuctionIdNative(dto.getAuctionId())
                .orElseThrow(() -> new AuctionItemNotFoundException(
                        "Auction not found: " + dto.getAuctionId()));
        Users buyer = usersRepo.findById(dto.getBuyerId())
                .orElseThrow(() -> new UserNotFound(
                        "Buyer not found: " + dto.getBuyerId()));

        if (dto.getBidTime() == null) dto.setBidTime(new Date());

        // 2) Capture previous top (if any)
        Optional<Bid> previousTop = bidRepo
                .findAllByAuctionItem_Id(item.getId())
                .stream()
                .max(Comparator.comparing(Bid::getReservePrice));

        // 3) Save the new bid
        Bid newBid   = mapper.toEntity(dto, item, buyer);
        Bid savedBid = bidRepo.save(newBid);

        // 4) Recompute currentBid
        List<Bid> allBids = bidRepo.findAllByAuctionItem_Id(item.getId());
        allBids.sort(Comparator.comparing(Bid::getReservePrice).reversed());

        Bid highestBid       = allBids.get(0);
        double secondReserve = allBids.size() > 1
                ? allBids.get(1).getReservePrice()
                : highestBid.getReservePrice();

        double newCurrentBid = secondReserve + item.getbid_increment();
        item.setCurrentBid(newCurrentBid);
        itemsRepo.save(item);

        // 5a) If this new bid DID NOT become the top, notify *this* bidder
        int newBidderId = savedBid.getBuyer().getUserId();
        int currentWinnerId = highestBid.getBuyer().getUserId();
        if (newBidderId != currentWinnerId) {
            String msg = String.format(
                    "Your bid on auction %d was not high enough. Current top bid: £%.2f",
                    dto.getAuctionId(), newCurrentBid);
            notificationService.alertOutbid(
                    savedBid.getBuyer().getUserId(),
                    dto.getAuctionId(),
                    msg
            );
            log.info("Notified bidder {} that they were outbid", savedBid.getBuyer().getUserId());
        }

        // 5b) If there *was* a previous top, and they got beaten by someone else, notify them too
        if (previousTop.isPresent()) {
            Bid oldTop = previousTop.get();
            int oldUserId = oldTop.getBuyer().getUserId();
            int newTopId  = highestBid.getBuyer().getUserId();
            if (oldUserId != newTopId) {
                String msg = String.format(
                        "You have been outbid on auction %d. New top bid: £%.2f",
                        dto.getAuctionId(), newCurrentBid);
                notificationService.alertOutbid(oldUserId, dto.getAuctionId(), msg);
                log.info("Notified previous top bidder {} of being outbid", oldUserId);
            }
        }

        // 6) Return the saved bid
        return mapper.toDto(savedBid);
    }

    /**
     * Fetches all reserve prices for this auction, finds the 2nd highest,
     * adds the auction's bidIncrement, and updates currentBid.
     */
    private void updateCurrentBid(AuctionItems item) {
        log.info("Updating the Current Bid in Auction Table");
        List<Double> sortedReserves = bidRepo
                .findAllByAuctionItem_Id(item.getId())
                .stream()
                .map(Bid::getReservePrice)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        if (sortedReserves.isEmpty()) {
            return;  // no bids yet
        }

        double secondHighest = sortedReserves.size() > 1
                ? sortedReserves.get(1)
                : sortedReserves.get(0);

        double newCurrentBid = secondHighest + item.getbid_increment();
        System.out.println("New current Bid is "+newCurrentBid);


        item.setCurrentBid(newCurrentBid);
        itemsRepo.save(item);
    }

//    private void updateCurrentBid(AuctionItems item) {
//        log.info("Recomputing current bid for auction {}", item.getauction_id());
//
//        // 1) Fetch all bids for this auction
//        List<Bid> bids = bidRepo.findAllByAuctionItem_Id(item.getId());
//        if (bids.isEmpty()) {
//            log.info("No bids yet for auction {}", item.getauction_id());
//            return;
//        }
//
//        // 2) Sort by reservePrice descending
//        bids.sort(Comparator.comparing(Bid::getReservePrice).reversed());
//
//        // 3) Identify highest‐ and second‐highest reserve prices
//        Bid highestBid       = bids.get(0);
//        double highestReserve = highestBid.getReservePrice();
//        double secondReserve  = bids.size() > 1
//                ? bids.get(1).getReservePrice()
//                : highestReserve;
//
//        // 4) Compute the new current bid
//        double newCurrentBid = secondReserve + item.getbid_increment();
//        item.setCurrentBid(newCurrentBid);
//        itemsRepo.save(item);
//
//        // 5) Log which buyer “wins” at the new current bid
//        int winnerId = highestBid.getBuyer().getUserId();
//        log.info("Buyer {} had the highest reserve (${}}) → current_bid set to ${}",
//                winnerId, highestReserve, newCurrentBid);
//    }


//    @Override
//    public List<BidDto> getBidsByAuction(int auctionId) {
//        return bidRepo.findAllByAuctionItem_Id((long)auctionId)
//                      .stream()
//                      .map(mapper::toDto)
//                      .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<BidDto> getBidsByBuyer(int buyerId) {
//        return bidRepo.findAllByBuyer_UserId(buyerId)
//                      .stream()
//                      .map(mapper::toDto)
//                      .collect(Collectors.toList());
//    }
@Override
public List<BidDto> getBidsByAuction(int auctionId) {
    AuctionItems item = itemsRepo
            .findByAuctionIdNative(auctionId)
            .orElseThrow(() -> new AuctionItemNotFoundException(
                    "Auction not found: " + auctionId));

    return bidRepo.findAllByAuctionItem_Id(item.getId())
            .stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
}

    @Override
    public List<BidDto> getBidsByBuyer(int buyerId) {
        // Ensure buyer exists
        usersRepo.findById(buyerId)
                .orElseThrow(() -> new UserNotFound(
                        "Buyer not found: " + buyerId));

        return bidRepo.findAllByBuyer_UserId(buyerId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
