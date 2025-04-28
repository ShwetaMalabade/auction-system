// src/main/java/com/database/auction/jobs/AuctionEndNotificationJob.java
package com.database.auction.scheduler;

import com.database.auction.entity.AuctionItems;
import com.database.auction.entity.Bid;
import com.database.auction.repository.AuctionItemsRepository;
import com.database.auction.repository.BidRepository;
import com.database.auction.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class AuctionEndNotificationJob implements Job {

    // Quartz will call the no-arg constructor
    public AuctionEndNotificationJob() { }

    @Autowired
    private AuctionItemsRepository itemsRepo;

    @Autowired
    private BidRepository bidRepo;

    @Autowired
    private NotificationService notificationService;

//    public AuctionEndNotificationJob(
//            AuctionItemsRepository itemsRepo,
//            BidRepository bidRepo,
//            NotificationService notificationService) {
//        this.itemsRepo          = itemsRepo;
//        this.bidRepo            = bidRepo;
//        this.notificationService = notificationService;
//    }

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap data = context.getMergedJobDataMap();
        int auctionId = data.getInt("auctionId");
        log.info("🏁 AuctionEndNotificationJob fired for auction {}", auctionId);

        // 1) load the auction
        AuctionItems item = itemsRepo
            .findByAuctionIdNative(auctionId)
            .orElseThrow(() -> new IllegalStateException(
                "Auction not found: " + auctionId));

        Integer winnerId = item.getWinningBuyerId();
        double  price    = Optional.ofNullable(item.getCurrentBid()).orElse(0.0);

        if (winnerId != null) {
            // Case A: a winner exists
            String msg = String.format(
                "🎉 Congrats! You won auction %d for £%.2f",
                 auctionId, price);
            notificationService.alertOutbid(winnerId, auctionId, msg);
            log.info("Sent winner notification to buyerId={}", winnerId);

        } else {
            // Case B: no winner → look at bids
            List<Bid> bids = bidRepo.findAllByAuctionItem_Id(item.getId());
            int sellerId = item.getseller_id();

            String msg;
            if (bids.isEmpty()) {
                // B1: no bids at all
                msg = "😞 No one bid on your auction.";
            } else {
                // B2: bids placed but none cleared min_price
                msg = "⚠️ Bids were placed but none crossed the minimum price.";
            }
            notificationService.alertOutbid(sellerId, auctionId, msg);
            log.info("Sent seller notification to sellerId={} : {}", sellerId, msg);
        }
    }
}
