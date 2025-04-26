package com.database.auction.repository;


import com.database.auction.dto.AuctionItemDto;
import com.database.auction.entity.AuctionItems;
import com.database.auction.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionItemsRepository extends JpaRepository<AuctionItems, Long> {
    @Query(
            value = "SELECT * FROM Auction_Items WHERE auction_id = :auctionId",
            nativeQuery = true
    )
    Optional<AuctionItems> findByAuctionIdNative(@Param("auctionId") int auctionId);

    List<AuctionItems> findAllByCategory(Category category);
    /** Returns the current maximum id in the table, or 0 if empty */
    @Query("SELECT COALESCE(MAX(a.id), 0) FROM AuctionItems a")
    Long findMaxId();
}
