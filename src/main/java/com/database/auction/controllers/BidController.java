package com.database.auction.controllers;

import com.database.auction.dto.BidDto;
import com.database.auction.service.BidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/auth/bids")
public class BidController {

    private final BidService bidService;

    @Autowired
    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    /** Place a new bid */
    @PostMapping
    public ResponseEntity<BidDto> placeBid(@RequestBody BidDto bidDto) {
        BidDto created = bidService.placeBid(bidDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** List all bids for a given auction */
    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<List<BidDto>> bidsByAuction(
            @PathVariable int auctionId) {
        return ResponseEntity.ok(bidService.getBidsByAuction(auctionId));
    }

    /** List all bids by a given buyer */
    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<BidDto>> bidsByBuyer(
            @PathVariable int buyerId) {
        return ResponseEntity.ok(bidService.getBidsByBuyer(buyerId));
    }
}
