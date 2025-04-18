package com.database.auction.controllers;


import com.database.auction.dto.AuctionItemDto;
import com.database.auction.dto.AuctionItemSummaryDto;
import com.database.auction.service.AuctionItemsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController

@RequestMapping("/auth/auction-items")
public class AuctionItemsController {

    private final AuctionItemsService auctionItemsService;

    @Autowired
    public AuctionItemsController(AuctionItemsService auctionItemsService) {
        this.auctionItemsService = auctionItemsService;
    }

    // Endpoint to retrieve all auction items
    @GetMapping("/all")
    public List<AuctionItemDto> getAllAuctionItems() {
        System.out.println("All items will be displayed now");
        return auctionItemsService.findAllAuctionItems();
    }

    @GetMapping("/summary")
    public List<AuctionItemSummaryDto> getAuctionItemSummaries() {
        return auctionItemsService.findAllAuctionItemSummaries();
    }

    @GetMapping("/{id}")
    public AuctionItemDto getAuctionItemById(@PathVariable Long id) {
        return auctionItemsService.findAuctionItemById(id);
    }
}
