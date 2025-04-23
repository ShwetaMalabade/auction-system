package com.database.auction.repository;


import com.database.auction.dto.AuctionItemDto;
import com.database.auction.entity.AuctionItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuctionItemsRepository extends JpaRepository<AuctionItems, Long> {
    @Query(
            value = "SELECT * FROM Auction_Items WHERE auction_id = :auctionId",
            nativeQuery = true
    )
    Optional<AuctionItems> findByAuctionIdNative(@Param("auctionId") int auctionId);
}
