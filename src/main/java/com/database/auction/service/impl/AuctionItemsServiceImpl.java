package com.database.auction.service.impl;


import com.database.auction.dto.AuctionItemDto;
import com.database.auction.dto.AuctionItemSummaryDto;
import com.database.auction.entity.AuctionImage;
import com.database.auction.entity.AuctionItems;
import com.database.auction.entity.Users;
import com.database.auction.enums.Category;
import com.database.auction.enums.RoleType;
import com.database.auction.exception.AuctionItemNotFoundException;
import com.database.auction.mapper.AuctionItemsMapper;
import com.database.auction.repository.AuctionItemsRepository;
import com.database.auction.repository.UsersRepository;
import com.database.auction.service.AuctionItemsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuctionItemsServiceImpl implements AuctionItemsService {

    private final AuctionItemsRepository auctionItemsRepository;
    private final AuctionItemsMapper auctionItemsMapper;
    private final UsersRepository userRepository;
    //private final UsersRepository usersRepository;

    @Autowired
    public AuctionItemsServiceImpl(AuctionItemsRepository auctionItemsRepository,
                                   AuctionItemsMapper auctionItemsMapper, UsersRepository userRepository) {
        this.auctionItemsRepository = auctionItemsRepository;
        this.auctionItemsMapper = auctionItemsMapper;
        this.userRepository = userRepository;
    }

    @Override
    public List<AuctionItemDto> findAllAuctionItems() {
        List<AuctionItems> items = auctionItemsRepository.findAll();
        return items.stream()
                .map(auctionItemsMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuctionItemSummaryDto> findAllAuctionItemSummaries() {
        List<AuctionItems> items = auctionItemsRepository.findAll();
        log.info(String.valueOf(items.stream()
                    .map(auctionItemsMapper::toSummaryDto).toList()));
        return items.stream()
                .map(auctionItemsMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public AuctionItemDto insertAuctionItem(AuctionItemDto auctionItemDto) {
        // Verify the seller exists and is of role SELLER
        Users seller = userRepository.findByUserId(auctionItemDto.getSellerId());
        if (seller == null || !RoleType.SELLER.equals(seller.getRole())) {
            throw new RuntimeException("Invalid seller: User not found or does not have SELLER role");
        }

        // Map the DTO to an AuctionItems entity
        AuctionItems auctionItem = auctionItemsMapper.toEntity(auctionItemDto);

        // If image URLs are provided in DTO, create AuctionImage entities
        if (auctionItemDto.getImages() != null && !auctionItemDto.getImages().isEmpty()) {
            List<AuctionImage> images = auctionItemDto.getImages().stream()
                    .map(url -> {
                        AuctionImage img = new AuctionImage();
                        img.setImageUrl(url);
                        img.setAuctionItem(auctionItem);
                        return img;
                    }).collect(Collectors.toList());
            auctionItem.setImages(images);
        }

        // initialize currentBid
        if (auctionItemDto.getCurrentBid() != null) {
            auctionItem.setCurrentBid(auctionItemDto.getCurrentBid());
        } else {
            auctionItem.setCurrentBid(auctionItem.getStartingPrice());
        }

        // Persist the auction item in the database
        AuctionItems savedItem = auctionItemsRepository.save(auctionItem);
        return auctionItemsMapper.toDto(savedItem);
    }

    @Override
    public AuctionItemDto findAuctionItemByAuctionId(int auction_id) {
        AuctionItems item = auctionItemsRepository.findByAuctionIdNative(auction_id)
                .orElseThrow(() -> new AuctionItemNotFoundException(
                        "Auction item not found with auctionId=" + auction_id));
        return auctionItemsMapper.toDto(item);
    }

    @Override
    public List<AuctionItemSummaryDto> findAuctionItemsByCategory(Category category) {
        List<AuctionItems> items = auctionItemsRepository.findAllByCategory(category);
        return items.stream()
                .map(auctionItemsMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

}
