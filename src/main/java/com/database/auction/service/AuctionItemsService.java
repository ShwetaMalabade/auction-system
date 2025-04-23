package com.database.auction.service;


import com.database.auction.dto.AuctionItemDto;
import com.database.auction.dto.AuctionItemSummaryDto;
import com.database.auction.entity.AuctionItems;
import com.database.auction.enums.Category;

import java.util.List;
import java.util.Optional;

public interface AuctionItemsService {
    List<AuctionItemDto> findAllAuctionItems();
    List<AuctionItemSummaryDto> findAllAuctionItemSummaries();
    AuctionItemDto findAuctionItemByAuctionId(int auctionId);
    List<AuctionItemSummaryDto> findAuctionItemsByCategory(Category category);
    AuctionItemDto insertAuctionItem(AuctionItemDto auctionItemDto);
}
