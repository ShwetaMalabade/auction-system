package com.database.auction.service;


import com.database.auction.dto.AuctionItemDto;
import com.database.auction.dto.AuctionItemSummaryDto;

import java.util.List;

public interface AuctionItemsService {
    List<AuctionItemDto> findAllAuctionItems();
    List<AuctionItemSummaryDto> findAllAuctionItemSummaries();
    AuctionItemDto findAuctionItemById(Long id);
    AuctionItemDto insertAuctionItem(AuctionItemDto auctionItemDto);
}
