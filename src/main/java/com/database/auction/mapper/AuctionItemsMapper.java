package com.database.auction.mapper;


import com.database.auction.dto.AuctionItemDto;
import com.database.auction.dto.AuctionItemSummaryDto;
import com.database.auction.entity.AuctionImage;
import com.database.auction.entity.AuctionItems;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuctionItemsMapper {

    public AuctionItemDto toDto(AuctionItems auctionItem) {
        if (auctionItem == null) {
            return null;
        }
        AuctionItemDto dto = new AuctionItemDto();
        dto.setId(auctionItem.getId());
        dto.setAuctionId(auctionItem.getAuctionId());
        dto.setSellerId(auctionItem.getSellerId());
        dto.setItemName(auctionItem.getItemName());
        dto.setCategory(auctionItem.getCategory());
        dto.setStartingPrice(auctionItem.getStartingPrice());
        dto.setBidIncrement(auctionItem.getBidIncrement());
        dto.setReservePrice(auctionItem.getReservePrice());
        dto.setClosingTime(auctionItem.getClosingTime());
        dto.setDescription(auctionItem.getDescription());
        List<String> imageUrls = auctionItem.getImages().stream()
                .map(AuctionImage::getImageUrl)
                .collect(Collectors.toList());
        dto.setImages(imageUrls);
        return dto;
    }

    public AuctionItems toEntity(AuctionItemDto dto) {
        if (dto == null) {
            return null;
        }
        AuctionItems auctionItem = new AuctionItems();
        auctionItem.setAuctionId(dto.getAuctionId());
        auctionItem.setSellerId(dto.getSellerId());
        auctionItem.setItemName(dto.getItemName());
        auctionItem.setCategory(dto.getCategory());
        auctionItem.setStartingPrice(dto.getStartingPrice());
        auctionItem.setBidIncrement(dto.getBidIncrement());
        auctionItem.setReservePrice(dto.getReservePrice());
        auctionItem.setClosingTime(dto.getClosingTime());
        auctionItem.setDescription(dto.getDescription());
        // The images will be processed in the service if provided.
        return auctionItem;
    }

    public AuctionItemSummaryDto toSummaryDto(AuctionItems auctionItem) {
        if (auctionItem == null) {
            return null;
        }
        AuctionItemSummaryDto summaryDto = new AuctionItemSummaryDto();
        List<String> imageUrls = auctionItem.getImages().stream()
                .map(AuctionImage::getImageUrl)
                .collect(Collectors.toList());
        summaryDto.setImages(imageUrls);
        summaryDto.setItemName(auctionItem.getItemName());
        // Pass the Category enum directly instead of converting it to string.
        summaryDto.setCategory(auctionItem.getCategory());
        summaryDto.setStartingPrice(auctionItem.getStartingPrice());
        summaryDto.setClosingTime(auctionItem.getClosingTime());
        return summaryDto;
    }
}