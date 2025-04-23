package com.database.auction.controllers;


import com.database.auction.dto.AuctionItemDto;
import com.database.auction.dto.AuctionItemSummaryDto;
import com.database.auction.entity.AuctionItems;
import com.database.auction.exception.AuctionItemNotFoundException;
import com.database.auction.mapper.AuctionItemsMapper;
import com.database.auction.repository.AuctionItemsRepository;
import com.database.auction.service.AuctionItemsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
@RestController

@RequestMapping("/auth/auction-items")
public class AuctionItemsController {

    private final AuctionItemsService auctionItemsService;
    private AuctionItemsRepository auctionItemsRepository;
    private AuctionItemsMapper auctionItemsMapper;

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
    public ResponseEntity<List<AuctionItemSummaryDto>> getAuctionItemSummaries() {
        log.info("We have received the calling from frontend");
        return ResponseEntity.ok(auctionItemsService.findAllAuctionItemSummaries());
    }

    // NEW: lookup by auctionId, return as ResponseEntity
    @GetMapping("/{auctionId}")
    public ResponseEntity<AuctionItemDto> getAuctionItemByAuctionId(
            @PathVariable int auctionId) {

        AuctionItemDto dto = auctionItemsService.findAuctionItemByAuctionId(auctionId);
        return ResponseEntity.ok(dto);
    }
}
